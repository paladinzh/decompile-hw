package com.huawei.gallery.app;

import android.content.Context;
import android.content.Intent;
import com.huawei.gallery.proguard.Keep;

@Keep
public class HwCustKeyEvent {
    public HwCustKeyEvent(Context context, Intent intent) {
    }

    protected int getFingerprintLeftKeyCode() {
        return -1;
    }

    protected int getFingerprintRightKeyCode() {
        return -1;
    }
}
