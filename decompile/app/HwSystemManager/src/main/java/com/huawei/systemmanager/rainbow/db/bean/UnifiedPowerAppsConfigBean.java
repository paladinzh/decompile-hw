package com.huawei.systemmanager.rainbow.db.bean;

import android.database.Cursor;

public class UnifiedPowerAppsConfigBean {
    private String mIsProtected;
    private String mIsShow;
    private String mPackageName;

    public static UnifiedPowerAppsConfigBean fromCursor(Cursor c, int pkgIndex, int showIndex, int protIndex) {
        UnifiedPowerAppsConfigBean result = new UnifiedPowerAppsConfigBean();
        result.mPackageName = c.getString(pkgIndex);
        result.mIsShow = c.getString(showIndex);
        result.mIsProtected = c.getString(protIndex);
        return result;
    }

    public String getPkgName() {
        return this.mPackageName;
    }

    public boolean isShow() {
        return "0".equals(this.mIsShow);
    }

    public boolean isProtected() {
        return "0".equals(this.mIsProtected);
    }

    public String toString() {
        return "[UnifiedPowerAppsConfigBean]mPackageName:" + this.mPackageName + ",mIsShow:" + this.mIsShow + ",mIsProtected:" + this.mIsProtected;
    }
}
