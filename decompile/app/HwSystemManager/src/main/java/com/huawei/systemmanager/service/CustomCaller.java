package com.huawei.systemmanager.service;

import android.os.Bundle;

public abstract class CustomCaller {
    public static final String KEY_PKG = "pkgName";

    public abstract Bundle call(Bundle bundle);

    public abstract String getMethodName();

    public boolean shouldEnforcePermission() {
        return true;
    }
}
