package com.android.gallery3d.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.android.gallery3d.R;

public class FreeShareFragment extends PreferenceFragment {
    private static final String TAG = "Settings_FreeShare";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_freeshare);
    }
}
