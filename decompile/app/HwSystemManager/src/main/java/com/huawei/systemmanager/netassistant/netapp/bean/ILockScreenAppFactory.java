package com.huawei.systemmanager.netassistant.netapp.bean;

import com.huawei.systemmanager.netassistant.traffic.appinfo.NetAppUtils;

public abstract class ILockScreenAppFactory {
    public abstract LockScreenApp create(int i);

    public static final LockScreenApp createLockScreenApp(int uid) {
        if (NetAppUtils.isRemovableUid(uid)) {
            return new InstalledLockScreenAppFactory().create(uid);
        }
        return new SystemLockScreenAppFactory().create(uid);
    }

    public static final LockScreenApp createLockScreenApp(int uid, long wifiTraffic, long mobileTraffic) {
        LockScreenApp lockScreenApp = createLockScreenApp(uid);
        lockScreenApp.mobileTraffic = mobileTraffic;
        lockScreenApp.wifiTraffic = wifiTraffic;
        return lockScreenApp;
    }
}
