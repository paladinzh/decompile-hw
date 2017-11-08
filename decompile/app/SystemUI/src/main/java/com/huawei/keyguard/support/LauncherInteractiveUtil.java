package com.huawei.keyguard.support;

import android.content.ContentProviderClient;
import android.content.Context;
import android.os.Bundle;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.OsUtils;

public class LauncherInteractiveUtil {
    private static boolean USE_COMMON_ANIM_FOR_LAUNCHER = true;
    private static long mBootCompleteTime = 0;
    private static int sHasSendLockEvent = -1;

    private static class Caller implements Runnable {
        private Context mContext;
        private boolean mIsLock;

        public Caller(Context context, boolean lock) {
            this.mContext = context;
            this.mIsLock = lock;
        }

        public void run() {
            ContentProviderClient contentProviderClient = null;
            try {
                contentProviderClient = this.mContext.getContentResolver().acquireUnstableContentProviderClient(OsUtils.getUserUri("com.huawei.android.launcher.settings/lock"));
                if (contentProviderClient == null) {
                    HwLog.e("LauncherInteractiveUtil", "Call launcher unlock fail as provider not found");
                    if (contentProviderClient != null) {
                        contentProviderClient.release();
                    }
                    return;
                }
                Bundle result;
                if (this.mIsLock) {
                    result = contentProviderClient.call("keyguard_locked", "lock", null);
                } else {
                    result = contentProviderClient.call("keyguard_unlocked", "unlock", null);
                }
                HwLog.i("LauncherInteractiveUtil", "Call launcher event succ. lock=" + this.mIsLock + ";  " + OsUtils.getCurrentUser() + "; " + result);
                if (contentProviderClient != null) {
                    contentProviderClient.release();
                }
            } catch (Exception e) {
                HwLog.e("LauncherInteractiveUtil", "Call launcher evnt fail. lock=" + this.mIsLock, e);
                if (contentProviderClient != null) {
                    contentProviderClient.release();
                }
            } catch (Throwable th) {
                if (contentProviderClient != null) {
                    contentProviderClient.release();
                }
            }
        }
    }

    private static final boolean hasSentLockevent(boolean dolock) {
        synchronized (LauncherInteractiveUtil.class) {
            int currentState = sHasSendLockEvent;
            sHasSendLockEvent = dolock ? 1 : 2;
            if (currentState != -1) {
                dolock = 1 == currentState;
            }
        }
        return dolock;
    }

    public static void sendLockedEventByCallProvider(Context context) {
        if (!USE_COMMON_ANIM_FOR_LAUNCHER) {
            if (0 == mBootCompleteTime || 3000 >= System.currentTimeMillis() - mBootCompleteTime) {
                HwLog.e("LauncherInteractiveUtil", "First time use the method after boot complete!");
                return;
            }
            if (hasSentLockevent(true)) {
                HwLog.w("LauncherInteractiveUtil", "Skip send lock event to Launcher");
            } else {
                sendEventToLauncher(context, true);
            }
        }
    }

    public static void sendUnockedEventByCallProvider(Context context) {
        if (!USE_COMMON_ANIM_FOR_LAUNCHER) {
            if (hasSentLockevent(false)) {
                sendEventToLauncher(context, false);
            } else {
                HwLog.w("LauncherInteractiveUtil", "Skip send unlock event to Launcher");
            }
        }
    }

    public static void sendEventToLauncher(Context context, boolean locked) {
        if (context != null) {
            GlobalContext.getBackgroundHandler().post(new Caller(context, locked));
        }
    }

    public static void setBootCompleteTime() {
        mBootCompleteTime = System.currentTimeMillis();
    }
}
