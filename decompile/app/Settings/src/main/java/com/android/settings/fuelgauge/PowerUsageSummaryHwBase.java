package com.android.settings.fuelgauge;

import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.search.Indexable;

public class PowerUsageSummaryHwBase extends PowerUsageBase implements Indexable {
    protected Preference mBatteryStatusPref;

    protected void deletePreference() {
        PreferenceScreen root = getPreferenceScreen();
        if (getPreferenceScreen() != null) {
            int index = 0;
            while (index < root.getPreferenceCount()) {
                Preference pref = root.getPreference(index);
                if (pref instanceof SwitchPreference) {
                    index++;
                } else {
                    getPreferenceScreen().removePreference(pref);
                }
            }
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
