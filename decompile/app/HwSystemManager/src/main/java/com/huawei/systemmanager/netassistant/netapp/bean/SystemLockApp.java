package com.huawei.systemmanager.netassistant.netapp.bean;

public class SystemLockApp extends LockScreenApp {
    public SystemLockApp(int uid) {
        super(uid);
    }

    public int getAppType() {
        return 1;
    }
}
