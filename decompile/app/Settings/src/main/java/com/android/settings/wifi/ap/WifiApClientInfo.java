package com.android.settings.wifi.ap;

public class WifiApClientInfo {
    private long mConnectedTime;
    private CharSequence mDeviceName;
    private CharSequence mIP;
    private int mId;
    private CharSequence mMAC;

    public WifiApClientInfo() {
        this.mId = -1;
    }

    public WifiApClientInfo(WifiApClientInfo info) {
        this.mId = info.getId();
        this.mDeviceName = info.getDeviceName();
        this.mMAC = info.getMAC();
        this.mIP = info.getIP();
        this.mConnectedTime = info.getConnectedTime();
    }

    public void setId(int id) {
        this.mId = id;
    }

    public void setDeviceName(CharSequence deviceName) {
        this.mDeviceName = deviceName;
    }

    public void setMAC(CharSequence mac) {
        this.mMAC = mac;
    }

    public void setIP(CharSequence ip) {
        this.mIP = ip;
    }

    public void setConnectedTime(long connectedTime) {
        this.mConnectedTime = connectedTime;
    }

    public int getId() {
        return this.mId;
    }

    public CharSequence getDeviceName() {
        return this.mDeviceName;
    }

    public CharSequence getMAC() {
        return this.mMAC;
    }

    public CharSequence getIP() {
        return this.mIP;
    }

    public long getConnectedTime() {
        return this.mConnectedTime;
    }

    public boolean isMACEquals(WifiApClientInfo info) {
        CharSequence mac = info.getMAC();
        if (this.mMAC == null && mac == null) {
            return true;
        }
        if (this.mMAC == null || mac == null || !this.mMAC.toString().equalsIgnoreCase(mac.toString())) {
            return false;
        }
        return true;
    }
}
