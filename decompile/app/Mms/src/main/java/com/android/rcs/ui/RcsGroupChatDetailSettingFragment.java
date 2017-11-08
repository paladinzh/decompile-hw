package com.android.rcs.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.amap.api.services.core.AMapException;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.EmuiSwitchPreference;
import com.android.mms.util.DraftCache;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.ui.EditTextWithSmiley;
import com.huawei.mms.ui.HwPreferenceFragment;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.NumberUtils.AddrMatcher;
import com.huawei.rcs.commonInterface.IfMsgplusCb;
import com.huawei.rcs.commonInterface.IfMsgplusCb.Stub;
import com.huawei.rcs.commonInterface.metadata.PeerInformation;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsTransaction;
import com.huawei.rcs.utils.RcseMmsExt;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RcsGroupChatDetailSettingFragment extends HwPreferenceFragment implements OnPreferenceClickListener {
    private GroupChatCreateBroadCastReceiver groupChatCreateBroadCastReceiver;
    private GroupChatStatusChangeBroadCastReceiver groupChatStatusChangeBroadCastReceiver;
    private Activity mActivity;
    private ContentResolver mContentResolver;
    private RcsGroupExitPreference mExitGroupPreference;
    private String mGroupID = "";
    private RcsGroupMemberAdapter mGroupMemberAdapter = null;
    private RcsGroupMemberPrefrence mGroupMemberPrefrence;
    private String mGroupName;
    private EmuiSwitchPreference mGroupNotifyPreference;
    private boolean mIsGroupchatCreateSucc = false;
    private boolean mIsInvalidGroupStatus = false;
    private HashMap<Integer, IfMsgplusCbImpl> mMsgplusListeners = new HashMap();
    private int mNotifySilent = 0;
    private OnPreferenceChangeListener mOnPreferenceChangeListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object obj) {
            if (!"pref_key_notify_silent".equals(preference.getKey())) {
                return false;
            }
            MLog.d("RcsGroupChatDetailSettingFragment", "onPreferenceChange,isNotifySilent=" + obj);
            int isNotifySilent = RcsGroupChatDetailSettingFragment.this.getNotifySilentValue(obj);
            RcsGroupChatDetailSettingFragment.this.saveNotifiedToDB(RcsGroupChatDetailSettingFragment.this.mGroupID, isNotifySilent);
            RcsGroupChatDetailSettingFragment.this.mNotifySilent = isNotifySilent;
            return true;
        }
    };
    private long mRcsThreadID = 0;
    private Handler mRcseEventHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.obj != null) {
                switch (msg.what) {
                    case AMapException.CODE_AMAP_SERVICE_MISSING_REQUIRED_PARAMS /*1201*/:
                        RcsGroupChatDetailSettingFragment.this.updateViewStatus();
                        break;
                }
            }
        }
    };
    private Intent mResultIntent = new Intent();
    private long mThreadID = 0;
    private Preference mUpadateTopicPreference;

    private class GroupChatCreateBroadCastReceiver extends BroadcastReceiver {
        private GroupChatCreateBroadCastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("com.huawei.groupchat.create".equals(intent.getAction()) && intent.hasExtra("groupId")) {
                String tmpGroupId = intent.getStringExtra("groupId");
                if (tmpGroupId != null && tmpGroupId.equals(RcsGroupChatDetailSettingFragment.this.mGroupID)) {
                    RcsGroupChatDetailSettingFragment.this.mIsGroupchatCreateSucc = true;
                    RcsGroupChatDetailSettingFragment.this.updateViewStatus();
                    MLog.d("RcsGroupChatDetailSettingFragment", "GroupChatCreateBroadCastReceiver mIsGroupchatCreateSucc = " + RcsGroupChatDetailSettingFragment.this.mIsGroupchatCreateSucc);
                }
            }
        }
    }

    private static class GroupChatPrivilegedAction implements PrivilegedAction {
        private Field mField;

        public GroupChatPrivilegedAction(Field field) {
            this.mField = field;
        }

        public Object run() {
            this.mField.setAccessible(true);
            return null;
        }
    }

    private class GroupChatStatusChangeBroadCastReceiver extends BroadcastReceiver {
        private GroupChatStatusChangeBroadCastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && intent.getAction().equals("com.huawei.rcs.message.groupcreated")) {
                String groupId = intent.getStringExtra("groupId");
                if (groupId != null && groupId.equals(RcsGroupChatDetailSettingFragment.this.mGroupID)) {
                    MLog.i("RcsGroupChatDetailSettingFragment", "GroupChatStatusChangeBroadCastReceiver mIsInvalidGroupStatus before=" + RcsGroupChatDetailSettingFragment.this.mIsInvalidGroupStatus);
                    RcsGroupChatDetailSettingFragment.this.mIsInvalidGroupStatus = RcseMmsExt.checkInvalidGroupStatus(RcsProfile.getRcsGroupStatus(groupId));
                    RcsGroupChatDetailSettingFragment.this.updateViewStatus();
                    MLog.i("RcsGroupChatDetailSettingFragment", "GroupChatStatusChangeBroadCastReceiver mIsInvalidGroupStatus after=" + RcsGroupChatDetailSettingFragment.this.mIsInvalidGroupStatus);
                }
            }
        }
    }

    public static class GroupChatTopicWatcher implements TextWatcher {
        private AlertDialog mAlertDialog;

        public GroupChatTopicWatcher(AlertDialog alertDialog) {
            this.mAlertDialog = alertDialog;
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (TextUtils.isEmpty(s.toString().replaceAll(" ", ""))) {
                this.mAlertDialog.getButton(-1).setEnabled(false);
            } else {
                this.mAlertDialog.getButton(-1).setEnabled(true);
            }
        }

        public void afterTextChanged(Editable s) {
            TextView tv = (TextView) this.mAlertDialog.findViewById(R.id.common_modify);
            String content = tv.getText().toString();
            if (!TextUtils.isEmpty(content) && content.length() >= 32) {
                tv.setError(this.mAlertDialog.getContext().getString(R.string.topic_max_tips));
            }
        }
    }

    private class IfMsgplusCbImpl extends Stub {
        private int mEventListener = 0;

        IfMsgplusCbImpl(int event) {
            this.mEventListener = event;
            RcsGroupChatDetailSettingFragment.this.mMsgplusListeners.put(Integer.valueOf(this.mEventListener), this);
        }

        public void handleEvent(int wEvent, Bundle bundle) throws RemoteException {
            if (wEvent == this.mEventListener) {
                Message msg = RcsGroupChatDetailSettingFragment.this.mRcseEventHandler.obtainMessage(wEvent);
                msg.obj = bundle;
                RcsGroupChatDetailSettingFragment.this.mRcseEventHandler.sendMessage(msg);
            }
        }
    }

    private void queryGroupInfoFromDB() {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r12 = this;
        r0 = "content://rcsim/rcs_groups";
        r0 = android.net.Uri.parse(r0);
        r10 = r0.buildUpon();
        r7 = 0;
        r0 = r12.mActivity;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r1 = r10.build();	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r2 = 2;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r2 = new java.lang.String[r2];	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r3 = "is_groupchat_notify_silent";	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r4 = 0;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r2[r4] = r3;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r3 = "chat_uri";	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r4 = 1;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r2[r4] = r3;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r3 = "name = ?";	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r4 = 1;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r4 = new java.lang.String[r4];	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r5 = r12.mGroupID;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r11 = 0;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r4[r11] = r5;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r5 = 0;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r7 = com.huawei.cspcommon.ex.SqliteWrapper.query(r0, r1, r2, r3, r4, r5);	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        if (r7 == 0) goto L_0x0082;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
    L_0x0033:
        r0 = r7.getCount();	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        if (r0 <= 0) goto L_0x0082;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
    L_0x0039:
        r0 = r7.moveToFirst();	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        if (r0 == 0) goto L_0x0082;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
    L_0x003f:
        r0 = com.huawei.rcs.utils.RcsTransaction.isEnableGroupSilentMode();	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        if (r0 == 0) goto L_0x0052;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
    L_0x0045:
        r0 = "is_groupchat_notify_silent";	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r0 = r7.getColumnIndex(r0);	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r0 = r7.getInt(r0);	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r12.mNotifySilent = r0;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
    L_0x0052:
        r0 = "chat_uri";	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r6 = r7.getColumnIndexOrThrow(r0);	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r9 = r7.getString(r6);	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r0 = android.text.TextUtils.isEmpty(r9);	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        if (r0 != 0) goto L_0x0088;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
    L_0x0063:
        r0 = 1;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r12.mIsGroupchatCreateSucc = r0;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
    L_0x0066:
        r0 = "RcsGroupChatDetailSettingFragment";	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r1 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r1.<init>();	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r2 = "get is_groupchat_notify_silent=";	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r1 = r1.append(r2);	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r2 = r12.mNotifySilent;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r1 = r1.append(r2);	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r1 = r1.toString();	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        com.huawei.cspcommon.MLog.d(r0, r1);	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
    L_0x0082:
        if (r7 == 0) goto L_0x0087;
    L_0x0084:
        r7.close();
    L_0x0087:
        return;
    L_0x0088:
        r0 = 0;
        r12.mIsGroupchatCreateSucc = r0;	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        goto L_0x0066;
    L_0x008c:
        r8 = move-exception;
        r0 = "RcsGroupChatDetailSettingFragment";	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        r1 = "get is_groupchat_notify_silent error";	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        com.huawei.cspcommon.MLog.e(r0, r1);	 Catch:{ RuntimeException -> 0x008c, all -> 0x009c }
        if (r7 == 0) goto L_0x0087;
    L_0x0098:
        r7.close();
        goto L_0x0087;
    L_0x009c:
        r0 = move-exception;
        if (r7 == 0) goto L_0x00a2;
    L_0x009f:
        r7.close();
    L_0x00a2:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.rcs.ui.RcsGroupChatDetailSettingFragment.queryGroupInfoFromDB():void");
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.rcs_groupchat_setting_preferences);
        this.mActivity = getActivity();
        this.mContentResolver = this.mActivity.getContentResolver();
        Intent intent = HwMessageUtils.isSplitOn() ? getIntent() : this.mActivity.getIntent();
        if (intent != null) {
            this.mGroupID = intent.getStringExtra("bundle_group_id");
            this.mThreadID = intent.getLongExtra("bundle_thread_id", 0);
            this.mRcsThreadID = intent.getLongExtra("bundle_rcs_thread_id", 0);
            this.mGroupName = intent.getStringExtra("bundle_group_name");
        }
        if (TextUtils.isEmpty(this.mGroupName)) {
            this.mGroupName = getString(R.string.chat_topic_default);
        }
        if (RcsProfile.getRcsService() != null && RcsProfile.isRcsImServiceSwitchEnabled()) {
            RcsProfile.registerRcsCallBack(Integer.valueOf(AMapException.CODE_AMAP_SERVICE_MISSING_REQUIRED_PARAMS), new IfMsgplusCbImpl(AMapException.CODE_AMAP_SERVICE_MISSING_REQUIRED_PARAMS));
        }
        queryGroupInfoFromDB();
        setMessagePreferences();
        this.mIsInvalidGroupStatus = RcseMmsExt.checkInvalidGroupStatus(RcsProfile.getRcsGroupStatus(this.mGroupID));
        registerGroupChatStatusChangeBroadCast();
        registerGroupChatCreateBroadCast();
    }

    public void onResume() {
        super.onResume();
        if (this.mGroupMemberAdapter != null) {
            showSummary();
            this.mGroupMemberAdapter.notifyDataSetChanged();
        }
    }

    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getListView().setVerticalScrollBarEnabled(false);
        getListView().setDivider(null);
    }

    private void showSummary() {
        if (TextUtils.isEmpty(this.mGroupName)) {
            this.mUpadateTopicPreference.setSummary(getString(R.string.chat_topic_default));
        } else {
            this.mUpadateTopicPreference.setSummary(this.mGroupName);
        }
        updateViewStatus();
    }

    public void onStart() {
        super.onStart();
    }

    public void onStop() {
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
        unRegisterGroupChatStatusChangeBroadCast();
        unRegisterGroupChatCreateBroadCast();
        if (RcsProfile.getRcsService() != null && RcsProfile.isRcsImServiceSwitchEnabled()) {
            RcsProfile.unregisterRcsCallBack(Integer.valueOf(AMapException.CODE_AMAP_SERVICE_MISSING_REQUIRED_PARAMS), (IfMsgplusCb) this.mMsgplusListeners.remove(Integer.valueOf(AMapException.CODE_AMAP_SERVICE_MISSING_REQUIRED_PARAMS)));
        }
        if (this.mGroupMemberAdapter != null) {
            this.mGroupMemberAdapter.removeLoaderTask();
            this.mGroupMemberAdapter.changeCursor(null);
        }
    }

    private void setMessagePreferences() {
        boolean z = false;
        this.mUpadateTopicPreference = findPreference("pref_key_update_group_title");
        this.mUpadateTopicPreference.setOnPreferenceClickListener(this);
        this.mGroupMemberPrefrence = (RcsGroupMemberPrefrence) findPreference("pref_key_groupmember");
        if (this.mGroupMemberPrefrence != null) {
            this.mGroupMemberAdapter = new RcsGroupMemberAdapter(this.mActivity, null, false, this.mThreadID);
            this.mGroupMemberAdapter.setFragment(this);
            this.mGroupMemberPrefrence.setAdapter(this.mGroupMemberAdapter);
        }
        this.mExitGroupPreference = (RcsGroupExitPreference) findPreference("pref_key_exit_group");
        this.mExitGroupPreference.setOnPreferenceClickListener(this);
        this.mGroupNotifyPreference = (EmuiSwitchPreference) findPreference("pref_key_notify_silent");
        if (RcsTransaction.isEnableGroupSilentMode()) {
            this.mGroupNotifyPreference.setOnPreferenceChangeListener(this.mOnPreferenceChangeListener);
            EmuiSwitchPreference emuiSwitchPreference = this.mGroupNotifyPreference;
            if (this.mNotifySilent > 0) {
                z = true;
            }
            emuiSwitchPreference.setChecked(z);
            return;
        }
        ((PreferenceGroup) findPreference("pref_key_group_settings")).removePreference(this.mGroupNotifyPreference);
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private final void setDialogCancelable(DialogInterface dialog, boolean cancelable, boolean needCancel) {
        try {
            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
            AccessController.doPrivileged(new GroupChatPrivilegedAction(field));
            field.set(dialog, Boolean.valueOf(cancelable));
        } catch (NoSuchFieldException e) {
            MLog.e("RcsGroupChatDetailSettingFragment", "setDialogCancelable NoSuchFieldException");
        } catch (SecurityException e2) {
            MLog.e("RcsGroupChatDetailSettingFragment", "setDialogCancelable SecurityException");
        } catch (IllegalAccessException e3) {
            MLog.e("RcsGroupChatDetailSettingFragment", "setDialogCancelable IllegalAccessException");
        }
        if (cancelable && needCancel) {
            dialog.cancel();
        }
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("pref_key_update_group_title")) {
            View inputTopicLayout = getActivity().getLayoutInflater().inflate(R.layout.common_phrase_modify_item, null);
            final EditText inputTopic = (EditTextWithSmiley) inputTopicLayout.findViewById(R.id.common_modify);
            inputTopic.setSingleLine(true);
            inputTopic.setFilters(new InputFilter[]{new LengthFilter(32)});
            EditText inputTopics = inputTopic;
            inputTopic.setHint(this.mActivity.getResources().getString(R.string.chat_topic_default));
            inputTopic.append(this.mUpadateTopicPreference.getSummary());
            inputTopic.setSelectAllOnFocus(true);
            AlertDialog alertDialog = new Builder(getActivity()).setTitle(R.string.chat_topic_hint).setIcon(17301659).setView(inputTopicLayout).setPositiveButton(R.string.yes, new OnClickListener(inputTopic) {
                final /* synthetic */ EditText val$inputTopic;

                public void onClick(DialogInterface dialog, int which) {
                    RcsGroupChatDetailSettingFragment.this.hideKeyboard(this.val$inputTopic);
                    try {
                        String nTopic = inputTopic.getText().toString();
                        if (TextUtils.isEmpty(nTopic.replaceAll(" ", ""))) {
                            inputTopic.setText("");
                            inputTopic.setSelection(0);
                            RcsGroupChatDetailSettingFragment.this.setDialogCancelable(dialog, false, false);
                            return;
                        }
                        RcsGroupChatDetailSettingFragment.this.setDialogCancelable(dialog, true, false);
                        if (RcsProfile.getRcsService() != null) {
                            RcsGroupChatDetailSettingFragment.this.mGroupName = nTopic;
                            RcsProfile.getRcsService().updateGroupTopic(RcsGroupChatDetailSettingFragment.this.mGroupID, nTopic);
                            RcsGroupChatDetailSettingFragment.this.mUpadateTopicPreference.setSummary(nTopic);
                            RcsGroupChatDetailSettingFragment.this.mResultIntent.putExtra("bundle_group_name", nTopic);
                            if (HwMessageUtils.isSplitOn()) {
                                ((ConversationList) RcsGroupChatDetailSettingFragment.this.getActivity()).setSplitResultData(SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE, -1, RcsGroupChatDetailSettingFragment.this.mResultIntent);
                            } else {
                                RcsGroupChatDetailSettingFragment.this.getActivity().setResult(-1, RcsGroupChatDetailSettingFragment.this.mResultIntent);
                            }
                        }
                        Contact.clear(RcsGroupChatDetailSettingFragment.this.mActivity);
                    } catch (RemoteException e) {
                        MLog.e("RcsGroupChatDetailSettingFragment", "updateGroupTopic error");
                    }
                }
            }).setNegativeButton(R.string.cancel_btn, new OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    RcsGroupChatDetailSettingFragment.this.hideKeyboard(inputTopic);
                    RcsGroupChatDetailSettingFragment.this.setDialogCancelable(dialog, true, true);
                }
            }).create();
            inputTopic.addTextChangedListener(new GroupChatTopicWatcher(alertDialog));
            inputTopic.requestFocus();
            alertDialog.getWindow().setSoftInputMode(37);
            alertDialog.show();
            alertDialog.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    RcsGroupChatDetailSettingFragment.this.setDialogCancelable(dialog, true, true);
                }
            });
        } else if ("pref_key_exit_group".equals(preference.getKey())) {
            exitRcsGroupChat();
        }
        return false;
    }

    private void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) this.mActivity.getSystemService("input_method");
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 2);
        }
    }

    public boolean isExitRcsGroupBtnEnable() {
        if (this.mIsInvalidGroupStatus || !RcsProfile.canProcessGroupChat(this.mGroupID)) {
            return false;
        }
        return MmsConfig.isSmsEnabled(this.mActivity);
    }

    private void registerGroupChatStatusChangeBroadCast() {
        this.groupChatStatusChangeBroadCastReceiver = new GroupChatStatusChangeBroadCastReceiver();
        this.mActivity.registerReceiver(this.groupChatStatusChangeBroadCastReceiver, new IntentFilter("com.huawei.rcs.message.groupcreated"), "com.huawei.rcs.RCS_BROADCASTER", null);
    }

    private void unRegisterGroupChatStatusChangeBroadCast() {
        if (this.groupChatStatusChangeBroadCastReceiver != null) {
            this.mActivity.unregisterReceiver(this.groupChatStatusChangeBroadCastReceiver);
        }
    }

    private void registerGroupChatCreateBroadCast() {
        this.groupChatCreateBroadCastReceiver = new GroupChatCreateBroadCastReceiver();
        this.mActivity.registerReceiver(this.groupChatCreateBroadCastReceiver, new IntentFilter("com.huawei.groupchat.create"), "com.huawei.rcs.RCS_BROADCASTER", null);
    }

    private void unRegisterGroupChatCreateBroadCast() {
        if (this.groupChatCreateBroadCastReceiver != null) {
            this.mActivity.unregisterReceiver(this.groupChatCreateBroadCastReceiver);
        }
    }

    public void exitRcsGroupChat() {
        String format = this.mActivity.getString(R.string.groupchat_exit);
        AlertDialog alertDialog = new Builder(this.mActivity).setTitle(String.format(format, new Object[]{this.mGroupName})).setPositiveButton(R.string.nickname_dialog_confirm, new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                try {
                    if (RcsProfile.getRcsService() != null) {
                        RcsProfile.getRcsService().exitGroup(RcsGroupChatDetailSettingFragment.this.mGroupID, false);
                        RcsGroupChatDetailSettingFragment.this.asyncDeleteDraftMessage(RcsGroupChatDetailSettingFragment.this.mThreadID);
                    }
                } catch (RemoteException e) {
                    MLog.e("RcsGroupChatDetailSettingFragment", "exitRcsGroupChat error");
                }
                if (!HwMessageUtils.isSplitOn()) {
                    RcsGroupChatDetailSettingFragment.this.mActivity.finish();
                } else if (RcsGroupChatDetailSettingFragment.this.mActivity instanceof ConversationList) {
                    RcsGroupChatDetailSettingFragment.this.mActivity.onBackPressed();
                    ((ConversationList) RcsGroupChatDetailSettingFragment.this.mActivity).updateGroupBottomStatus();
                }
            }
        }).setNegativeButton(R.string.no, null).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void updateViewStatus() {
        if (isExitRcsGroupBtnEnable() && this.mIsGroupchatCreateSucc) {
            this.mGroupMemberAdapter.setShowExtBtn(true);
            if (this.mExitGroupPreference != null) {
                this.mExitGroupPreference.setEnabled(true);
            }
        } else {
            this.mGroupMemberAdapter.setShowExtBtn(false);
            if (this.mExitGroupPreference != null) {
                this.mExitGroupPreference.setEnabled(false);
            }
        }
        if (this.mIsInvalidGroupStatus) {
            if (this.mExitGroupPreference != null) {
                this.mExitGroupPreference.setExitTextColor(this.mActivity.getResources().getColor(R.color.text_color_black_sub_2));
            }
        } else if (this.mExitGroupPreference != null) {
            this.mExitGroupPreference.setExitTextColor(this.mActivity.getResources().getColor(R.drawable.text_color_red));
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == -1 && data != null) {
                    List<PeerInformation> members = RcsGroupChatConversationDetailFragment.processPickIMcontactResult(this.mActivity, data);
                    if (members == null) {
                        members = new ArrayList();
                    }
                    Iterator<PeerInformation> ite = members.iterator();
                    while (ite.hasNext()) {
                        String addr = ((PeerInformation) ite.next()).getNumber();
                        Iterator<String> iteCur = this.mGroupMemberAdapter.getMemberList().iterator();
                        boolean flag = false;
                        while (iteCur.hasNext()) {
                            if (AddrMatcher.isNumberMatch((String) iteCur.next(), addr) > 0) {
                                flag = true;
                                if (flag) {
                                    this.mGroupMemberAdapter.getMemberList().add(addr);
                                } else {
                                    ite.remove();
                                }
                            }
                        }
                        if (flag) {
                            this.mGroupMemberAdapter.getMemberList().add(addr);
                        } else {
                            ite.remove();
                        }
                    }
                    MLog.i("RcsGroupChatDetailSettingFragment", "add member to a Groupchat and members.size() = " + members.size() + "");
                    if (RcsProfile.getRcsService() != null) {
                        try {
                            RcsProfile.getRcsService().addGroupMembers(this.mGroupID, members);
                            return;
                        } catch (RemoteException e) {
                            MLog.e("RcsGroupChatDetailSettingFragment", "addGroupMembers error");
                            return;
                        }
                    }
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void saveNotifiedToDB(String groupID, int isNotifySilent) {
        ContentValues value = new ContentValues();
        value.put("is_groupchat_notify_silent", Integer.valueOf(isNotifySilent));
        value.put("name", groupID);
        SqliteWrapper.update(this.mActivity, Uri.parse("content://rcsim/rcs_groups"), value, "name = ?", new String[]{groupID});
    }

    private int getNotifySilentValue(Object obj) {
        if (((Boolean) obj).booleanValue()) {
            return 1;
        }
        return 0;
    }

    private void asyncDeleteDraftMessage(final long mThreadID) {
        HwBackgroundLoader.getInst().postTask(new Runnable() {
            public void run() {
                try {
                    DraftCache.getInstance().setSavingDraft(true);
                    RcsGroupChatDetailSettingFragment.this.deleteDraftMessage(mThreadID);
                    if (DraftCache.getInstance().getHwCust() != null) {
                        DraftCache.getInstance().getHwCust().setDraftGroupState(RcsGroupChatDetailSettingFragment.this.mRcsThreadID, false);
                    }
                    DraftCache.getInstance().setSavingDraft(false);
                } catch (Throwable th) {
                    DraftCache.getInstance().setSavingDraft(false);
                }
            }
        });
    }

    private void deleteDraftMessage(long threadId) {
        SqliteWrapper.delete(getActivity(), this.mContentResolver, ContentUris.withAppendedId(RcsGroupChatComposeMessageFragment.sDeleteDraftUri, this.mThreadID), null, null);
    }
}
