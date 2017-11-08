package com.huawei.systemmanager.secpatch.util;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import com.huawei.systemmanager.secpatch.common.ConstValues;
import com.huawei.systemmanager.util.HwLog;

public class SecPatchHelper {
    public static final String TAG = "SecPatchHelper";

    public static boolean isNetworkAvaialble(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        NetworkInfo network = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (network != null) {
            z = network.isConnected();
        }
        return z;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isVersionNameRuleChange(String lastName, String currentName) {
        boolean z = false;
        if (TextUtils.isEmpty(lastName) || TextUtils.isEmpty(currentName) || !lastName.contains(ConstValues.B_VERSION_CHAR) || !currentName.contains(ConstValues.B_VERSION_CHAR)) {
            return true;
        }
        if (currentName.equals(lastName)) {
            return false;
        }
        if (!currentName.startsWith(lastName.substring(0, lastName.indexOf(ConstValues.B_VERSION_CHAR)))) {
            z = true;
        }
        return z;
    }

    public static long getLocalVersionByKeyName(Context context, String keyName) {
        long versionValue = 0;
        if (context == null) {
            HwLog.w("SecPatchHelper", "getLastSystemVersion: Invalid context");
            return versionValue;
        }
        try {
            versionValue = context.getSharedPreferences(ConstValues.SPF_SECURITY_FILE_NAME, 0).getLong(keyName, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionValue;
    }

    public static void setLocalVersionWhenUpdate(Context context, String keyName, long version) {
        if (context == null) {
            HwLog.w("SecPatchHelper", "setNewSystemVersionWhenUpdate: Invalid context");
            return;
        }
        try {
            Editor editor = context.getSharedPreferences(ConstValues.SPF_SECURITY_FILE_NAME, 0).edit();
            editor.putLong(keyName, version);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getLastRomVersion(Context context) {
        String versionName = "";
        if (context == null) {
            HwLog.w("SecPatchHelper", "getLastRomVersion: Invalid context");
            return versionName;
        }
        try {
            versionName = context.getSharedPreferences(ConstValues.SPF_SECURITY_FILE_NAME, 0).getString(ConstValues.SPF_LAST_VERSION_NAME_KEY, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

    private static void setNewRomVersion(Context context, String versionName) {
        if (context == null) {
            HwLog.w("SecPatchHelper", "setNewRomVersion: Invalid context");
            return;
        }
        try {
            Editor editor = context.getSharedPreferences(ConstValues.SPF_SECURITY_FILE_NAME, 0).edit();
            editor.putString(ConstValues.SPF_LAST_VERSION_NAME_KEY, versionName);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isRomVersionChange(Context context) {
        if (getLastRomVersion(context).equalsIgnoreCase(getSystemVersionName())) {
            return false;
        }
        setLocalVersionWhenUpdate(context, ConstValues.SPF_ALL_VERSION_KEY, -1);
        setLocalVersionWhenUpdate(context, ConstValues.SPF_AVA_VERSION_KEY, -1);
        setNewRomVersion(context, getSystemVersionName());
        return true;
    }

    public static String getSystemVersionName() {
        String ver = Build.DISPLAY;
        HwLog.i("SecPatchHelper", "getSystemVersionName = " + ver);
        return ver;
    }

    public static int checkServerResponseCode(int resultCode) {
        HwLog.i("SecPatchHelper", "checkServerResponseCode: resultCode = " + resultCode);
        switch (resultCode) {
            case 0:
                return 0;
            case 1:
                return 3;
            case 2:
                HwLog.w("SecPatchHelper", "checkServerResponseCode: Invalid param");
                return 3;
            case 210:
                return 0;
            case 211:
                return 0;
            case ConstValues.SRV_CODE_AUTH_FAIL /*401*/:
                HwLog.w("SecPatchHelper", "checkServerResponseCode: Auth fail");
                return 3;
            case ConstValues.SRV_CODE_INNER_ERR /*508*/:
                HwLog.w("SecPatchHelper", "checkServerResponseCode: Inner error");
                return 3;
            default:
                HwLog.e("SecPatchHelper", "checkServerResponseCode: Unexpected result code: " + resultCode + " of response");
                return 3;
        }
    }
}
