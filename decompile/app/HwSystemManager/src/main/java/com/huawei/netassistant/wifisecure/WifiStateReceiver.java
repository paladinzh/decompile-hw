package com.huawei.netassistant.wifisecure;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;

public class WifiStateReceiver extends HsmBroadcastReceiver {
    private static final String TAG = "WifiStateReceiver";

    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            HwLog.w(TAG, "onReceive: Invalid params");
            return;
        }
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            HwLog.w(TAG, "onReceive: Invalid action");
            return;
        }
        HwLog.i(TAG, "onReceive: action = " + action);
        sendToBackground(context, intent);
    }

    public void doInBackground(Context context, Intent intent) {
        HsmWifiDetectManager.getInstance().handleWifiStateChangeEvent(context, intent);
    }
}
