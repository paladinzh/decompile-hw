package com.huawei.systemmanager.power.ui;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private Drawable appIcon;
    private String pkgName;

    public Drawable getAppIcon() {
        return this.appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public String getPackageName() {
        return this.pkgName;
    }

    public void setPackgeName(String pkgName) {
        this.pkgName = pkgName;
    }
}
