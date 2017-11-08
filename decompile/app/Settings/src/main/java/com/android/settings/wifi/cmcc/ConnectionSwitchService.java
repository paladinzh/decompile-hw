package com.android.settings.wifi.cmcc;

import android.app.Service;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

public class ConnectionSwitchService extends Service {
    public void onCreate() {
        super.onCreate();
        int themeId = getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        if (themeId != 0) {
            setTheme(themeId);
        }
    }

    public int onStartCommand(Intent serviceIntent, int flags, int startId) {
        if (serviceIntent == null) {
            return 2;
        }
        Intent broadcastIntent = (Intent) serviceIntent.getParcelableExtra("broadcast_intent");
        if (broadcastIntent == null || TextUtils.isEmpty(broadcastIntent.getAction())) {
            return 2;
        }
        String action = broadcastIntent.getAction();
        if ("android.net.wifi.STATE_CHANGE".equals(action)) {
            SwitchToWifiUtils.getInstance(this).onWifiNetworkStateChanged((NetworkInfo) broadcastIntent.getParcelableExtra("networkInfo"));
        } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
            SwitchToWifiUtils.getInstance(this).onWifiStateChanged(broadcastIntent);
        } else if ("android.net.wifi.SCAN_RESULTS".equals(action)) {
            SwitchToWifiUtils.getInstance(this).onScanFinished();
        } else if ("android.intent.action.WIFI_NETWORK_CONNECTION_CHANGED".equals(action)) {
            boolean isConnectingOrConnected = broadcastIntent.getBooleanExtra("connect_state", false);
            Log.i("ConnectionSwitchService", "action:" + action + ", isConnectingOrConnected:" + isConnectingOrConnected);
            SwitchToWifiUtils.getInstance(this).onWifiConnectionChanged(isConnectingOrConnected);
        } else if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action)) {
            SwitchToWifiUtils.getInstance(this).updateSupplicantState((SupplicantState) broadcastIntent.getParcelableExtra("newState"));
        } else if ("com.android.settings.action.START_SCAN_WIFI".equals(action)) {
            SwitchToWifiUtils.getInstance(this).startScan();
        } else if ("com.android.settings.action.ALARM_SWITCH_TO_MOBILE_NETWORK".equals(action)) {
            SwitchToWifiUtils.getInstance(this).handleSwitchToMoblie();
        } else {
            Log.e("ConnectionSwitchService", "Illegal action: " + action);
        }
        return super.onStartCommand(serviceIntent, flags, startId);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
