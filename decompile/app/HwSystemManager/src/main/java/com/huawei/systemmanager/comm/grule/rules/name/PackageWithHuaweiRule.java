package com.huawei.systemmanager.comm.grule.rules.name;

import com.google.common.base.Ascii;

public class PackageWithHuaweiRule extends PackageNameRuleBase {
    private static final String NAME_KEY_HUAWEI = ".huawei.";
    private static final String NAME_KEY_HW = ".hw";

    boolean nameMatch(String pkgName) {
        String lowerCaseName = Ascii.toLowerCase(pkgName);
        return !lowerCaseName.contains(NAME_KEY_HUAWEI) ? lowerCaseName.contains(NAME_KEY_HW) : true;
    }
}
