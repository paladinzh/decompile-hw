package com.huawei.systemmanager.rainbow.client.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.huawei.systemmanager.rainbow.CloudClientOperation;
import com.huawei.systemmanager.useragreement.UserAgreementHelper;
import com.huawei.systemmanager.util.HwLog;

public class NetWorkHelper {
    private static final String TAG = "NetWorkHelper";

    public static boolean isAccessNetworkAllowAndNetAvailable(Context ctx) {
        if (!CloudClientOperation.getSystemManageCloudsStatus(ctx)) {
            HwLog.e(TAG, "allowAccessNetwork is not allowed!");
            return false;
        } else if (UserAgreementHelper.getUserAgreementState(ctx)) {
            return isNetworkAvaialble(ctx);
        } else {
            HwLog.e(TAG, "allowAccessNetwork: User agreement is not agreed");
            return false;
        }
    }

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
}
