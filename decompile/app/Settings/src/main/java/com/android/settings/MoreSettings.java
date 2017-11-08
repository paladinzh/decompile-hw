package com.android.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

public class MoreSettings extends RestrictedSettingsFragment {
    public MoreSettings() {
        super(null);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onResume() {
        updatePreferenceList();
        super.onResume();
    }

    protected void updatePreferenceList() {
        PreferenceScreen root = getPreferenceScreen();
        int index = 0;
        while (index < root.getPreferenceCount()) {
            Preference preference = root.getPreference(index);
            Intent intent = preference.getIntent();
            if (!(intent == null || Utils.hasIntentActivity(getPackageManager(), intent))) {
                root.removePreference(preference);
                index--;
            }
            index++;
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        ItemUseStat.getInstance().handleClick(getActivity(), 2, preference.getKey());
        return super.onPreferenceTreeClick(preference);
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
