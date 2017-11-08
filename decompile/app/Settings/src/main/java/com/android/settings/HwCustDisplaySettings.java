package com.android.settings;

import android.content.Context;
import android.support.v7.preference.ListPreference;
import java.util.ArrayList;

public class HwCustDisplaySettings {
    public DisplaySettings mDisplaySettings;

    public HwCustDisplaySettings(DisplaySettings displaySettings) {
        this.mDisplaySettings = displaySettings;
    }

    public void setCurrentScreenOffTimeoutValue() {
    }

    public void changeScreenOffTimeoutArrays(ArrayList<CharSequence> arrayList, ArrayList<CharSequence> arrayList2) {
    }

    public void updateCustPreference(Context context) {
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public int getCustomTimeout(Context context, int value) {
        return value;
    }

    public void addSwitchPreference(Context context, DisplaySettings displaySettings) {
    }

    public String[] getScreenTimeOutValues(String[] screen_timeout_values) {
        return screen_timeout_values;
    }

    public void updateScreenTimeoutPreference(ListPreference mScreenTimeoutPreference) {
    }
}
