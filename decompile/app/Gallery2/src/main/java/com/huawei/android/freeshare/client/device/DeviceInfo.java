package com.huawei.android.freeshare.client.device;

public abstract class DeviceInfo {
    private static int CURRENT_ID = 0;
    private int mID = getAndIncrementID();

    public abstract boolean equal(DeviceInfo deviceInfo);

    public abstract int getDeviceType();

    public abstract String getMacAddress();

    public abstract String getName();

    public abstract void setName(String str);

    public static int getAndIncrementID() {
        int id = CURRENT_ID;
        CURRENT_ID = id + 1;
        return id;
    }
}
