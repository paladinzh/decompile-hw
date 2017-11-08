package com.huawei.systemmanager.comm.module;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import com.huawei.systemmanager.AppManager.AppMarketActivity;
import com.huawei.systemmanager.comm.module.IHsmModule.AbsHsmModule;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;

public class ModuleAppManager extends AbsHsmModule {
    private static final String MARKET_PACKAGE_NAME = "com.huawei.appmarket";
    private static final int VALID_MARKET_VERSION = 2300;

    public boolean entryEnabled(Context ctx) {
        return checkMarketVersionValid(ctx);
    }

    public Intent getMainEntry(Context ctx) {
        if (checkMarketVersionValid(ctx)) {
            return new Intent(ctx, AppMarketActivity.class);
        }
        return null;
    }

    private static boolean checkMarketVersionValid(Context context) {
        boolean z = false;
        try {
            if (Integer.compare(PackageManagerWrapper.getPackageInfo(context.getPackageManager(), "com.huawei.appmarket", 4160).versionCode, VALID_MARKET_VERSION) >= 0) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            HwLog.w("ModuleAppManager", "com.huawei.appmarket:: NameNotFoundException");
            return false;
        }
    }
}
