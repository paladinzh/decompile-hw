package com.huawei.systemmanager.comm.component;

import android.graphics.drawable.Drawable;

public class HsmBean {
    private boolean isRogue;
    private Drawable mAppIcon;
    private String mLabel;
    private String mPkg;

    public String getmPkg() {
        return this.mPkg;
    }

    public void setmPkg(String mPkg) {
        this.mPkg = mPkg;
    }

    public boolean isRogue() {
        return this.isRogue;
    }

    public void setRogue(boolean isRogue) {
        this.isRogue = isRogue;
    }

    public Drawable getmAppIcon() {
        return this.mAppIcon;
    }

    public void setmAppIcon(Drawable mAppIcon) {
        this.mAppIcon = mAppIcon;
    }

    public String getmLabel() {
        return this.mLabel;
    }

    public void setmLabel(String mLabel) {
        this.mLabel = mLabel;
    }
}
