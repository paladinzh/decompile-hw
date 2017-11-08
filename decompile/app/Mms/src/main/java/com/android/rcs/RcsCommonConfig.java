package com.android.rcs;

import android.app.ActivityManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.UserManager;
import android.provider.Settings.System;
import com.android.mms.MmsApp;
import com.huawei.rcs.util.RcsFeatureEnabler;

public class RcsCommonConfig {
    private static final boolean IS_CURRENT_USER_OWNER;
    private static final boolean IS_RCS_WHOLE_ON = isRcsWholeOn();
    private static final boolean IS_SUPPORT_MULTI_USERS = UserManager.supportsMultipleUsers();
    private static boolean mIsRcsOn = RcsFeatureEnabler.getInstance().isRcsEnabled();

    static {
        boolean z = true;
        if (IS_SUPPORT_MULTI_USERS && ActivityManager.getCurrentUser() != 0) {
            z = false;
        }
        IS_CURRENT_USER_OWNER = z;
    }

    private RcsCommonConfig() {
    }

    public static boolean isRCSSwitchOn() {
        return (mIsRcsOn || IS_RCS_WHOLE_ON) ? IS_CURRENT_USER_OWNER : false;
    }

    private static boolean checkApkExist(String packageName) {
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        try {
            MmsApp.getApplication().getApplicationContext().getPackageManager().getApplicationInfo(packageName, 1);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private static boolean checkSignatures(String packageName) {
        if (MmsApp.getApplication().getApplicationContext().getPackageManager().checkSignatures("com.android.mms", packageName) == 0) {
            return true;
        }
        return false;
    }

    private static boolean isRcsWholeOn() {
        boolean z = true;
        if (!checkApkExist("com.android.rcssettingon") || !checkSignatures("com.android.rcssettingon") || !checkApkExist("com.huawei.rcsserviceapplication")) {
            return false;
        }
        if (System.getInt(MmsApp.getApplication().getApplicationContext().getContentResolver(), "rcs_whole_on", 0) != 1) {
            z = false;
        }
        return z;
    }
}
