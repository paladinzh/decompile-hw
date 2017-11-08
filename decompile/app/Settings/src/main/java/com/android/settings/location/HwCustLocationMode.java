package com.android.settings.location;

import android.content.Context;
import android.support.v7.preference.Preference;

public class HwCustLocationMode {
    public LocationMode mLocationMode;

    public HwCustLocationMode(LocationMode locationMode) {
        this.mLocationMode = locationMode;
    }

    public void updateCustPreference(Context context) {
    }

    public boolean onPreferenceTreeClick(Preference preference, Context context) {
        return false;
    }

    public void onModeChanged(int mode) {
    }
}
