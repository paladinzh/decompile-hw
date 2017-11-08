package com.android.settings.wifi;

import android.app.Dialog;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settings.SettingsExtUtils;
import com.android.settings.wifi.cmcc.WifiExt;

public class AdvancedWifiSettingsHwBase extends RestrictedSettingsFragment {
    protected boolean mUnavailable;
    protected WifiExt mWifiExt;
    protected PreferenceGroup mWifiSettingsCategory;

    public AdvancedWifiSettingsHwBase(String restrictionKey) {
        super(restrictionKey);
    }

    protected int getMetricsCategory() {
        return 104;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity().getIntent() != null && getActivity().getIntent().getBooleanExtra("extra_not_ip_settings", false)) {
            getActivity().setTitle(2131625020);
        }
        if (isUiRestricted()) {
            this.mUnavailable = true;
            setPreferenceScreen(new PreferenceScreen(getPrefContext(), null));
        } else {
            addPreferencesFromResource(2131230934);
        }
        this.mWifiExt = new WifiExt(getActivity());
        this.mWifiSettingsCategory = (PreferenceGroup) findPreference("wifi_settings_category");
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mWifiExt.initConnectView(getActivity(), this.mWifiSettingsCategory);
        this.mWifiExt.initPreference(getContentResolver());
    }

    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
            case 2:
                return new WpsDialog(getActivity(), 0);
            case 3:
                return new WpsDialog(getActivity(), 1);
            default:
                return super.onCreateDialog(dialogId);
        }
    }

    protected void initWifiSecurityCheckPreference(OnPreferenceChangeListener onPreferenceChangeListener) {
        boolean z = true;
        TwoStatePreference wifiCloudSecurityCheck = (TwoStatePreference) findPreference("wifi_cloud_security_check");
        if (SettingsExtUtils.isGlobalVersion()) {
            removePreference("wifi_settings_category", "wifi_cloud_security_check");
            removeEmptyCategory("wifi_settings_category");
        } else if (wifiCloudSecurityCheck != null) {
            if (Global.getInt(getContentResolver(), "wifi_cloud_security_check", 0) != 1) {
                z = false;
            }
            wifiCloudSecurityCheck.setChecked(z);
            wifiCloudSecurityCheck.setOnPreferenceChangeListener(onPreferenceChangeListener);
        }
    }
}
