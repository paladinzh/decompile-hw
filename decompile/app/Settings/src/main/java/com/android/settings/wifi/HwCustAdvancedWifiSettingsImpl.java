package com.android.settings.wifi;

import android.content.ContentResolver;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Toast;

public class HwCustAdvancedWifiSettingsImpl extends HwCustAdvancedWifiSettings {
    private static final String AUTOCONNECT = "auto_connect_att";
    private static final int AUTO_CONNECT_SWITCH_OPEN = 1;
    private static final int AUTO_CONNECT_SWITCH_VALUE = SystemProperties.getInt("ro.config.auto_connect_attwifi", 0);
    private static final boolean CHECK_INTERNET_ACCESSIBILITY = SystemProperties.getBoolean("ro.config.check_wifi_internet", false);
    private static final int DISABLE = 0;
    private static final int ENABLE = 1;
    private static final String GLOBAL_SETTING_WIFI_NETWORK_ENABLED = "wifi_network_detect_enabled";
    private static final String KEY_AUTO_CONNECT = "auto_connect_wifi_hotspot";
    private static final String KEY_CHECK_INTERNET = "check_internet_accessbility";
    protected static final String TAG = "HwCustAdvancedWifiSettingsImpl";
    private static final int WIFI_ADVANCED = 3;
    private AdvancedWifiSettings mAdvancedWifiSettings;
    private SwitchPreference mAutoConnectWifiHotspot;
    private SwitchPreference mCheckInternetAccessibility;

    public HwCustAdvancedWifiSettingsImpl(Context context) {
        super(context);
    }

    public boolean getIsShowsleepPolicyPref() {
        return SystemProperties.getInt("ro.config.sleep_policy_hide", 0) == 1;
    }

    public void removeSleepPolicyPref(PreferenceGroup mWifiSettingsCategory, ListPreference sleepPolicyPref) {
        if (mWifiSettingsCategory != null && sleepPolicyPref != null) {
            mWifiSettingsCategory.removePreference(sleepPolicyPref);
        }
    }

    public void initCustPreference(AdvancedWifiSettings advancedWifiSettings) {
        this.mAdvancedWifiSettings = advancedWifiSettings;
        if (AUTO_CONNECT_SWITCH_VALUE == 1) {
            SwitchPreference notifyNetworks = (SwitchPreference) advancedWifiSettings.getPreferenceScreen().findPreference("notify_open_networks");
            this.mAutoConnectWifiHotspot = new SwitchPreference(this.mContext);
            insertCustPref(this.mAutoConnectWifiHotspot, KEY_AUTO_CONNECT, 2131629162, false, null, "wifi_settings_category", notifyNetworks.getOrder() - 1);
            this.mAutoConnectWifiHotspot.setSummaryOn(2131629163);
            this.mAutoConnectWifiHotspot.setSummaryOff(2131629164);
            this.mAutoConnectWifiHotspot.setOnPreferenceChangeListener(advancedWifiSettings);
        }
        if (CHECK_INTERNET_ACCESSIBILITY) {
            SwitchPreference sleepPolicy = (SwitchPreference) advancedWifiSettings.getPreferenceScreen().findPreference("sleep_policy");
            this.mCheckInternetAccessibility = new SwitchPreference(this.mContext);
            insertCustPref(this.mCheckInternetAccessibility, KEY_CHECK_INTERNET, 2131629173, false, null, "wifi_settings_category", sleepPolicy != null ? sleepPolicy.getOrder() + 1 : -1);
            this.mCheckInternetAccessibility.setSummary(2131629174);
            this.mCheckInternetAccessibility.setOnPreferenceChangeListener(advancedWifiSettings);
        }
    }

    public void resumeCustPreference(AdvancedWifiSettings advancedWifiSettings) {
        if (AUTO_CONNECT_SWITCH_VALUE == 1) {
            PreferenceScreen root = advancedWifiSettings.getPreferenceScreen();
            if (this.mAutoConnectWifiHotspot != null) {
                this.mAutoConnectWifiHotspot.setChecked(System.getInt(this.mContext.getContentResolver(), AUTOCONNECT, 1) == 1);
            }
        }
        if (CHECK_INTERNET_ACCESSIBILITY && this.mCheckInternetAccessibility != null) {
            this.mCheckInternetAccessibility.setChecked(Global.getInt(this.mContext.getContentResolver(), GLOBAL_SETTING_WIFI_NETWORK_ENABLED, 0) == 1);
        }
    }

    public void onCustPreferenceChange(String key, Object newValue) {
        int i = 1;
        ContentResolver contentResolver;
        String str;
        if (KEY_AUTO_CONNECT.equals(key)) {
            boolean autoConnectEnable = ((Boolean) newValue).booleanValue();
            WifiManager mWifiManager = (WifiManager) this.mContext.getApplicationContext().getSystemService("wifi");
            contentResolver = this.mContext.getContentResolver();
            str = AUTOCONNECT;
            if (!autoConnectEnable) {
                i = 0;
            }
            System.putInt(contentResolver, str, i);
            if (mWifiManager.isWifiEnabled()) {
                Toast.makeText(this.mContext, 2131629165, 0).show();
            }
        } else if (KEY_CHECK_INTERNET.equals(key)) {
            boolean checkInternetEnalbe = ((Boolean) newValue).booleanValue();
            contentResolver = this.mContext.getContentResolver();
            str = GLOBAL_SETTING_WIFI_NETWORK_ENABLED;
            if (!checkInternetEnalbe) {
                i = 0;
            }
            Global.putInt(contentResolver, str, i);
        }
    }

    private void insertCustPref(Preference preference, String key, int titleId, boolean persistent, String summary, String containerKey, int order) {
        preference.setKey(key);
        preference.setTitle(titleId);
        preference.setPersistent(persistent);
        preference.setSummary((CharSequence) summary);
        ((PreferenceGroup) this.mAdvancedWifiSettings.getPreferenceScreen().findPreference(containerKey)).addPreference(preference);
        if (order != -1) {
            preference.setOrder(order);
        }
    }

    public void savesleepPolicyPrefValue(String stringValue) {
        ReportTool.getInstance(this.mContext).report(3, String.format("{KEEP_SLEEP:%s}", new Object[]{stringValue}));
    }

    public void saveNotifyOpenNetworksValue(Object newValue) {
        ReportTool.getInstance(this.mContext).report(3, String.format("{NETWORK_NOTIFY:%s}", new Object[]{newValue}));
    }

    public void saveScanAlwaysAvailableValue(Object newValue) {
        ReportTool.getInstance(this.mContext).report(3, String.format("{SCAN_ALWAYS:%s}", new Object[]{newValue}));
    }
}
