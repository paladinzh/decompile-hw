package com.huawei.keyguard.amazinglockscreen.data;

public class OwnerInfo {
    private String mOwnerInfo;
    private boolean mShow;

    public OwnerInfo(String text, boolean isShow) {
        this.mOwnerInfo = text;
        this.mShow = isShow;
    }

    public void setText(String text) {
        this.mOwnerInfo = text;
    }

    public String getText() {
        return this.mOwnerInfo;
    }

    public void setShow(boolean isShow) {
        this.mShow = isShow;
    }

    public boolean getShow() {
        return this.mShow;
    }
}
