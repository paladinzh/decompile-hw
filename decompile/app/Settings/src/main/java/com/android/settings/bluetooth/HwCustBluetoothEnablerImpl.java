package com.android.settings.bluetooth;

import android.support.v14.preference.SwitchPreference;
import com.android.settings.UtilsCustEx;
import java.io.File;

public class HwCustBluetoothEnablerImpl extends HwCustBluetoothEnabler {
    public static final String DMPROPERTY_BLUETOOTH = "bluetooth.disable";
    public static final String DMPROPERTY_DIRECTORY = "/data/OtaSave/Extensions/";

    public boolean custBluetoothDisable(SwitchPreference bluetoothSwitchPreference) {
        if (!UtilsCustEx.IS_SPRINT || bluetoothSwitchPreference == null || !isBluetoothRestricted()) {
            return false;
        }
        bluetoothSwitchPreference.setChecked(false);
        bluetoothSwitchPreference.setEnabled(false);
        bluetoothSwitchPreference.setOnPreferenceChangeListener(null);
        return true;
    }

    private boolean isBluetoothRestricted() {
        if (new File("/data/OtaSave/Extensions/bluetooth.disable").exists()) {
            return true;
        }
        return false;
    }
}
