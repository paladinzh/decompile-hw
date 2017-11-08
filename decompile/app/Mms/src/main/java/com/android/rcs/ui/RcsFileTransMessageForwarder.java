package com.android.rcs.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.widget.Toast;
import cn.com.xy.sms.sdk.net.NetUtil;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ComposeMessageFragment;
import com.android.mms.ui.FavoritesListAdapter;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageListAdapter;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.ResEx;
import com.huawei.rcs.ui.RcsFileTransGroupMessageItem;
import com.huawei.rcs.ui.RcsFileTransMessageItem;
import com.huawei.rcs.ui.RcsGroupChatComposeMessageActivity;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsProfileUtils;
import com.huawei.rcs.utils.RcsTransaction;
import com.huawei.rcs.utils.RcsTransaction.LocationData;
import com.huawei.rcs.utils.RcsUtility;
import com.huawei.rcs.utils.map.abs.RcsMapLoader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RcsFileTransMessageForwarder extends RcsBaseForwarder<Map<String, List<?>>> {
    private boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private Map<String, List<?>> mSelectionMsgItemData;

    public boolean isCursorValid(Cursor cursor) {
        if (!this.isRcsOn) {
            return false;
        }
        if (!cursor.isClosed() && !cursor.isBeforeFirst() && !cursor.isAfterLast()) {
            return true;
        }
        MLog.e("RcsFileTransMessageForwarder", "isCursorValid Bad cursor.", new RuntimeException());
        return false;
    }

    public boolean isFileTransMessageItem(MessageItem msgItem) {
        if (!this.isRcsOn) {
            return false;
        }
        try {
            if (msgItem instanceof RcsFileTransMessageItem) {
                MLog.v("RcsFileTransMessageForwarder", "isFileTransMessageItem this item is RcsFileTransMessageItem.");
                return true;
            }
        } catch (ClassCastException e) {
        }
        return false;
    }

    public boolean isFileTransGroupChatMessageItem(RcsGroupChatMessageItem msgItem) {
        if (this.isRcsOn) {
            return msgItem.isFileTransMessage();
        }
        return false;
    }

    public boolean isMultiMediaMessageItem(Cursor cursor, int position) {
        if (!this.isRcsOn || cursor == null) {
            return false;
        }
        boolean result = false;
        int prePosition = cursor.getPosition();
        cursor.moveToPosition(position);
        int rcsMsgType = RcsProfileUtils.getRcsAnyMsgType(cursor);
        MLog.v("RcsFileTransMessageForwarder", "isMultiMediaMessageItem rcsMsgType: " + rcsMsgType + ", prePosition = " + prePosition);
        switch (rcsMsgType) {
            case 3:
            case 5:
            case 6:
                result = true;
                break;
        }
        cursor.moveToPosition(prePosition);
        return result;
    }

    private Intent getGroupSmsIntent(List<String> addrList, List<Uri> filetranstUris) {
        StringBuilder sb = new StringBuilder();
        sb.append("smsto:");
        for (int i = 0; i < addrList.size(); i++) {
            sb.append((String) addrList.get(i)).append(";");
        }
        sb.deleteCharAt(sb.length() - 1);
        Uri uri = Uri.parse(sb.toString());
        String nonRcsUser = "";
        for (String number : addrList) {
            String normalizeKey = PhoneNumberUtils.normalizeNumber(number);
            try {
                if (!RcsUtility.isRcsLogin() || !RcsProfile.getRcsService().isRcsUeser(normalizeKey)) {
                    nonRcsUser = normalizeKey;
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Intent chatIntent = new Intent("android.intent.action.SENDTO", uri);
        chatIntent.setClass(this.mContext, ComposeMessageActivity.class);
        chatIntent.putExtras(formatBundle(filetranstUris));
        chatIntent.putExtra("Contacts", "RCS_FT");
        chatIntent.putExtra("ADDRESS", nonRcsUser);
        if (!RcsUtility.isRcsLogin()) {
            chatIntent.putExtra("ForwardFlag", true);
        }
        return chatIntent;
    }

    private Bundle formatBundle(List<Uri> filetranstUris) {
        StringBuffer uriBuffer = new StringBuffer();
        uriBuffer.append("[");
        for (Uri fileUri : filetranstUris) {
            uriBuffer.append(fileUri.toString()).append(",");
        }
        uriBuffer.append("]").deleteCharAt(uriBuffer.length() - 2);
        Bundle bundle = new Bundle();
        bundle.putString("android.intent.extra.STREAM", uriBuffer.toString());
        return bundle;
    }

    public boolean isGroupLocItem(Cursor cursor, int position) {
        if (!this.isRcsOn) {
            return false;
        }
        boolean result;
        int prePosition = cursor.getPosition();
        cursor.moveToPosition(position);
        if (RcsProfileUtils.getRcsMsgExtType(cursor) == 6) {
            MLog.d("RcsFileTransMessageForwarder", "isGroupLocItem: this is a group loc item.");
            result = true;
        } else {
            MLog.d("RcsFileTransMessageForwarder", "isGroupLocItem: this is not a group loc item.");
            result = false;
        }
        cursor.moveToPosition(prePosition);
        return result;
    }

    public boolean detectMessageToForwardForLoc(Integer[] selection, Cursor cursor) {
        if (!this.isRcsOn) {
            return false;
        }
        if (selection == null || selection.length < 1) {
            MLog.w("RcsFileTransMessageForwarder", "detectMessageToForwardForLoc selection is empty.");
            return false;
        }
        setSelection(selection);
        switch (this.mMsgKind) {
            case 1:
            case 3:
                if (RcsMapLoader.isLocItem(cursor, selection[0].intValue())) {
                    return true;
                }
                MLog.d("RcsFileTransMessageForwarder", "detectMessageToForwardForLoc: this is not loc message.");
                return false;
            case 2:
                return isGroupLocItem(cursor, selection[0].intValue());
            default:
                return false;
        }
    }

    public void forwardLoc() {
        if (!this.isRcsOn) {
            return;
        }
        if (this.mFragment == null || this.mCursorAdapter == null) {
            MLog.e("RcsFileTransMessageForwarder", "forwardLoc forwardMessage: mListAdapter is null.");
            return;
        }
        if (this.mSelectionMsgItemData != null) {
            this.mSelectionMsgItemData.clear();
            this.mSelectionMsgItemData = null;
        }
        this.mSelectionMsgItemData = getForwardMsgItemData();
        launchContactsPicker(160126, this.mSelectionMsgItemData);
    }

    public void onForwardResult(Intent data) {
        if (this.isRcsOn) {
            super.rcsActivityResult(data);
        }
    }

    private Map<String, List<?>> getForwardMsgItemData() {
        MLog.d("RcsFileTransMessageForwarder", "getForwardMsgItemData start.mMsgKind = " + this.mMsgKind);
        switch (this.mMsgKind) {
            case 1:
                return getSingleForwardMsgItemData();
            case 2:
                return getGourpChatForwardMsgItemData();
            case 3:
                return getFAVForwardMsgItemData();
            default:
                return null;
        }
    }

    private Map<String, List<?>> getSingleForwardMsgItemData() {
        MLog.d("RcsFileTransMessageForwarder", "getSingleForwardMsgItemData start.");
        Map<String, List<?>> msgData = new HashMap();
        List<LocationData> locations = new ArrayList();
        List<Uri> voiceMsgUris = new ArrayList();
        List<Uri> filetranstUris = new ArrayList();
        ComposeMessageFragment comFragment = this.mFragment;
        try {
            Cursor cursor = comFragment.getRcsComposeMessage().getComposeMessageListAdapter().getCursor();
            if (cursor == null || !RcsTransaction.isCursorValid(cursor)) {
                MLog.w("RcsFileTransMessageForwarder", "getSingleForwardMsgItemData cursor is invalid.");
                return msgData;
            } else if (this.mSelectionStorage == null || this.mSelectionStorage.length < 1) {
                MLog.w("RcsFileTransMessageForwarder", "getSingleForwardMsgItemData count < 1.");
                return msgData;
            } else {
                Arrays.sort(this.mSelectionStorage);
                Integer[] selectedItems = this.mSelectionStorage;
                int prePosition = cursor.getPosition();
                for (Integer intValue : selectedItems) {
                    int position = intValue.intValue();
                    if (position >= 0) {
                        MessageItem msgItem;
                        if (comFragment.getRcsComposeMessage().getComposeMessageListAdapter().getRcsMessageListAdapter() != null) {
                            msgItem = comFragment.getRcsComposeMessage().getComposeMessageListAdapter().getRcsMessageListAdapter().getMessageItemWithIdAssigned(position, cursor);
                        } else {
                            msgItem = null;
                        }
                        if (msgItem != null) {
                            cursor.moveToPosition(position);
                            switch (RcsProfileUtils.getRcsMsgType(cursor)) {
                                case 3:
                                case 5:
                                case 6:
                                    try {
                                        if (isFileTransMessageItem(msgItem) && (msgItem instanceof RcsFileTransMessageItem)) {
                                            Uri uri = ((RcsFileTransMessageItem) msgItem).getFileUri();
                                            if (!RcsTransaction.isFileExist(uri)) {
                                                break;
                                            }
                                            filetranstUris.add(uri);
                                            break;
                                        }
                                    } catch (ClassCastException e) {
                                        break;
                                    }
                                default:
                                    if (RcsProfileUtils.getRcsMsgExtType(cursor) != 6) {
                                        break;
                                    }
                                    comFragment.getWorkingMessage().syncWorkingRecipients();
                                    locations.add(getLocationDataFromBody(msgItem.mBody));
                                    break;
                            }
                        }
                        MLog.w("RcsFileTransMessageForwarder", "getSingleForwardMsgItemData msgItem is null.");
                    } else {
                        MLog.w("RcsFileTransMessageForwarder", "getSingleForwardMsgItemData position error.");
                    }
                }
                cursor.moveToPosition(prePosition);
                msgData.put(NetUtil.REQ_QUERY_LOCATION, locations);
                msgData.put("voicemessage", voiceMsgUris);
                msgData.put("filetrans", filetranstUris);
                MLog.v("RcsFileTransMessageForwarder", "getForwardMsgItemData end.");
                return msgData;
            }
        } catch (ClassCastException e2) {
            MLog.e("RcsFileTransMessageForwarder", "getSingleForwardMsgItemData ((ComposeMessageFragment)comFragment failed");
            return msgData;
        }
    }

    private Map<String, List<?>> getGourpChatForwardMsgItemData() {
        MLog.v("RcsFileTransMessageForwarder", "getGourpChatForwardMsgItemData start.");
        Map<String, List<?>> msgData = new HashMap();
        List<LocationData> locations = new ArrayList();
        List<Uri> voiceMsgUris = new ArrayList();
        List<Uri> filetranstUris = new ArrayList();
        try {
            Cursor cursor = ((RcsGroupChatMessageListAdapter) this.mCursorAdapter).getCursor();
            if (cursor == null || !isCursorValid(cursor)) {
                MLog.w("RcsFileTransMessageForwarder", "getGourpChatForwardMsgItemData cursor is invalid.");
                return msgData;
            } else if (this.mSelectionStorage == null || this.mSelectionStorage.length < 1) {
                MLog.w("RcsFileTransMessageForwarder", "getGourpChatForwardMsgItemData count < 1.");
                return msgData;
            } else {
                Arrays.sort(this.mSelectionStorage);
                Integer[] selectedItems = this.mSelectionStorage;
                int prePosition = cursor.getPosition();
                for (Integer intValue : selectedItems) {
                    int position = intValue.intValue();
                    if (position < 0) {
                        MLog.e("RcsFileTransMessageForwarder", "getGourpChatForwardMsgItemData position error.");
                    } else {
                        RcsGroupChatMessageItem msgItem = ((RcsGroupChatMessageListAdapter) this.mCursorAdapter).getMessageItemWithIdAssigned(position, cursor);
                        if (msgItem != null) {
                            cursor.moveToPosition(position);
                            switch (RcsProfileUtils.getGroupChatRcsMsgType(cursor, ((RcsGroupChatMessageListAdapter) this.mCursorAdapter).getGroupMessageColumn())) {
                                case 100:
                                case 101:
                                case 102:
                                case OfflineMapStatus.EXCEPTION_SDCARD /*103*/:
                                case 106:
                                case 107:
                                case 110:
                                case 111:
                                    try {
                                        if (!isFileTransGroupChatMessageItem(msgItem)) {
                                            break;
                                        }
                                        RcsFileTransGroupMessageItem ftItem = msgItem.mFtGroupMsgItem;
                                        Uri uri = ftItem.getFileUri();
                                        if (RcsTransaction.isFileExist(uri)) {
                                            if (!ftItem.isVoiceMessage()) {
                                                filetranstUris.add(uri);
                                                break;
                                            }
                                            voiceMsgUris.add(uri);
                                            break;
                                        }
                                        break;
                                    } catch (ClassCastException e) {
                                        break;
                                    }
                                default:
                                    if (RcsProfileUtils.getRcsMsgExtType(cursor) != 6) {
                                        break;
                                    }
                                    locations.add(getLocationDataFromBody(msgItem.mBody));
                                    break;
                            }
                        }
                    }
                }
                cursor.moveToPosition(prePosition);
                msgData.put(NetUtil.REQ_QUERY_LOCATION, locations);
                msgData.put("voicemessage", voiceMsgUris);
                msgData.put("filetrans", filetranstUris);
                MLog.d("RcsFileTransMessageForwarder", "getForwardMsgItemData end.");
                return msgData;
            }
        } catch (ClassCastException e2) {
            MLog.e("RcsFileTransMessageForwarder", "getGourpChatForwardMsgItemData ((RcsGroupChatMessageListAdapter)comActivity failed");
            return msgData;
        }
    }

    public LocationData getLocationDataFromBody(String body) {
        HashMap<String, String> locHashMap = RcsMapLoader.getLocInfo(body);
        return new LocationData(Double.parseDouble((String) locHashMap.get("latitude")), Double.parseDouble((String) locHashMap.get("longitude")), (String) locHashMap.get("subtitle"), (String) locHashMap.get("title"));
    }

    private Map<String, List<?>> getFAVForwardMsgItemData() {
        Map<String, List<?>> msgData = new HashMap();
        List<LocationData> locations = new ArrayList();
        List<Uri> voiceMsgUris = new ArrayList();
        List<Uri> filetranstUris = new ArrayList();
        MessageListAdapter mListAdapter = this.mCursorAdapter;
        Cursor cursor = mListAdapter.getCursor();
        if (cursor == null || !isCursorValid(cursor)) {
            MLog.w("RcsFileTransMessageForwarder", "getFAVForwardMsgItemData cursor is invalid.");
            return msgData;
        } else if (this.mSelectionStorage == null || this.mSelectionStorage.length < 1) {
            MLog.w("RcsFileTransMessageForwarder", "getFAVForwardMsgItemData count < 1.");
            return msgData;
        } else {
            Arrays.sort(this.mSelectionStorage);
            Integer[] selectedItems = this.mSelectionStorage;
            int prePosition = cursor.getPosition();
            for (Integer intValue : selectedItems) {
                int position = intValue.intValue();
                if (position >= 0) {
                    cursor.moveToPosition(position);
                    MessageItem msgItem = ((FavoritesListAdapter) mListAdapter).getCachedMessageItem(cursor.getString(mListAdapter.mColumnsMap.mColumnMsgType), cursor.getLong(mListAdapter.mColumnsMap.mColumnMsgId), cursor);
                    switch (RcsProfileUtils.getRcsAnyMsgType(cursor)) {
                        case 3:
                        case 5:
                        case 6:
                            if (msgItem == null) {
                                break;
                            }
                            RcsFileTransGroupMessageItem ftItem = msgItem.getRcsMessageItem().getFileItem();
                            if (ftItem == null) {
                                break;
                            }
                            Uri uri = ftItem.getFileUri();
                            if (RcsTransaction.isFileExist(uri)) {
                                if (!ftItem.isVoiceMessage()) {
                                    filetranstUris.add(uri);
                                    break;
                                }
                                voiceMsgUris.add(uri);
                                break;
                            }
                            Toast.makeText(this.mContext, R.string.text_file_not_exist, 0).show();
                            break;
                        default:
                            if (msgItem != null && RcsMapLoader.isLocItem(msgItem.mBody)) {
                                locations.add(getLocationDataFromBody(msgItem.mBody));
                                break;
                            }
                    }
                }
                MLog.w("RcsFileTransMessageForwarder", "getFAVForwardMsgItemData position error." + position);
            }
            cursor.moveToPosition(prePosition);
            msgData.put(NetUtil.REQ_QUERY_LOCATION, locations);
            msgData.put("voicemessage", voiceMsgUris);
            msgData.put("filetrans", filetranstUris);
            return msgData;
        }
    }

    public boolean detectMessageToForwardForFt(Integer[] selection, Cursor cursor) {
        if (!this.isRcsOn) {
            return false;
        }
        if (selection == null || selection.length < 1) {
            MLog.w("RcsFileTransMessageForwarder", "detectMessageToForwardForFt selection is empty.");
            return false;
        }
        setSelection(selection);
        switch (this.mMsgKind) {
            case 1:
            case 3:
                if (isMultiMediaMessageItem(cursor, selection[0].intValue())) {
                    return true;
                }
                MLog.v("RcsFileTransMessageForwarder", "detectMessageToForwardForFt: this is not multimedia message.");
                return false;
            case 2:
                return isGroupMultiMediaMessageItem(cursor, selection[0].intValue());
            default:
                return false;
        }
    }

    public void forwardFt() {
        if (!this.isRcsOn) {
            return;
        }
        if (this.mFragment == null || this.mCursorAdapter == null) {
            MLog.e("RcsFileTransMessageForwarder", "forwardMessage: mListAdapter is null.");
            return;
        }
        if (this.mSelectionMsgItemData != null) {
            this.mSelectionMsgItemData.clear();
            this.mSelectionMsgItemData = null;
        }
        this.mSelectionMsgItemData = getForwardMsgItemData();
        launchContactsPicker(160125, this.mSelectionMsgItemData);
    }

    public boolean isGroupMultiMediaMessageItem(Cursor cursor, int position) {
        if (!this.isRcsOn) {
            return false;
        }
        RcsGroupChatMessageItem msgItem = ((RcsGroupChatMessageListAdapter) this.mCursorAdapter).getMessageItemWithIdAssigned(position, cursor);
        if (msgItem == null) {
            return false;
        }
        MLog.v("RcsFileTransMessageForwarder", "isGroupMultiMediaMessageItem rcsMsgType:" + msgItem.mType);
        boolean result = false;
        switch (msgItem.mType) {
            case 100:
            case 101:
            case 102:
            case OfflineMapStatus.EXCEPTION_SDCARD /*103*/:
            case 106:
            case 107:
            case 110:
            case 111:
                result = true;
                break;
        }
        return result;
    }

    public void processMassOrSingle(List<String> rcsList, int rcsUsers) {
        if (this.isRcsOn) {
            List list = null;
            List list2 = null;
            if (((Map) this.mMessageData).get("filetrans") != null) {
                list = (List) ((Map) this.mMessageData).get("filetrans");
            }
            if (((Map) this.mMessageData).get(NetUtil.REQ_QUERY_LOCATION) != null) {
                list2 = (List) ((Map) this.mMessageData).get(NetUtil.REQ_QUERY_LOCATION);
            }
            if (!(list == null || list.size() == 0)) {
                processMassOrSingleForFt(rcsList, rcsUsers, list);
            }
            if (!(list2 == null || list2.size() == 0)) {
                processMassOrSingleForLoc(rcsList, rcsUsers, list2);
            }
        }
    }

    private void processMassOrSingleForFt(List<String> rcsList, int rcsUsers, List<Uri> filetranstUris) {
        if (rcsList.size() <= 1) {
            if (this.mFragment != null) {
                this.mFragment.startActivity(getChatIntent(rcsList, formatBundle(filetranstUris)));
            }
        } else if (this.mFragment != null) {
            this.mFragment.startActivity(getGroupSmsIntent(rcsList, filetranstUris));
        }
    }

    private void processMassOrSingleForLoc(List<String> rcsList, int rcsUsers, List<LocationData> locations) {
        String recipient = (String) rcsList.get(0);
        if (RcsProfile.isResendImAvailable(recipient)) {
            for (LocationData data : locations) {
                RcsTransaction.sendLocationSingleChat(data.x, data.y, data.myAddress, data.city, recipient);
            }
            this.mFragment.startActivity(getLocIntent(rcsList));
            return;
        }
        ResEx.makeToast((int) R.string.rcs_im_resend_error_message, 0);
    }

    public void dispatchGroupChatIntent(String groupId) {
        if (this.isRcsOn) {
            List<Uri> picUriList = (List) ((Map) this.mMessageData).get("filetrans");
            List<LocationData> locations = (List) ((Map) this.mMessageData).get(NetUtil.REQ_QUERY_LOCATION);
            if (!(picUriList == null || picUriList.size() == 0)) {
                dispatchGroupChatIntentForFt(groupId, picUriList);
            }
            if (!(locations == null || locations.size() == 0)) {
                dispatchGroupChatIntentForLoc(groupId, locations);
            }
        }
    }

    private void dispatchGroupChatIntentForFt(String groupId, List<Uri> picUriList) {
        Intent intent = new Intent(this.mContext, RcsGroupChatComposeMessageActivity.class);
        intent.putExtra("bundle_group_id", groupId);
        if (picUriList instanceof ArrayList) {
            intent.putParcelableArrayListExtra("bundle_pic_uri", (ArrayList) picUriList);
        }
        intent.putExtras(formatBundle(picUriList));
        this.mContext.startActivity(intent);
        if (this.mFragment != null && (this.mFragment instanceof ComposeMessageFragment)) {
            ((ComposeMessageFragment) this.mFragment).finishSelf(false);
        }
    }

    private void dispatchGroupChatIntentForLoc(String groupId, List<LocationData> locations) {
        Intent intent = new Intent(this.mContext, RcsGroupChatComposeMessageActivity.class);
        intent.putExtra("bundle_group_id", groupId);
        intent.putExtra("bundle_loc_data", (Serializable) locations.get(0));
        this.mContext.startActivity(intent);
    }

    public void setMessageData(Map<String, List<?>> map) {
        if (this.isRcsOn) {
            this.mMessageData = this.mSelectionMsgItemData;
        }
    }
}
