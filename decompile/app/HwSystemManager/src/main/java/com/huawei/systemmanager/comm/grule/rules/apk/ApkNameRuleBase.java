package com.huawei.systemmanager.comm.grule.rules.apk;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import com.google.common.base.Preconditions;
import com.huawei.systemmanager.comm.grule.rules.IRule;
import com.huawei.systemmanager.util.app.HsmPackageManager;

abstract class ApkNameRuleBase implements IRule<String> {
    abstract boolean nameMatch(String str);

    ApkNameRuleBase() {
    }

    public boolean match(Context context, String pkgName) {
        try {
            return nameMatch((String) Preconditions.checkNotNull(HsmPackageManager.getInstance().getPkgInfo(pkgName, 8192).mFileName));
        } catch (NameNotFoundException e) {
            return false;
        } catch (NullPointerException e2) {
            return false;
        }
    }
}
