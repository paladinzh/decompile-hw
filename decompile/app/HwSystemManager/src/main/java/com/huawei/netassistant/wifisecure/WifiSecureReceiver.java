package com.huawei.netassistant.wifisecure;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;

public class WifiSecureReceiver extends HsmBroadcastReceiver {
    private static final String TAG = "WifiSecureReceiver";

    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            HwLog.w(TAG, "onReceive: Invalid params");
            return;
        }
        String action = intent.getAction();
        if (HsmWifiDetectManager.ACTION_WIFI_SECURE_NOTIFICATION.equals(action)) {
            HwLog.i(TAG, "onReceive: action = " + action);
            sendToBackground(context, intent);
            return;
        }
        HwLog.w(TAG, "onReceive: Not expteced action , " + action);
    }

    public void doInBackground(Context context, Intent intent) {
        WifiConfiguration wifiConfig = (WifiConfiguration) intent.getParcelableExtra(HsmWifiDetectManager.KEY_EXTRA_WIFICONFIG);
        if (wifiConfig == null) {
            HwLog.w(TAG, "doInBackground: Fail to get wifi config");
            return;
        }
        disconnectWifi(context, wifiConfig);
        startWifiSettings(context);
        recordStatEvent();
    }

    private void disconnectWifi(Context context, WifiConfiguration wifiConfig) {
        if (WifiConfigHelper.isCurrentlyConnected(context, wifiConfig)) {
            HwLog.i(TAG, "disconnectWifi: isDisconnect = " + WifiConfigHelper.disconnectWifi(context));
            return;
        }
        HwLog.w(TAG, "disconnectWifi: Wifi connection expired, skip");
    }

    private void startWifiSettings(Context context) {
        Intent intent = new Intent("android.settings.WIFI_SETTINGS");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setFlags(805306368);
        intent.setPackage(HsmStatConst.SETTING_PACKAGE_NAME);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            HwLog.e(TAG, "startWifiSettings:Exception", e);
        }
    }

    private void recordStatEvent() {
        HsmStat.statE(Events.E_WIFI_SECURE_CLICK_NOTIFICATION);
    }
}
