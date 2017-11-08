package com.huawei.systemmanager.netassistant.traffic.datasaver;

import android.graphics.drawable.Drawable;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.traffic.appinfo.SpecialUid;
import com.huawei.systemmanager.util.app.HsmPackageManager;

public class DataSaverEntry {
    boolean isBlackListed;
    public boolean isProtectListed;
    boolean isWhiteListed;
    String pkgName = "";
    String summary = "";
    String title = "";
    int uid;

    public Drawable getIcon() {
        Drawable icon = null;
        if (SpecialUid.isSpecialNameUid(this.uid)) {
            return HsmPackageManager.getInstance().getDefaultIcon();
        }
        String[] pkgs = GlobalContext.getContext().getPackageManager().getPackagesForUid(this.uid);
        if (pkgs != null && pkgs.length > 0) {
            icon = HsmPackageManager.getInstance().getIcon(pkgs[0]);
        }
        if (icon == null) {
            return HsmPackageManager.getInstance().getDefaultIcon();
        }
        return icon;
    }

    public String toString() {
        return "DataSaverEntry{uid=" + this.uid + ", pkgName='" + this.pkgName + '\'' + ", title='" + this.title + '\'' + ", summary='" + this.summary + '\'' + ", isWhiteListed=" + this.isWhiteListed + ", isBlackListed=" + this.isBlackListed + ", isProtectListed=" + this.isProtectListed + '}';
    }
}
