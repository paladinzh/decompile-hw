package com.huawei.rcs.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import com.huawei.cspcommon.MLog;

public class RcsNetworkAdapter {
    private static final String FT_TAG = (TAG + " FileTrans: ");
    private static String TAG = "RcsNetworkAdapter";
    public static final Uri apnUri = Uri.parse("content://telephony/carriers/preferapn");

    public static NetworkInfo getActiveNetworkInfo(Context aContext) {
        ConnectivityManager connect = (ConnectivityManager) aContext.getSystemService("connectivity");
        if (connect != null) {
            return connect.getActiveNetworkInfo();
        }
        return null;
    }

    public static boolean isNetworkAvailable(Context context) {
        boolean z = false;
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        if (networkInfo == null) {
            MLog.w(FT_TAG, "networkInfo is null ");
            return false;
        }
        MLog.i(FT_TAG, "isNetworkAvailable(), networkInfo.getTypeName = " + networkInfo.getTypeName());
        if (!(!networkInfo.isAvailable() || TextUtils.isEmpty(networkInfo.getTypeName()) || "UNKNOW".equalsIgnoreCase(networkInfo.getTypeName()))) {
            z = true;
        }
        return z;
    }
}
