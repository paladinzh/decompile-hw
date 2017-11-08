package com.huawei.systemmanager.netassistant.netapp.bean;

public class SystemAppInfo extends AbsNetAppInfo {
    public SystemAppInfo(int uid) {
        super(uid);
    }

    public int getUidType() {
        return 1;
    }
}
