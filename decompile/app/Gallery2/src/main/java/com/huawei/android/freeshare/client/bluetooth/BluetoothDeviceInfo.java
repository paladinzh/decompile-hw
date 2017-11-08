package com.huawei.android.freeshare.client.bluetooth;

import com.huawei.android.freeshare.client.device.DeviceInfo;

public class BluetoothDeviceInfo extends DeviceInfo {
    private String mMacAddress;
    private String mName;

    public BluetoothDeviceInfo(String name, String mac) {
        this.mName = name;
        this.mMacAddress = mac;
    }

    public String getName() {
        return this.mName;
    }

    public String getMacAddress() {
        return this.mMacAddress;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public int getDeviceType() {
        return 2;
    }

    public boolean equal(DeviceInfo deviceInfo) {
        if (deviceInfo != null && deviceInfo.getDeviceType() == 2 && this.mMacAddress.equals(deviceInfo.getMacAddress())) {
            return true;
        }
        return false;
    }
}
