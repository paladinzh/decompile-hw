package com.android.settings.wifi.ap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Log;
import java.util.ArrayList;

public class ApListChangedReceiver extends BroadcastReceiver {
    private boolean mPowerModeOn = SystemProperties.getBoolean("ro.config.hotspot_power_mode_on", false);

    public void onReceive(Context context, Intent intent) {
        if (this.mPowerModeOn && intent != null) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_AP_STA_JOIN".equals(action) || "android.net.wifi.WIFI_AP_STA_LEAVE".equals(action)) {
                startHotspotManagerService(context);
            } else if (!"android.net.conn.TETHER_STATE_CHANGED".equals(action)) {
                Log.e("ApListChangedReceiver", "Useless intent: " + intent);
            } else if (intent.hasExtra("activeArray") && hasWifiActive(intent.getStringArrayListExtra("activeArray"), context.getResources().getStringArray(17235988))) {
                startHotspotManagerService(context);
            }
        }
    }

    private void startHotspotManagerService(Context context) {
        context.startService(new Intent(context, HotspotPowerManagerService.class));
    }

    private boolean hasWifiActive(ArrayList<String> activeList, String[] tetherableWifiRegexs) {
        if (activeList == null || activeList.size() == 0) {
            return false;
        }
        for (String active : activeList) {
            for (String regex : tetherableWifiRegexs) {
                if (active.matches(regex)) {
                    return true;
                }
            }
        }
        return false;
    }
}
