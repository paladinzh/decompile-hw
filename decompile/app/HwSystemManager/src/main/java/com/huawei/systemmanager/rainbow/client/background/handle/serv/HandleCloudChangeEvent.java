package com.huawei.systemmanager.rainbow.client.background.handle.serv;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.push.CustomTaskHandler;
import com.huawei.systemmanager.rainbow.CloudSwitchHelper;
import com.huawei.systemmanager.rainbow.client.background.handle.IIntentHandler;
import com.huawei.systemmanager.rainbow.client.background.service.RainbowCommonService;
import com.huawei.systemmanager.rainbow.client.base.ClientConstant.CloudActions;
import com.huawei.systemmanager.rainbow.client.base.ClientConstant.CloudTimeConst;
import com.huawei.systemmanager.rainbow.client.base.ClientConstant.DelayTimeArray;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys;
import com.huawei.systemmanager.rainbow.client.helper.LocalSharedPrefrenceHelper;
import com.huawei.systemmanager.rainbow.client.util.NetWorkHelper;
import com.huawei.systemmanager.useragreement.UserAgreementHelper;
import com.huawei.systemmanager.util.HwLog;

public class HandleCloudChangeEvent implements IIntentHandler {
    private static final String TAG = "HandleCloudChangeEvent";

    public void handleIntent(Context context, Intent intent) {
        if (!CloudSwitchHelper.isCloudEnabled()) {
            HwLog.e(TAG, "HandleCloudChangeEvent the rainbow is not enabled!");
        } else if (intent == null) {
            HwLog.e(TAG, "intent is null");
        } else {
            String action = intent.getAction();
            HwLog.d(TAG, "action = " + action);
            if (!Utility.isTokenRegistered(context) && UserAgreementHelper.getUserAgreementState(context)) {
                boolean isConnected = false;
                if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                    NetworkInfo activeNetwork = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
                    isConnected = activeNetwork != null ? activeNetwork.isConnectedOrConnecting() : false;
                }
                if (isConnected) {
                    CustomTaskHandler.getInstance(context).removeMessages(1);
                    CustomTaskHandler.getInstance(context).sendEmptyMessageDelayed(1, 60000);
                }
            }
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                dealWithConnectChangeEvent(context);
            }
        }
    }

    private void startRainbowService(Context context) {
        new LocalSharedPrefrenceHelper(context).putBoolean(CloudSpfKeys.GET_CLOUD_SERVER_DATA_SUCCESS, false);
        Intent serviceIntent = new Intent(CloudActions.INTENT_UPDATE_ALL_DATA);
        serviceIntent.setClass(context, RainbowCommonService.class);
        context.startService(serviceIntent);
    }

    private void dealWithConnectChangeEvent(Context context) {
        if (NetWorkHelper.isNetworkAvaialble(context)) {
            long currentTime = System.currentTimeMillis();
            LocalSharedPrefrenceHelper sharedService = new LocalSharedPrefrenceHelper(context);
            long lastUpdateTime = sharedService.getLong(CloudSpfKeys.LAST_ALARM_TIME, 0);
            long cycleTime = sharedService.getLong(CloudTimeConst.CHECKVERSION_CYCLE_SPF, 259200000);
            if (currentTime >= lastUpdateTime) {
                HwLog.d(TAG, "We should startService now because last updateTime is 3 days ago!");
                startRainbowService(context);
                return;
            } else if ((2 * cycleTime) + currentTime < lastUpdateTime) {
                HwLog.d(TAG, "We should update the time becauseOf use change the systemTime too early!");
                sharedService.putLong(CloudSpfKeys.LAST_ALARM_TIME, currentTime);
                return;
            } else {
                long lastChangeTime = sharedService.getLong(CloudSpfKeys.LAST_CONNECT_CHANGE_AVAILABLE_TIME, 0);
                if (sharedService.getBoolean(CloudSpfKeys.GET_CLOUD_SERVER_DATA_SUCCESS, false)) {
                    HwLog.w(TAG, "Last time get cloudData success, just return!");
                    return;
                }
                if (currentTime - lastChangeTime > DelayTimeArray.getDelayTime(sharedService.getInt(CloudSpfKeys.RECONNECT_CLOUD_SERVER_COUNT, 0)) + 240000) {
                    sharedService.putLong(CloudSpfKeys.LAST_CONNECT_CHANGE_AVAILABLE_TIME, currentTime);
                    HwLog.d(TAG, "We should startService now because last update failure!");
                    startRainbowService(context);
                }
                return;
            }
        }
        HwLog.w(TAG, "Now the network changeto unavailable, no need update!");
    }
}
