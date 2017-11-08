package com.android.rcs.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.RemoteException;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.widget.CursorAdapter;
import android.widget.EditText;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.util.MccMncConfig;
import com.huawei.mms.util.NumberUtils.AddrMatcher;
import com.huawei.rcs.commonInterface.IfMsgplus;
import com.huawei.rcs.commonInterface.metadata.PeerInformation;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsTransaction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public abstract class RcsBaseForwarder<T> {
    protected final boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();
    protected boolean isSameMemberForward = true;
    protected Context mContext;
    protected CursorAdapter mCursorAdapter;
    protected Handler mForwardMessageHandler = new Handler();
    protected HwBaseFragment mFragment;
    protected T mMessageData;
    protected int mMsgKind;
    protected IfMsgplus mRcsService = RcsProfile.getRcsService();
    protected Integer[] mSelectionStorage = null;

    public abstract void dispatchGroupChatIntent(String str);

    public abstract void processMassOrSingle(List<String> list, int i);

    public abstract void setMessageData(T t);

    public void setFragment(HwBaseFragment fragment) {
        if (this.isRcsOn) {
            this.mFragment = fragment;
            this.mContext = fragment.getContext();
        }
    }

    public void setMessageListAdapter(CursorAdapter adapter) {
        if (this.isRcsOn) {
            this.mCursorAdapter = adapter;
        }
    }

    public void setMessageKind(int kind) {
        if (this.isRcsOn) {
            this.mMsgKind = kind;
        }
    }

    public void setSelection(Integer[] selection) {
        if (!this.isRcsOn) {
            return;
        }
        if (selection == null) {
            MLog.w("RcsBaseForwarder", "selection is null.");
            this.mSelectionStorage = null;
            return;
        }
        if (this.mSelectionStorage != null) {
            this.mSelectionStorage = null;
        }
        this.mSelectionStorage = new Integer[selection.length];
        for (int i = 0; i < selection.length; i++) {
            this.mSelectionStorage[i] = selection[i];
        }
    }

    public void launchContactsPicker(int requestCode, T messageData) {
        if (!this.isRcsOn) {
            return;
        }
        if (this.mFragment == null) {
            MLog.w("RcsBaseForwarder", "forwardMessage: mFragment is null.");
            return;
        }
        int maxCount = MmsConfig.getRecipientLimit();
        Intent contactIntent = new Intent();
        contactIntent.setAction("android.intent.action.PICK");
        contactIntent.setType("vnd.android.cursor.dir/phone_v2");
        contactIntent.putExtra("com.huawei.community.action.ADD_EMAIL", true);
        contactIntent.putExtra("com.huawei.community.action.MULTIPLE_PICK", true);
        contactIntent.putExtra("com.huawei.community.action.MAX_SELECT_COUNT", maxCount);
        setMessageData(messageData);
        this.mFragment.startActivityForResult(contactIntent, requestCode);
    }

    public void rcsActivityResult(final Intent data) {
        if (this.isRcsOn && data != null) {
            new Thread(new Runnable() {
                public void run() {
                    List<String> rcsList = new ArrayList();
                    HashMap<String, String> map = new HashMap();
                    ContactList list = RcsBaseForwarder.this.processPickResult(data);
                    RcsBaseForwarder.this.isSameMemberForward = true;
                    ContactList contacts = null;
                    ArrayList<String> nameList = data.getStringArrayListExtra("address");
                    if (nameList != null) {
                        contacts = ContactList.getByNumbers(nameList, false);
                    }
                    if (list != null) {
                        Iterator<Contact> iter = list.iterator();
                        String LoginNumInList = "";
                        while (iter.hasNext()) {
                            Contact contact = (Contact) iter.next();
                            String orgNubmer = contact.getNumber();
                            String name = contact.getName();
                            if (!Contact.isEmailAddress(MccMncConfig.getFilterNumberByMCCMNC(orgNubmer))) {
                                rcsList.add(orgNubmer);
                                map.put(orgNubmer, name);
                            }
                            if (contacts == null || !contacts.contains(contact)) {
                                RcsBaseForwarder.this.isSameMemberForward = false;
                            }
                            MLog.d("RcsBaseForwarder", "isSameMemberForward = " + RcsBaseForwarder.this.isSameMemberForward);
                        }
                        if (rcsList.size() > 0) {
                            int rcsUsers = 0;
                            int maxGroupSize = 0;
                            String currentLoginNum = "";
                            if (RcsBaseForwarder.this.mRcsService != null) {
                                try {
                                    maxGroupSize = RcsBaseForwarder.this.mRcsService.getMaxGroupMemberSize();
                                    currentLoginNum = RcsBaseForwarder.this.mRcsService.getCurrentLoginUserNumber();
                                } catch (RemoteException e) {
                                    MLog.e("RcsBaseForwarder", "RcsService RemoteException");
                                }
                                for (String number : rcsList) {
                                    String normalizeKey = PhoneNumberUtils.normalizeNumber(number);
                                    try {
                                        if (RcsBaseForwarder.this.mRcsService.isRcsUeser(normalizeKey)) {
                                            rcsUsers++;
                                        }
                                        if (AddrMatcher.isNumberMatch(currentLoginNum, normalizeKey) > 0) {
                                            LoginNumInList = number;
                                        }
                                    } catch (RemoteException e2) {
                                        MLog.e("RcsBaseForwarder", "RcsService RemoteException");
                                    }
                                }
                                if (rcsUsers == rcsList.size() && rcsList.contains(LoginNumInList)) {
                                    if (rcsUsers > 2) {
                                        rcsList.remove(LoginNumInList);
                                        map.remove(LoginNumInList);
                                    }
                                    rcsUsers--;
                                }
                            }
                            RcsBaseForwarder.this.handleRcsActivityResult(rcsList, map, maxGroupSize, rcsUsers);
                        }
                    }
                }
            }).start();
            MLog.d("RcsBaseForwarder", "forwardMsgForFt end.");
        }
    }

    private ContactList processPickResult(Intent data) {
        ArrayList<Uri> uriList = data.getParcelableArrayListExtra("SelItemData_KeyValue");
        if (uriList == null) {
            uriList = new ArrayList();
            uriList.add(data.getData());
        }
        if (uriList.size() == 0) {
            MLog.w("RcsBaseForwarder", "processPickResult uriList is null.");
            return null;
        }
        Parcelable[] uris = new Parcelable[uriList.size()];
        for (int i = 0; i < uriList.size(); i++) {
            uris[i] = (Parcelable) uriList.get(i);
        }
        ContactList<Contact> list = ContactList.blockingGetByUris(uris);
        ContactList<Contact> uniquelyList = new ContactList();
        boolean isContain = false;
        boolean hasDuplicate = false;
        for (Contact c : list) {
            String number = c.getOriginNumber();
            if (TextUtils.isEmpty(number)) {
                number = c.getNumber();
            }
            for (Contact contact : uniquelyList) {
                if (contact.getKey() == c.getKey()) {
                    if (!contact.isEmail() || !c.isEmail()) {
                        if (!(contact.isEmail() || c.isEmail() || AddrMatcher.isNumberMatch(number, contact.getNumber()) <= 0)) {
                            isContain = true;
                            break;
                        }
                    } else if (c.getNumber().equals(contact.getNumber())) {
                        isContain = true;
                        break;
                    }
                }
            }
            if (isContain) {
                hasDuplicate = true;
                isContain = false;
            } else {
                if (hasDuplicate) {
                    MLog.d("RcsBaseForwarder", "Has duplicate contact.");
                }
                uniquelyList.add(c);
            }
        }
        return uniquelyList;
    }

    protected void handleRcsActivityResult(final List<String> rcsList, final HashMap<String, String> personMap, int maxGroupSize, final int rcsUsers) {
        if (this.isRcsOn) {
            this.mForwardMessageHandler.post(new Runnable() {
                public void run() {
                    MLog.d("RcsBaseForwarder", "list size:" + rcsList.size());
                    MLog.d("RcsBaseForwarder", "rcs user size:" + rcsUsers);
                    if (rcsList.size() != rcsUsers || rcsList.size() <= 1) {
                        RcsBaseForwarder.this.processMassOrSingle(rcsList, rcsUsers);
                    } else {
                        RcsBaseForwarder.this.showCreateGroupChatDialog(RcsBaseForwarder.this.mContext, personMap);
                    }
                }
            });
        }
    }

    private String createGroupChat(String topic, List<PeerInformation> members) {
        String groupId = "";
        try {
            if (this.mRcsService != null) {
                groupId = this.mRcsService.createGroup(topic, members);
            }
        } catch (RemoteException e) {
            MLog.e("RcsBaseForwarder", "RcsService remote exception");
        }
        RcsTransaction.requesetCapabilitybeforeGroupChat(members);
        return groupId;
    }

    protected void showCreateGroupChatDialog(final Context context, HashMap<String, String> personMap) {
        if (this.isRcsOn) {
            final List<PeerInformation> peerList = new ArrayList();
            for (Entry<String, String> entry : personMap.entrySet()) {
                PeerInformation p = new PeerInformation();
                p.setNumber((String) entry.getKey());
                p.setName((String) entry.getValue());
                peerList.add(p);
            }
            RcsGroupChatDialog dialog = RcsConversationListFragment.setTopicAndCreateGroup(context, peerList);
            if (dialog != null) {
                final EditText etTopic = dialog.getTvTopic();
                dialog.setButton(-1, context.getString(R.string.yes), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String groupId = "";
                        String chatTopic = etTopic.getText().toString();
                        if (chatTopic.trim().length() == 0) {
                            chatTopic = context.getString(R.string.chat_topic_default);
                        }
                        RcsBaseForwarder.this.dispatchGroupChatIntent(RcsBaseForwarder.this.createGroupChat(chatTopic, peerList));
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        }
    }

    protected Intent getGroupSmsIntent(List<String> addrList, Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        sb.append("smsto:");
        for (int i = 0; i < addrList.size(); i++) {
            sb.append((String) addrList.get(i)).append(";");
        }
        sb.deleteCharAt(sb.length() - 1);
        Uri uri = Uri.parse(sb.toString());
        String nonRcsUser = "";
        for (String number : addrList) {
            try {
                String normalizeKey = PhoneNumberUtils.normalizeNumber(number);
                if (this.mRcsService != null && !this.mRcsService.isRcsUeser(normalizeKey)) {
                    nonRcsUser = normalizeKey;
                    break;
                }
            } catch (RemoteException e) {
                MLog.e("RcsBaseForwarder", "RemoteException error:");
            }
        }
        Intent chatIntent = new Intent("android.intent.action.SENDTO", uri);
        chatIntent.putExtras(bundle);
        chatIntent.putExtra("Contacts", "RCS_FT");
        chatIntent.putExtra("ADDRESS", nonRcsUser);
        return chatIntent;
    }

    protected Intent getChatIntent(List<String> addrList, Bundle bundle) {
        String number = (String) addrList.get(0);
        boolean canSendFt = false;
        try {
            if (this.mRcsService != null) {
                canSendFt = this.mRcsService.isFtAvailable(number) ? RcsProfile.isRcsServiceEnabledAndUserLogin() : false;
            }
        } catch (RemoteException e) {
            MLog.e("RcsBaseForwarder", "RemoteException error:");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("smsto:");
        for (int i = 0; i < addrList.size(); i++) {
            sb.append((String) addrList.get(i)).append(";");
        }
        sb.deleteCharAt(sb.length() - 1);
        Intent chatIntent = new Intent("android.intent.action.SENDTO", Uri.parse(sb.toString()));
        chatIntent.setClass(this.mContext, ComposeMessageActivity.class);
        chatIntent.putExtras(bundle);
        chatIntent.putExtra("Contacts", "RCS_FT");
        chatIntent.putExtra("ADDRESS", (String) addrList.get(0));
        chatIntent.addFlags(131072);
        if (!(!canSendFt || RcsTransaction.isSupportFtOutDate() || bundle.containsKey("android.intent.extra.TEXT"))) {
            chatIntent.putExtra("send_mode", 1);
            chatIntent.putExtra("force_set_send_mode", true);
        }
        return chatIntent;
    }

    protected Intent getLocIntent(List<String> addrList) {
        StringBuilder sb = new StringBuilder();
        sb.append("smsto:");
        for (int i = 0; i < addrList.size(); i++) {
            sb.append((String) addrList.get(i)).append(";");
        }
        sb.deleteCharAt(sb.length() - 1);
        Intent locIntent = new Intent("android.intent.action.SENDTO", Uri.parse(sb.toString()));
        locIntent.putExtra("ADDRESS", (String) addrList.get(0));
        locIntent.addFlags(131072);
        return locIntent;
    }
}
