package com.android.settings.wifi.p2p;

import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.view.MenuItem;
import com.android.settings.ProgressCategory;

public class HwCustWifiP2pSettings {
    public WifiP2pSettings mWifiP2pSettings;

    public HwCustWifiP2pSettings(WifiP2pSettings wifiP2pSettings) {
        this.mWifiP2pSettings = wifiP2pSettings;
    }

    @Deprecated
    public void setP2pSearchMenuEnabled(MenuItem menuItem, boolean enabled) {
    }

    @Deprecated
    public void updateDevicePrefEnabled(Preference thisDevicePref) {
    }

    @Deprecated
    public void updateAllDevicePrefEnabled(PreferenceCategory deviceCatotgory, Preference thisDevicePref, ProgressCategory peersGroup, PreferenceGroup persistentGroup) {
    }

    @Deprecated
    public boolean isSupportStaP2pCoexist() {
        return true;
    }

    @Deprecated
    public boolean isWifiP2pEnabled() {
        return false;
    }
}
