package com.android.mms.transaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemProperties;
import com.android.messaging.util.OsUtil;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.ui.BaseConversationListFragment;
import com.android.mms.util.DraftCache;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.transaction.RcsMmsSystemEventReceiver;
import com.google.android.gms.Manifest.permission;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.mms.util.SmartArchiveSettingUtils;

public class MmsSystemEventReceiver extends BroadcastReceiver {
    private static Runnable draftRefesher = new Runnable() {
        public void run() {
            DraftCache.getInstance().refresh();
        }
    };
    private static MmsSystemEventReceiver sMmsSystemEventReceiver;
    private ConnectivityManager mConnMgr = null;
    private RcsMmsSystemEventReceiver mCust = null;
    Handler mRefreshHandler = new Handler();

    private static class AutoDeleteRunnalbe implements Runnable {
        private AutoDeleteRunnalbe() {
        }

        public void run() {
            SmartArchiveSettingUtils.autoDeleteNotiMessages(MmsApp.getApplication());
        }
    }

    private void freshDraft() {
        this.mRefreshHandler.removeCallbacks(draftRefesher);
        this.mRefreshHandler.postDelayed(draftRefesher, 600);
    }

    public static synchronized MmsSystemEventReceiver getInstance() {
        MmsSystemEventReceiver mmsSystemEventReceiver;
        synchronized (MmsSystemEventReceiver.class) {
            if (sMmsSystemEventReceiver == null) {
                sMmsSystemEventReceiver = new MmsSystemEventReceiver();
            }
            mmsSystemEventReceiver = sMmsSystemEventReceiver;
        }
        return mmsSystemEventReceiver;
    }

    public static void wakeUpService(Context context) {
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.v("Mms_TX_SysEvent", "wakeUpService: start transaction service ...");
        }
        TransactionService.startMe(context);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onReceive(Context context, Intent intent) {
        if (OsUtil.isAtLeastL() && OsUtil.isSecondaryUser()) {
            MLog.w("Mms_TX_SysEvent", "MmsSystemEventReceiver in SecondaryUser, Skipped.");
            return;
        }
        String action = intent.getAction();
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.v("Mms_TX_SysEvent", "Intent received: action: " + action);
        }
        if ("android.intent.action.CONTENT_CHANGED".equals(action)) {
            Uri changed = (Uri) intent.getParcelableExtra("deleted_contents");
            if (changed != null) {
                MmsApp.getApplication().getPduLoaderManager().removePdu(changed);
            }
        } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
            if (this.mConnMgr == null) {
                this.mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
            }
            try {
                if (!this.mConnMgr.getMobileDataEnabled()) {
                    if (MLog.isLoggable("Mms_TXN", 2)) {
                        MLog.v("Mms_TX_SysEvent", "mobile data turned off, bailing return");
                    }
                    return;
                }
            } catch (Exception e) {
                MLog.e("Mms_TX_SysEvent", "exception occurs : ", (Throwable) e);
            }
            NetworkInfo mmsNetworkInfo = this.mConnMgr.getNetworkInfo(2);
            if (mmsNetworkInfo != null) {
                boolean available = mmsNetworkInfo.isAvailable();
                boolean isConnected = mmsNetworkInfo.isConnected();
                if (MLog.isLoggable("Mms_TXN", 2)) {
                    MLog.v("Mms_TX_SysEvent", "TYPE_MOBILE_MMS available = " + available + ", isConnected = " + isConnected);
                }
                if (available && !isConnected) {
                    wakeUpService(context);
                }
            }
        } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
            if (cm != null) {
                try {
                } catch (RuntimeException e2) {
                    MLog.e("Mms_TX_SysEvent", "RomoteException rethrow DeadSystemException, because ConnectivityManager disconnected");
                    if ("true".equals(SystemProperties.get("ro.config.hw_allow_rs_mms", "false"))) {
                        setRetryAlarm(context, 120000);
                    }
                }
            }
            if ("true".equals(SystemProperties.get("ro.config.hw_allow_rs_mms", "false"))) {
                setRetryAlarm(context, 120000);
            }
            if (MmsConfig.noticeNewMessageWhenBootup()) {
                MessagingNotification.nonBlockingUpdateNewMessageIndicator(context, -2, false);
            }
            wakeUpService(context);
            if (SmartArchiveSettingUtils.isSmartArchiveEnabled(context) && SmartArchiveSettingUtils.isSmartArchiveAutoDeleteEnable(context)) {
                SmartArchiveSettingUtils.enableAutoDelete();
            }
            if (RcsCommonConfig.isRCSSwitchOn() && this.mCust == null) {
                this.mCust = new RcsMmsSystemEventReceiver();
            }
            if (this.mCust != null) {
                this.mCust.fileStatusUpdateService(context);
            }
        } else if ("com.android.huawei.notification.DRAFT_CHANGE".equals(action)) {
            Long threadId = Long.valueOf(intent.getLongExtra("thread_id", 0));
            if (threadId.longValue() > 0) {
                DraftCache.getInstance().setDraftState(threadId.longValue(), true);
            } else {
                freshDraft();
            }
        } else if ("com.huawei.KoBackup.intent.action.RESTORE_COMPLETE".equals(action)) {
            freshDraft();
        } else if ("com.huawei.mms.AUTO_DELETE_ALARM".equals(action)) {
            MLog.d("Mms_TX_SysEvent", "auto delete notification messages alarm");
            ThreadEx.execute(new AutoDeleteRunnalbe());
        } else if ("android.intent.action.USER_SWITCHED".equals(action)) {
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
            if (userId < 0) {
                MLog.w("Mms_TX_SysEvent", "Switch to invalid u-" + userId);
            } else if (OsUtil.isSmsDisabledForUser(context, userId)) {
                MLog.w("Mms_TX_SysEvent", "Switch to restricted u-" + userId);
                MessagingNotification.cancelAllNotification(context);
            } else {
                MLog.w("Mms_TX_SysEvent", "Switch to  u-" + userId);
                MessagingNotification.blockingUpdateAllNotifications(context, -2);
            }
        } else if ("com.android.huawei.notification.RECEIVE_SERVICE".equals(action)) {
            long[] threads = new long[]{intent.getLongExtra("threadid", -1)};
            if (!intent.getBooleanExtra("hasUnread", false) && MmsConfig.getUnreadPinupEnable(context)) {
                BaseConversationListFragment.pinUpThreads(threads, true, true, context);
            }
        }
    }

    public static void registerForDraftChanges(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.huawei.notification.DRAFT_CHANGE");
        intentFilter.addAction("com.huawei.KoBackup.intent.action.RESTORE_COMPLETE");
        intentFilter.addAction("com.android.huawei.notification.RECEIVE_SERVICE");
        context.registerReceiver(getInstance(), intentFilter, permission.REFRESH_DRAFT, null);
    }

    public static void registerForConnectivityChanges(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        context.registerReceiver(getInstance(), intentFilter);
    }

    private static void setRetryAlarm(Context context, int delay) {
        TransactionService.retryStart(context, System.currentTimeMillis() + ((long) delay));
    }
}
