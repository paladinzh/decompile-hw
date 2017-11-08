package com.huawei.systemmanager.netassistant.netapp.entry;

import android.graphics.drawable.Drawable;

public interface INetApp {
    Drawable getIcon();

    CharSequence getLabel();

    int getUid();

    boolean isMultiApp();
}
