package com.huawei.permissionmanager.utils;

import android.os.Bundle;
import com.huawei.systemmanager.service.CustomCaller;

public class PermissionInfoCaller extends CustomCaller {
    private static final String KEY_PERM_VERSION = "perm_version";
    private static final String METHOD_NAME = "PermissionInfoCaller";
    private static final int PERM_VERSION = 1;

    public String getMethodName() {
        return METHOD_NAME;
    }

    public Bundle call(Bundle params) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_PERM_VERSION, 1);
        return bundle;
    }
}
