package com.android.settings.deviceinfo;

import android.content.Context;

public class HwCustUsbReceiver {
    public UsbReceiver mUsbReceiver;

    public HwCustUsbReceiver(UsbReceiver usbReceiver) {
        this.mUsbReceiver = usbReceiver;
    }

    public boolean notStartUsbSettings(Context context) {
        return false;
    }
}
