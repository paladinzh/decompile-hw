package com.android.systemui.tuner;

import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import com.android.systemui.R;

public class OtherPrefs extends PreferenceFragment {
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.other_settings);
    }
}
