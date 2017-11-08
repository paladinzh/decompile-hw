package com.huawei.systemmanager.netassistant.netapp.bean;

public class SystemLockScreenAppFactory extends ILockScreenAppFactory {
    public LockScreenApp create(int uid) {
        return new SystemLockApp(uid);
    }
}
