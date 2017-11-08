package com.android.settings.bluetooth;

public class BluetoothUsbAutoPairUtils {
    private String mAddress;
    private int mStatus;

    public void parseAddress(byte[] buffer) {
        this.mAddress = byteToString(buffer);
        this.mStatus = buffer[9];
        HwLog.i("BTUAutoPairUtils", "checkBuffer: mAddress = " + this.mAddress);
    }

    public void parseStatus(byte[] buffer) {
        this.mStatus = buffer[9];
        HwLog.i("BTUAutoPairUtils", "checkBuffer: status = " + this.mStatus);
    }

    public String getDeviceAddress() {
        return this.mAddress;
    }

    public int getDeviceStatus() {
        return this.mStatus;
    }

    private String byteToString(byte[] buffer) {
        return String.format("%02X:%02X:%02X:%02X:%02X:%02X", new Object[]{Byte.valueOf(buffer[3]), Byte.valueOf(buffer[4]), Byte.valueOf(buffer[5]), Byte.valueOf(buffer[6]), Byte.valueOf(buffer[7]), Byte.valueOf(buffer[8])});
    }
}
