package com.huawei.rcs.utils;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.mms.data.Conversation;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.data.RcsConversation;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.rcs.commonInterface.IfMsgplus;
import com.huawei.rcs.commonInterface.IfMsgplus.Stub;
import com.huawei.rcs.commonInterface.IfMsgplusCb;
import com.huawei.rcs.util.RcsXmlParser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

public class RcsProfile {
    private static final boolean IS_GROUP_CHAT_MEMBER_TOPIC_ENABLED;
    private static final boolean IS_GROUP_CHAT_NICKNAME_ENABLED = (!"0".equals(RcsXmlParser.getValueByNameFromXml("CONFIG_GROUPCHAT_NICKNAME_ENABLE")));
    private static Context mContext = null;
    private static boolean mExistingState = false;
    private static boolean mIsRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private static BindServiceListener mListener;
    private static final Object mMainHandlerLock = new Object();
    private static HashMap<Integer, IfMsgplusCb> mRcsCallbackList = new HashMap();
    private static IfMsgplus mRcsService = null;
    private static Handler mainHandler;
    private static ServiceConnection mrcsServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName aClassName, IBinder aService) {
            try {
                RcsProfile.mRcsService = Stub.asInterface(aService);
                RcsProfile.mRcsService.setRequestDeliveryStatus(RcsProfile.isRcsImDeliveryReportEnabled());
                RcsProfile.mRcsService.setRequestDisplayStatus(RcsProfile.isRcsImDisplayReportEnabled());
                RcsProfile.mRcsService.setAllowSendDisplayStatus(RcsProfile.isRcsImDisplayReportResponseEnabled());
                RcsProfile.mRcsService.setAutoAcceptGroupInviter(RcsProfile.isGroupAutoAccept());
                RcsProfile.mRcsService.setGroupMessageRequestDeliveryStatus(RcsProfile.isRcsGroupMessageDeliveryReportEnabled());
                for (Entry<Integer, IfMsgplusCb> entry : RcsProfile.mRcsCallbackList.entrySet()) {
                    RcsProfile.mRcsService.registerCallback(((Integer) entry.getKey()).intValue(), (IfMsgplusCb) entry.getValue());
                }
                if (RcsProfile.mListener != null) {
                    RcsProfile.mListener.onBindServiceListenerSet();
                }
            } catch (RemoteException e) {
                MLog.e("RcsProfile", "remote error");
            }
            RcsProfile.sendRcsLoginStatusBroadcast();
        }

        public void onServiceDisconnected(ComponentName aClassName) {
            RcsProfile.mRcsService = null;
            if (!RcsProfile.mExistingState) {
                RcsProfile.bindservice();
            }
        }
    };
    private static Runnable runnable = new Runnable() {
        public void run() {
            if (RcsProfile.mRcsService == null) {
                Intent bindAction = new Intent();
                bindAction.setPackage("com.huawei.rcsserviceapplication");
                bindAction.setClassName("com.huawei.rcsserviceapplication", "com.huawei.rcs.service.RcsService");
                bindAction.setAction("com.huawei.msgplus.IfMsgplus");
                bindAction.setType("vnd.android.cursor.item/rcs");
                bindAction.putExtra("from_mms", true);
                RcsProfile.mContext.bindService(bindAction, RcsProfile.mrcsServiceConnection, 1);
            }
        }
    };

    public interface BindServiceListener {
        void onBindServiceListenerSet();
    }

    static {
        boolean z = false;
        if (!"0".equals(RcsXmlParser.getValueByNameFromXml("CONFIG_GROUPCHAT_MEMBER_TOPIC_ENABLE"))) {
            z = true;
        }
        IS_GROUP_CHAT_MEMBER_TOPIC_ENABLED = z;
    }

    public static void setBindServiceListener(BindServiceListener listener) {
        if (listener == null) {
            MLog.d("RcsProfile", "set a null setBindServiceListener in RcsProfile");
        }
        mListener = listener;
    }

    protected RcsProfile() {
    }

    private static void bindservice() {
        if (mContext != null && mRcsService == null) {
            synchronized (mMainHandlerLock) {
                if (mainHandler == null) {
                    mainHandler = new Handler(mContext.getMainLooper());
                }
                mainHandler.post(runnable);
            }
        }
    }

    public static void init(Context context) {
        mContext = context;
        synchronized (mMainHandlerLock) {
            if (mainHandler == null) {
                mainHandler = new Handler(mContext.getMainLooper());
            }
        }
        if (mRcsService == null) {
            bindservice();
        }
    }

    public static void deInit() {
        if (mContext != null) {
            mExistingState = true;
            mContext.unbindService(mrcsServiceConnection);
        }
    }

    public static IfMsgplus getRcsService() {
        if (mRcsService == null) {
            bindservice();
        }
        return mRcsService;
    }

    public static boolean isRcsImServiceSwitchEnabled() {
        if (mContext == null) {
            return false;
        }
        return isRcsSwitchEnabled();
    }

    public static boolean isRcsSwitchEnabled() {
        int rcsSwitchStatus = 0;
        try {
            rcsSwitchStatus = Secure.getInt(mContext.getContentResolver(), "huawei_rcs_switcher", 1);
        } catch (Exception e) {
            MLog.e("RcsProfile", "Settings System getInt error");
        }
        if (1 == rcsSwitchStatus) {
            return true;
        }
        return false;
    }

    public static boolean isRcsImDeliveryReportEnabled() {
        if (mContext == null) {
            return true;
        }
        return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("pref_key_im_enable_delivery_report", true);
    }

    public static boolean isRcsImDisplayReportEnabled() {
        if (mContext == null) {
            return true;
        }
        return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("pref_key_im_enable_display_report", true);
    }

    public static boolean isRcsImDisplayReportResponseEnabled() {
        if (mContext == null) {
            return true;
        }
        return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("pref_key_im_enable_display_report_response", true);
    }

    public static void registerRcsCallBack(Integer eventId, IfMsgplusCb rcsCallback) {
        if (mRcsService == null) {
            mRcsCallbackList.put(eventId, rcsCallback);
            return;
        }
        try {
            mRcsService.registerCallback(eventId.intValue(), rcsCallback);
            mRcsCallbackList.put(eventId, rcsCallback);
        } catch (RemoteException e) {
            Log.e("RcsProfile", e.toString());
        }
    }

    public static void unregisterRcsCallBack(Integer eventId, IfMsgplusCb rcsCallback) {
        if (mRcsService == null) {
            mRcsCallbackList.remove(eventId);
            return;
        }
        try {
            mRcsService.unRegisterCallback(eventId.intValue(), rcsCallback);
            mRcsCallbackList.remove(eventId);
        } catch (RemoteException e) {
            Log.e("RcsProfile", e.toString());
        }
    }

    public static boolean isGroupAutoAccept() {
        if (mContext == null) {
            return true;
        }
        return System.getInt(mContext.getContentResolver(), "group_invit_auto_accept_switcher", 1) == 1;
    }

    public static boolean canProcessGroupChat(String groupID) {
        boolean z = false;
        boolean isOwner = false;
        if (getRcsService() != null) {
            try {
                z = getRcsService().getLoginState();
                isOwner = getRcsService().isGroupOwner(groupID);
            } catch (RemoteException e) {
                MLog.e("RcsProfile", "remote error");
            }
        }
        MLog.d("RcsProfile", "canProcessGroupChat isLogin=" + z + " isOwner=" + isOwner);
        if (z && isOwner && isRcsImServiceSwitchEnabled()) {
            return true;
        }
        return false;
    }

    public static boolean setIMThreadDisplayMergeStatus(Context context, int value) {
        boolean bResult = false;
        try {
            bResult = System.putInt(context.getContentResolver(), "im_thread_display_switcher", value);
        } catch (Exception e) {
            MLog.e("RcsProfile", "Settings System putInt error");
        }
        return bResult;
    }

    public static boolean setGroupInviteAutoAccept(Context context, int value) {
        boolean bResult = false;
        boolean isChecked = value == 1;
        IfMsgplus aMsgPlus = getRcsService();
        if (aMsgPlus != null) {
            try {
                aMsgPlus.setAutoAcceptGroupInviter(isChecked);
                bResult = System.putInt(context.getContentResolver(), "group_invit_auto_accept_switcher", value);
            } catch (Exception e) {
                MLog.e("RcsProfile", "setAutoAcceptGroupInviter error");
            }
        }
        return bResult;
    }

    public static boolean isRcsServiceEnabledAndUserLogin() {
        boolean z = false;
        try {
            if (mRcsService != null) {
                if (isRcsImServiceSwitchEnabled()) {
                    z = mRcsService.getLoginState();
                }
                return z;
            }
            MLog.w("RcsProfile", "getRcsService is null.isRcsServiceEnabledAndUserLogin().return false.");
            return false;
        } catch (Exception e) {
            MLog.e("RcsProfile", "RemoteException in isRcsServiceEnabledAndUserLogin().return false.");
            return false;
        }
    }

    public static int getRcsImSessionStartValue() {
        int iImSessionStart = 0;
        try {
            if (getRcsService() != null) {
                iImSessionStart = getRcsService().getImSesionStart();
            }
        } catch (Exception e) {
            MLog.e("RcsProfile", "getImSesionStart error");
        }
        return iImSessionStart;
    }

    public static void acceptRcsImSession(String number) {
        try {
            if (getRcsService() != null) {
                getRcsService().acceptImSession(number);
            }
        } catch (Exception e) {
            MLog.e("RcsProfile", "acceptImSession error");
        }
    }

    public static boolean isImAvailable(String address) {
        boolean isImAvailable = false;
        try {
            if (getRcsService() != null && !TextUtils.isEmpty(address) && isRcsImServiceSwitchEnabled() && getRcsService().getLoginState()) {
                isImAvailable = getRcsService().isImAvailable(address);
            }
        } catch (RemoteException e) {
            Log.e("RcsProfile", e.toString());
        }
        return isImAvailable;
    }

    public static boolean isImAvailable(String address, boolean ignoreTimeOut) {
        boolean isImAvailable = false;
        try {
            if (getRcsService() != null && !TextUtils.isEmpty(address) && isRcsImServiceSwitchEnabled() && getRcsService().getLoginState()) {
                isImAvailable = getRcsService().isImAvailableWithTimeOut(address, ignoreTimeOut);
            }
        } catch (RemoteException e) {
            Log.e("RcsProfile", e.toString());
        }
        return isImAvailable;
    }

    public static boolean isResendImAvailable(String address) {
        try {
            if (getRcsService() == null || TextUtils.isEmpty(address) || !isRcsImServiceSwitchEnabled() || !getRcsService().getLoginState()) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            MLog.e("RcsProfile", "isResendImAvailable error");
            return false;
        }
    }

    public static int getRcsGroupStatus(String groupID) {
        int groupStatus = 0;
        Cursor cursor = null;
        if (TextUtils.isEmpty(groupID)) {
            return 0;
        }
        try {
            cursor = SqliteWrapper.query(mContext, Uri.parse("content://rcsim/rcs_groups"), null, "name = ?", new String[]{groupID}, null);
            if (cursor != null && cursor.moveToFirst()) {
                groupStatus = cursor.getInt(cursor.getColumnIndexOrThrow("status"));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (RuntimeException e) {
            MLog.e("RcsProfile", "cursor unknowable error");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return groupStatus;
    }

    public static boolean isRcsImSupportSF() {
        boolean isSupportSF = false;
        if (getRcsService() != null) {
            try {
                isSupportSF = getRcsService().isImSupportStoreAndFoward();
            } catch (RemoteException e) {
                MLog.e("RcsProfile", "remote error");
            }
        }
        return isSupportSF;
    }

    public static boolean isRcsGroupMessageDeliveryReportEnabled() {
        if (mContext == null) {
            return true;
        }
        return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("pref_key_group_message_enable_delivery_report", true);
    }

    private static boolean sendRcsLoginStatusBroadcast() {
        try {
            boolean isLogin = mRcsService.getLoginState();
            if (mContext == null) {
                return false;
            }
            Intent rcsStatusIntent = new Intent();
            rcsStatusIntent.setAction("com.huawei.rcs.loginstatus");
            rcsStatusIntent.putExtra("new_status", isLogin ? 1 : 2);
            mContext.sendBroadcast(rcsStatusIntent);
            return true;
        } catch (Exception e) {
            MLog.e("RcsProfile", "rcs service not run");
            return false;
        }
    }

    public static String getGroupMemberNickname(String addr, long threadId) {
        if (mContext == null) {
            return null;
        }
        Cursor cursor = null;
        String name = getGroupMemberNicknameByAddress(addr);
        if (TextUtils.isEmpty(name)) {
            try {
                cursor = SqliteWrapper.query(mContext, mContext.getContentResolver(), Uri.parse("content://rcsim/rcs_group_members"), new String[]{"nickname"}, "thread_id=? AND PHONE_NUMBERS_EQUAL(rcs_id, ?, 1)", new String[]{String.valueOf(threadId), addr}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    name = cursor.getString(0);
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable e) {
                MLog.e("RcsProfile", "SQLiteException error", e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return name;
    }

    private static String getGroupMemberNicknameByAddress(String addr) {
        if (mContext == null) {
            return null;
        }
        Cursor cursor = null;
        String str = null;
        try {
            cursor = SqliteWrapper.query(mContext, mContext.getContentResolver(), Uri.parse("content://rcsim/rcs_group_members"), new String[]{"nickname"}, "thread_id=0 AND PHONE_NUMBERS_EQUAL(rcs_id, ?, 1)", new String[]{addr}, null);
            if (cursor != null && cursor.moveToFirst()) {
                str = cursor.getString(0);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            MLog.e("RcsProfile", "getGroupMemberNicknameByAddress error");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return str;
    }

    public static boolean isGroupChatNicknameEnabled() {
        if (mIsRcsOn) {
            return IS_GROUP_CHAT_NICKNAME_ENABLED;
        }
        return false;
    }

    public static boolean isGroupChatMemberTopicEnable() {
        if (mIsRcsOn) {
            return IS_GROUP_CHAT_MEMBER_TOPIC_ENABLED;
        }
        return false;
    }

    public static void startContactDetailActivityFromGroupChat(String name, String addr, Context context) {
        Intent intent = new Intent();
        intent.setClassName("com.android.contacts", "com.android.contacts.activities.ContactDetailActivity");
        intent.putExtra("isFromRcsGroupChat", true);
        intent.putExtra("nickName", name);
        intent.putExtra("address", addr);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("RcsProfile", "ContactDetailActivity is not exist");
        }
    }

    public static void setftFileAceeptSwitch(Context context, int value, String Key) {
        MLog.i("RcsProfile FileTrans: ", "setftFileAceeptSwitch " + value);
        try {
            System.putInt(context.getContentResolver(), Key, value);
        } catch (Exception e) {
            MLog.e("RcsProfile", "setftFileAceeptSwitch error");
        }
    }

    public static boolean isRcsFTSupportSF() {
        boolean isSupportSF = false;
        if (getRcsService() != null) {
            try {
                isSupportSF = getRcsService().isFtSupportStoreAndFoward();
            } catch (RemoteException e) {
                MLog.e("RcsProfile", "remote error");
            }
        }
        return isSupportSF;
    }

    public static Collection<Conversation> queryAllConversation() {
        Collection<Conversation> allConv = new ArrayList();
        if (mContext == null) {
            return allConv;
        }
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(mContext, RcsConversation.sRcsAllMixedThreads, RcsConversation.getRcsAllThreadProjection(), null, null, "priority DESC, date DESC");
            if (cursor != null) {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    allConv.add(Conversation.from(mContext, cursor));
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return allConv;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean rcsIsLogin() {
        boolean isLogin = false;
        if (getRcsService() != null) {
            try {
                isLogin = getRcsService().getLoginState();
            } catch (RemoteException e) {
                MLog.e("RcsProfile", "remote error");
            }
        }
        return isLogin;
    }

    public static boolean isShowDisconnectedNotify() {
        if (mIsRcsOn) {
            return RcsXmlParser.getBoolean("mms_disconnected_notify_enable", false);
        }
        return false;
    }

    public static void startService() {
        if (mContext != null) {
            Intent bindAction = new Intent();
            bindAction.setPackage("com.huawei.rcsserviceapplication");
            bindAction.setClassName("com.huawei.rcsserviceapplication", "com.huawei.rcs.service.RcsService");
            bindAction.setAction("com.huawei.msgplus.IfMsgplus");
            bindAction.setType("vnd.android.cursor.item/rcs");
            bindAction.putExtra("from_mms", true);
            mContext.startService(bindAction);
        }
    }

    public static boolean isSupportAllCharacters() {
        if (!mIsRcsOn) {
            return false;
        }
        String valueXml = "";
        boolean value = false;
        valueXml = RcsXmlParser.getValueByNameFromXml("GROUP_NICKNAME_SUPPORT_ALL_CHARACTERS");
        if (!valueXml.isEmpty()) {
            value = !"0".equals(valueXml);
        }
        return value;
    }

    public static boolean isGroupOwner(String groupId) {
        boolean isOwner = false;
        if (getRcsService() != null) {
            try {
                isOwner = getRcsService().isGroupOwner(groupId);
            } catch (RemoteException e) {
                MLog.e("RcsProfile", "isGroupOwner error: " + e);
            }
        }
        return isOwner;
    }

    public static void insertGroupOwner(String groupId, int groupStatus) {
        boolean isRcsLogin = rcsIsLogin();
        MLog.d("RcsProfile", "insertGroupOwner groupId: " + groupId + "; isRcsLogin: " + isRcsLogin);
        if (!isRcsLogin || TextUtils.isEmpty(groupId)) {
            MLog.w("RcsProfile", "insertGroupOwner rcs is not login or group id is null");
            return;
        }
        boolean isInvalidGroup = RcseMmsExt.checkInvalidGroupStatus(groupStatus);
        MLog.d("RcsProfile", "insertGroupOwner groupStatus: " + groupStatus + "; isInvalidGroup: " + isInvalidGroup);
        if (isInvalidGroup || groupStatus < 0) {
            MLog.w("RcsProfile", "insertGroupOwner the group is invalid or groupStatus is less than 0");
            return;
        }
        boolean isGroupOwner = isGroupOwner(groupId);
        MLog.d("RcsProfile", "insertGroupOwner isGroupOwner: " + isGroupOwner);
        if (isGroupOwner) {
            recoverGroupStatus(groupId, groupStatus);
        } else {
            changeGroupStatus(groupId, groupStatus);
        }
    }

    private static void recoverGroupStatus(String groupId, int groupStatus) {
        if (groupStatus >= 100) {
            insertGroupOwnerNotificationMessage(groupId, 258);
            updateStatusInRcsGroups(groupId, groupStatus - 100);
        }
    }

    private static void changeGroupStatus(String groupId, int groupStatus) {
        if (groupStatus < 100 && groupStatus >= 0) {
            insertGroupOwnerNotificationMessage(groupId, 257);
            updateStatusInRcsGroups(groupId, groupStatus + 100);
        }
    }

    public static void updateStatusInRcsGroups(String groupId, int groupStatus) {
        MLog.d("RcsProfile", "updateStatusInRcsGroups groupId: " + groupId + "; groupStatus: " + groupStatus);
        if (TextUtils.isEmpty(groupId) || groupStatus < 0 || mContext == null) {
            MLog.w("RcsProfile", "updateStatusInRcsGroups groupId is null or groupStatus is less than 0");
            return;
        }
        ContentResolver cr = mContext.getContentResolver();
        Uri updateGroupUri = Uri.parse("content://rcsim/rcs_groups_update");
        ContentValues contentValues = new ContentValues();
        contentValues.put("status", Integer.valueOf(groupStatus));
        if (SqliteWrapper.update(mContext, cr, updateGroupUri, contentValues, "name = ?", new String[]{groupId}) < 0) {
            MLog.w("RcsProfile", "updateStatusInRcsGroups error");
        }
    }

    private static void insertGroupOwnerNotificationMessage(String groupId, int isOwnerCode) {
        if (TextUtils.isEmpty(groupId) || isOwnerCode < 0 || mContext == null) {
            MLog.w("RcsProfile", "insertGroupOwnerNotificationMessage groupId is null or isOwnerCode is less than 0");
            return;
        }
        ContentResolver cr = mContext.getContentResolver();
        Uri groupUri = Uri.parse("content://rcsim/rcs_groups");
        Cursor cursor = null;
        long threadId = -1;
        try {
            cursor = SqliteWrapper.query(mContext, cr, groupUri, new String[]{"thread_id"}, "name = ?", new String[]{groupId}, null);
            if (cursor != null && cursor.moveToFirst()) {
                threadId = cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            MLog.e("RcsProfile", "Get thread id error: " + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (threadId < 0) {
            MLog.e("RcsProfile", "insertGroupOwnerNotificationMessage error");
            return;
        }
        long now = System.currentTimeMillis();
        Uri groupMessageUri = Uri.parse("content://rcsim/rcs_group_message");
        ContentValues msg = new ContentValues();
        msg.put(NumberInfo.TYPE_KEY, Integer.valueOf(isOwnerCode));
        msg.put("thread_id", Long.valueOf(threadId));
        msg.put("date", Long.valueOf(now));
        if (SqliteWrapper.insert(mContext, cr, groupMessageUri, msg) == null) {
            MLog.w("RcsProfile", "insertGroupOwnerNotificationMessage error");
        }
    }
}
