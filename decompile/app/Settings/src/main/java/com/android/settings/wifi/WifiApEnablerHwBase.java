package com.android.settings.wifi;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.support.v7.preference.TwoStatePreference;
import com.android.settings.MLog;

public class WifiApEnablerHwBase {
    private Context mContext;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(intent.getAction()) && WifiApEnablerHwBase.this.mWaitForWifiStateChange) {
                WifiApEnablerHwBase.this.handleWifiStateChanged(intent.getIntExtra("wifi_state", 4));
            }
        }
    };
    protected boolean mWaitForWifiStateChange;
    protected WifiManager mWifiManager;

    public WifiApEnablerHwBase(Context context, TwoStatePreference checkBox) {
        this.mContext = context;
        this.mWifiManager = (WifiManager) context.getApplicationContext().getSystemService("wifi");
    }

    public void resume() {
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED"));
    }

    public void pause() {
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    protected void enableWifiSwitch() {
    }

    protected void handleWifiApStateChanged(int state, int reason) {
        MLog.d("WifiApEnabler", "handleWifiApStateChanged state=" + state);
    }

    protected void checkWifiState(boolean enable) {
        MLog.d("WifiApEnabler", "checkWifiState enable=" + enable);
        if (!enable) {
            ContentResolver cr = this.mContext.getContentResolver();
            int wifiSavedState = 0;
            this.mWaitForWifiStateChange = false;
            try {
                wifiSavedState = Global.getInt(cr, "wifi_saved_state");
            } catch (SettingNotFoundException e) {
                e.printStackTrace();
            }
            if (wifiSavedState == 1) {
                this.mWaitForWifiStateChange = true;
                MLog.d("WifiApEnabler", "wifiSavedState =1 ");
            }
        }
    }

    protected void handleWifiApStateChanged(Intent intent) {
        if (intent != null) {
            int wifiApState = intent.getIntExtra("wifi_state", -1);
            int wifiState = intent.getIntExtra("previous_wifi_state", -1);
            MLog.i("WifiApEnabler", "BroadcastReceiver wifiAp state change, wifiApState = " + wifiApState + " -- wifiState = " + wifiState);
            if (wifiApState == 13 || wifiApState == 12) {
                this.mWaitForWifiStateChange = false;
            }
            if (wifiApState == 11 && wifiState == 4) {
                this.mWaitForWifiStateChange = false;
            }
            int state = intent.getIntExtra("wifi_state", 14);
            if (state == 14) {
                handleWifiApStateChanged(state, intent.getIntExtra("wifi_ap_error_code", 0));
            } else {
                handleWifiApStateChanged(state, 0);
            }
        }
    }

    private void handleWifiStateChanged(int state) {
        switch (state) {
            case 3:
            case 4:
                MLog.d("WifiApEnabler", "handleWifiStateChanged enableWifiCheckBox state=" + state);
                enableWifiSwitch();
                this.mWaitForWifiStateChange = false;
                return;
            default:
                return;
        }
    }

    public boolean isSameState(boolean enable) {
        boolean z = true;
        int state = this.mWifiManager.getWifiApState();
        if (enable) {
            if (!(state == 13 || state == 12)) {
                z = false;
            }
            return z;
        } else if (this.mWaitForWifiStateChange) {
            MLog.w("WifiApEnabler", "has try to disable ap and need waiting wifi open");
            return true;
        } else {
            if (!(state == 11 || state == 10)) {
                z = false;
            }
            return z;
        }
    }
}
