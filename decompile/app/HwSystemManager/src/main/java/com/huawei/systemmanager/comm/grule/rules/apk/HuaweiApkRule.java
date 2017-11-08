package com.huawei.systemmanager.comm.grule.rules.apk;

public class HuaweiApkRule extends ApkNameRuleBase {
    public static final String HW_APK_PREFIX = "Hw";

    boolean nameMatch(String apkName) {
        return apkName.startsWith(HW_APK_PREFIX);
    }
}
