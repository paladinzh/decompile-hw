package com.huawei.systemmanager.applock.fingerprint;

import android.content.Context;
import com.huawei.systemmanager.util.HwLog;

public class FingerprintAuthUtils {
    private static final String TAG = "FingerprintAuthUtils";

    public static IFingerprintAuth createAuth(Context ctx) {
        try {
            return FingerprintAdapter.create(ctx);
        } catch (Exception ex) {
            HwLog.e(TAG, "createAuth catch exception:" + ex.getMessage());
            ex.printStackTrace();
            HwLog.e(TAG, "createAuth return null!");
            return null;
        }
    }

    public static void closeAuth(IFingerprintAuth fingerAuth) {
        if (fingerAuth != null) {
            fingerAuth.cancelAuthenticate();
        }
    }

    public static boolean checkFingerprintReadyNotClose(IFingerprintAuth authMgr) {
        boolean support = authMgr.isFingerprintReady();
        HwLog.v(TAG, "checkFingerprintReadyNotClose result: " + support);
        return support;
    }
}
