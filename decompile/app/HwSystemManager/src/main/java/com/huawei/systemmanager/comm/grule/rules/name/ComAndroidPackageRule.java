package com.huawei.systemmanager.comm.grule.rules.name;

public class ComAndroidPackageRule extends PackageNameRuleBase {
    public static final String ANDROID_PACKAGE_PREFIX = "com.android.";

    boolean nameMatch(String pkgName) {
        return pkgName.startsWith(ANDROID_PACKAGE_PREFIX);
    }
}
