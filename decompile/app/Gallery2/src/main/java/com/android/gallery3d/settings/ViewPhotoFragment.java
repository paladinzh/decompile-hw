package com.android.gallery3d.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.android.gallery3d.R;

public class ViewPhotoFragment extends PreferenceFragment {
    private static final String TAG = "View_Settings";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_view);
    }
}
