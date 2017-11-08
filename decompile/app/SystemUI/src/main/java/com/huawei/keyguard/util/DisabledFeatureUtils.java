package com.huawei.keyguard.util;

import android.app.admin.DevicePolicyManager;
import android.content.Context;

public class DisabledFeatureUtils {
    private static boolean sCameraDisabled = false;

    private static boolean getDisabledFeatures(Context context, int disableType, int userId) {
        boolean z = false;
        if (context == null) {
            HwLog.e("DisabledFeatureUtils", "getDisabledFeatures with null context", new Exception());
            return false;
        }
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
            if (dpm != null) {
                int disabled = dpm.getKeyguardDisabledFeatures(null, userId);
                HwLog.w("DisabledFeatureUtils", "Dis-Feature : " + disabled);
                if ((disabled & disableType) != 0) {
                    z = true;
                }
                return z;
            }
        } catch (SecurityException e) {
            HwLog.e("DisabledFeatureUtils", "getKeyguardDisabledFeatures got SecurityException.", e);
        } catch (NullPointerException e2) {
            HwLog.e("DisabledFeatureUtils", "getKeyguardDisabledFeatures got NullPointerException", e2);
        }
        return false;
    }

    private static boolean getCameraDisabled(Context context, int userId) {
        if (context == null) {
            HwLog.e("DisabledFeatureUtils", "getCameraDisabled with null context", new Exception());
            return false;
        }
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
            if (dpm != null) {
                int disabled = dpm.getKeyguardDisabledFeatures(null, userId);
                HwLog.w("DisabledFeatureUtils", "Dis-Feature : " + disabled);
                return (disabled & 2) == 0 ? dpm.getCameraDisabled(null, userId) : true;
            }
        } catch (SecurityException e) {
            HwLog.e("DisabledFeatureUtils", "getCameraDisabled got SecurityException.", e);
        } catch (Exception e2) {
            HwLog.e("DisabledFeatureUtils", "getCameraDisabled got Exception.", e2);
        }
        return false;
    }

    public static boolean refreshCameraDisabled(Context context) {
        sCameraDisabled = getCameraDisabled(context, OsUtils.getCurrentUser());
        return sCameraDisabled;
    }

    public static boolean getCameraDisabled() {
        return sCameraDisabled;
    }

    public static boolean getFingerprintDisabled(Context context, int userId) {
        return getDisabledFeatures(context, 32, userId);
    }
}
