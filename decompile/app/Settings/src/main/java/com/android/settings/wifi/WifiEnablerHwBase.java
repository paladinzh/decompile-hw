package com.android.settings.wifi;

import android.content.Context;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiSsid;
import android.support.v14.preference.SwitchPreference;
import android.text.TextUtils;
import com.android.settings.dashboard.DashBoardTileEnabler;
import com.android.settings.wifi.cmcc.WifiExt;

public class WifiEnablerHwBase extends DashBoardTileEnabler {
    protected SwitchPreference mSwitch;
    protected WifiExt mWifiExt = null;
    protected WifiManager mWifiManager;

    public WifiEnablerHwBase(Context context) {
        this.mWifiExt = new WifiExt(context);
    }

    protected void setSwitchEnabled(boolean enabled) {
        if (this.mSwitch != null) {
            this.mSwitch.setEnabled(enabled);
        }
    }

    public void updateStatusText(boolean isEnabled) {
        if (this.mContext != null) {
            int resId;
            if (isEnabled) {
                resId = 2131626191;
            } else {
                resId = 2131627699;
            }
            updateStatusText(this.mContext.getResources().getString(resId));
        }
    }

    protected void handleStateChanged(DetailedState state) {
        if (state == null) {
            return;
        }
        if (DetailedState.DISCONNECTED == state || DetailedState.FAILED == state) {
            updateStatusText(this.mWifiManager.isWifiEnabled());
        } else if (DetailedState.CONNECTED == state) {
            updateStatusText(WifiInfo.removeDoubleQuotes(this.mWifiManager.getConnectionInfo().getSSID()));
        }
    }

    protected String getSsid() {
        WifiSsid wifiSsid = this.mWifiManager.getConnectionInfo().getWifiSsid();
        if (wifiSsid == null || TextUtils.isEmpty(wifiSsid.toString())) {
            return null;
        }
        return this.mWifiManager.getConnectionInfo().getSSID();
    }

    protected void updateTextByWifiStatus() {
        boolean isEnabled = 3 == this.mWifiManager.getWifiState();
        updateStatusText(isEnabled);
        if (isEnabled) {
            String connectedSsid = getSsid();
            if (connectedSsid != null) {
                updateStatusText(WifiInfo.removeDoubleQuotes(connectedSsid));
            }
        }
    }
}
