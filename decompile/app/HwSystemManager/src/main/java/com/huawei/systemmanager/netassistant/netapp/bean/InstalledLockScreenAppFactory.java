package com.huawei.systemmanager.netassistant.netapp.bean;

public class InstalledLockScreenAppFactory extends ILockScreenAppFactory {
    public LockScreenApp create(int uid) {
        return new InstalledLockApp(uid);
    }
}
