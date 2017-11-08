package com.huawei.permissionmanager.utils;

import android.content.Context;
import android.provider.Settings.Secure;
import com.huawei.systemmanager.util.HwLog;

public class SettingsDbUtils {
    private static final String SYSTEM_FIRST_BOOT = "system_first_boot";
    private static final int SYSTEM_FIRST_BOOT_DEFULT = 1;
    private static final int SYSTEM_NOT_FIRST_BOOT = 0;
    private static final String TAG = "SettingsDbUtils";

    public static boolean isFirstBootForPermissionInit(Context context) {
        boolean isFirstBoot = true;
        if (context == null) {
            HwLog.w(TAG, "isFirstBootForPermissionInit get null context.");
            return false;
        }
        if (Secure.getInt(context.getContentResolver(), SYSTEM_FIRST_BOOT, 1) != 1) {
            isFirstBoot = false;
        }
        return isFirstBoot;
    }

    public static void setNotFirstBootForPermissionInit(Context context) {
        if (context == null) {
            HwLog.w(TAG, "setFirstBootForPermissionInit get null context.");
        } else {
            Secure.putInt(context.getContentResolver(), SYSTEM_FIRST_BOOT, 0);
        }
    }
}
