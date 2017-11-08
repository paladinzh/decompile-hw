package com.android.settings.wifi;

import android.os.Bundle;
import com.android.settings.SettingsPreferenceFragment;

public class WifiInfo extends SettingsPreferenceFragment {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230910);
    }

    protected int getMetricsCategory() {
        return 89;
    }
}
