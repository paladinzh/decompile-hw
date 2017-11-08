package com.huawei.systemmanager.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import com.huawei.android.provider.SettingsEx.Systemex;
import com.huawei.systemmanager.comm.misc.GlobalContext;

public class HwCustUtils {
    private static final String CUSTOM_WORKSPACE = Systemex.getString(GlobalContext.getContext().getContentResolver(), "hw_virtual_net_launcher_brand_def");
    private static final String TAG = "HwCustUtils";

    public static boolean isPackageDisabled(Context aContext, String aPackageName) {
        if (!(aContext == null || TextUtils.isEmpty(aPackageName) || TextUtils.isEmpty(CUSTOM_WORKSPACE))) {
            try {
                PackageManager pm = aContext.getPackageManager();
                if (pm != null && 2 == pm.getApplicationEnabledSetting(aPackageName)) {
                    return true;
                }
            } catch (IllegalArgumentException e) {
                HwLog.e(TAG, "IllegalArgumentException,PackageName does not exist :" + aPackageName);
            }
        }
        return false;
    }
}
