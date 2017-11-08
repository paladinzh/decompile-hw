package com.android.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import com.huawei.cust.HwCustUtils;

public class TestingSettingsHwBase extends SettingsPreferenceFragment implements OnPreferenceClickListener {
    private HwCustTestingSettingsHwBase mCustTestingSettings;
    private PreferenceScreen timePreference;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230909);
        this.timePreference = (PreferenceScreen) getPreferenceScreen().findPreference("used_time_stat");
        if (this.timePreference != null && "0".equals(SystemProperties.get("ro.config.hw.usedtime.statis", "0"))) {
            getPreferenceScreen().removePreference(this.timePreference);
        }
        if (Utils.isMultiSimEnabled()) {
            getPreferenceScreen().findPreference("radio_info_settings").setOnPreferenceClickListener(this);
        }
        this.mCustTestingSettings = (HwCustTestingSettingsHwBase) HwCustUtils.createObj(HwCustTestingSettingsHwBase.class, new Object[]{getActivity()});
        if (this.mCustTestingSettings != null) {
            this.mCustTestingSettings.hideWifiInfoMenu(getPreferenceScreen());
        }
    }

    public Dialog onCreateDialog(int id) {
        return new Builder(getActivity()).setTitle(2131627400).setItems(2131361916, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = TestingSettingsHwBase.this.getIntent();
                intent.setClassName("com.android.settings", "com.android.settings.RadioInfo");
                intent.putExtra("SUBSCRIPTION_ID", which);
                TestingSettingsHwBase.this.startActivity(intent);
            }
        }).create();
    }

    public boolean onPreferenceClick(Preference preference) {
        if (!preference.getKey().equals("radio_info_settings")) {
            return false;
        }
        showDialog(0);
        return true;
    }

    protected int getMetricsCategory() {
        return 89;
    }
}
