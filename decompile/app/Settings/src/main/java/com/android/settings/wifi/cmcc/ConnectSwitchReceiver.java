package com.android.settings.wifi.cmcc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import com.android.settings.wifi.cmcc.SwitchToWifiUtils.Config;

public class ConnectSwitchReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (Config.isCMCC() || SystemProperties.getBoolean("ro.config.hw_wifi_connect_mode", false)) {
            Intent serviceIntent = new Intent(context, ConnectionSwitchService.class);
            serviceIntent.putExtra("broadcast_intent", intent);
            context.startService(serviceIntent);
        }
    }
}
