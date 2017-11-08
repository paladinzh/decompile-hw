package com.android.settings;

import android.content.Context;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;

public class HwCustTetherSettings {
    public TetherSettings mTetherSettings;

    public HwCustTetherSettings(TetherSettings tetherSettings) {
        this.mTetherSettings = tetherSettings;
    }

    public boolean setBluetoothTetheringVisibility(Context context, boolean isBluetoothTetheringOn) {
        return isBluetoothTetheringOn;
    }

    public void configureDefaultWifiHotspotName(Context context) {
    }

    public boolean hideSettingsUsbTether() {
        return false;
    }

    public void custUsbTetherDisable(TwoStatePreference usbTether, CharSequence summary) {
    }

    public void customizePreferenceScreen(PreferenceScreen prefRoot) {
    }
}
