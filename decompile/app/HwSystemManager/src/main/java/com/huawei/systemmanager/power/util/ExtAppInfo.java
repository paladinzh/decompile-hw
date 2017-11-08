package com.huawei.systemmanager.power.util;

import android.graphics.drawable.Drawable;

public class ExtAppInfo {
    private Drawable mIcon;
    private boolean mIsShareUid;
    private String mPkgLabel;
    private String mPkgName;
    private int mUid;

    public int getmUid() {
        return this.mUid;
    }

    public void setmUid(int mUid) {
        this.mUid = mUid;
    }

    public Drawable getmIcon() {
        return this.mIcon;
    }

    public void setmIcon(Drawable mIcon) {
        this.mIcon = mIcon;
    }

    public String getmPkgName() {
        return this.mPkgName;
    }

    public void setmPkgName(String mPkgName) {
        this.mPkgName = mPkgName;
    }

    public String getmPkgLabel() {
        return this.mPkgLabel;
    }

    public void setmPkgLabel(String mPkgLabel) {
        this.mPkgLabel = mPkgLabel;
    }

    public boolean ismIsShareUid() {
        return this.mIsShareUid;
    }

    public void setmIsShareUid(boolean mIsShareUid) {
        this.mIsShareUid = mIsShareUid;
    }
}
