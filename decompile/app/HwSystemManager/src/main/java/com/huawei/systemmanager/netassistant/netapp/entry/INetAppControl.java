package com.huawei.systemmanager.netassistant.netapp.entry;

public interface INetAppControl {
    void accessMobile();

    void accessWifi();

    void denyMobile();

    void denyWifi();

    boolean isMobileAccess();

    boolean isWifiAccess();
}
