package com.android.settings.wifi.cmcc;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.util.Log;

public class WifiSettingsExt {
    private Preference mAddOtherNetworkPreference;
    private PreferenceCategory mConfigedAP;
    private Context mContext;
    private PreferenceCategory mNewAP;
    private PreferenceCategory mWifiListCategory;
    private WifiManager mWifiManager = ((WifiManager) this.mContext.getSystemService("wifi"));

    public WifiSettingsExt(Context context, PreferenceCategory configedPref, PreferenceCategory newPref, PreferenceCategory wifiListCategory) {
        this.mContext = context;
        setCategory(configedPref, newPref, wifiListCategory);
    }

    public void setCategory(PreferenceCategory configedPref, PreferenceCategory newPref, PreferenceCategory wifiListCategory) {
        this.mConfigedAP = configedPref;
        this.mNewAP = newPref;
        this.mWifiListCategory = wifiListCategory;
    }

    public void setAddNetworkPreference(Preference preference) {
        this.mAddOtherNetworkPreference = preference;
    }

    public void evaluateCmccCategory(PreferenceGroup root) {
        if (Features.shouldShownByCategory()) {
            root.removePreference(this.mWifiListCategory);
            return;
        }
        root.removePreference(this.mConfigedAP);
        root.removePreference(this.mNewAP);
    }

    public void cleanWifiRootList(PreferenceGroup root) {
        removeWifiDeviceList(root);
        root.removePreference(this.mAddOtherNetworkPreference);
    }

    public void removeWifiDeviceList(PreferenceGroup root) {
        if (Features.shouldShownByCategory()) {
            root.removePreference(this.mConfigedAP);
            root.removePreference(this.mNewAP);
            return;
        }
        root.removePreference(this.mWifiListCategory);
    }

    public void clearWifiApList(PreferenceGroup root) {
        if (Features.shouldShownByCategory()) {
            root.addPreference(this.mConfigedAP);
            root.addPreference(this.mNewAP);
            this.mConfigedAP.removeAll();
            this.mNewAP.removeAll();
        } else {
            root.addPreference(this.mWifiListCategory);
            this.mWifiListCategory.removeAll();
        }
        root.addPreference(this.mAddOtherNetworkPreference);
    }

    public boolean isCatogoryExist() {
        return Features.shouldShownByCategory();
    }

    public boolean shouldAddDisconnectMenu(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivity != null && connectivity.getNetworkInfo(1).isConnected() && Features.shouldAddDisconnectMenu()) {
            return true;
        }
        return false;
    }

    public void disconnect(int networkId) {
        Log.d("WifiSettingsExt", "disconnect() from current active AP");
        this.mWifiManager.disableNetwork(networkId);
    }
}
