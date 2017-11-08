package com.android.rcs.transaction;

import android.app.Notification;
import android.app.Notification.Action;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.transaction.HwCustMessagingNotification.IHwCustMessagingNotificationCallback;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.transaction.MessagingNotification.NotificationInfo;
import com.android.mms.transaction.NotificationReceiver;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.MessageUtils;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.data.RcsConversationUtils;
import com.android.rcs.ui.RcsComposeMessage;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.util.AvatarCache;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.NumberUtils;
import com.huawei.mms.util.NumberUtils.AddrMatcher;
import com.huawei.mms.util.PrivacyModeReceiver;
import com.huawei.mms.util.PrivacyModeReceiver.PrivacyStateListener;
import com.huawei.rcs.media.RcsMediaFileUtils;
import com.huawei.rcs.media.RcsMediaFileUtils.MediaFileType;
import com.huawei.rcs.ui.RcsGroupChatComposeMessageActivity;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsProfileUtils;
import com.huawei.rcs.utils.RcsTransaction;
import com.huawei.rcs.utils.RcsUtility;
import com.huawei.rcs.utils.map.abs.RcsMapLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class RcsMessagingNotification {
    private static final Uri CANONICAL_ADDRESS_URI = Uri.parse("content://mms-sms/canonical-address");
    private static final Uri CHAT_UNDELIVERED_URI = Uri.parse("content://rcsim/chat");
    private static final Uri GROUPCHAT_UNDELIVERED_URI = Uri.parse("content://rcsim/rcs_group_message");
    private static int GROUP_NOTIFICATION_ID = new Random().nextInt(10000);
    private static final Uri IM_GROUPCHAT_MESSAGE_URI = Uri.parse("content://rcsim/rcs_group_message");
    private static final Uri IM_GROUPCHAT_URI = Uri.parse("content://rcsim/rcs_groups");
    private static final boolean IS_VIBRATION_TYPE_ENABLED = SystemProperties.getBoolean("ro.config.hw_vibration_type", false);
    private static final String[] MMS_THREAD_ID_PROJECTION = new String[]{"thread_id"};
    private static final String[] SMS_STATUS_PROJECTION = new String[]{"thread_id", "date", "address", "subject", "body", "sub_id", "_id", "service_center"};
    private static final String[] SMS_THREAD_ID_PROJECTION = new String[]{"thread_id"};
    private static final Uri UNDELIVERED_URI = Uri.parse("content://mms-sms/undelivered");
    private static boolean isChat = false;
    private static boolean isGroup = false;
    private static boolean isMms = false;
    private static boolean isSms = false;
    private static IHwCustMessagingNotificationCallback mCallback;
    private static final HashMap<Long, Integer> mHsFailedGROUPMap = new HashMap();
    private static final HashMap<Long, Integer> mHsFailedIMMap = new HashMap();
    private static final HashMap<Long, Integer> mHsFailedSMSMap = new HashMap();
    private static final HashMap<Long, String> mReceiveTypeMap = new HashMap();
    private static final HashMap<String, Integer> notifyForThread = new HashMap();
    private static long sCurrentlyDisplayedRcsType;

    public static class StatusBroadCastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (!(!RcsCommonConfig.isRCSSwitchOn() || intent == null || intent.getAction() == null)) {
                MLog.d("RcsMessagingNotification", "StatusBroadCastReceiver onReceive " + intent.getAction());
                if (intent.getAction().equals("im_send_failed")) {
                    MessagingNotification.notifySendFailed(context, true);
                }
            }
        }
    }

    private static final void addImGroupChatNotificationInfos(android.content.Context r28, java.util.Set<java.lang.Long> r29, java.util.SortedSet<com.android.mms.transaction.MessagingNotification.NotificationInfo> r30) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:33:0x013c in {2, 8, 11, 19, 23, 25, 29, 31, 32, 35, 36, 38, 39, 40, 41} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
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
        r5 = r28.getContentResolver();
        r6 = IM_GROUPCHAT_MESSAGE_URI;
        r8 = "(( type = 1 OR type = 101 )  AND seen = 0 )";
        r10 = "date desc";
        r7 = 0;
        r9 = 0;
        r4 = r28;
        r23 = com.huawei.cspcommon.ex.SqliteWrapper.query(r4, r5, r6, r7, r8, r9, r10);
        if (r23 != 0) goto L_0x0017;
    L_0x0016:
        return;
    L_0x0017:
        r4 = r23.moveToNext();	 Catch:{ all -> 0x0141, all -> 0x013c }
        if (r4 == 0) goto L_0x0148;	 Catch:{ all -> 0x0141, all -> 0x013c }
    L_0x001d:
        r4 = "address";	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r23;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = r0.getColumnIndexOrThrow(r4);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r23;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r21 = r0.getString(r4);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = 0;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r21;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r17 = com.android.mms.data.Contact.get(r0, r4);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = "body";	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r23;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = r0.getColumnIndexOrThrow(r4);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r23;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r25 = r0.getString(r4);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = "thread_id";	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r23;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = r0.getColumnIndexOrThrow(r4);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r23;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r12 = r0.getLong(r4);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = "date";	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r23;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = r0.getColumnIndexOrThrow(r4);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r23;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r14 = r0.getLong(r4);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = "_id";	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r23;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = r0.getColumnIndex(r4);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r23;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = r0.getInt(r4);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = (long) r4;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r26 = r0;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = com.huawei.rcs.utils.RcsProfileUtils.checkIsGroupFile(r23);	 Catch:{ all -> 0x0141, all -> 0x013c }
        if (r4 == 0) goto L_0x0081;	 Catch:{ all -> 0x0141, all -> 0x013c }
    L_0x0078:
        r4 = 0;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r28;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r1 = r25;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r25 = codeFileNoticeContent(r0, r4, r1);	 Catch:{ all -> 0x0141, all -> 0x013c }
    L_0x0081:
        r4 = com.huawei.rcs.utils.map.abs.RcsMapLoader.isLocItem(r25);	 Catch:{ all -> 0x0141, all -> 0x013c }
        if (r4 == 0) goto L_0x0092;	 Catch:{ all -> 0x0141, all -> 0x013c }
    L_0x0087:
        r4 = r28.getResources();	 Catch:{ all -> 0x0141, all -> 0x013c }
        r6 = 2131494503; // 0x7f0c0667 float:1.8612516E38 double:1.053098208E-314;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r25 = r4.getString(r6);	 Catch:{ all -> 0x0141, all -> 0x013c }
    L_0x0092:
        r11 = 0;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r20 = 0;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r19 = 0;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = "RcsMessagingNotification";	 Catch:{ all -> 0x0141, all -> 0x013c }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r6.<init>();	 Catch:{ all -> 0x0141, all -> 0x013c }
        r7 = "addImGroupChatNotificationInfos: count=";	 Catch:{ all -> 0x0141, all -> 0x013c }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r7 = r23.getCount();	 Catch:{ all -> 0x0141, all -> 0x013c }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r7 = ", thread_id=";	 Catch:{ all -> 0x0141, all -> 0x013c }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r6 = r6.append(r12);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r6 = r6.toString();	 Catch:{ all -> 0x0141, all -> 0x013c }
        com.huawei.cspcommon.MLog.d(r4, r6);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r22 = 0;
        r6 = IM_GROUPCHAT_URI;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4.<init>();	 Catch:{ all -> 0x0141, all -> 0x013c }
        r7 = " thread_id = ";	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = r4.append(r7);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = r4.append(r12);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r8 = r4.toString();	 Catch:{ all -> 0x0141, all -> 0x013c }
        r7 = 0;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r9 = 0;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r10 = 0;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = r28;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r22 = com.huawei.cspcommon.ex.SqliteWrapper.query(r4, r5, r6, r7, r8, r9, r10);	 Catch:{ all -> 0x0141, all -> 0x013c }
        if (r22 == 0) goto L_0x0135;	 Catch:{ all -> 0x0141, all -> 0x013c }
    L_0x00e3:
        r4 = r22.moveToFirst();	 Catch:{ all -> 0x0141, all -> 0x013c }
        if (r4 == 0) goto L_0x0135;	 Catch:{ all -> 0x0141, all -> 0x013c }
    L_0x00e9:
        r4 = "subject";	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r22;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = r0.getColumnIndexOrThrow(r4);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r22;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r11 = r0.getString(r4);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = "name";	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r22;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = r0.getColumnIndexOrThrow(r4);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r22;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r20 = r0.getString(r4);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r8 = 0;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r16 = 0;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r18 = 0;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r7 = r28;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r9 = r25;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r10 = r21;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r24 = getNewRCSMessageNotificationInfo(r7, r8, r9, r10, r11, r12, r14, r16, r17, r18, r19, r20);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r24;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r1 = r26;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0.setMsgId(r1);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r6 = 0;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r6 = r6 + r12;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r24;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0.setNotificationId(r6);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r30;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r1 = r24;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0.add(r1);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r4 = java.lang.Long.valueOf(r12);	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0 = r29;	 Catch:{ all -> 0x0141, all -> 0x013c }
        r0.add(r4);	 Catch:{ all -> 0x0141, all -> 0x013c }
    L_0x0135:
        if (r22 == 0) goto L_0x0017;
    L_0x0137:
        r22.close();
        goto L_0x0017;
    L_0x013c:
        r4 = move-exception;
        r23.close();
        throw r4;
    L_0x0141:
        r4 = move-exception;
        if (r22 == 0) goto L_0x0147;
    L_0x0144:
        r22.close();
    L_0x0147:
        throw r4;	 Catch:{ all -> 0x0141, all -> 0x013c }
    L_0x0148:
        r23.close();
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.rcs.transaction.RcsMessagingNotification.addImGroupChatNotificationInfos(android.content.Context, java.util.Set, java.util.SortedSet):void");
    }

    public void initExt(Context context) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            init(context);
        }
    }

    public void removeGlobalGroupId(String globalGroupId) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            notifyForThread.remove(globalGroupId);
        }
    }

    private static void init(Context context) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            IntentFilter statusIntentFilter = new IntentFilter();
            statusIntentFilter.addAction("com.huawei.reporttoast");
            statusIntentFilter.addAction("im_send_failed");
            statusIntentFilter.addAction("rcs.ft.readReport");
            statusIntentFilter.addAction("rcs.ft.trans.failed");
            statusIntentFilter.addAction("rcs.ft.trans.canceld");
            context.registerReceiver(new StatusBroadCastReceiver(), statusIntentFilter, "com.huawei.rcs.RCS_BROADCASTER", null);
        }
    }

    public void setCurrentlyDisplayedThreadId(long threadId, long RCSType) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            synchronized (MessagingNotification.getCurrentlyDisplayedThreadLock()) {
                MessagingNotification.setCurrentlyDisplayedThreadId(threadId);
                setCurrentlyDisplayedRcsType(RCSType);
                MLog.d("RcsMessagingNotification", "setCurrentlyDisplayedThreadId: " + MessagingNotification.getCurrentlyDisplayedThreadId());
            }
        }
    }

    private static void setCurrentlyDisplayedRcsType(long RCSType) {
        sCurrentlyDisplayedRcsType = RCSType;
    }

    public boolean isRcsSwitchOn() {
        return RcsCommonConfig.isRCSSwitchOn();
    }

    public static void blockingUpdateNewMessageIndicator(Context context, long newMsgThreadId, boolean checked, Uri messageUri, int isIncomingMessageType, Bundle bundle) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            updateRcsNotifications(context, newMsgThreadId, messageUri, checked, isIncomingMessageType, bundle);
        }
    }

    private static String codeFileNoticeContent(Context context, Bundle bundle, String message) {
        String fileContent;
        String fileNoticeContent;
        if (bundle != null) {
            fileContent = bundle.getString("file_content");
        } else if (message != null) {
            fileContent = message;
        } else {
            MLog.w("RcsMessagingNotification", "codeFileNoticeContent is null");
            return null;
        }
        MediaFileType fileType = RcsMediaFileUtils.getFileType(fileContent);
        if (fileType != null) {
            if (RcsMediaFileUtils.isAudioFileType(fileType.fileType)) {
                fileNoticeContent = context.getResources().getString(R.string.rcs_notification_bar_voice);
            } else if (RcsMediaFileUtils.isImageFileType(fileType.fileType)) {
                fileNoticeContent = context.getResources().getString(R.string.rcs_notification_bar_image);
            } else if (RcsMediaFileUtils.isVideoFileType(fileType.fileType)) {
                fileNoticeContent = context.getResources().getString(R.string.rcs_notification_bar_video);
            } else if (RcsMediaFileUtils.isVCardFileType(fileType.fileType)) {
                fileNoticeContent = context.getResources().getString(R.string.rcs_notification_bar_contacts);
            } else {
                fileNoticeContent = context.getResources().getString(R.string.rcs_notification_bar_file);
            }
        } else if (bundle == null || !bundle.getBoolean("rcs.location", false)) {
            fileNoticeContent = context.getResources().getString(R.string.rcs_notification_bar_file);
        } else {
            fileNoticeContent = context.getResources().getString(R.string.rcs_notification_bar_location);
        }
        return fileNoticeContent;
    }

    private static final int addImSingleNotificationInfos(Context context, Set<Long> rcsThreads, Set<Long> threads, SortedSet<NotificationInfo> notificationSet) {
        Set<Long> hashSet = new HashSet(4);
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), Uri.parse("content://rcsim/chat"), SMS_STATUS_PROJECTION, "(type = 1 AND seen = 0)", null, "date desc");
        if (cursor == null) {
            return 0;
        }
        while (cursor.moveToNext()) {
            String address = cursor.getString(2);
            Contact contact = Contact.get(address, false);
            if (!contact.getSendToVoicemail()) {
                String message = cursor.getString(4);
                long threadId = cursor.getLong(0);
                long timeMillis = cursor.getLong(1);
                int subId = cursor.getInt(5);
                if (3 == RcsProfileUtils.getRcsMsgType(cursor)) {
                    if (mReceiveTypeMap.get(Long.valueOf(timeMillis)) != null) {
                        message = (String) mReceiveTypeMap.get(Long.valueOf(timeMillis));
                        clearReceiveTypeMap();
                    } else {
                        try {
                            message = codeFileNoticeContent(context, null, message);
                        } finally {
                            cursor.close();
                        }
                    }
                }
                if (RcsMapLoader.isLocItem(message)) {
                    message = context.getResources().getString(R.string.attach_map_location);
                }
                MLog.d("RcsMessagingNotification", "addImSingleNotificationInfos: count=" + cursor.getCount() + ", thread_id=" + threadId);
                NotificationInfo info = getNewRCSMessageNotificationInfo(context, false, message, address, null, threadId, timeMillis, null, contact, 0, subId, null);
                int messageId = cursor.getInt(cursor.getColumnIndex("_id"));
                info.setMsgId((long) messageId);
                info.setNotificationId(((long) messageId) + 0);
                notificationSet.add(info);
                long smsThreadId = RcsConversationUtils.getHwCustUtils().querySmsThreadIdWithAddress(address, context);
                MLog.d("RcsMessagingNotification", "addImSingleNotificationInfos  smsThreadId = " + smsThreadId);
                if (!threads.contains(Long.valueOf(smsThreadId)) || smsThreadId == -1) {
                    hashSet.add(Long.valueOf(threadId));
                    hashSet.add(Long.valueOf(cursor.getLong(0)));
                }
                rcsThreads.add(Long.valueOf(threadId));
            }
        }
        int addThreadNumber = hashSet.size();
        hashSet.clear();
        return addThreadNumber;
    }

    public static void clearReceiveTypeMap() {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            mReceiveTypeMap.clear();
        }
    }

    public HashMap<Long, String> getReceiveTypeMap() {
        return mReceiveTypeMap;
    }

    private static final NotificationInfo getNewRCSMessageNotificationInfo(Context context, boolean isSms, String message, String address, String subject, long threadId, long timeMillis, Bitmap attachmentBitmap, Contact contact, int attachmentType, int subId, String groupChatID) {
        int imChatType;
        String senderInfoName;
        CharSequence ticker;
        String RcsSubject;
        Intent clickIntent;
        String senderInfo;
        if (groupChatID != null) {
            if (TextUtils.isEmpty(subject)) {
                subject = context.getString(R.string.chat_topic_default);
            }
            String RcsSubject2 = subject;
            Intent intent = new Intent(context, RcsGroupChatComposeMessageActivity.class);
            intent.putExtra("received_flag", true);
            intent.putExtra("fromNotification", true);
            intent.putExtra("bundle_group_id", groupChatID);
            intent.setData(Uri.parse("content://rcsim/rcs_group_message" + threadId));
            imChatType = 3;
            senderInfo = buildRcsGroupTickerMessage(context, null, subject, null, subId, threadId).toString();
            senderInfoName = senderInfo.substring(0, senderInfo.length() - 2);
            ticker = buildRcsGroupTickerMessage(context, address, subject, message, subId, threadId);
            RcsSubject = RcsSubject2;
            clickIntent = intent;
        } else {
            imChatType = 2;
            Intent clickIntent2 = RcsComposeMessage.createIntent(context, threadId, 2);
            clickIntent2.putExtra("received_flag", true);
            clickIntent2.putExtra("fromNotification", true);
            senderInfo = MessagingNotification.buildTickerMessage(context, address, null, null, subId, 0).toString();
            if (senderInfo.length() <= 2) {
                senderInfoName = address;
                MLog.d("RcsMessagingNotification", "senderInfo length <= 2 ");
            } else {
                senderInfoName = senderInfo.substring(0, senderInfo.length() - 2);
            }
            ticker = MessagingNotification.buildTickerMessage(context, address, subject, message, subId, 0);
            RcsSubject = null;
            clickIntent = clickIntent2;
        }
        clickIntent.setFlags(872415232);
        return new NotificationInfo(isSms, clickIntent, message, RcsSubject, ticker, timeMillis, senderInfoName, attachmentBitmap, contact, attachmentType, threadId, subId, imChatType);
    }

    private static CharSequence buildRcsGroupTickerMessage(Context context, String address, String subject, String body, int subId, long threadId) {
        StringBuilder buf = new StringBuilder();
        if (!TextUtils.isEmpty(subject)) {
            buf.append(subject.replace('\n', ' ').replace('\r', ' '));
        }
        int offset = buf.length();
        if (!TextUtils.isEmpty(address)) {
            buf.append(' ');
            String displayAddress = RcsUtility.getGroupContactShowName(address, threadId);
            if (!MessageUtils.isNeedLayoutRtl() || TextUtils.isEmpty(body)) {
                buf.append(displayAddress == null ? "" : displayAddress.replace('\n', ' ').replace('\r', ' '));
            } else {
                buf.append('‪');
                buf.append(displayAddress == null ? "" : displayAddress.replace('\n', ' ').replace('\r', ' '));
                buf.append('‬');
            }
        }
        buf.append(':').append(' ');
        if (!TextUtils.isEmpty(body)) {
            buf.append(body.replace('\n', ' ').replace('\r', ' '));
        }
        SpannableString spanText = new SpannableString(buf.toString());
        spanText.setSpan(new StyleSpan(1), 0, offset, 33);
        return spanText;
    }

    public String getDescription(Context context, String oldDescription, int totalFailedCount) {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return oldDescription;
        }
        if (mHsFailedGROUPMap.size() == 0 && mHsFailedIMMap.size() == 0) {
            return oldDescription;
        }
        return context.getResources().getQuantityString(R.plurals.notification_im_failed_multiple, totalFailedCount, new Object[]{Integer.valueOf(totalFailedCount)});
    }

    public Intent getNewIntentWithoutDownload(Context context, Intent oldIntent, long threadId) {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return oldIntent;
        }
        Intent failedIntent;
        if (mHsFailedSMSMap.size() == 1) {
            failedIntent = RcsComposeMessage.createIntent(context, threadId, 1);
        } else if (mHsFailedIMMap.size() == 1) {
            failedIntent = RcsComposeMessage.createIntent(context, threadId, 2);
        } else if (mHsFailedGROUPMap.size() == 1) {
            failedIntent = new Intent(context, RcsGroupChatComposeMessageActivity.class);
            failedIntent.putExtra("bundle_group_thread_id", threadId);
        } else {
            failedIntent = new Intent(context, ComposeMessageActivity.class);
        }
        return failedIntent;
    }

    public void clearData() {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            mHsFailedGROUPMap.clear();
            mHsFailedSMSMap.clear();
            mHsFailedIMMap.clear();
        }
    }

    public int getAllFailedMsgCount(Context context, long[] threadIdResult) {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return 0;
        }
        Cursor cursor = null;
        int totalFailedMsgCount = 0;
        try {
            cursor = SqliteWrapper.query(context, context.getContentResolver(), UNDELIVERED_URI, MMS_THREAD_ID_PROJECTION, "read=0", null, null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return 0;
            }
            int i;
            String selection;
            int count = cursor.getCount();
            totalFailedMsgCount = count;
            for (i = 0; i < count; i++) {
                cursor.moveToPosition(i);
                mHsFailedSMSMap.put(Long.valueOf(cursor.getLong(0)), Integer.valueOf(1));
            }
            if (mHsFailedSMSMap.size() >= 2 && threadIdResult != null) {
                threadIdResult[1] = 0;
            }
            if (cursor != null) {
                cursor.close();
            }
            Cursor groupchatUndeliveredCursor = SqliteWrapper.query(context, context.getContentResolver(), GROUPCHAT_UNDELIVERED_URI, MMS_THREAD_ID_PROJECTION, "read=0 AND status=4 AND (type = 4 OR type = 100 )", null, null);
            if (groupchatUndeliveredCursor != null) {
                count = groupchatUndeliveredCursor.getCount();
                totalFailedMsgCount += count;
                i = 0;
                while (i < count) {
                    try {
                        groupchatUndeliveredCursor.moveToPosition(i);
                        mHsFailedGROUPMap.put(Long.valueOf(groupchatUndeliveredCursor.getLong(0)), Integer.valueOf(3));
                        i++;
                    } catch (Throwable th) {
                        groupchatUndeliveredCursor.close();
                    }
                }
                if (mHsFailedGROUPMap.size() >= 2 && threadIdResult != null) {
                    threadIdResult[1] = 0;
                }
                groupchatUndeliveredCursor.close();
            }
            if (RcsTransaction.isShowUndeliveredIcon()) {
                selection = "read=0 AND type=5 ";
            } else {
                selection = "read=0 AND ( type=5 OR (type=2 AND status=2) )";
            }
            Cursor chatUndeliveredCursor = SqliteWrapper.query(context, context.getContentResolver(), CHAT_UNDELIVERED_URI, MMS_THREAD_ID_PROJECTION, selection, null, null);
            if (chatUndeliveredCursor != null) {
                totalFailedMsgCount += chatUndeliveredCursor.getCount();
                i = 0;
                while (i < chatUndeliveredCursor.getCount()) {
                    try {
                        chatUndeliveredCursor.moveToPosition(i);
                        mHsFailedIMMap.put(Long.valueOf(chatUndeliveredCursor.getLong(0)), Integer.valueOf(2));
                        i++;
                    } catch (Throwable th2) {
                        chatUndeliveredCursor.close();
                    }
                }
                if (mHsFailedIMMap.size() >= 2) {
                    if (threadIdResult != null) {
                        threadIdResult[1] = 0;
                    }
                    chatUndeliveredCursor.close();
                    return totalFailedMsgCount;
                }
                chatUndeliveredCursor.close();
            }
            if (threadIdResult == null || threadIdResult[1] == 0) {
                return totalFailedMsgCount;
            }
            int totalSize = (mHsFailedIMMap.size() + mHsFailedGROUPMap.size()) + mHsFailedSMSMap.size();
            if (totalSize >= 3) {
                threadIdResult[1] = 0;
            } else if (totalSize == 2) {
                if (mHsFailedGROUPMap.size() == 1) {
                    threadIdResult[1] = 0;
                } else {
                    long threadId1 = getkeyValue(mHsFailedSMSMap);
                    long threadId2 = getkeyValue(mHsFailedIMMap);
                    String address1 = getAddressByThreadId(context, threadId1, 1);
                    String address2 = getAddressByThreadId(context, threadId2, 2);
                    if (address1 == null || address1.isEmpty() || address2 == null || address2.isEmpty()) {
                        MLog.e("RcsMessagingNotification", "Exception, address should not be null or empty");
                        return 0;
                    }
                    if (AddrMatcher.isNumberMatch(NumberUtils.normalizeNumber(address1), NumberUtils.normalizeNumber(address2)) > 0) {
                        threadIdResult[0] = threadId1;
                        threadIdResult[1] = 1;
                    } else {
                        threadIdResult[1] = 0;
                    }
                }
            } else if (totalSize == 1) {
                long j;
                threadIdResult[1] = 1;
                if (mHsFailedSMSMap.size() > 0) {
                    j = getkeyValue(mHsFailedSMSMap);
                } else if (mHsFailedIMMap.size() > 0) {
                    j = getkeyValue(mHsFailedIMMap);
                } else {
                    j = getkeyValue(mHsFailedGROUPMap);
                }
                threadIdResult[0] = j;
            }
            return totalFailedMsgCount;
        } catch (Exception e) {
            MLog.e("RcsMessagingNotification", "getUndeliveredMessageCount occur exception: " + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th3) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static long getkeyValue(HashMap<Long, Integer> hsMap) {
        long threadId = 0;
        for (Long longValue : hsMap.keySet()) {
            threadId = longValue.longValue();
        }
        return threadId;
    }

    private static String getAddressByThreadId(Context context, long threadId, int type) {
        String addr = "";
        String recipientId = queryRecipientWithThreadId(context, threadId, type);
        if (recipientId == null) {
            return "";
        }
        try {
            Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), ContentUris.withAppendedId(CANONICAL_ADDRESS_URI, Long.parseLong(recipientId)), null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        addr = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    }
                } catch (RuntimeException e) {
                    MLog.e("RcsMessagingNotification", "getAddressByThreadId cursor Format error");
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return addr;
        } catch (NumberFormatException e2) {
            MLog.e("RcsMessagingNotification", "getAddressByThreadId Number Format error");
            return "";
        }
    }

    private static String queryRecipientWithThreadId(Context mContext, long thread_id, int type) {
        Uri resultUri;
        String recipient = "";
        Cursor cursor = null;
        if (type == 1) {
            resultUri = ContentUris.withAppendedId(Uri.parse("content://mms-sms/conversations/"), thread_id);
        } else {
            resultUri = ContentUris.withAppendedId(Uri.parse("content://rcsim/conversations/"), thread_id);
        }
        try {
            cursor = SqliteWrapper.query(mContext, Uri.withAppendedPath(resultUri, "recipients"), null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                recipient = cursor.getString(cursor.getColumnIndexOrThrow("recipient_ids"));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (RuntimeException e) {
            MLog.e("RcsMessagingNotification", "queryRecipientWithThreadId ContentResolver unknowable error");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return recipient;
    }

    public static void addGroupInviteNotificationID(Context context, String notificationId) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Set<String> notificationIDs = new HashSet();
            Editor editor = prefs.edit();
            notificationIDs = prefs.getStringSet("GroupInviteNotifications", notificationIDs);
            notificationIDs.add(notificationId);
            editor.putStringSet("GroupInviteNotifications", notificationIDs);
            editor.commit();
        }
    }

    public void deleteGroupInviteNotificationID(Context context, String notificationId) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Set<String> notificationIDs = new HashSet();
            Editor editor = prefs.edit();
            notificationIDs = prefs.getStringSet("GroupInviteNotifications", notificationIDs);
            notificationIDs.remove(notificationId);
            editor.putStringSet("GroupInviteNotifications", notificationIDs);
            editor.commit();
        }
    }

    public static void clearGroupInviteNotification(Context context) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            NotificationManager nm = (NotificationManager) context.getSystemService("notification");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Set<String> notificationIDs = new HashSet();
            Editor editor = prefs.edit();
            for (String id : prefs.getStringSet("GroupInviteNotifications", notificationIDs)) {
                nm.cancel(Integer.parseInt(id));
            }
            editor.remove("GroupInviteNotifications");
            editor.commit();
        }
    }

    public static void notifyNewGroupInviteIndicator(Context context, String globalgroupId, String number, String topic) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_enable_notifications", true)) {
                String contactNumber = Contact.get(number, true).getName();
                if (TextUtils.isEmpty(contactNumber)) {
                    contactNumber = number;
                }
                contactNumber = "\"" + contactNumber + "\"";
                if (TextUtils.isEmpty(topic)) {
                    topic = context.getString(R.string.chat_topic_default);
                }
                String body = String.format(context.getString(R.string.rcs_invite_to_group_message), new Object[]{contactNumber, topic});
                String from = context.getString(R.string.chat_topic_default);
                NotificationManager nm = (NotificationManager) context.getSystemService("notification");
                int notificationId = GROUP_NOTIFICATION_ID + 1;
                GROUP_NOTIFICATION_ID = notificationId;
                if (notifyForThread.containsKey(globalgroupId)) {
                    nm.cancel(((Integer) notifyForThread.get(globalgroupId)).intValue());
                }
                notifyForThread.put(globalgroupId, Integer.valueOf(notificationId));
                addGroupInviteNotificationID(context, String.valueOf(notificationId));
                Intent clickIntent = new Intent(context, ConversationList.class);
                clickIntent.setAction(String.valueOf(System.currentTimeMillis()));
                clickIntent.putExtra("body", body);
                clickIntent.putExtra("isGroupInviteNotify", true);
                clickIntent.putExtra("notificationId", notificationId);
                clickIntent.putExtra("globalgroupId", globalgroupId);
                clickIntent.putExtra("chairMan", number);
                clickIntent.putExtra("topic", topic);
                Builder noti = new Builder(context).setWhen(System.currentTimeMillis());
                noti.setSmallIcon(R.drawable.stat_notify_sms);
                noti.setLargeIcon(MessagingNotification.getSmsAppBitmap(context));
                noti.setTicker(body);
                Notification notification = noti.getNotification();
                setSoundAndVibrateForNewGroupNotification(context, notification);
                Context context2 = context;
                notification.setLatestEventInfo(context2, from, body, PendingIntent.getActivityAsUser(context, 0, clickIntent, 0, null, UserHandle.CURRENT));
                notification.flags |= 1;
                notification.defaults |= 4;
                nm.notifyAsUser(null, notificationId, notification, UserHandle.CURRENT);
            }
        }
    }

    public static void notifyNewGroupCreatedIndicator(Context context, String groupId, String number, String topic) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_enable_notifications", true)) {
            if (TextUtils.isEmpty(topic)) {
                topic = context.getString(R.string.chat_topic_default);
            }
            String body = String.format(context.getString(R.string.rcs_group_create_toast), new Object[]{topic});
            String from = context.getString(R.string.chat_topic_default);
            NotificationManager nm = (NotificationManager) context.getSystemService("notification");
            int notificationId = GROUP_NOTIFICATION_ID + 1;
            GROUP_NOTIFICATION_ID = notificationId;
            if (notifyForThread.containsKey(groupId)) {
                nm.cancel(((Integer) notifyForThread.get(groupId)).intValue());
            }
            notifyForThread.put(groupId, Integer.valueOf(notificationId));
            Intent clickIntent = new Intent(context, RcsGroupChatComposeMessageActivity.class);
            clickIntent.putExtra("bundle_group_id", groupId);
            clickIntent.setAction(String.valueOf(System.currentTimeMillis()));
            PendingIntent pendingIntent = PendingIntent.getActivityAsUser(context, 0, clickIntent, 0, null, UserHandle.CURRENT);
            Builder noti = new Builder(context).setWhen(System.currentTimeMillis());
            noti.setSmallIcon(R.drawable.stat_notify_sms);
            noti.setLargeIcon(MessagingNotification.getSmsAppBitmap(context));
            noti.setTicker(body);
            enableHeadsUpNotification(noti);
            Notification notification = noti.getNotification();
            setSoundAndVibrateForNewGroupNotification(context, notification);
            notification.setLatestEventInfo(context, from, body, pendingIntent);
            notification.flags |= 1;
            notification.flags |= 16;
            notification.defaults |= 4;
            nm.notifyAsUser(null, notificationId, notification, UserHandle.CURRENT);
        }
    }

    private static void enableHeadsUpNotification(Builder noti) {
        noti.setCategory("msg");
        MessagingNotification.enableHeadsUp(noti, true);
        MLog.d("RcsMessagingNotification", "notifyNewGroupCreatedNotification enable headsup");
    }

    private static void setSoundAndVibrateForNewGroupNotification(Context context, Notification noti) {
        boolean vibrate;
        String ringtoneStr;
        Uri uri = null;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        AudioManager audioManager = (AudioManager) context.getSystemService("audio");
        if (audioManager.getRingerMode() == 2) {
            vibrate = sp.getBoolean("pref_key_vibrateWhen", false);
            if (MmsConfig.isContainsTheKey(context)) {
                ringtoneStr = HwMessageUtils.getRingtoneString(context);
            } else {
                ringtoneStr = sp.getString("pref_key_ringtone", null);
                MmsConfig.setRingToneUriToDatabase(context, ringtoneStr);
            }
        } else if (audioManager.getRingerMode() == 1) {
            vibrate = true;
            ringtoneStr = sp.getString("pref_key_ringtoneSp", null);
        } else {
            vibrate = false;
            ringtoneStr = sp.getString("pref_key_ringtoneSp", null);
        }
        if (TelephonyManager.getDefault().getCallState() == 0) {
            if (!TextUtils.isEmpty(ringtoneStr)) {
                uri = Uri.parse(ringtoneStr);
            }
            noti.sound = uri;
        } else if (MmsConfig.isRingWhentalk() && TelephonyManager.getDefault().getCallState() == 2) {
            noti.sound = getMediaVolumeUri(context);
            vibrate = false;
        } else {
            vibrate = false;
        }
        if (vibrate) {
            noti.vibrate = new long[]{100, 10, 100, 1000};
            return;
        }
        noti.vibrate = new long[]{0};
    }

    private static Uri getMediaVolumeUri(Context context) {
        return Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.arrived);
    }

    public static void clearGroupCreateNotificationByGroupId(Context context, String groupId) {
        NotificationManager nm = (NotificationManager) context.getSystemService("notification");
        if (notifyForThread.containsKey(groupId)) {
            nm.cancel(((Integer) notifyForThread.get(groupId)).intValue());
        }
    }

    private static int getNewRCSGroupMessageNotified(Context context, long threadId) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        int notifySilent = 0;
        try {
            cursor = SqliteWrapper.query(context, resolver, IM_GROUPCHAT_URI, null, " thread_id = " + threadId, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                notifySilent = cursor.getInt(cursor.getColumnIndexOrThrow("is_groupchat_notify_silent"));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (RuntimeException e) {
            notifySilent = 0;
            MLog.e("RcsMessagingNotification", "getNewRCSGroupMessageNotified error");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return notifySilent;
    }

    public static void updateUndeliveredStatus(long rcsThreadId, Context context) {
        if (RcsProfile.isRcsImSupportSF() || RcsProfile.isRcsFTSupportSF()) {
            MLog.w("RcsMessagingNotification", "updateUndeliveredStatus getEnableSmsDeliverToast isRcsImSupportSF or isRcsFTSupportSF");
            return;
        }
        Cursor cursor = null;
        boolean isNeedUpdate = false;
        ContentValues values = new ContentValues();
        String selection = "";
        String selectString = "thread_id = " + rcsThreadId + " and status = " + 16 + " and network_type != " + 9966;
        try {
            cursor = SqliteWrapper.query(context, Uri.parse("content://rcsim/chat"), new String[]{"_id"}, selectString, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                isNeedUpdate = true;
                selection = getSelectionString(cursor);
            }
            if (cursor != null) {
                cursor.close();
            }
            if (isNeedUpdate) {
                try {
                    values.put("status", Integer.valueOf(2));
                    SqliteWrapper.update(context, Uri.parse("content://rcsim/chat"), values, selectString, null);
                    ContentValues ftValues = new ContentValues();
                    try {
                        ftValues.put("transfer_status", Integer.valueOf(Place.TYPE_PREMISE));
                        Context context2 = context;
                        SqliteWrapper.update(context2, Uri.parse("content://rcsim/file_trans"), ftValues, "thread_id = " + rcsThreadId + " and transfer_status = " + 1002 + selection, null);
                    } catch (NumberFormatException e) {
                        MLog.w("RcsMessagingNotification", "updateUndeliveredStatus ftValues NumberFormatException");
                    }
                } catch (NumberFormatException e2) {
                    MLog.w("RcsMessagingNotification", "updateUndeliveredStatus NumberFormatException");
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static String getSelectionString(Cursor c) {
        if (c == null || !c.moveToFirst()) {
            return " ";
        }
        StringBuilder selection = new StringBuilder(" and ").append("msg_id").append(" in (").append(String.valueOf(c.getLong(c.getColumnIndexOrThrow("_id"))));
        while (c.moveToNext()) {
            selection.append(", ").append(String.valueOf(c.getLong(c.getColumnIndexOrThrow("_id"))));
        }
        return selection.append(" )").toString();
    }

    public void setHwCustCallback(IHwCustMessagingNotificationCallback callback) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            mCallback = callback;
        }
    }

    public void updateNotificationsExt(Context context, long newMsgThreadId, Uri msgUri, boolean checked, int isIncomingMessageType, Bundle bundle) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            updateRcsNotifications(context, newMsgThreadId, msgUri, checked, isIncomingMessageType, bundle);
        }
    }

    public void checkAndUpdateNotifications(Context context, long newMsgThreadId, boolean isBannerMode, Uri uri) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            if (mCallback == null) {
                mCallback = MessagingNotification.getHolderInstance();
            }
            if ((!isMms && !isSms) || (!mCallback.isNotificationBlocked(context, newMsgThreadId) && NotificationReceiver.getInst().canSendNotification(context, null))) {
                if (isNotificationBlockForRcs(context, newMsgThreadId, 1)) {
                    MLog.d("RcsMessagingNotification", "isNotificationBlockForRcs");
                    return;
                }
                updateMultyNotificationsForRcs(context, newMsgThreadId, mCallback.isMmsBannerEnabled(context), isBannerMode, 1, uri);
            }
        }
    }

    public static void updateRcsNotifications(Context context, long newMsgThreadId, Uri msgUri, boolean checked, int isIncomingMessageType, Bundle bundle) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            MLog.d("RcsMessagingNotification", "updateRcsNotifications---- newMsgThreadId:" + newMsgThreadId + " msgUri:" + msgUri + " checked:" + checked + " isIncomingMessageType:" + isIncomingMessageType);
            if (msgUri != null) {
                if (newMsgThreadId > 0 && RcsTransaction.isShowUndeliveredIcon()) {
                    Collection<Long> smsThreadIds = new ArrayList();
                    smsThreadIds.add(Long.valueOf(newMsgThreadId));
                    Collection<Long> otherRcsThreadIds = RcsConversationUtils.getHwCustUtils().getOtherThreadFromGivenThread(context, smsThreadIds, 1);
                    if (otherRcsThreadIds != null && otherRcsThreadIds.size() == 1) {
                        updateUndeliveredStatus(((Long) otherRcsThreadIds.iterator().next()).longValue(), context);
                    }
                }
                String authority = msgUri.getAuthority();
                String tableName = (String) msgUri.getPathSegments().get(0);
                MLog.d("RcsMessagingNotification", "authority:" + authority);
                MLog.d("RcsMessagingNotification", "tableName:" + tableName);
                isMms = Mms.CONTENT_URI.getAuthority().equals(authority);
                isSms = Sms.CONTENT_URI.getAuthority().equals(authority);
                isChat = "chat".equals(tableName);
                isGroup = "rcs_group_message".equals(tableName);
                boolean isFirstMessage = false;
                if (mCallback == null) {
                    mCallback = MessagingNotification.getHolderInstance();
                }
                if ((isMms || isSms) && mCallback.isNotificationBlocked(context, newMsgThreadId)) {
                    MLog.d("RcsMessagingNotification", "isNotificationBlocked");
                } else if ((isChat || isGroup) && isNotificationBlockForRcs(context, newMsgThreadId, isIncomingMessageType)) {
                    MLog.d("RcsMessagingNotification", "isNotificationBlockForRcs");
                } else {
                    if (checked) {
                        MLog.d("RcsMessagingNotification", "updateRcsNotifications checked!");
                    } else if (!NotificationReceiver.getInst().canSendNotification(context, msgUri)) {
                        MLog.d("RcsMessagingNotification", "can not SendNotification");
                        if (newMsgThreadId > 0) {
                            NotificationReceiver.getInst().cacheMessage(newMsgThreadId, msgUri);
                            MessagingNotification.addNotificationMap(msgUri, newMsgThreadId);
                        }
                        return;
                    } else if (NotificationReceiver.getInst().isUserPresent(context)) {
                        boolean isBannerEnabled = mCallback.isMmsBannerEnabled(context);
                        if (!isBannerEnabled) {
                            MLog.d("RcsMessagingNotification", "isBannerEnabled:" + isBannerEnabled);
                        } else if (newMsgThreadId <= 0) {
                            MLog.d("RcsMessagingNotification", "not a new message");
                        } else {
                            checked = true;
                        }
                    } else {
                        MLog.d("RcsMessagingNotification", "user not unlock");
                    }
                    if (checked) {
                        if (NotificationReceiver.getInst().getCacheMessageSize() == 0) {
                            isFirstMessage = true;
                        }
                        updateRcsSingleNotifications(context, newMsgThreadId, msgUri, isIncomingMessageType, bundle, isFirstMessage);
                    } else {
                        MLog.d("RcsMessagingNotification", "updateMultyNotifications");
                        updateMultyNotificationsForRcs(context, newMsgThreadId, true, false, isIncomingMessageType, msgUri);
                    }
                }
            }
        }
    }

    private static void updateRcsSingleNotifications(Context context, long newMsgThreadId, Uri msgUri, int isIncomingMessageType, Bundle bundle, boolean isFirstMessage) {
        if (!RcsTransaction.isIncallChatting(newMsgThreadId)) {
            if (isMms || isSms) {
                mCallback.updateSingleNotifications(context, newMsgThreadId, msgUri);
            } else {
                NotificationInfo info;
                if (isChat) {
                    info = createNotificationInfoSingle(context, msgUri, bundle);
                } else if (isGroup) {
                    info = createNotificationInfoGroup(context, msgUri, bundle);
                } else {
                    MLog.d("RcsMessagingNotification", "updateRcsSingleNotifications unsupport uri " + msgUri);
                    return;
                }
                notifyRcs(context, msgUri, newMsgThreadId, info, isIncomingMessageType, isFirstMessage);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void updateMultyNotificationsForRcs(Context context, long newMsgThreadId, boolean preViewEnabled, boolean isBannerMode, int isIncomingMessageType, Uri uri) {
        SortedSet<NotificationInfo> notificationSet = new TreeSet(MessagingNotification.INFO_COMPARATOR);
        Set<Long> hashSet = new HashSet(4);
        MessagingNotification.addMmsNotificationInfos(context, hashSet, notificationSet);
        MessagingNotification.addSmsNotificationInfos(context, hashSet, notificationSet);
        Set<Long> groupChatThreads = new HashSet(4);
        addImGroupChatNotificationInfos(context, groupChatThreads, notificationSet);
        Set<Long> imSingleThreads = new HashSet(4);
        int addImSingleThreadNumber = addImSingleNotificationInfos(context, imSingleThreads, hashSet, notificationSet);
        if (notificationSet.isEmpty()) {
            MLog.d("RcsMessagingNotification", "updateMultyNotificationsForRcs: notificationSet is empty, canceling existing notifications");
            MessagingNotification.cancelNotification(context, 123);
            return;
        }
        synchronized (MessagingNotification.getCurrentlyDisplayedThreadLock()) {
            MLog.d("RcsMessagingNotification", "updateMultyNotificationsForRcs newMsgThreadId=" + newMsgThreadId + " sCurrentlyDisplayedThreadId=" + MessagingNotification.getCurrentlyDisplayedThreadId() + " sCurrentlyDisplayedRcsType=" + sCurrentlyDisplayedRcsType);
            if (isIncomingMessageType == 3) {
                if ((newMsgThreadId > 0 && newMsgThreadId == MessagingNotification.getCurrentlyDisplayedThreadId() && 3 == sCurrentlyDisplayedRcsType && groupChatThreads.contains(Long.valueOf(newMsgThreadId))) || (RcsTransaction.isEnableGroupSilentMode() && getNewRCSGroupMessageNotified(context, newMsgThreadId) > 0)) {
                    MLog.d("RcsMessagingNotification", "updateMultyNotificationsForRcs newMsgThreadId == sCurrentlyDisplayedThreadId so NOT showing notification, but playing soft sound. threadId: " + newMsgThreadId);
                }
            } else if (isIncomingMessageType != 0) {
                long newMsgThreadIdTrue = newMsgThreadId;
                if (newMsgThreadId > 0) {
                    if (3 != sCurrentlyDisplayedRcsType) {
                        if (sCurrentlyDisplayedRcsType == ((long) isIncomingMessageType) || sCurrentlyDisplayedRcsType == 0) {
                            if (newMsgThreadId == MessagingNotification.getCurrentlyDisplayedThreadId()) {
                                if (!imSingleThreads.contains(Long.valueOf(newMsgThreadId))) {
                                }
                                MLog.d("RcsMessagingNotification", "blockingUpdateNewMessageIndicator merge incoming ");
                            }
                        } else if (isIncomingMessageType == 1) {
                            if (RcsConversationUtils.getHwCustUtils().queryChatThreadIdWithAddress(RcsConversationUtils.getHwCustUtils().queryAddressWithid(newMsgThreadId, 1, context), context) == MessagingNotification.getCurrentlyDisplayedThreadId()) {
                                if (hashSet.contains(Long.valueOf(newMsgThreadId))) {
                                    MLog.d("RcsMessagingNotification", "blockingUpdateNewMessageIndicator merge incoming sms ");
                                }
                            }
                        } else if (isIncomingMessageType == 2) {
                            if (RcsConversationUtils.getHwCustUtils().querySmsThreadIdWithAddress(RcsConversationUtils.getHwCustUtils().queryAddressWithid(newMsgThreadId, 2, context), context) == MessagingNotification.getCurrentlyDisplayedThreadId() && imSingleThreads.contains(Long.valueOf(newMsgThreadId))) {
                                MLog.d("RcsMessagingNotification", "blockingUpdateNewMessageIndicator merge incoming im ");
                            }
                        }
                    }
                }
            }
        }
    }

    private static NotificationInfo createNotificationInfoSingle(Context context, Uri msgUri, Bundle bundle) {
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), msgUri, SMS_STATUS_PROJECTION, "(type = 1 AND seen = 0)", null, null);
        if (cursor == null) {
            MLog.d("RcsMessagingNotification", "createNotificationInfoSingle cursor is null");
            return null;
        }
        NotificationInfo notificationInfo = null;
        try {
            if (cursor.moveToFirst()) {
                String address = cursor.getString(2);
                Contact contact = Contact.get(address, false);
                if (contact.getSendToVoicemail()) {
                    return null;
                }
                String message = cursor.getString(4);
                long threadId = cursor.getLong(0);
                long timeMillis = cursor.getLong(1);
                int subId = cursor.getInt(5);
                if (3 == RcsProfileUtils.getRcsMsgType(cursor)) {
                    message = codeFileNoticeContent(context, bundle, message);
                    if (mReceiveTypeMap.size() > 0) {
                        clearReceiveTypeMap();
                    }
                    mReceiveTypeMap.put(Long.valueOf(timeMillis), message);
                }
                if (RcsMapLoader.isLocItem(message)) {
                    message = context.getResources().getString(R.string.attach_map_location);
                }
                notificationInfo = getNewRCSMessageNotificationInfo(context, false, message, address, null, threadId, timeMillis, null, contact, 0, subId, null);
                notificationInfo.setMsgId(cursor.getLong(6));
            }
            cursor.close();
            return notificationInfo;
        } finally {
            cursor.close();
        }
    }

    private static NotificationInfo createNotificationInfoGroup(Context context, Uri msgUri, Bundle bundle) {
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), msgUri, null, "(( type = 1 OR type = 101 )  AND seen = 0 )", null, "date desc");
        if (cursor == null) {
            MLog.d("RcsMessagingNotification", "createNotificationInfoGroup  cursor is null");
            return null;
        }
        NotificationInfo info = null;
        try {
            if (cursor.moveToFirst()) {
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                Contact contact = Contact.get(address, false);
                String message = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                long threadId = cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"));
                long timeMillis = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                if (RcsProfileUtils.checkIsGroupFile(cursor)) {
                    message = codeFileNoticeContent(context, null, message);
                }
                if (RcsMapLoader.isLocItem(message)) {
                    message = context.getResources().getString(R.string.attach_map_location);
                }
                MLog.d("RcsMessagingNotification", "addImGroupChatNotificationInfos: count=" + cursor.getCount() + ", thread_id=" + threadId);
                String[] groupStr = getGroupChatTopicAndGroupId(context, threadId);
                Context context2 = context;
                info = getNewRCSMessageNotificationInfo(context2, false, message, address, groupStr[1], threadId, timeMillis, null, contact, 0, 0, groupStr[0]);
            }
            cursor.close();
            return info;
        } catch (Throwable th) {
            cursor.close();
        }
    }

    private static String[] getGroupChatTopicAndGroupId(Context context, long threadId) {
        String[] groupFactor = new String[2];
        Cursor cursor = null;
        ContentResolver resolver = context.getContentResolver();
        String str = null;
        String groupChatID = null;
        try {
            cursor = SqliteWrapper.query(context, resolver, IM_GROUPCHAT_URI, null, " thread_id = " + threadId, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                str = cursor.getString(cursor.getColumnIndexOrThrow("subject"));
                groupChatID = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            }
            groupFactor[0] = groupChatID;
            groupFactor[1] = str;
            return groupFactor;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String[] getGroupId(Context context, long threadId) {
        return getGroupChatTopicAndGroupId(context, threadId);
    }

    private static void notifyRcs(Context context, Uri msgUri, long newMsgThreadId, NotificationInfo notificationInfo, int isIncomingMessageType, boolean isFirstMessage) {
        MLog.e("RcsMessagingNotification", "notifyRcs");
        if (notificationInfo == null) {
            MLog.e("RcsMessagingNotification", "Can't notifyRcs, notificationInfo is null");
            return;
        }
        Bundle bundle;
        PendingIntent clickPendingIntent;
        PendingIntent viewPendingIntent;
        String title = notificationInfo.getSenderName(context);
        if (mCallback.bluetoothHeadset(context)) {
            mCallback.headsetTone(context);
        }
        CharSequence content = notificationInfo.formatMessage(context);
        int notificationId = NotificationReceiver.getInst().getNotificationId(context);
        if (isChat) {
            MLog.e("RcsMessagingNotification", "isChat getclickIntent newMsgThreadId:" + newMsgThreadId);
            bundle = new Bundle();
            bundle.putString(NumberInfo.TYPE_KEY, "isChat");
            bundle.putString("address", notificationInfo.getSenderNumber());
            clickPendingIntent = RcsNotificationReceiver.getClickIntent(context, msgUri, newMsgThreadId, notificationInfo.mSubId, "isChat", notificationInfo.getSenderNumber(), isFirstMessage, notificationId, notificationInfo, true);
            viewPendingIntent = RcsNotificationReceiver.getContentIntent(context, msgUri, newMsgThreadId, bundle);
        } else if (isGroup) {
            String senderNumber = notificationInfo.getSenderNumber();
            if (MessageUtils.isNeedLayoutRtl()) {
                senderNumber = formatSenderNumber(senderNumber);
            }
            content = formatMessageForGroup(senderNumber, notificationInfo.mMessage, newMsgThreadId);
            MLog.e("RcsMessagingNotification", "isGroup getclickIntent newMsgThreadId:" + newMsgThreadId);
            String[] groupStr = getGroupChatTopicAndGroupId(context, newMsgThreadId);
            String groupChatID = groupStr[0];
            String subject = groupStr[1];
            if (TextUtils.isEmpty(subject)) {
                subject = context.getString(R.string.chat_topic_default);
            }
            title = subject;
            bundle = new Bundle();
            bundle.putString(NumberInfo.TYPE_KEY, "isGroup");
            bundle.putString("groupId", groupChatID);
            clickPendingIntent = RcsNotificationReceiver.getClickIntent(context, msgUri, newMsgThreadId, notificationInfo.mSubId, "isGroup", groupChatID, isFirstMessage, notificationId, notificationInfo, true);
            viewPendingIntent = RcsNotificationReceiver.getContentIntent(context, msgUri, newMsgThreadId, bundle);
        } else {
            clickPendingIntent = NotificationReceiver.getClickIntent(context, msgUri, newMsgThreadId, notificationInfo.mSubId);
            viewPendingIntent = NotificationReceiver.getContentIntent(context, msgUri, newMsgThreadId);
        }
        PendingIntent deletPendingIntent = NotificationReceiver.getDeleteIntent(context);
        Builder builder = new Builder(context);
        mCallback.initSoundAndVibrateSettings(context, notificationInfo.mSender.getNumber(), builder, notificationInfo.mSubId);
        bundle = new Bundle();
        Bundle bundle2 = bundle;
        bundle2.putInt("sub_id", notificationInfo.mSubId);
        bundle.putBoolean("hw_rcs", true);
        Action action = new Action.Builder(R.drawable.mms_ic_add_contact_dark, context.getString(R.string.quick_reply), clickPendingIntent).addRemoteInput(new RemoteInput.Builder("Quick_Reply").setLabel(context.getString(R.string.type_to_compose_text_enter_to_send)).build()).build();
        Conversation conv = Conversation.get(context, notificationInfo.mThreadId, true);
        ContactList contacts = conv.getRecipients();
        Contact contact = null;
        if (contacts != null && contacts.size() > 0) {
            contact = (Contact) contacts.get(0);
        }
        builder.setWhen(notificationInfo.mTimeMillis).setCategory("msg").setPriority(6).setSmallIcon(R.drawable.stat_notify_sms).setLargeIcon(AvatarCache.drawableToBitmap(mCallback.getDefaultAvatar(context, contact, conv))).setContentIntent(viewPendingIntent).setDeleteIntent(deletPendingIntent).setExtras(bundle).setContentTitle(title).setContentText(content).addAction(action);
        mCallback.setNotificationIcon(context, contact, conv, builder);
        MessagingNotification.enableHeadsUp(builder, true);
        Notification notification = builder.build();
        MLog.d("RcsMessagingNotification", " notifyRcs normal. sub=" + notificationInfo.mSubId);
        ((NotificationManager) context.getSystemService("notification")).notify(notificationId, notification);
        NotificationReceiver.getInst().markCurrentNotifyMessage(newMsgThreadId, msgUri, notificationInfo.mSubId);
        if (notification.largeIcon != null) {
            notification.largeIcon.recycle();
        }
    }

    private static String formatSenderNumber(String senderNumber) {
        if (TextUtils.isEmpty(senderNumber)) {
            return senderNumber;
        }
        return new StringBuffer().append('‪').append(senderNumber).append('‬').toString();
    }

    private static CharSequence formatMessageForGroup(String address, String message, long threadId) {
        SpannableStringBuilder strSpanBuilder = new SpannableStringBuilder();
        strSpanBuilder.append("\r");
        strSpanBuilder.append(RcsUtility.getGroupContactShowName(address, threadId));
        if (!TextUtils.isEmpty(message)) {
            message = message.replaceAll("\\n\\s+", "\n");
            strSpanBuilder.append(":");
            strSpanBuilder.append(message);
        }
        return strSpanBuilder;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean isNotificationBlockForRcs(Context context, long newMsgThreadId, int isIncomingMessageType) {
        if (MmsConfig.isSupportPrivacy() && !PrivacyStateListener.self().isInPrivacyMode() && PrivacyModeReceiver.isPrivacyThread(context, newMsgThreadId)) {
            MLog.d("RcsMessagingNotification", "isInPrivacyMode with threadid: " + newMsgThreadId);
            return true;
        }
        synchronized (MessagingNotification.getCurrentlyDisplayedThreadLock()) {
            MLog.d("RcsMessagingNotification", "isNotificationBlockForRcs newMsgThreadId=" + newMsgThreadId + " sCurrentlyDisplayedThreadId=" + MessagingNotification.getCurrentlyDisplayedThreadId() + " sCurrentlyDisplayedRcsType=" + sCurrentlyDisplayedRcsType);
            if (isIncomingMessageType == 3) {
                if (!(newMsgThreadId > 0 && newMsgThreadId == MessagingNotification.getCurrentlyDisplayedThreadId() && 3 == sCurrentlyDisplayedRcsType)) {
                    if (RcsTransaction.isEnableGroupSilentMode()) {
                    }
                }
                MLog.d("RcsMessagingNotification", "isNotificationBlockForRcs newMsgThreadId == sCurrentlyDisplayedThreadId so NOT showing notification, but playing soft sound. threadId: " + newMsgThreadId);
                return true;
            } else if (isIncomingMessageType != 0) {
                long newMsgThreadIdTrue = newMsgThreadId;
                if (newMsgThreadId > 0) {
                    if (3 != sCurrentlyDisplayedRcsType) {
                        if (sCurrentlyDisplayedRcsType == ((long) isIncomingMessageType) || sCurrentlyDisplayedRcsType == 0) {
                            if (newMsgThreadId == MessagingNotification.getCurrentlyDisplayedThreadId()) {
                                MLog.d("RcsMessagingNotification", "isNotificationBlockForRcs merge incoming ");
                                return true;
                            }
                        } else if (isIncomingMessageType == 1) {
                            if (RcsConversationUtils.getHwCustUtils().queryChatThreadIdWithAddress(RcsConversationUtils.getHwCustUtils().queryAddressWithid(newMsgThreadId, 1, context), context) == MessagingNotification.getCurrentlyDisplayedThreadId()) {
                                MLog.d("RcsMessagingNotification", "isNotificationBlockForRcs merge incoming sms ");
                                return true;
                            }
                        } else if (isIncomingMessageType == 2) {
                            if (RcsConversationUtils.getHwCustUtils().querySmsThreadIdWithAddress(RcsConversationUtils.getHwCustUtils().queryAddressWithid(newMsgThreadId, 2, context), context) == MessagingNotification.getCurrentlyDisplayedThreadId()) {
                                MLog.d("RcsMessagingNotification", "isNotificationBlockForRcs merge incoming im ");
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
    }
}
