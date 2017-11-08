package com.huawei.systemmanager.power.ui;

import android.graphics.drawable.Drawable;

class HistoryAppInfo {
    private boolean isShareUidApps;
    private Drawable mIcon;
    private String mLabel;
    private Long mTime;

    HistoryAppInfo() {
    }

    public Drawable getmIcon() {
        return this.mIcon;
    }

    public void setmIcon(Drawable mIcon) {
        this.mIcon = mIcon;
    }

    public String getmLabel() {
        return this.mLabel;
    }

    public void setmLabel(String mLabel) {
        this.mLabel = mLabel;
    }

    public boolean isShareUidApps() {
        return this.isShareUidApps;
    }

    public void setShareUidApps(boolean isShareUidApps) {
        this.isShareUidApps = isShareUidApps;
    }

    public Long getmTime() {
        return this.mTime;
    }

    public void setmTime(Long mTime) {
        this.mTime = mTime;
    }
}
