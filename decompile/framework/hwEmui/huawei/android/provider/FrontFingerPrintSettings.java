package huawei.android.provider;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.os.SystemProperties;
import android.provider.Settings.System;
import huawei.android.os.HwGeneralManager;

public class FrontFingerPrintSettings {
    public static final boolean FRONT_FINGERPRINT_NAVIGATION = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    public static final int FRONT_FINGERPRINT_NAVIGATION_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized boolean isNaviBarEnabled(ContentResolver resolver) {
        boolean z = true;
        synchronized (FrontFingerPrintSettings.class) {
            int NAVI_BAR_DEFAULT_STATUS = 1;
            if (FRONT_FINGERPRINT_NAVIGATION) {
                if (FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 0) {
                    if (isChinaArea()) {
                        NAVI_BAR_DEFAULT_STATUS = 0;
                    } else {
                        NAVI_BAR_DEFAULT_STATUS = 1;
                    }
                } else if (FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1) {
                    return false;
                }
                if (System.getIntForUser(resolver, HwSettings.System.NAVIGATION_BAR_ENABLE, NAVI_BAR_DEFAULT_STATUS, ActivityManager.getCurrentUser()) != 1) {
                    z = false;
                }
            } else {
                return true;
            }
        }
    }

    public static int getDefaultNaviMode() {
        if (!isSupportTrikey()) {
            return -1;
        }
        if (FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 0) {
            return -1;
        }
        if (FRONT_FINGERPRINT_NAVIGATION_TRIKEY != 1) {
            return -1;
        }
        if (isChinaArea()) {
            return -1;
        }
        return 0;
    }

    public static boolean isSupportTrikey() {
        return HwGeneralManager.getInstance().isSupportTrikey();
    }

    public static boolean isChinaArea() {
        return SystemProperties.get("ro.config.hw_optb", HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF).equals("156");
    }
}
