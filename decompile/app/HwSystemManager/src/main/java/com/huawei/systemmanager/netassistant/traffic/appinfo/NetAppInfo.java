package com.huawei.systemmanager.netassistant.traffic.appinfo;

import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.app.HsmPackageManager;

public class NetAppInfo {
    public boolean isMultiPkg = false;
    public String mAppLabel;
    public int mUid;

    protected NetAppInfo() {
    }

    public static NetAppInfo buildInfo(int uid) {
        return AppLabelByUidFilter.buildUidDetail(uid, new NetAppInfo());
    }

    public Drawable getIcon() {
        Drawable icon = null;
        if (SpecialUid.isSpecialNameUid(this.mUid)) {
            return HsmPackageManager.getInstance().getDefaultIcon();
        }
        String[] pkgs = GlobalContext.getContext().getPackageManager().getPackagesForUid(this.mUid);
        if (pkgs != null && pkgs.length > 0) {
            icon = HsmPackageManager.getInstance().getIcon(pkgs[0]);
        }
        if (icon == null) {
            return HsmPackageManager.getInstance().getDefaultIcon();
        }
        return icon;
    }
}
