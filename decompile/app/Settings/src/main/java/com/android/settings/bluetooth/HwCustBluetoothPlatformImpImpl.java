package com.android.settings.bluetooth;

import android.os.SystemProperties;

public class HwCustBluetoothPlatformImpImpl extends HwCustBluetoothPlatformImp {
    public boolean getBluetoothDiscoverable() {
        return SystemProperties.getBoolean("ro.config.bt_discovery_default", true);
    }
}
