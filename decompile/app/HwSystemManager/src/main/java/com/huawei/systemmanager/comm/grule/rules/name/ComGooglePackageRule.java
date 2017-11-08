package com.huawei.systemmanager.comm.grule.rules.name;

public class ComGooglePackageRule extends PackageNameRuleBase {
    public static final String GOOGLE_PACKAGE_PREFIX = "com.google.";

    boolean nameMatch(String pkgName) {
        return pkgName.startsWith(GOOGLE_PACKAGE_PREFIX);
    }
}
