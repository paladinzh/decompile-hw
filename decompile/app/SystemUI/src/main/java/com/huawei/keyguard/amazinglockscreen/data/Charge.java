package com.huawei.keyguard.amazinglockscreen.data;

public class Charge {
    private int mBatteryLevel;
    private String mChargePercent;
    private String mChargeText;
    private boolean mShowCharge;

    public Charge(String text, boolean isShow, int level, String chargePercent) {
        this.mChargeText = text;
        this.mShowCharge = isShow;
        this.mBatteryLevel = level;
        this.mChargePercent = chargePercent;
    }

    public void setText(String text) {
        this.mChargeText = text;
    }

    public String getText() {
        return this.mChargeText;
    }

    public void setShow(boolean isShow) {
        this.mShowCharge = isShow;
    }

    public boolean getShow() {
        return this.mShowCharge;
    }

    public void setLevel(int level) {
        this.mBatteryLevel = level;
    }

    public int getLevel() {
        return this.mBatteryLevel;
    }

    public String getPercent() {
        return this.mChargePercent;
    }
}
