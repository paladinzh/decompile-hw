package com.huawei.systemmanager.comm.grule.rules.path;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import com.google.common.base.Preconditions;
import com.huawei.systemmanager.comm.grule.rules.IRule;
import com.huawei.systemmanager.util.app.HsmPackageManager;

abstract class APKPathRuleBase implements IRule<String> {
    abstract boolean pathMatch(String str);

    APKPathRuleBase() {
    }

    public boolean match(Context context, String pkgName) {
        try {
            return pathMatch((String) Preconditions.checkNotNull(HsmPackageManager.getInstance().getPkgInfo(pkgName, 8192).mPath));
        } catch (NameNotFoundException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }
}
