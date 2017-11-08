package com.huawei.systemmanager.rainbow.client.helper;

import android.content.Context;
import android.util.Log;
import com.huawei.systemmanager.rainbow.client.base.ClientConstant;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys;
import com.huawei.systemmanager.rainbow.client.util.NetWorkHelper;
import com.huawei.systemmanager.util.HwLog;

public class ServerRequestHelper {
    public static final String TAG = "ServerRequestHelper";

    public static int checkServerResponseCode(int resultCode) {
        HwLog.i(TAG, "checkServerResponseCode: resultCode = " + resultCode);
        switch (resultCode) {
            case 0:
                return 0;
            case 20000:
                Log.v(TAG, "checkServerResponseCode: NO_NEED_UPDATE");
                return 2;
            default:
                Log.e(TAG, "checkServerResponseCode: Unexpected result code: " + resultCode + " of response");
                return 3;
        }
    }

    public static boolean shouldDoRequest(Context ctx) {
        if (!ClientConstant.SYSTEM_CLOUD_OPEN.equals(new LocalSharedPrefrenceHelper(ctx).getString(CloudSpfKeys.SYSTEM_MANAGER_CLOUD, ClientConstant.SYSTEM_CLOUD_OPEN))) {
            HwLog.w(TAG, "shouldDoRequest: Cloud feature is disabled!");
            return false;
        } else if (NetWorkHelper.isAccessNetworkAllowAndNetAvailable(ctx)) {
            return true;
        } else {
            HwLog.w(TAG, "shouldDoRequest: Network is not available or restricted!");
            return false;
        }
    }
}
