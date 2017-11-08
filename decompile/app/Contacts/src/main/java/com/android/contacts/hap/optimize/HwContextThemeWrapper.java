package com.android.contacts.hap.optimize;

import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;

public class HwContextThemeWrapper extends ContextThemeWrapper {
    public HwContextThemeWrapper(Context base, int theme) {
        super(base, theme);
    }

    public void startActivity(Intent intent) {
        intent.addFlags(268435456);
        super.startActivity(intent);
    }
}
