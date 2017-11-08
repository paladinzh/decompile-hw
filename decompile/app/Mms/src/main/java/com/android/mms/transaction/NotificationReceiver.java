package com.android.mms.transaction;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.UserHandle;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.HarassNumberUtil;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.amap.api.services.core.AMapException;
import com.android.messaging.util.OsUtil;
import com.android.mms.MmsApp;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.provider.NewMessageContentObserver;
import com.android.mms.transaction.MessagingNotification.NotificationInfo;
import com.android.mms.transaction.MmsPermReceiver.IEventListener;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.SmartNewNotificationManager;
import com.android.rcs.transaction.RcsMessagingNotification;
import com.android.rcs.transaction.RcsNotificationReceiver;
import com.google.android.gms.Manifest.permission;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.mms.crypto.CryptoMessageUtil;
import com.huawei.mms.util.AvatarCache;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.StatisticalHelper;
import com.huawei.rcs.utils.RcsUtility;
import java.util.ArrayList;

public class NotificationReceiver implements IEventListener {
    private static Bundle mFirstBundle = null;
    private static RcsNotificationReceiver mHwCustReceiver = new RcsNotificationReceiver();
    private static Intent sNotificationOnDeleteIntent = null;
    private static NotificationReceiver sNotificationReceiver = new NotificationReceiver();
    private final ArrayList<CachedMsg> mCachedReceivedMsgs = new ArrayList();
    private CachedMsg mCurrentItem = null;
    private BroadcastReceiver mNopermReciever = new BroadcastReceiver() {
        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            int i = 3;
            if (intent == null || intent.getAction() == null) {
                MLog.e("Mms_TX_NOTIFY", "recieve emtpy no perm message");
                return;
            }
            String action = intent.getAction();
            MLog.d("Mms_TX_NOTIFY", "Get no-perm notification callback " + action);
            if ("com.huawei.android.cover.STATE".equals(action)) {
                if (intent.getBooleanExtra("coverOpen", false)) {
                    MLog.d("Mms_TX_NOTIFY", "Action cover Opened, skip ");
                    return;
                }
                NotificationReceiver.this.mScreenState = 2;
                synchronized (NotificationReceiver.this.mCachedReceivedMsgs) {
                    if (NotificationReceiver.this.getCacheMessageSize() == 0 && NotificationReceiver.this.mCurrentItem == null) {
                        return;
                    }
                }
            }
            if ("android.intent.action.SCREEN_OFF".equals(action)) {
                NotificationReceiver.this.mScreenState = 1;
                synchronized (NotificationReceiver.this.mCachedReceivedMsgs) {
                    if (NotificationReceiver.this.getCacheMessageSize() == 0 && NotificationReceiver.this.mCurrentItem == null) {
                        return;
                    }
                }
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                if (NotificationReceiver.this.mScreenState != 3) {
                    NotificationReceiver notificationReceiver = NotificationReceiver.this;
                    if (NotificationReceiver.isScreenLocked(context)) {
                        i = 2;
                    }
                    notificationReceiver.mScreenState = i;
                }
            } else if ("android.intent.action.KEYGUARD_UNLOCK".equals(action) || "android.intent.action.USER_PRESENT".equals(action)) {
                NotificationReceiver.this.mScreenState = 3;
            } else if ("android.intent.action.USER_SWITCHED".equals(action)) {
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (userId < 0) {
                    MLog.d("Mms_TX_NOTIFY", "Switch to invalid u-" + userId);
                } else if (OsUtil.isSmsDisabledForUser(context, userId)) {
                    MLog.d("Mms_TX_NOTIFY", "Switch to restricted u-" + userId);
                    MessagingNotification.cancelAllNotification(context);
                    NotificationReceiver.this.updateLauncherUnreadCountZero(true);
                } else if (OsUtil.isOwner()) {
                    MLog.d("Mms_TX_NOTIFY", "Switch to  u-" + userId);
                    NotificationReceiver.this.updateLauncherUnreadCountZero(false);
                    MessagingNotification.blockingUpdateAllNotifications(context, -2);
                } else {
                    NotificationReceiver.this.updateLauncherUnreadCountZero(false);
                }
            } else if ("com.android.mms.HEADSUP_NOTIFICATION_REMAIN".equals(action)) {
                int notificationId = intent.getIntExtra("mms_notification_id", -1);
                if (1390 == notificationId) {
                    HwBackgroundLoader.getBackgroundHandler().removeCallbacks(NotificationReceiver.this.mNotificationTimeoutChecker);
                }
                MLog.d("Mms_TX_NOTIFY", "uri = " + ((Uri) intent.getParcelableExtra("msg_uri")) + ", notificationId = " + notificationId);
                NotificationReceiver.this.markMessageRead(context, intent);
            } else {
                MLog.e("Mms_TX_NOTIFY", "NotificationReceiver get unknow action " + action);
            }
        }
    };
    private Runnable mNotificationTimeoutChecker = new Runnable() {
        public void run() {
            NotificationReceiver.this.procNextMessage(MmsApp.getApplication());
        }
    };
    public int mScreenState = -1;

    private static class CachedMsg {
        private Uri mMsgUri;
        private long mRSTime;
        private int mSubId;
        private long mTId;

        private CachedMsg(long id, Uri uri) {
            this(id, uri, 0);
        }

        private CachedMsg(long id, Uri uri, int subId) {
            this.mTId = id;
            this.mMsgUri = uri;
            this.mSubId = subId;
            this.mRSTime = System.currentTimeMillis();
        }

        private void sendMe(Context context) {
            MessagingNotification.blockingUpdateNewMessageIndicator(context, this.mTId, this.mMsgUri);
        }

        public String toString() {
            return "CachedMsg@" + this.mRSTime + ":" + this.mSubId + "-" + this.mTId;
        }
    }

    public static NotificationReceiver getInst() {
        return sNotificationReceiver;
    }

    public int getNotificationId(Context context) {
        return 1390;
    }

    public void registe(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.KEYGUARD_UNLOCK");
        intentFilter.addAction("com.huawei.android.cover.STATE");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        context.registerReceiver(this.mNopermReciever, intentFilter);
        IntentFilter intentFilterHeadsUp = new IntentFilter();
        intentFilterHeadsUp.addAction("com.android.mms.HEADSUP_NOTIFICATION_REMAIN");
        context.registerReceiver(this.mNopermReciever, intentFilterHeadsUp, permission.HEADSUP_NOTIFICATION_REMAIN_ACTION, null);
    }

    private void updateLauncherUnreadCountZero(boolean isUpdateZero) {
        NewMessageContentObserver newMessageContentObserver = MmsApp.getApplication().getNewMessageContentObserver();
        if (newMessageContentObserver != null) {
            MLog.d("Mms_TX_NOTIFY", "updateLauncherUnreadCountZero method start, " + isUpdateZero);
            if (isUpdateZero) {
                newMessageContentObserver.resetLauncherUnreadCount();
            } else {
                newMessageContentObserver.updateLauncherUnreadCount();
            }
        }
    }

    public boolean onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        MLog.d("Mms_TX_NOTIFY", "Get perm notification callback " + action);
        if ("com.huawei.mms.action.headsup.viewmsg".equals(action)) {
            viewMessage(context, intent);
        } else if ("com.huawei.mms.action.headsup.clicked".equals(action)) {
            handleHeadsupClickAction(context, intent);
        } else if (permission.NOTIFICATION_UPDATE_ACTION.equals(action)) {
            handleNotificationUpdate(context, intent);
        } else if ("com.android.mms.NOTIFICATION_DELETED_ACTION".equals(action)) {
            CachedMsg currentItem;
            int nId = intent.getIntExtra("mms_notification_id", -1);
            synchronized (this.mCachedReceivedMsgs) {
                currentItem = this.mCurrentItem;
            }
            MLog.d("Mms_TX_NOTIFY", "Notification deleteId " + nId + " " + currentItem);
            if (nId == 1390) {
                cancelNotification(context, nId);
                MLog.i("Mms_TX_NOTIFY", "procNextMessageDelayed NOTIFICATION_DELETED_ACTION. SystemUI start.100");
                procNextMessageDelayed(100);
            } else if (getCacheMessageSize() > 0 || currentItem != null) {
                MLog.i("Mms_TX_NOTIFY", "procNextMessageDelayed NOTIFICATION_DELETED_ACTION. Has msg to show.100");
                procNextMessageDelayed(100);
            } else {
                Conversation.markAllConversationsAsSeen(context);
            }
            if (-1 != nId) {
                MessagingNotification.removeFromNotifiactionList(Integer.valueOf(nId));
                MessagingNotification.saveNotificationIdListToPreference(context);
            }
        } else if ("com.huawei.mms.action.headsup.subuser.viewmsg".equals(action)) {
            if (!OsUtil.isAtLeastL() || OsUtil.isInLoginUser()) {
                MLog.d("Mms_TX_NOTIFY", "HEADS_UP_VIEW_INSUB_ACTION in current");
                intent.setAction(null);
                startActivityInCurrentUser(context, intent);
            } else {
                MLog.d("Mms_TX_NOTIFY", "HEADS_UP_VIEW_INSUB_ACTION not in current");
                return true;
            }
        } else if (!"android.intent.action.USER_SWITCHED".equals(action)) {
            return false;
        } else {
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
            if (userId < 0) {
                MLog.w("Mms_TX_NOTIFY", "Switch to invalid u-" + userId);
            } else if (OsUtil.isSmsDisabledForUser(context, userId)) {
                MLog.w("Mms_TX_NOTIFY", "Switch to restricted u-" + userId);
                MessagingNotification.cancelAllNotification(context);
            } else if (OsUtil.isOwner()) {
                MLog.w("Mms_TX_NOTIFY", "Switch to  u-" + userId);
                MessagingNotification.blockingUpdateAllNotifications(context, -2);
            }
        }
        return true;
    }

    private static void handleNotificationUpdate(final Context context, Intent intent) {
        final int id_msg = intent.getIntExtra("_id", 0);
        ThreadEx.execute(new Runnable() {
            public void run() {
                if (id_msg != 0) {
                    SqliteWrapper.update(context, Uri.parse("content://sms/inbox"), Conversation.getReadContentValues(), "_id = " + id_msg, null);
                }
                MessagingNotification.blockingUpdateAllNotifications(context, -1);
                MLog.d("Mms_TX_NOTIFY", "start to update MMS Notifications");
            }
        });
    }

    private void handleHeadsupClickAction(Context context, Intent intent) {
        boolean z = false;
        int type = intent.getIntExtra("HandleType", -1);
        int nId = intent.getIntExtra("mms_notification_id", -1);
        MLog.d("Mms_TX_NOTIFY", "Headsup Click Action callback : " + type + "; " + nId);
        HwBackgroundLoader.getBackgroundHandler().removeCallbacks(this.mNotificationTimeoutChecker);
        switch (type) {
            case 0:
                StatisticalHelper.incrementReportCount(context, 2039);
                markMessageRead(context, intent);
                MessagingNotification.nonBlockingUpdateNewMessageIndicator(context, -2, false);
                break;
            case 1:
                viewMessage(context, intent);
                return;
            case 2:
                markMessageRead(context, intent);
                cancelNotification(context, nId);
                MessagingNotification.updateSummaryNotification(context);
                boolean needSaveDraft = intent.getBooleanExtra("hw_save_mms", false);
                if (mHwCustReceiver != null) {
                    boolean z2;
                    RcsNotificationReceiver rcsNotificationReceiver = mHwCustReceiver;
                    if (needSaveDraft) {
                        z2 = false;
                    } else {
                        z2 = true;
                    }
                    if (rcsNotificationReceiver.sendOrSaveMessage(context, intent, z2)) {
                        MLog.i("Mms_TX_NOTIFY", "for rcs sendOrSaveMessage");
                        break;
                    }
                }
                StatisticalHelper.incrementReportCount(context, AMapException.CODE_AMAP_CLIENT_UPLOADAUTO_STARTED_ERROR);
                if (!needSaveDraft) {
                    z = true;
                }
                sendOrSaveMessage(context, intent, z);
                break;
            case 3:
            case 4:
                markMessageRead(context, intent);
                if (mHwCustReceiver != null && mHwCustReceiver.sendOrSaveMessage(context, intent, false)) {
                    MLog.i("Mms_TX_NOTIFY", "for rcs sendOrSaveMessage");
                    break;
                } else {
                    sendOrSaveMessage(context, intent, false);
                    break;
                }
                break;
            default:
                MLog.e("Mms_TX_NOTIFY", "Headsup Action not set, do view.");
                break;
        }
        cancelNotification(context, nId);
        if (nId == 1390) {
            MLog.e("Mms_TX_NOTIFY", "procNextMessageDelayed HeadsupClickAction ,nId = " + nId);
            procNextMessageDelayed(100);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void showFirstNotification(Context context) {
        Bundle bundle = mFirstBundle;
        if (bundle != null) {
            PendingIntent clickPendingIntent;
            long notificationId;
            NotificationManagerCompat nm;
            Notification notification;
            PendingIntent pendingIntent = PendingIntent.getActivityAsUser(context, 0, (Intent) bundle.getParcelable("clickIntent"), 134217728, null, UserHandle.CURRENT);
            long mMsgId = bundle.getLong("msgId", -1);
            PendingIntent deleteIntent = getDeleteIntent(context, (int) mMsgId);
            String description = bundle.getString("description");
            int mSubId = bundle.getInt("sub_id", 0);
            long mThreadId = bundle.getLong("threadId", -1);
            Builder builder = new Builder(context);
            long mTimeMillis = bundle.getLong("timeMillis", -1);
            String mMessage = bundle.getString("message");
            String senderName = bundle.getString("senderName");
            String senderNumber = bundle.getString("senderNumber");
            Conversation conv = Conversation.get(context, mThreadId, true);
            Uri msgUri = Uri.parse(bundle.getString("msg_uri"));
            Bundle notificationBundle = new Bundle();
            if (!bundle.getBoolean("isIMChat", false)) {
                if (!bundle.getBoolean("isGroupChat", false)) {
                    clickPendingIntent = getClickIntent(context, msgUri, mThreadId, mSubId, false, (int) mMsgId, null, senderName);
                    builder.setWhen(mTimeMillis).setSmallIcon(R.drawable.stat_notify_sms).setLargeIcon(AvatarCache.drawableToBitmap(MessagingNotification.getDefaultAvatar(context, conv.getRecipients().size() <= 0 ? (Contact) conv.getRecipients().get(0) : null, conv))).setContentIntent(pendingIntent).setContentTitle(senderName).setDeleteIntent(deleteIntent).setAutoCancel(true).setPriority(6).addAction(new Action.Builder(R.drawable.mms_ic_add_contact_dark, context.getString(R.string.quick_reply), clickPendingIntent).addRemoteInput(new RemoteInput.Builder("Quick_Reply").setLabel(context.getString(R.string.type_to_compose_text_enter_to_send)).build()).build()).setGroup("group_key_mms").setExtras(notificationBundle);
                    MessagingNotification.setNotificationIcon(context, conv.getRecipients().size() <= 0 ? (Contact) conv.getRecipients().get(0) : null, conv, builder);
                    if (CryptoMessageUtil.isMsgEncrypted(mMessage)) {
                        builder.setContentText(description);
                    } else {
                        builder.setContentText(context.getResources().getQuantityString(R.plurals.encrypted_sms_notification_counts, 1, new Object[]{Integer.valueOf(1)}));
                    }
                    notificationId = mMsgId;
                    if (bundle.getBoolean("isGroupChat", false)) {
                        notificationId = bundle.getLong("groupNid", mMsgId);
                        builder.setContentText(bundle.getString("groupContent"));
                    }
                    nm = NotificationManagerCompat.from(context);
                    SmartNewNotificationManager.bindDropSmartNotifyView(context, builder, SmartNewNotificationManager.getSmartSmsResult(String.valueOf(mMsgId), senderName, mMessage, false, null), String.valueOf(mMsgId), senderName, mMessage, null);
                    MessagingNotification.enableHeadsUp(builder, false);
                    notification = builder.build();
                    nm.notify((int) notificationId, notification);
                    MessagingNotification.addToNotiticationList(Integer.valueOf((int) mMsgId));
                    if (notification.largeIcon != null) {
                        notification.largeIcon.recycle();
                    }
                }
            }
            if (bundle.getBoolean("isIMChat", false)) {
                clickPendingIntent = RcsNotificationReceiver.getClickIntent(context, msgUri, mThreadId, mSubId, "isChat", senderNumber, false, (int) mMsgId, null, false);
            } else {
                clickPendingIntent = RcsNotificationReceiver.getClickIntent(context, msgUri, mThreadId, mSubId, "isGroup", RcsMessagingNotification.getGroupId(context, mThreadId)[0], false, (int) mMsgId, null, false);
            }
            notificationBundle.putInt("sub_id", mSubId);
            notificationBundle.putBoolean("hw_rcs", true);
            if (conv.getRecipients().size() <= 0) {
            }
            builder.setWhen(mTimeMillis).setSmallIcon(R.drawable.stat_notify_sms).setLargeIcon(AvatarCache.drawableToBitmap(MessagingNotification.getDefaultAvatar(context, conv.getRecipients().size() <= 0 ? (Contact) conv.getRecipients().get(0) : null, conv))).setContentIntent(pendingIntent).setContentTitle(senderName).setDeleteIntent(deleteIntent).setAutoCancel(true).setPriority(6).addAction(new Action.Builder(R.drawable.mms_ic_add_contact_dark, context.getString(R.string.quick_reply), clickPendingIntent).addRemoteInput(new RemoteInput.Builder("Quick_Reply").setLabel(context.getString(R.string.type_to_compose_text_enter_to_send)).build()).build()).setGroup("group_key_mms").setExtras(notificationBundle);
            if (conv.getRecipients().size() <= 0) {
            }
            MessagingNotification.setNotificationIcon(context, conv.getRecipients().size() <= 0 ? (Contact) conv.getRecipients().get(0) : null, conv, builder);
            if (CryptoMessageUtil.isMsgEncrypted(mMessage)) {
                builder.setContentText(description);
            } else {
                builder.setContentText(context.getResources().getQuantityString(R.plurals.encrypted_sms_notification_counts, 1, new Object[]{Integer.valueOf(1)}));
            }
            notificationId = mMsgId;
            if (bundle.getBoolean("isGroupChat", false)) {
                notificationId = bundle.getLong("groupNid", mMsgId);
                builder.setContentText(bundle.getString("groupContent"));
            }
            nm = NotificationManagerCompat.from(context);
            SmartNewNotificationManager.bindDropSmartNotifyView(context, builder, SmartNewNotificationManager.getSmartSmsResult(String.valueOf(mMsgId), senderName, mMessage, false, null), String.valueOf(mMsgId), senderName, mMessage, null);
            MessagingNotification.enableHeadsUp(builder, false);
            notification = builder.build();
            nm.notify((int) notificationId, notification);
            MessagingNotification.addToNotiticationList(Integer.valueOf((int) mMsgId));
            if (notification.largeIcon != null) {
                notification.largeIcon.recycle();
            }
        }
    }

    private void markHeadsupDismissed() {
        synchronized (this.mCachedReceivedMsgs) {
            if (this.mCurrentItem != null) {
                this.mCurrentItem.mRSTime = 0;
                this.mCurrentItem = null;
            }
        }
    }

    private void cancelNotification(Context context, int nId) {
        if (nId == -1) {
            MLog.w("Mms_TX_NOTIFY", "ID not assigned from notification callback");
            nId = 1390;
        }
        NotificationManager nm = NotificationManager.from(context);
        nm.cancel(nId);
        if (nId != 1390) {
            MLog.w("Mms_TX_NOTIFY", "Headsup Notification not canceld" + nId);
            nm.cancel(1390);
        } else {
            MLog.d("Mms_TX_NOTIFY", "cancel Headsup Notification");
        }
        markHeadsupDismissed();
    }

    private static synchronized void clearFirstBundle() {
        synchronized (NotificationReceiver.class) {
            if (mFirstBundle != null) {
                mFirstBundle = null;
            }
        }
    }

    private void procNextMessage(Context context) {
        cancelNotification(context, 1390);
        int size = getCacheMessageSize();
        if (size == 0) {
            MessagingNotification.nonBlockingUpdateNewMessageIndicator(context, -2, false);
            clearFirstBundle();
        } else if (getScreenState(context) != 3) {
            MLog.e("Mms_TX_NOTIFY", "procNextMessage for lock " + size);
            showFirstNotification(context);
            clearFirstBundle();
            MessagingNotification.nonBlockingUpdateNewMessageIndicator(context, -2, true);
            getCacheMessage();
            if (size > 1) {
                procNextMessageDelayed(500);
            }
        } else {
            if (size <= 5) {
                showFirstNotification(context);
                clearFirstBundle();
                synchronized (this.mCachedReceivedMsgs) {
                    this.mCurrentItem = getCacheMessage();
                    MLog.e("Mms_TX_NOTIFY", "procNextMessage single " + this.mCurrentItem);
                    if (this.mCurrentItem != null) {
                        MessagingNotification.nonBlockingUpdateNewMessageIndicatorUri(context, this.mCurrentItem.mTId, false, false, this.mCurrentItem.mMsgUri);
                        this.mCurrentItem.sendMe(context);
                        return;
                    }
                }
            }
            if (size > 0) {
                showFirstNotification(context);
                clearFirstBundle();
                synchronized (this.mCachedReceivedMsgs) {
                    Context contextUri = context;
                    while (getCacheMessageSize() > 0) {
                        this.mCurrentItem = getCacheMessage();
                        if (this.mCurrentItem != null) {
                            MessagingNotification.nonBlockingUpdateNewMessageIndicatorUri(context, this.mCurrentItem.mTId, false, false, this.mCurrentItem.mMsgUri);
                        }
                    }
                }
                MessagingNotification.nonBlockingUpdateNewMessageIndicatorUri(context, -2, true, false, null);
                clearCacheMessage();
            }
        }
    }

    private void markMessageRead(Context context, Intent intent) {
        synchronized (this.mCachedReceivedMsgs) {
            if (this.mCurrentItem == null || this.mCurrentItem.mTId != intent.getLongExtra("thread_id", 0)) {
                MLog.e("Mms_TX_NOTIFY", "MessageViewed no Current Item set or tid mismatch" + this.mCurrentItem);
            }
            if (this.mCurrentItem != null) {
                CachedMsg cachedMsg = this.mCurrentItem;
                cachedMsg.mRSTime = cachedMsg.mRSTime + 292000;
            }
        }
        Uri msgUri = (Uri) intent.getParcelableExtra("msg_uri");
        if (msgUri == null) {
            MLog.e("Mms_TX_NOTIFY", "markMessageRead no uri");
        } else {
            SqliteWrapper.update(context, msgUri, Conversation.getReadContentValues(), null, null);
        }
    }

    private void viewMessage(Context context, Intent intent) {
        int nId = intent.getIntExtra("mms_notification_id", -1);
        HwBackgroundLoader.getBackgroundHandler().removeCallbacks(this.mNotificationTimeoutChecker);
        cancelNotification(context, nId);
        Object stringExtra = intent.hasExtra("EditText") ? intent.getStringExtra("EditText") : null;
        long tId = intent.getLongExtra("thread_id", 0);
        if (mHwCustReceiver == null || !mHwCustReceiver.viewMessage(context, intent)) {
            if (tId != 0) {
                Intent composeIntent = ComposeMessageActivity.createIntent(context, tId);
                composeIntent.putExtra("fromNotification", true);
                putHwContactExtra(composeIntent, intent);
                composeIntent.setFlags(872415232);
                if (!TextUtils.isEmpty(stringExtra)) {
                    MLog.e("Mms_TX_NOTIFY", "view Message with input");
                    composeIntent.putExtra("sms_body", stringExtra);
                }
                startActivityInCurrentUser(context, composeIntent);
            } else {
                MLog.e("Mms_TX_NOTIFY", "Headsup Click callback unassigned TID");
            }
        }
    }

    private void startActivityInCurrentUser(Context context, Intent activityIntent) {
        if (OsUtil.isInLoginUser()) {
            context.startActivity(activityIntent);
            return;
        }
        activityIntent.setAction("com.huawei.mms.action.headsup.subuser.viewmsg");
        activityIntent.setComponent(null);
        context.sendBroadcastAsUser(activityIntent, UserHandle.ALL);
    }

    private boolean isVaildSubscription(int subscription) {
        if (MessageUtils.isMultiSimEnabled()) {
            if (1 != MessageUtils.getIccCardStatus(subscription)) {
                return false;
            }
        } else if (1 != MessageUtils.getIccCardStatus()) {
            return false;
        }
        return true;
    }

    private void sendOrSaveMessage(Context context, Intent intent, boolean send) {
        Bundle results = android.app.RemoteInput.getResultsFromIntent(intent);
        if (results != null) {
            String msg = results.getString("Quick_Reply");
            if (TextUtils.isEmpty(msg)) {
                MLog.e("Mms_TX_NOTIFY", "Can't send or save message as nothing input");
                Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
                if (remoteInput != null) {
                    CharSequence body = remoteInput.getCharSequence("Quick_Reply");
                    Object charSequence = body == null ? null : body.toString();
                    long[] threadId = intent.getLongArrayExtra("threadIds");
                    int[] subscription = intent.getIntArrayExtra("subscriptions");
                    if (!(threadId == null || threadId[0] <= 0 || subscription == null || !isVaildSubscription(subscription[0]) || TextUtils.isEmpty(charSequence))) {
                        HwMessageUtils.sendSms(context, threadId[0], charSequence, subscription[0]);
                    }
                }
                return;
            }
            handleSendOrSaveContent(context, msg, intent, send);
        }
    }

    private void handleSendOrSaveContent(Context context, String msg, Intent intent, boolean send) {
        Uri threadUri = (Uri) intent.getParcelableExtra("msg_uri");
        long tid = intent.getLongExtra("thread_id", 0);
        synchronized (this.mCachedReceivedMsgs) {
            if (this.mCurrentItem == null) {
                MLog.e("Mms_TX_NOTIFY", "mCurrentItem is cleared as timeout");
            } else if (tid == 0) {
                MLog.e("Mms_TX_NOTIFY", "Intent not contanis TID");
                tid = this.mCurrentItem.mTId;
            } else if (this.mCurrentItem.mTId != tid) {
                MLog.e("Mms_TX_NOTIFY", "Save or send with mismatched thread-uri");
            }
        }
        if (tid == 0) {
            MLog.e("Mms_TX_NOTIFY", "Save or send fail as unkonw thread");
        } else if (send) {
            int subId = intent.getIntExtra("sub_id", 0);
            MLog.d("Mms_TX_NOTIFY", "todo removed send message " + tid + " " + subId + "  " + msg.length());
            HwMessageUtils.sendSms(context, tid, msg, subId);
        } else {
            MLog.d("Mms_TX_NOTIFY", "todo removed save message " + threadUri);
            HwMessageUtils.saveDraft(context, tid, msg, 0);
        }
    }

    public void cacheMessage(long newMsgThreadId, Uri msgUri) {
        synchronized (this.mCachedReceivedMsgs) {
            this.mCachedReceivedMsgs.add(new CachedMsg(newMsgThreadId, msgUri));
        }
    }

    public void markCurrentNotifyMessage(long newMsgThreadId, Uri msgUri, int subId) {
        synchronized (this.mCachedReceivedMsgs) {
            if (this.mCurrentItem != null) {
                MLog.e("Mms_TX_NOTIFY", "Current has Nofication in show");
            }
            this.mCurrentItem = new CachedMsg(newMsgThreadId, msgUri, subId);
        }
        procNextMessageDelayed(8000);
    }

    private void procNextMessageDelayed(long delay) {
        HwBackgroundLoader.getBackgroundHandler().removeCallbacks(this.mNotificationTimeoutChecker);
        HwBackgroundLoader.getInst().postTaskDelayed(this.mNotificationTimeoutChecker, delay);
    }

    public int getCacheMessageSize() {
        int size;
        synchronized (this.mCachedReceivedMsgs) {
            size = this.mCachedReceivedMsgs.size();
        }
        return size;
    }

    public CachedMsg getCacheMessage() {
        synchronized (this.mCachedReceivedMsgs) {
            while (this.mCachedReceivedMsgs.size() > 0) {
                CachedMsg msg = (CachedMsg) this.mCachedReceivedMsgs.remove(0);
                if (msg != null && MessagingNotification.isUnreadMsg(msg.mTId)) {
                    return msg;
                }
            }
            return null;
        }
    }

    public void clearCacheMessage() {
        synchronized (this.mCachedReceivedMsgs) {
            this.mCachedReceivedMsgs.clear();
        }
    }

    public boolean canSendNotification(Context context, Uri uri) {
        if (getScreenState(context) != 3) {
            return true;
        }
        synchronized (this.mCachedReceivedMsgs) {
            if (uri != null) {
                if (this.mCurrentItem != null && uri.equals(this.mCurrentItem.mMsgUri)) {
                    return true;
                }
            }
            boolean hasCachedMsg = this.mCachedReceivedMsgs.size() > 0;
            long waitTime = this.mCurrentItem == null ? 0 : (this.mCurrentItem.mRSTime + 8000) - System.currentTimeMillis();
            MLog.d("Mms_TX_NOTIFY", "canSendNotification hasCachedMsg=" + hasCachedMsg + "; waitTime=" + waitTime + "; mCurrentItem " + this.mCurrentItem);
            if (waitTime > 0) {
                MLog.e("Mms_TX_NOTIFY", "procNextMessageDelayed waitTime exceed");
                procNextMessageDelayed(waitTime);
                return false;
            } else if (hasCachedMsg) {
                MLog.e("Mms_TX_NOTIFY", "procNextMessageDelayed hasCachedMsg");
                procNextMessageDelayed(100);
                return false;
            } else {
                return true;
            }
        }
    }

    public static PendingIntent getClickIntent(Context context, Uri msgUri, long tid, int subId, boolean isFirstMessage, int notificationId, NotificationInfo notificationInfo, String number) {
        Intent clickIntent = new Intent("com.huawei.mms.action.headsup.clicked");
        clickIntent.putExtra("HandleType", 2);
        clickIntent.putExtra("msg_uri", msgUri);
        clickIntent.putExtra("thread_id", tid);
        clickIntent.putExtra("mms_notification_id", notificationId);
        clickIntent.putExtra("sub_id", subId);
        clickIntent.putExtra("firstMessage", isFirstMessage);
        int[] subscriptions = new int[]{subId};
        long[] threadIds = new long[]{tid};
        clickIntent.putExtra(HarassNumberUtil.NUMBER, number);
        clickIntent.putExtra("subscriptions", subscriptions);
        clickIntent.putExtra("threadIds", threadIds);
        if (isFirstMessage) {
            initFirstBundle(context, msgUri, subId, isFirstMessage, notificationId, notificationInfo);
        }
        clickIntent.setPackage("com.android.mms");
        return PendingIntent.getBroadcastAsUser(context, notificationId, clickIntent, 134217728, UserHandle.OWNER);
    }

    public static synchronized void initFirstBundle(Context context, Uri msgUri, int subId, boolean isFirstMessage, int notificationId, NotificationInfo notificationInfo) {
        synchronized (NotificationReceiver.class) {
            if (mFirstBundle == null) {
                mFirstBundle = new Bundle();
            }
            mFirstBundle.putLong("msgId", notificationInfo.mMsgId);
            mFirstBundle.putString("message", notificationInfo.mMessage);
            mFirstBundle.putString("title", notificationInfo.mTitle);
            mFirstBundle.putCharSequence("description", notificationInfo.formatMessage(context).toString());
            mFirstBundle.putString("senderName", notificationInfo.getSenderName(context));
            mFirstBundle.putString("senderNumber", notificationInfo.getSenderNumber());
            mFirstBundle.putLong("threadId", notificationInfo.mThreadId);
            mFirstBundle.putLong("timeMillis", notificationInfo.mTimeMillis);
            mFirstBundle.putParcelable("clickIntent", notificationInfo.mClickIntent);
            mFirstBundle.putString("msg_uri", msgUri.toString());
            mFirstBundle.putInt("mms_notification_id", notificationId);
            mFirstBundle.putInt("sub_id", subId);
            mFirstBundle.putBoolean("firstMessage", isFirstMessage);
            if (notificationInfo.mHwCustInnerInfo != null) {
                if (notificationInfo.mHwCustInnerInfo.isGroupChat()) {
                    StringBuilder notificationDescription = new StringBuilder();
                    notificationDescription.append("(").append(context.getResources().getString(R.string.rcs_group_new_message_count, new Object[]{Integer.valueOf(1)})).append(") ");
                    notificationDescription.append(RcsUtility.getGroupContactShowName(notificationInfo.getSenderNumber(), notificationInfo.mThreadId));
                    notificationDescription.append(":");
                    notificationDescription.append(notificationInfo.mMessage);
                    String description = notificationDescription.toString();
                    long nId = notificationInfo.getNotificationId();
                    mFirstBundle.putBoolean("isGroupChat", true);
                    mFirstBundle.putLong("groupNid", nId);
                    mFirstBundle.putString("groupContent", description);
                } else if (notificationInfo.mHwCustInnerInfo.isRcsChat()) {
                    mFirstBundle.putBoolean("isIMChat", true);
                }
            }
        }
    }

    public static PendingIntent getClickIntent(Context context, Uri msgUri, long tid, int subId) {
        Intent clickIntent = new Intent("com.huawei.mms.action.headsup.clicked");
        clickIntent.putExtra("msg_uri", msgUri);
        clickIntent.putExtra("thread_id", tid);
        clickIntent.putExtra("mms_notification_id", 1390);
        clickIntent.putExtra("sub_id", subId);
        clickIntent.setPackage("com.android.mms");
        return PendingIntent.getBroadcastAsUser(context, (int) ContentUris.parseId(msgUri), clickIntent, 134217728, UserHandle.OWNER);
    }

    public static PendingIntent getContentIntent(Context context, Uri msgUri, long tid) {
        return getContentIntent(context, msgUri, tid, null);
    }

    public static PendingIntent getContentIntent(Context context, Uri msgUri, long tid, Intent hwContactSource) {
        Intent viewIntent = new Intent("com.huawei.mms.action.headsup.viewmsg");
        viewIntent.putExtra("msg_uri", msgUri);
        viewIntent.putExtra("thread_id", tid);
        viewIntent.putExtra("mms_notification_id", 1390);
        putHwContactExtra(viewIntent, hwContactSource);
        viewIntent.setPackage("com.android.mms");
        return PendingIntent.getBroadcastAsUser(context, SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE, viewIntent, 134217728, UserHandle.OWNER);
    }

    private static void putHwContactExtra(Intent intent, Intent hwContactSource) {
        if (intent != null && hwContactSource != null) {
            if (hwContactSource.hasExtra("sender_for_huawei_message")) {
                intent.putExtra("sender_for_huawei_message", hwContactSource.getStringExtra("sender_for_huawei_message"));
            }
            if (hwContactSource.hasExtra("contact_id")) {
                intent.putExtra("contact_id", hwContactSource.getLongExtra("contact_id", 0));
            }
            if (hwContactSource.hasExtra("name")) {
                intent.putExtra("name", hwContactSource.getStringExtra("name"));
            }
        }
    }

    public static PendingIntent getDeleteIntent(Context context, int notificationId) {
        Intent delteIntent;
        synchronized (NotificationReceiver.class) {
            if (sNotificationOnDeleteIntent == null) {
                sNotificationOnDeleteIntent = new Intent("com.android.mms.NOTIFICATION_DELETED_ACTION");
                sNotificationOnDeleteIntent.setPackage("com.android.mms");
                sNotificationOnDeleteIntent.setClass(context.getApplicationContext(), NotificationReceiver.class);
            }
            sNotificationOnDeleteIntent.putExtra("mms_notification_id", notificationId);
            delteIntent = sNotificationOnDeleteIntent;
        }
        return PendingIntent.getBroadcastAsUser(context, 0, delteIntent, 0, UserHandle.OWNER);
    }

    public static PendingIntent getDeleteIntent(Context context) {
        return getDeleteIntent(context, -1);
    }

    public boolean isUserPresent(Context context) {
        return getScreenState(context) == 3;
    }

    @TargetApi(16)
    private int getScreenState(Context context) {
        int i = 2;
        if (this.mScreenState == -1) {
            this.mScreenState = ((PowerManager) context.getSystemService("power")).isInteractive() ? 2 : 1;
        }
        if (this.mScreenState == 2) {
            if (!isScreenLocked(context)) {
                i = 3;
            }
            this.mScreenState = i;
        } else {
            MLog.d("Mms_TX_NOTIFY", "ScreenState " + this.mScreenState);
        }
        return this.mScreenState;
    }

    private static boolean isScreenLocked(Context context) {
        if (((KeyguardManager) context.getSystemService("keyguard")).isKeyguardLocked()) {
            MLog.d("Mms_TX_NOTIFY", "ScreenState lock");
            return true;
        }
        MLog.d("Mms_TX_NOTIFY", "ScreenState present");
        return false;
    }

    public void clearNotifications(NotificationManager nm) {
        synchronized (this.mCachedReceivedMsgs) {
            this.mCurrentItem = null;
            this.mCachedReceivedMsgs.clear();
        }
        nm.cancelAsUser(null, 1390, UserHandle.ALL);
    }
}
