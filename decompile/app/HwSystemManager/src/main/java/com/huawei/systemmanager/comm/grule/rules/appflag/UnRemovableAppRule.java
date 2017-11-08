package com.huawei.systemmanager.comm.grule.rules.appflag;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;

public class UnRemovableAppRule extends SystemFlagRule {
    public static final String TAG = "UnRemovableAppRule";

    public boolean match(Context context, String pkgName) {
        try {
            HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(pkgName, 8192);
            boolean isSystemApp = flagMatch(info.mFlag);
            boolean isRemovable = info.isRemoveAblePreInstall();
            if (!isSystemApp || isRemovable) {
                return false;
            }
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
}
