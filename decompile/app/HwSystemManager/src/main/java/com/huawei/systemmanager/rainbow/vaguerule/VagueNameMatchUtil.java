package com.huawei.systemmanager.rainbow.vaguerule;

import android.text.TextUtils;

public class VagueNameMatchUtil {
    public static boolean isVaguePkgName(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        return pkgName.contains("*") || pkgName.contains(VagueRegConst.REG_ONE_CHAR) || pkgName.startsWith(VagueRegConst.PTAH_PREFIX) || pkgName.equalsIgnoreCase(VagueRegConst.SYSTEM_FLAG) || pkgName.equalsIgnoreCase(VagueRegConst.REG_DEFAULT);
    }
}
