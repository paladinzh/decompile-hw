package com.android.settings;

import android.content.Context;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.location.RadioButtonPreference;
import java.util.ArrayList;

public class HwCustVirtualKeySettings {
    public VirtualKeySettings mVirtualKeySettings;

    public HwCustVirtualKeySettings(VirtualKeySettings virtualKeySettings) {
        this.mVirtualKeySettings = virtualKeySettings;
    }

    public void addScreenLockPreference(Context context, int virtualKeyType, ArrayList<RadioButtonPreference> arrayList, PreferenceScreen root) {
    }

    public void addVirtualKeyPreference(Context context, int virtualKeyType, ArrayList<RadioListPreference> arrayList, PreferenceScreen root) {
    }

    public boolean handleCustItemUseStatClick(int index) {
        return false;
    }

    public boolean isShowNavigationBarSwitch() {
        return false;
    }

    public boolean isShowNavigationBarFootView() {
        return false;
    }

    public void initVirtualKeyPositionPreferences(OnPreferenceChangeListener mPreferenceChangedListener) {
    }

    public SettingNavigationBarPositionPreference getTextRadioPreference() {
        return null;
    }
}
