package com.huawei.systemmanager.rainbow.client.background.handle.serv;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.rainbow.client.background.handle.IIntentHandler;
import com.huawei.systemmanager.rainbow.client.base.ClientConstant;
import com.huawei.systemmanager.rainbow.client.base.ClientConstant.CloudActions;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys;
import com.huawei.systemmanager.rainbow.client.connect.RequestMgr;
import com.huawei.systemmanager.rainbow.client.connect.result.ClientServerSync;
import com.huawei.systemmanager.rainbow.client.helper.LocalSharedPrefrenceHelper;
import com.huawei.systemmanager.rainbow.client.util.NetWorkHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.Random;

public class UpdateAllDataServHandle implements IIntentHandler {
    private static final String TAG = "UpdateAllDataServHandle";

    public void handleIntent(Context ctx, Intent intent) {
        if (AbroadUtils.isAbroad()) {
            HwLog.e(TAG, "The cloud is not enabled");
        } else {
            handleUpdateAllIntent(ctx, intent);
        }
    }

    private void handleUpdateAllIntent(Context ctx, Intent intent) {
        if (NetWorkHelper.isAccessNetworkAllowAndNetAvailable(ctx)) {
            String action = intent.getAction();
            try {
                if (ClientConstant.SYSTEM_CLOUD_OPEN.equals(new LocalSharedPrefrenceHelper(ctx).getString(CloudSpfKeys.SYSTEM_MANAGER_CLOUD, ClientConstant.SYSTEM_CLOUD_OPEN)) && CloudActions.INTENT_UPDATE_ALL_DATA.equals(action)) {
                    dealPostRequest(ctx);
                }
            } catch (Exception e) {
                HwLog.e(TAG, "startCheckData Exception=:" + e.toString());
            }
            return;
        }
        HwLog.e(TAG, "onHandleIntent check access network is denied!");
    }

    private void dealPostRequest(Context ctx) {
        boolean z;
        boolean result = RequestMgr.generateCloudRequest(ctx).processRequest(ctx);
        boolean flag = RequestMgr.generateCloudRequestForFile(ctx);
        String str = TAG;
        StringBuilder append = new StringBuilder().append("dealPostRequest result&&flag: ");
        if (result) {
            z = flag;
        } else {
            z = false;
        }
        HwLog.i(str, append.append(z).toString());
        if (!result) {
            flag = false;
        }
        changeUpdateFlags(ctx, flag);
    }

    private void changeUpdateFlags(Context ctx, boolean requestResult) {
        LocalSharedPrefrenceHelper sharedService = new LocalSharedPrefrenceHelper(ctx);
        sharedService.putBoolean(CloudSpfKeys.GET_CLOUD_SERVER_DATA_SUCCESS, requestResult);
        sharedService.putLong(CloudSpfKeys.LAST_ALARM_TIME, (System.currentTimeMillis() + ClientServerSync.getIntervalTimeFromServer()) + ((long) new Random().nextInt(21600000)));
        int lastReconnectCount = sharedService.getInt(CloudSpfKeys.RECONNECT_CLOUD_SERVER_COUNT, 0);
        if (requestResult) {
            sharedService.putInt(CloudSpfKeys.RECONNECT_CLOUD_SERVER_COUNT, 0);
            return;
        }
        if (5 > lastReconnectCount) {
            lastReconnectCount++;
        }
        sharedService.putInt(CloudSpfKeys.RECONNECT_CLOUD_SERVER_COUNT, lastReconnectCount);
    }
}
