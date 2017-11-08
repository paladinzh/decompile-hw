package com.huawei.systemmanager.power.ui;

import android.graphics.drawable.Drawable;
import com.android.internal.os.BatterySipper;
import java.util.Map;

class BackgroundConsumeInfo {
    private Drawable mIcon;
    private boolean mIsChecked;
    private boolean mIsSharedId;
    private String mPkgName;
    private String mPkgTitle;
    private int mPowerLevel;
    private int mRogueType;
    private BatterySipper mSipper;
    private int mUid;
    protected Map<String, Double> procPowerMap;

    BackgroundConsumeInfo() {
    }

    public Drawable getmIcon() {
        return this.mIcon;
    }

    public void setmIcon(Drawable mIcon) {
        this.mIcon = mIcon;
    }

    public String getmPkgTitle() {
        return this.mPkgTitle;
    }

    public void setmPkgTitle(String mPkgTitle) {
        this.mPkgTitle = mPkgTitle;
    }

    public String getmPkgName() {
        return this.mPkgName;
    }

    public void setmPkgName(String mPkgName) {
        this.mPkgName = mPkgName;
    }

    public int getmUid() {
        return this.mUid;
    }

    public void setmUid(int mUid) {
        this.mUid = mUid;
    }

    public int getmPowerLevel() {
        return this.mPowerLevel;
    }

    public void setmPowerLevel(int mPowerLevel) {
        this.mPowerLevel = mPowerLevel;
    }

    public int getmRogueType() {
        return this.mRogueType;
    }

    public void setmRogueType(int mRogueType) {
        this.mRogueType = mRogueType;
    }

    public boolean ismIsChecked() {
        return this.mIsChecked;
    }

    public void setmIsChecked(boolean mIsChecked) {
        this.mIsChecked = mIsChecked;
    }

    public boolean ismIsSharedId() {
        return this.mIsSharedId;
    }

    public void setmIsSharedId(boolean mIsSharedId) {
        this.mIsSharedId = mIsSharedId;
    }

    public BatterySipper getmSipper() {
        return this.mSipper;
    }

    public void setmSipper(BatterySipper mSipper) {
        this.mSipper = mSipper;
    }
}
