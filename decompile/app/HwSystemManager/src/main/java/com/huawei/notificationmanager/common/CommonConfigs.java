package com.huawei.notificationmanager.common;

import android.content.ContentValues;
import android.text.TextUtils;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPkgUtils;
import java.util.ArrayList;
import java.util.List;

public class CommonConfigs {
    private static final String[] SPECAIL_PACKAGES = new String[]{"com.huawei.hwid", "com.huawei.appmarket", "com.huawei.KoBackup", "com.huawei.hidisk", HsmStatConst.PHONE_SERVICE_PACKAGE_NAME, "com.huawei.gamebox", "com.huawei.hwcloudservice", "com.huawei.android.ds", "com.huawei.hwvideocall", "com.vmall.client", "com.huawei.fans", "com.huawei.dbank.v7", "com.android.easou.search", "com.tencent.mm"};
    private static final int SPECIAL_CONFIG_DEFAULT = 1;
    private static final String TAG = "CommonConfigs";

    public static List<ContentValues> getDefaultConfigForSpecialPackage() {
        List<ContentValues> valueList = new ArrayList();
        for (String cloudPackage : SPECAIL_PACKAGES) {
            if (-1 == HsmPkgUtils.getPackageUid(cloudPackage)) {
                HwLog.d(TAG, "getDefaultConfigForSpecialPackage: package is not installed, " + cloudPackage);
            } else {
                ContentValues packageConfigValues = new ContentValues();
                packageConfigValues.put("packageName", cloudPackage);
                packageConfigValues.put(ConstValues.NOTIFICATION_CFG, Integer.valueOf(1));
                valueList.add(packageConfigValues);
            }
        }
        return valueList;
    }

    public static boolean isSpecialPackage(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        for (String pkg : SPECAIL_PACKAGES) {
            if (pkg.equalsIgnoreCase(pkgName)) {
                return true;
            }
        }
        return false;
    }
}
