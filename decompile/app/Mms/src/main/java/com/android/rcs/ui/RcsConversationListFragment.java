package com.android.rcs.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.amap.api.services.core.AMapException;
import com.android.mms.MmsConfig;
import com.android.mms.data.Conversation;
import com.android.mms.data.HwCustConversation.ParmWrapper;
import com.android.mms.ui.BaseConversationListFragment.BaseFragmentMenu;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.ConversationListFragment;
import com.android.mms.ui.twopane.RightPaneComposeMessageFragment;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.data.RcsConversationUtils;
import com.android.rcs.ui.RcsGroupChatDetailSettingFragment.GroupChatTopicWatcher;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.CspFragment;
import com.huawei.mms.ui.EditTextWithSmiley;
import com.huawei.mms.ui.EmuiMenu;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.rcs.commonInterface.metadata.PeerInformation;
import com.huawei.rcs.ui.RcsGroupChatComposeMessageActivity;
import com.huawei.rcs.util.RcsXmlParser;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsUtility;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RcsConversationListFragment {
    private static int POS_ADDRESS = 2;
    private static int POS_INDEX_TEXT = 6;
    private LruCache<Long, String> groupSubjectCache = null;
    private boolean isRcsEnable = RcsCommonConfig.isRCSSwitchOn();
    private Activity mActivity;
    private Context mContext;
    private String mGlobalGroupId;
    private int mNotificationId;
    private RcsSettingContent mRcsSettingsContentObserver = null;
    private View notifyView = null;
    private RcsLoginStatusChangeBroadCastReceiver rcsLoginStatusChangeBroadCastReceiver;
    private boolean userPressDelete = false;

    private class RcsLoginStatusChangeBroadCastReceiver extends BroadcastReceiver {
        private BaseFragmentMenu mMenuEx = null;

        public RcsLoginStatusChangeBroadCastReceiver(BaseFragmentMenu menu) {
            this.mMenuEx = menu;
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getExtras() != null) {
                int newStatus = intent.getExtras().getInt("new_status");
                MLog.i("RcsConversationListFragment", "newStatus==" + newStatus);
                if (newStatus == 1) {
                    this.mMenuEx.setItemEnabled(278927469, true);
                    RcsConversationListFragment.this.hideNotifyView();
                    RcsConversationListFragment.this.startUserGuide();
                    return;
                }
                this.mMenuEx.setItemEnabled(278927469, false);
                RcsConversationListFragment.this.showOrHideDisconnectedNotify(false);
            }
        }
    }

    private class RcsSettingContent extends ContentObserver {
        public RcsSettingContent() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            int mRcsSwitchStatus = 0;
            try {
                mRcsSwitchStatus = Secure.getInt(RcsConversationListFragment.this.mContext.getContentResolver(), "huawei_rcs_switcher", 1);
            } catch (Exception e) {
                MLog.e("RcsConversationListFragment", "RcsSettingContent : mRcsSwitchStatus = " + mRcsSwitchStatus);
            }
            if (mRcsSwitchStatus == 0) {
                RcsConversationListFragment.this.hideNotifyView();
            }
        }
    }

    public RcsConversationListFragment(Activity activity) {
        this.mActivity = activity;
        this.mContext = activity;
    }

    public boolean onCustomMenuItemClick(Fragment fragment, MenuItem item) {
        if (!this.isRcsEnable) {
            return false;
        }
        boolean ret = true;
        switch (item.getItemId()) {
            case 278927469:
                Intent contactIntent = new Intent();
                contactIntent.setAction("android.intent.action.PICK");
                contactIntent.setType("vnd.android.cursor.item/rcs_contacts_for_message");
                contactIntent.putExtra("com.huawei.community.action.MULTIPLE_PICK", true);
                contactIntent.putExtra("from_activity_key", 2);
                fragment.startActivityForResult(contactIntent, 109);
                break;
            default:
                ret = false;
                break;
        }
        return ret;
    }

    public void setRcsMenu(EmuiMenu menu, int id, int groupId, boolean isInLandscape) {
        if (this.isRcsEnable) {
            boolean z;
            menu.addMenu((int) R.id.mms_options, 278927469, (int) R.string.write_im_group_chat, groupId);
            boolean loginState = false;
            boolean isSendIm = RcsProfile.isRcsImServiceSwitchEnabled();
            if (isSendIm) {
                try {
                    if (RcsProfile.getRcsService() != null) {
                        loginState = RcsProfile.getRcsService().getLoginState();
                    }
                } catch (Exception e) {
                    MLog.e("RcsConversationListFragment", "setRcsMenu rcs service not run");
                }
            }
            if (isSendIm) {
                z = loginState;
            } else {
                z = false;
            }
            menu.setItemEnabled(278927469, z);
            if (isSendIm && loginState) {
                hideNotifyView();
            } else {
                showOrHideDisconnectedNotify(false);
            }
        }
    }

    public void registerOtherListenerOnCreate(BaseFragmentMenu menu) {
        if (this.isRcsEnable) {
            registerRcsLoginStatusChange(menu);
            if (RcsProfile.isShowDisconnectedNotify()) {
                if (this.mRcsSettingsContentObserver == null && RcsCommonConfig.isRCSSwitchOn()) {
                    this.mRcsSettingsContentObserver = new RcsSettingContent();
                }
                if (this.mRcsSettingsContentObserver != null) {
                    this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("huawei_rcs_switcher"), true, this.mRcsSettingsContentObserver);
                }
            }
        }
    }

    public void unRegisterOtherListenerOnDestroy() {
        if (this.isRcsEnable) {
            unRegisterRcsLoginStatusChange();
            if (RcsProfile.isShowDisconnectedNotify() && this.mRcsSettingsContentObserver != null) {
                this.mContext.getContentResolver().unregisterContentObserver(this.mRcsSettingsContentObserver);
                this.mRcsSettingsContentObserver = null;
            }
        }
    }

    public String getNewSelection(int token, Object cookie, String selection) {
        if (!this.isRcsEnable) {
            return selection;
        }
        if (AMapException.CODE_AMAP_CLIENT_SOCKET_TIMEOUT_EXCEPTION == token) {
            Collection<Long> xmsThreadIds = new ArrayList();
            Collection<Long> rcsThreadIds = new ArrayList();
            for (Long thread_id : (Collection) cookie) {
                long thread_id_value = thread_id.longValue();
                if (thread_id_value < 0) {
                    rcsThreadIds.add(Long.valueOf(-thread_id_value));
                } else {
                    xmsThreadIds.add(Long.valueOf(thread_id_value));
                }
            }
            xmsThreadIds.addAll(RcsConversationUtils.getHwCustUtils().getOtherThreadFromGivenThread(this.mContext, rcsThreadIds, 2));
            selection = Conversation.getThreadsSelection("thread_id", xmsThreadIds);
        }
        return selection;
    }

    public boolean openRcsThreadId(Conversation conv, boolean isPreview) {
        if (!this.isRcsEnable || conv == null || conv.getHwCust() == null) {
            return false;
        }
        String groupChatID = conv.getHwCust().getRcsGroupChatID();
        Intent intent;
        if (groupChatID == null || groupChatID.length() <= 0) {
            intent = RcsComposeMessage.createIntent(this.mContext, conv.getThreadId(), conv.getHwCust().getRcsThreadType());
            if (isPreview) {
                intent.putExtra("EXTRA_RCS_NEW_ID", conv.getHwCust().getRcsThreadId(conv));
                MmsConfig.addPreviewFlag(intent);
            }
            if (!HwMessageUtils.isSplitOn()) {
                this.mContext.startActivity(intent);
            } else if (this.mContext instanceof ConversationList) {
                HwBaseFragment fragment = new RightPaneComposeMessageFragment();
                fragment.setIntent(intent);
                ((ConversationList) this.mContext).openRightClearStack(fragment);
            }
        } else {
            intent = new Intent(this.mContext, RcsGroupChatComposeMessageActivity.class);
            intent.putExtra("bundle_group_id", groupChatID);
            if (isPreview) {
                intent.putExtra("EXTRA_RCS_NEW_ID", conv.getHwCust().getRcsThreadId(conv));
                MmsConfig.addPreviewFlag(intent);
            }
            if (!HwMessageUtils.isSplitOn()) {
                this.mContext.startActivity(intent);
            } else if (this.mContext instanceof ConversationList) {
                HwBaseFragment fragment2 = new RcsGroupChatComposeMessageFragment();
                fragment2.setIntent(intent);
                ((ConversationList) this.mContext).openRightClearStack(fragment2);
            }
        }
        return true;
    }

    public long getNewThreadId(Conversation conv, long threadId) {
        if (!this.isRcsEnable || conv == null || conv.getHwCust() == null) {
            return threadId;
        }
        return conv.getHwCust().getRcsThreadId(conv);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!this.isRcsEnable) {
            return;
        }
        if (resultCode != -1) {
            MLog.w("RcsConversationListFragment", "Fail due to resultCode=" + resultCode);
            return;
        }
        MLog.i("RcsConversationListFragment", "onActivityResult requestCode = " + requestCode);
        switch (requestCode) {
            case 109:
                if (data != null) {
                    setTopicAndCreateGroup(RcsGroupChatConversationDetailFragment.processPickIMcontactResult(this.mContext, data));
                    break;
                }
                return;
        }
    }

    public List<Long> getNewArrayList(List<Long> list) {
        if (this.isRcsEnable) {
            return new ArrayList(list);
        }
        return list;
    }

    private void registerRcsLoginStatusChange(BaseFragmentMenu menu) {
        if (RcsCommonConfig.isRCSSwitchOn() && this.rcsLoginStatusChangeBroadCastReceiver == null) {
            this.rcsLoginStatusChangeBroadCastReceiver = new RcsLoginStatusChangeBroadCastReceiver(menu);
        }
        if (this.rcsLoginStatusChangeBroadCastReceiver != null) {
            this.mContext.getApplicationContext().registerReceiver(this.rcsLoginStatusChangeBroadCastReceiver, new IntentFilter("com.huawei.rcs.loginstatus"), "com.huawei.rcs.RCS_BROADCASTER", null);
        }
    }

    private void unRegisterRcsLoginStatusChange() {
        if (this.rcsLoginStatusChangeBroadCastReceiver != null) {
            this.mContext.getApplicationContext().unregisterReceiver(this.rcsLoginStatusChangeBroadCastReceiver);
        }
    }

    private boolean setTopicAndCreateGroup(List<PeerInformation> members) {
        FragmentManager fm = this.mActivity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("RcsGroupCreateDialog");
        if (prev != null) {
            ft.remove(prev);
            ft.commit();
        }
        Bundle args = new Bundle();
        if (members instanceof ArrayList) {
            args.putParcelableArrayList("memberList", (ArrayList) members);
        }
        RcsGroupCreateDialog dialog = new RcsGroupCreateDialog();
        dialog.setArguments(args);
        dialog.show(fm, "RcsGroupCreateDialog");
        return true;
    }

    public static RcsGroupChatDialog setTopicAndCreateGroup(Context context, List<PeerInformation> members) {
        if (members == null || members.size() <= 0) {
            return null;
        }
        View inputTopicLayout = LayoutInflater.from(context).inflate(R.layout.common_phrase_modify_item, null);
        EditTextWithSmiley inputTopic = (EditTextWithSmiley) inputTopicLayout.findViewById(R.id.common_modify);
        inputTopic.setSingleLine(true);
        inputTopic.setFilters(new InputFilter[]{new LengthFilter(32)});
        inputTopic.append(context.getResources().getString(R.string.chat_topic_default));
        inputTopic.setSelectAllOnFocus(true);
        RcsGroupChatDialog rcsGroupChatDialog = new RcsGroupChatDialog(context, inputTopic);
        rcsGroupChatDialog.setTitle(R.string.chat_topic_hint);
        rcsGroupChatDialog.setIcon(17301659);
        rcsGroupChatDialog.setView(inputTopicLayout);
        rcsGroupChatDialog.setCanceledOnTouchOutside(false);
        inputTopic.addTextChangedListener(new GroupChatTopicWatcher(rcsGroupChatDialog));
        inputTopic.requestFocus();
        rcsGroupChatDialog.getWindow().setSoftInputMode(37);
        return rcsGroupChatDialog;
    }

    public void showGroupInviteDialogIfNeeded() {
        if (this.isRcsEnable && Boolean.valueOf(this.mActivity.getIntent().getBooleanExtra("isGroupInviteNotify", false)).booleanValue()) {
            Intent intent = this.mActivity.getIntent();
            intent.putExtra("isGroupInviteNotify", false);
            this.mActivity.setIntent(intent);
            Bundle bundle = this.mActivity.getIntent().getExtras();
            if (bundle != null) {
                this.mNotificationId = bundle.getInt("notificationId");
                this.mGlobalGroupId = bundle.getString("globalgroupId");
                FragmentManager fm = this.mActivity.getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Fragment prev = fm.findFragmentByTag("RcsGroupInviteDialog");
                if (prev != null) {
                    ft.remove(prev);
                    ft.commit();
                }
                Bundle args = new Bundle();
                args.putInt("notificationId", this.mNotificationId);
                args.putString("globalgroupId", this.mGlobalGroupId);
                args.putString("body", bundle.getString("body"));
                RcsGroupInviteDialog dialog = new RcsGroupInviteDialog();
                dialog.setArguments(args);
                dialog.show(fm, "RcsGroupInviteDialog");
            }
        }
    }

    public void inflateRcsDisconnectNotify(View rootView) {
        if (this.isRcsEnable && shouldShowRcsDisconnectNotify()) {
            ViewStub disconnectNotify = (ViewStub) rootView.findViewById(R.id.stub_rcs_disconnect_notify);
            if (disconnectNotify != null) {
                disconnectNotify.setLayoutResource(R.layout.rcs_disconnect_notify_layout);
                this.notifyView = disconnectNotify.inflate();
                if (this.notifyView != null) {
                    showOrHideDisconnectedNotify(true);
                }
            }
        }
    }

    private boolean checkRcsLoginState() {
        boolean z = false;
        if (RcsProfile.getRcsService() == null) {
            return false;
        }
        try {
            z = RcsProfile.getRcsService().getLoginState();
            MLog.i("RcsConversationListFragment", "checkRcsLoginState = " + z);
            return z;
        } catch (RemoteException e) {
            MLog.e("RcsConversationListFragment", "checkRcsLoginState rcs service not run");
            return z;
        }
    }

    private void showNotifyView() {
        if (this.notifyView != null && !this.userPressDelete) {
            this.notifyView.setVisibility(0);
        }
    }

    private void hideNotifyView() {
        if (this.notifyView != null) {
            this.notifyView.setVisibility(8);
        }
        this.userPressDelete = false;
    }

    private boolean shouldShowRcsDisconnectNotify() {
        if (this.isRcsEnable) {
            return RcsXmlParser.getBoolean("show_rcs_disconnect_notify_in_MMS", false);
        }
        return false;
    }

    public void startUserGuide() {
        if (this.isRcsEnable) {
            boolean loginState = false;
            try {
                if (RcsProfile.getRcsService() != null) {
                    loginState = RcsProfile.getRcsService().getLoginState();
                }
            } catch (Exception e) {
                MLog.e("RcsConversationListFragment", "startUserGuide rcs service not run");
            }
            if (loginState) {
                boolean isActive = false;
                Fragment fm = this.mActivity.getFragmentManager().findFragmentByTag("cl_fragment_tag");
                if (fm != null && (fm instanceof ConversationListFragment)) {
                    ConversationListFragment conversationFm = (ConversationListFragment) fm;
                    isActive = CspFragment.isFragmentActived();
                }
                boolean isActionValid = !"com.kris.contasts.file.trans.action".equals(this.mActivity.getIntent().getAction());
                if (isActive && isActionValid) {
                    RcsUserGuideFragment.startUserGuide(this.mActivity, 1);
                }
            }
        }
    }

    public void setActionValid() {
        if (this.isRcsEnable) {
            Log.d("RcsConversationListFragment", "setActionValid");
        }
    }

    public void onStop() {
        if (this.isRcsEnable) {
            setActionValid();
            RcsUtility.clearGroupNameCache();
        }
    }

    public void onContentChanged() {
        if (this.isRcsEnable) {
            RcsUtility.clearGroupNameCache();
        }
    }

    private void showOrHideDisconnectedNotify(boolean isCheckState) {
        boolean loginState = false;
        if (isCheckState) {
            loginState = checkRcsLoginState();
        }
        boolean hideView = !loginState ? !RcsProfile.isRcsSwitchEnabled() ? RcsProfile.isShowDisconnectedNotify() : false : true;
        if (hideView) {
            hideNotifyView();
        } else {
            showNotifyView();
        }
    }

    public void clearRcsGroupSubject() {
        if (this.isRcsEnable && this.groupSubjectCache != null) {
            this.groupSubjectCache.evictAll();
        }
    }

    public boolean isRcsTable(int tableToUse) {
        boolean z = true;
        if (!this.isRcsEnable) {
            return false;
        }
        if (!(tableToUse == 100 || tableToUse == SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE || tableToUse == 301 || tableToUse == 302)) {
            z = false;
        }
        return z;
    }

    public String getAddress(Context context, int tableToUse, long threadId, Cursor cursor) {
        if (!this.isRcsEnable) {
            return null;
        }
        switch (tableToUse) {
            case 100:
                return Conversation.get(context, threadId, true, new ParmWrapper(null, Integer.valueOf(tableToUse))).getRecipients().formatNames(", ");
            case SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE /*200*/:
                return getGroupSubject(context, threadId);
            case 301:
            case 302:
                if (cursor != null) {
                    return cursor.getString(POS_ADDRESS);
                }
                break;
        }
        return null;
    }

    private String getGroupSubject(Context context, long groupThreadId) {
        if (this.groupSubjectCache == null) {
            this.groupSubjectCache = new LruCache(VTMCDataCache.MAXSIZE);
        }
        String subject = (String) this.groupSubjectCache.get(Long.valueOf(groupThreadId));
        if (subject == null) {
            MLog.d("RcsConversationListFragment", "query groupChat subject from DB");
            if (RcsConversationUtils.getHwCustUtils() != null) {
                subject = RcsConversationUtils.getHwCustUtils().getGroupSubjectByThreadID(context, groupThreadId);
            }
            if (TextUtils.isEmpty(subject)) {
                subject = context.getString(R.string.chat_topic_default);
            }
            this.groupSubjectCache.put(Long.valueOf(groupThreadId), subject);
        }
        return subject;
    }

    public String getMatch(int tableToUse, Cursor cursor) {
        if (!this.isRcsEnable) {
            return null;
        }
        switch (tableToUse) {
            case 301:
            case 302:
                if (cursor != null) {
                    return HwMessageUtils.formatRegexString(cursor.getString(POS_INDEX_TEXT));
                }
                break;
        }
        return null;
    }

    public boolean isThread(int tableToUse, boolean defaultValue) {
        boolean z = true;
        if (!this.isRcsEnable) {
            return defaultValue;
        }
        if (!(tableToUse == 301 || tableToUse == 302)) {
            z = false;
        }
        return z;
    }
}
