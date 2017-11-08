package com.android.settings.wifi.ap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.huawei.cust.HwCustUtils;

public class WifiApReceiver extends BroadcastReceiver {
    private HwCustWifiApReceiver mCustWifiApReceiver;

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        this.mCustWifiApReceiver = (HwCustWifiApReceiver) HwCustUtils.createObj(HwCustWifiApReceiver.class, new Object[0]);
        if (this.mCustWifiApReceiver != null) {
            this.mCustWifiApReceiver.handleCustIntent(context, intent);
        }
        if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
            handleWifiApStateChanged(context, intent.getIntExtra("wifi_state", 14));
        } else if ("android.net.wifi.WIFI_AP_STA_JOIN".equals(action)) {
            WifiApClientUtils.getInstance(context).addConnectedDevice(intent.getStringExtra("macInfo"), intent.getStringExtra("currentTime"));
            context.sendBroadcast(new Intent("com.android.settings.wifi.action.connected_devices_changed"));
        } else if ("android.net.wifi.WIFI_AP_STA_LEAVE".equals(action)) {
            WifiApClientUtils.getInstance(context).removeConnectedDevice(intent.getStringExtra("macInfo"));
            Intent connectedChanged = new Intent("com.android.settings.wifi.action.connected_devices_changed");
            connectedChanged.setPackage(context.getPackageName());
            context.sendBroadcast(connectedChanged);
        }
    }

    private void handleWifiApStateChanged(Context context, int state) {
        switch (state) {
            case 11:
                WifiApClientUtils.getInstance(context).removeAllConnectedDevice();
                return;
            case 13:
                WifiApClientUtils.getInstance(context).setMacFilters(context);
                return;
            default:
                return;
        }
    }
}
