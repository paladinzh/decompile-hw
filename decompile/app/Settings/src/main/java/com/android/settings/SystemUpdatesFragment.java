package com.android.settings;

import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;

public class SystemUpdatesFragment extends PreferenceFragment {
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230908);
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }
}
