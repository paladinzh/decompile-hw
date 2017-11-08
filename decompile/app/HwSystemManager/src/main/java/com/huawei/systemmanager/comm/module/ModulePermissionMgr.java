package com.huawei.systemmanager.comm.module;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import com.huawei.permissionmanager.ui.MainActivity;
import com.huawei.systemmanager.comm.module.IHsmModule.AbsHsmModule;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;

public class ModulePermissionMgr extends AbsHsmModule {
    public boolean entryEnabled(Context ctx) {
        return CustomizeWrapper.isPermissionEnabled(ctx);
    }

    public Intent getMainEntry(Context ctx) {
        return getIntent(ctx);
    }

    public static Intent getIntent(Context ctx) {
        if (!AbroadUtils.isAbroad()) {
            return new Intent(ctx, MainActivity.class);
        }
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MANAGE_PERMISSIONS");
        try {
            PackageManagerWrapper.getPackageInfo(ctx.getPackageManager(), "com.google.android.packageinstaller", 0);
            intent.setPackage("com.google.android.packageinstaller");
        } catch (NameNotFoundException e) {
            intent.setPackage("com.android.packageinstaller");
        }
        return intent;
    }
}
