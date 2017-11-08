package com.huawei.gallery.app;

import android.content.Context;
import android.content.Intent;

public class HwCustKeyEventImpl extends HwCustKeyEvent {
    public HwCustKeyEventImpl(Context context, Intent intent) {
        super(context, intent);
    }

    protected int getFingerprintLeftKeyCode() {
        return 513;
    }

    protected int getFingerprintRightKeyCode() {
        return 514;
    }
}
