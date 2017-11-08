package com.huawei.systemmanager.netassistant.netapp.bean;

import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;

public class SystemAppFactory implements INetAppFactory {
    private static final String TAG = "SystemAppFactory";

    public AbsNetAppInfo create(String pkgName, int permCfg) {
        HsmPkgInfo pkgInfo = HsmPackageManager.getInstance().getPkgInfo(pkgName);
        if (pkgInfo == null) {
            return null;
        }
        SystemAppInfo appInfo = new SystemAppInfo(pkgInfo.mUid);
        appInfo.permissionMobile = AbsNetAppInfo.getMobilePermission(permCfg);
        appInfo.permissionWifi = AbsNetAppInfo.getWifiPermission(permCfg);
        return appInfo;
    }

    public int getType() {
        return 1;
    }
}
