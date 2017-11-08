package com.huawei.systemmanager.comm.component;

import android.graphics.drawable.Drawable;

public interface IAppInfo {
    Drawable getAppIcon();

    String getAppLabel();

    String getPackageName();
}
