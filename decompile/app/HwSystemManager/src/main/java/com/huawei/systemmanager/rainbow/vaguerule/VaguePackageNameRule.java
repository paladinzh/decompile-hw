package com.huawei.systemmanager.rainbow.vaguerule;

import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import com.huawei.systemmanager.util.app.HsmPackageManager;

public class VaguePackageNameRule {
    public static boolean isRegCommonPackageMatch(String pkgName, String rule) {
        return pkgName.startsWith(rule.substring(0, rule.length() - 1));
    }

    public static boolean isRegOneCharPackageMatch(String pkgName, String rule) {
        int lengthPkgName = pkgName.length();
        if (lengthPkgName == rule.length()) {
            return pkgName.startsWith(rule.substring(0, lengthPkgName - 1));
        }
        return false;
    }

    public static boolean isAppPathMatch(String pkgName, String rule) {
        int lengthRule = rule.length();
        try {
            return HsmPackageManager.getInstance().getPkgInfo(pkgName, 8192).mPath.contains(rule.substring(VagueRegConst.PTAH_PREFIX.length(), lengthRule));
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isRegPathMatch(String pkgName, String rule) {
        int lengthRule = rule.length();
        try {
            return HsmPackageManager.getInstance().getPkgInfo(pkgName, 8192).mPath.contains(rule.substring(VagueRegConst.PTAH_PREFIX.length(), lengthRule - 1));
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isSystemFlagMatch(String pkgName) {
        boolean z = false;
        try {
            if ((HsmPackageManager.getInstance().getPkgInfo(pkgName, 8192).mFlag & 1) != 0) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static boolean matchRule(String pkgName, String rule) {
        if (TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(rule)) {
            return false;
        }
        if (rule.contains(VagueRegConst.REG_ONE_CHAR) && !rule.contains(VagueRegConst.PTAH_PREFIX)) {
            return isRegOneCharPackageMatch(pkgName, rule);
        }
        if (rule.contains("*") && !rule.contains(VagueRegConst.PTAH_PREFIX)) {
            return isRegCommonPackageMatch(pkgName, rule);
        }
        if (rule.contains(VagueRegConst.PTAH_PREFIX) && !rule.contains("*")) {
            return isAppPathMatch(pkgName, rule);
        }
        if (rule.contains("*") && rule.contains(VagueRegConst.PTAH_PREFIX)) {
            return isRegPathMatch(pkgName, rule);
        }
        if (rule.equalsIgnoreCase(VagueRegConst.SYSTEM_FLAG)) {
            return isSystemFlagMatch(pkgName);
        }
        if (rule.equalsIgnoreCase(VagueRegConst.REG_DEFAULT)) {
            return true;
        }
        return false;
    }
}
