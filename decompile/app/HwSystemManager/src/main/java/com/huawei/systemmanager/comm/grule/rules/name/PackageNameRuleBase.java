package com.huawei.systemmanager.comm.grule.rules.name;

import android.content.Context;
import com.google.common.base.Strings;
import com.huawei.systemmanager.comm.grule.rules.IRule;

abstract class PackageNameRuleBase implements IRule<String> {
    abstract boolean nameMatch(String str);

    PackageNameRuleBase() {
    }

    public boolean match(Context context, String pkgName) {
        if (Strings.isNullOrEmpty(pkgName)) {
            return false;
        }
        return nameMatch(pkgName);
    }
}
