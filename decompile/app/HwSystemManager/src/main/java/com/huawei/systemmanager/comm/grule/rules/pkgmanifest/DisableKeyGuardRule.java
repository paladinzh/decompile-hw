package com.huawei.systemmanager.comm.grule.rules.pkgmanifest;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import com.huawei.systemmanager.comm.grule.rules.IRule;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;

public class DisableKeyGuardRule implements IRule<String> {
    private static final String TAG = DisableKeyGuardRule.class.getSimpleName();

    public boolean match(Context context, String pkgName) {
        try {
            String[] permissionArray = PackageManagerWrapper.getPackageInfo(context.getPackageManager(), pkgName, 4096).requestedPermissions;
            if (!(permissionArray == null || permissionArray.length == 0)) {
                for (String equals : permissionArray) {
                    if (equals.equals("android.permission.DISABLE_KEYGUARD")) {
                        return true;
                    }
                }
            }
        } catch (NameNotFoundException ex) {
            HwLog.e(TAG, "catch NameNotFoundException:" + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex2) {
            HwLog.e(TAG, "catch Exception:" + ex2.getMessage());
            ex2.printStackTrace();
        }
        return false;
    }
}
