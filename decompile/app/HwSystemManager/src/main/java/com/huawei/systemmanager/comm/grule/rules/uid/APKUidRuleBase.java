package com.huawei.systemmanager.comm.grule.rules.uid;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import com.huawei.systemmanager.comm.grule.rules.IRule;

abstract class APKUidRuleBase implements IRule<String> {
    abstract boolean uidMatch(int i);

    APKUidRuleBase() {
    }

    public boolean match(Context context, String pkgName) {
        try {
            return uidMatch(context.getPackageManager().getApplicationInfo(pkgName, 8192).uid);
        } catch (NameNotFoundException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }
}
