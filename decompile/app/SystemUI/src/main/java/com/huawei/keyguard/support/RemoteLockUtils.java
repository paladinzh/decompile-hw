package com.huawei.keyguard.support;

import android.content.Context;
import android.provider.Settings.Global;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.OsUtils;
import fyusion.vislib.BuildConfig;

public class RemoteLockUtils {
    private static Runnable mUserSwitcher = new Runnable() {
        public void run() {
            OsUtils.switchUser(0);
        }
    };

    public static boolean isDeviceRemoteLocked(Context context) {
        boolean z = true;
        if (context == null) {
            return false;
        }
        int value = Global.getInt(context.getContentResolver(), "device_remote_lock", 0);
        if (value == 1 && !OsUtils.isOwner()) {
            mUserSwitcher.run();
        }
        if (value != 1) {
            z = false;
        }
        return z;
    }

    public static void resetDeviceRemoteLocked(Context context) {
        if (context != null && isDeviceRemoteLocked(context)) {
            HwLog.i("RemoteLockUtils", "resetDeviceRemoteLocked");
            Global.putInt(context.getContentResolver(), "device_remote_lock", 0);
        }
    }

    public static String getDeviceRemoteLockedInfo(Context context) {
        if (context == null) {
            return BuildConfig.FLAVOR;
        }
        String value = Global.getString(context.getContentResolver(), "device_remote_lock_info");
        if (value == null) {
            value = BuildConfig.FLAVOR;
        }
        return value;
    }
}
