package com.android.systemui.qs;

import android.content.Context;

public class HwCustQSTile {
    protected QSTile mParent;

    public HwCustQSTile(QSTile parent) {
        this.mParent = parent;
    }

    public boolean hasCustomForClick() {
        return false;
    }

    public void requestStateClick(Context context, boolean isEnable) {
    }

    public void showNotificationForVowifi(Context context) {
    }
}
