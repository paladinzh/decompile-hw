package com.huawei.systemmanager.customize;

import android.content.Context;
import android.provider.Settings.Secure;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;

public class CustomizeWrapper {
    private static final String TAG = "CustomizeWrapper";
    private static int lastestStatus = -1;

    @Deprecated
    public static boolean isPermissionEnabled() {
        return CustomizeManager.getInstance().isFeatureEnabled(3);
    }

    public static boolean isPermissionEnabled(Context ctx) {
        return CustomizeManager.getInstance().isFeatureEnabled(ctx, 3);
    }

    public static boolean isBootstartupEnabled() {
        if (isOverseaEnabled()) {
            return true;
        }
        return CustomizeManager.getInstance().isFeatureEnabled(3);
    }

    public static synchronized boolean shouldEnableIntelligentEngine() {
        boolean isFeatureEnabled;
        synchronized (CustomizeWrapper.class) {
            isFeatureEnabled = CustomizeManager.getInstance().isFeatureEnabled(2);
        }
        return isFeatureEnabled;
    }

    public static boolean isNumberLocationEnabled() {
        return CustomizeManager.getInstance().isFeatureEnabled(9);
    }

    private static boolean isOverseaEnabled() {
        Context context = GlobalContext.getContext();
        if (context == null) {
            return false;
        }
        return isOverseaBootstartEnabled(context);
    }

    public static boolean isOverseaBootstartEnabled(Context context) {
        int permissionStatus = Secure.getInt(context.getContentResolver(), OverseaCfgConst.SETTINGS_SECURITY_OVERSEA_STATUS, 0);
        if (lastestStatus != permissionStatus) {
            lastestStatus = permissionStatus;
            HwLog.i(TAG, "permissionStatus is " + permissionStatus);
        }
        if (1 == permissionStatus) {
            return true;
        }
        return false;
    }
}
