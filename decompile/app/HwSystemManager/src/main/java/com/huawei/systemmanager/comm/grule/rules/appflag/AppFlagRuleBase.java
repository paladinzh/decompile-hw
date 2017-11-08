package com.huawei.systemmanager.comm.grule.rules.appflag;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import com.huawei.systemmanager.comm.grule.rules.IRule;
import com.huawei.systemmanager.util.app.HsmPackageManager;

abstract class AppFlagRuleBase implements IRule<String> {
    abstract boolean flagMatch(int i);

    AppFlagRuleBase() {
    }

    public boolean match(Context context, String pkgName) {
        try {
            return flagMatch(HsmPackageManager.getInstance().getPkgInfo(pkgName, 8192).mFlag);
        } catch (NameNotFoundException e) {
            return false;
        }
    }
}
