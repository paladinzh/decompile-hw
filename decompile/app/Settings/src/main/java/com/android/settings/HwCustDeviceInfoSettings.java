package com.android.settings;

import android.app.Dialog;
import android.content.Context;

public class HwCustDeviceInfoSettings {
    public static final String KEY_OPERATOR_COUNTRY_INFO = "operator_country";
    public static final String OPERATOR_COUNTRY_FILE_NAME = "data/custom.bin";
    public static final int OPERATOR_COUNTRY_INFO = 1;
    public DeviceInfoSettings mDeviceInfoSettings;

    public HwCustDeviceInfoSettings(DeviceInfoSettings deviceInfoSettings) {
        this.mDeviceInfoSettings = deviceInfoSettings;
    }

    public void updateCustPreference(Context context) {
    }

    public Dialog getOperatorAndCountryDialog() {
        return null;
    }
}
