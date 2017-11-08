package com.android.gallery3d.settings;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.android.gallery3d.R;

@TargetApi(11)
public class DisplayFragment extends PreferenceFragment {
    private static final String TAG = "Settings_Display";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_display);
        if (!(GallerySettings.SUPPORTED_MENU.get(GallerySettings.KEY_DISPLAY_TIME_INFO) == null || ((Boolean) GallerySettings.SUPPORTED_MENU.get(GallerySettings.KEY_DISPLAY_TIME_INFO)).booleanValue())) {
            getPreferenceScreen().removePreference(findPreference(GallerySettings.KEY_DISPLAY_TIME_INFO));
        }
        if (GallerySettings.SUPPORTED_MENU.get(GallerySettings.KEY_DISPLAY_LOCATION_INFO) != null && !((Boolean) GallerySettings.SUPPORTED_MENU.get(GallerySettings.KEY_DISPLAY_LOCATION_INFO)).booleanValue()) {
            getPreferenceScreen().removePreference(findPreference(GallerySettings.KEY_DISPLAY_LOCATION_INFO));
        }
    }
}
