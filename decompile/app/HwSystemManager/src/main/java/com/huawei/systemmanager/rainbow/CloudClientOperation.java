package com.huawei.systemmanager.rainbow;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.rainbow.client.background.service.RainbowCommonService;
import com.huawei.systemmanager.rainbow.client.base.ClientConstant;
import com.huawei.systemmanager.rainbow.client.base.ClientConstant.CloudActions;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys;
import com.huawei.systemmanager.rainbow.client.helper.LocalSharedPrefrenceHelper;
import com.huawei.systemmanager.rainbow.client.util.OperationLocal;
import com.huawei.systemmanager.util.HwLog;

public class CloudClientOperation {
    private static final String TAG = "CloudClientOperation";

    public static boolean closeSystemManageClouds(Context context) {
        return OperationLocal.closeSystemManageClouds(context);
    }

    public static boolean openSystemManageClouds(Context context) {
        return OperationLocal.openSystemManageClouds(context);
    }

    public static void openSystemManageCloudsWithInit(Context context) {
        openSystemManageClouds(context);
        Intent serviceIntent = new Intent(CloudActions.INTENT_INIT_CLOUDDB);
        serviceIntent.setClass(context, RainbowCommonService.class);
        context.startService(serviceIntent);
    }

    public static boolean getSystemManageCloudsStatus(Context context) {
        try {
            if (ClientConstant.SYSTEM_CLOUD_CLOSE.equals(new LocalSharedPrefrenceHelper(context).getString(CloudSpfKeys.SYSTEM_MANAGER_CLOUD, ClientConstant.SYSTEM_CLOUD_CLOSE))) {
                return false;
            }
            return true;
        } catch (Exception e) {
            HwLog.e(TAG, e.toString(), e);
            return false;
        }
    }
}
