package com.android.settings.accessibility;

import android.content.pm.ServiceInfo;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;

public class HwCustAccessibilitySettings {
    public static final String TalkBack_TITLE = "TalkBack";
    public boolean isTalkBackPositiveButtonClicked = false;
    public AccessibilitySettings mAccessibilitySettings;
    public Preference mTalkBackPreferenceScreen = null;

    public HwCustAccessibilitySettings(AccessibilitySettings abs) {
        this.mAccessibilitySettings = abs;
    }

    public void addCustPreferences() {
    }

    public void updateCustPreference() {
    }

    public void factoryReset() {
    }

    public boolean onTalkBackPreferenceClick() {
        return false;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        return false;
    }

    public void custamizeServicePreferences(ServiceInfo serviceInfo, PreferenceCategory servicesCategory, Preference preference) {
    }
}
