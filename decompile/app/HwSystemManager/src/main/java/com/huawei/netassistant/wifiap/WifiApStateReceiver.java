package com.huawei.netassistant.wifiap;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.UserHandle;
import android.text.TextUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;

public class WifiApStateReceiver extends HsmBroadcastReceiver {
    private static final String TAG = "WifiApStateReceiver";

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
        String action = intent.getAction();
        if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
            handleWifiAPStateChange(context, intent.getIntExtra("wifi_state", 11));
        } else if ("android.net.conn.TETHER_STATE_CHANGED".equals(action)) {
            handleTetherStateChange(context, intent);
        }
    }

    private void handleWifiAPStateChange(Context context, int apState) {
        switch (apState) {
            case 13:
                WifiApHelper.setApState(true);
                HwLog.i(TAG, "handleWifiAPStateChange: Wifi AP is enabled");
                return;
            default:
                return;
        }
    }

    private void handleTetherStateChange(Context context, Intent intent) {
        boolean isWifiApEnable = false;
        try {
            isWifiApEnable = ((WifiManager) context.getSystemService("wifi")).isWifiApEnabled();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (WifiApHelper.getApState() || r1) {
            switch (WifiApHelper.getInstance().parseTetherState(intent)) {
                case 0:
                    HwLog.i(TAG, "handleTetherStateChange: tether state error");
                    WifiApHelper.setApState(false);
                    WifiApHelper.destroyInstance();
                    break;
                case 1:
                    HwLog.i(TAG, "handleTetherStateChange: tether state success");
                    startWatchingService(context, WifiApWatchingService.ACTION_START_WATCHING_WIFIAP);
                    break;
            }
            return;
        }
        HwLog.d(TAG, "handleTetherStateChange: Wifi AP is not enabled ,skip");
    }

    private void startWatchingService(Context context, String action) {
        try {
            Intent intent = new Intent(context, WifiApWatchingService.class);
            intent.setAction(action);
            context.startServiceAsUser(intent, UserHandle.OWNER);
        } catch (Exception e) {
            HwLog.e(TAG, "startWatchingService: Exception", e);
        }
    }
}
