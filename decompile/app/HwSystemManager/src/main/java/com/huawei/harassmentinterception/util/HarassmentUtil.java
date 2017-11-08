package com.huawei.harassmentinterception.util;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.huawei.systemmanager.util.HwLog;

public class HarassmentUtil {
    private static final String TAG = "HarassmentUtil";

    public static boolean checkNetworkAvaliable(Context ctx) {
        if (ctx == null) {
            HwLog.w(TAG, "checkNetworkAvaliable false, ctx is null!");
            return false;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService("connectivity");
        if (connectivityManager == null) {
            HwLog.w(TAG, "checkNetworkAvaliable false, connectivityManager is null!");
            return false;
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            HwLog.i(TAG, "checkNetworkAvaliable: Fail to get network info");
            return false;
        } else if (!networkInfo.isConnected()) {
            HwLog.i(TAG, "checkNetworkAvaliable: network is not connected");
            return false;
        } else if (networkInfo.isAvailable()) {
            return true;
        } else {
            HwLog.i(TAG, "checkNetworkAvaliable: network is not available");
            return false;
        }
    }

    public static void requestInputMethod(Dialog dialog) {
        dialog.getWindow().setSoftInputMode(5);
    }
}
