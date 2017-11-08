package com.huawei.systemmanager.securitythreats.background;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import com.huawei.systemmanager.util.HwLog;
import org.json.JSONObject;

class VirusNotifyPref {
    private static final String SP_FILE_NAME = "security_threats_pref";
    private static final String SP_KEY_INSTALL_VIRUS = "install_virus";
    private static final String TAG = "VirusNotifyPref";

    VirusNotifyPref() {
    }

    public static boolean setInstallVirus(Context context, JSONObject pkgs) {
        return setPackages(context, pkgs, SP_KEY_INSTALL_VIRUS);
    }

    public static JSONObject getInstallVirus(Context context) {
        return getPackages(context, SP_KEY_INSTALL_VIRUS);
    }

    private static boolean setPackages(Context context, JSONObject pkgs, String key) {
        if (context == null) {
            HwLog.e(TAG, "setPackages, context is null");
            return false;
        } else if (pkgs == null) {
            HwLog.e(TAG, "setPackages, pkgs is null");
            return false;
        } else {
            try {
                Editor editor = context.getSharedPreferences(SP_FILE_NAME, 0).edit();
                editor.putString(key, pkgs.toString());
                return editor.commit();
            } catch (Exception e) {
                HwLog.e(TAG, "setPackages, Exception:", e);
                return false;
            }
        }
    }

    private static JSONObject getPackages(Context context, String key) {
        if (context == null) {
            HwLog.e(TAG, "getPackages, context is null");
            return new JSONObject();
        }
        try {
            String pkgs = context.getSharedPreferences(SP_FILE_NAME, 0).getString(key, "");
            if (TextUtils.isEmpty(pkgs)) {
                return new JSONObject();
            }
            return new JSONObject(pkgs);
        } catch (Exception e) {
            HwLog.w(TAG, "getPackages, Exception:", e);
            return new JSONObject();
        }
    }
}
