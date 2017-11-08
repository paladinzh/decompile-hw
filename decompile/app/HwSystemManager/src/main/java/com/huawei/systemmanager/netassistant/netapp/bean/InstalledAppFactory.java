package com.huawei.systemmanager.netassistant.netapp.bean;

import com.huawei.systemmanager.netassistant.traffic.appinfo.NetAppUtils;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;

public class InstalledAppFactory implements INetAppFactory {
    public AbsNetAppInfo create(String pkgName, int permCfg) {
        InstalledAppInfo appInfo = null;
        HsmPkgInfo pkgInfo = HsmPackageManager.getInstance().getPkgInfo(pkgName);
        if (pkgInfo != null) {
            if (!NetAppUtils.isRemovableUid(pkgInfo.mUid)) {
                return null;
            }
            appInfo = new InstalledAppInfo(pkgInfo.mUid);
            appInfo.permissionMobile = AbsNetAppInfo.getMobilePermission(permCfg);
            appInfo.permissionWifi = AbsNetAppInfo.getWifiPermission(permCfg);
        }
        return appInfo;
    }

    public int getType() {
        return 0;
    }
}
