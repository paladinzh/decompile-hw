package com.android.util;

import android.content.Context;
import android.drm.DrmManagerClient;
import android.os.SystemProperties;

public class DrmUtils {
    public static final boolean DRM_ENABLED = SystemProperties.getBoolean("ro.huawei.cust.oma_drm", false);
    private static DrmManagerClient sDrmManagerClient;

    public static synchronized void initialize(Context context) {
        synchronized (DrmUtils.class) {
            if (sDrmManagerClient == null) {
                sDrmManagerClient = new DrmManagerClient(context);
            }
        }
    }

    public static boolean isDrmFile(String path) {
        if (!DRM_ENABLED || path == null) {
            return false;
        }
        return path.regionMatches(true, path.length() - 4, ".dcf", 0, 4);
    }

    public static boolean haveRightsForAction(String path, int action) {
        boolean z = false;
        try {
            if (sDrmManagerClient.canHandle(path, null)) {
                if (sDrmManagerClient.checkRightsStatus(path, action) == 0) {
                    z = true;
                }
                return z;
            }
        } catch (Exception e) {
            Log.i("DrmUtils", "haveRightsForAction : exception = " + e.getMessage());
        }
        return false;
    }
}
