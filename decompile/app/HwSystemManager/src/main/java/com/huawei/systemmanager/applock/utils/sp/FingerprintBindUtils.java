package com.huawei.systemmanager.applock.utils.sp;

import android.content.Context;
import com.huawei.systemmanager.applock.utils.DatabaseSharePrefUtil;

public class FingerprintBindUtils {
    public static final String FINGERPRINT_BIND_STATUS_KEY = "fingerprint_bind_status";

    public static boolean getFingerprintBindStatus(Context context) {
        return DatabaseSharePrefUtil.getPref(context, FINGERPRINT_BIND_STATUS_KEY, false, true);
    }

    public static void setFingerprintBindStatus(Context context, boolean value) {
        DatabaseSharePrefUtil.setPref(context, FINGERPRINT_BIND_STATUS_KEY, value, true);
    }
}
