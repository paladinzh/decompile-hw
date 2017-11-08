package com.huawei.systemmanager.comm.grule.rules.name;

public class ComHuaweiPackageRule extends PackageNameRuleBase {
    public static final String HUAWEI_PACKAGE_PREFIX = "com.huawei.";

    boolean nameMatch(String pkgName) {
        return pkgName.startsWith(HUAWEI_PACKAGE_PREFIX);
    }
}
