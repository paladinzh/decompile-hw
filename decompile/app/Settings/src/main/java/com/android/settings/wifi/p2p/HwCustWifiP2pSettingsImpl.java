package com.android.settings.wifi.p2p;

import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.util.Log;
import android.view.MenuItem;
import com.android.settings.ProgressCategory;
import com.huawei.android.wifi.p2p.WifiP2pManagerCustExt;

public class HwCustWifiP2pSettingsImpl extends HwCustWifiP2pSettings {
    private static final String TAG = "HwCustWifiP2pSettingsImpl";
    WifiP2pManagerCustExt mWifiP2pManagerCustExt;

    public HwCustWifiP2pSettingsImpl(WifiP2pSettings wifiP2pSettings) {
        super(wifiP2pSettings);
        if (this.mWifiP2pManagerCustExt == null) {
            this.mWifiP2pManagerCustExt = new WifiP2pManagerCustExt();
        }
    }

    public void setP2pSearchMenuEnabled(MenuItem menuItem, boolean enabled) {
        Log.d(TAG, "setP2pSearchMenuEnabled, default enabled argument is " + enabled);
        if (!isSupportStaP2pCoexist()) {
            boolean wifiP2pEnabled = enabled;
            if (!isWifiP2pEnabled()) {
                wifiP2pEnabled = false;
            }
            if (menuItem != null) {
                menuItem.setEnabled(wifiP2pEnabled);
            }
        }
    }

    public void updateDevicePrefEnabled(Preference thisDevicePref) {
        Log.d(TAG, "updateDevicePrefEnabled is called!");
        if (!isSupportStaP2pCoexist()) {
            if (isWifiP2pEnabled()) {
                thisDevicePref.setEnabled(true);
            } else {
                thisDevicePref.setEnabled(false);
            }
        }
    }

    public void updateAllDevicePrefEnabled(PreferenceCategory deviceCatotgory, Preference thisDevicePref, ProgressCategory peersGroup, PreferenceGroup persistentGroup) {
        Log.d(TAG, "updateAllDevicePrefEnabled is called!");
        if (!isSupportStaP2pCoexist() && !isWifiP2pEnabled()) {
            deviceCatotgory.setEnabled(false);
            thisDevicePref.setEnabled(false);
            peersGroup.setEnabled(false);
            persistentGroup.setEnabled(false);
        }
    }

    public boolean isSupportStaP2pCoexist() {
        if (SystemProperties.get("ro.connectivity.sta_p2p_coex").equals("false")) {
            return false;
        }
        return true;
    }

    public boolean isWifiP2pEnabled() {
        if (isSupportStaP2pCoexist()) {
            return false;
        }
        boolean ret = this.mWifiP2pManagerCustExt.isWifiP2pEnabled();
        Log.d(TAG, "isWifiP2pEnabled=" + ret);
        return ret;
    }
}
