package com.huawei.systemmanager.optimize.process;

import android.content.Context;
import android.os.SystemProperties;
import com.huawei.systemmanager.util.HwCustUtils;

public class HwCustProtectAppControlImpl extends HwCustProtectAppControl {
    public static final boolean IS_VODAFONE;

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.config.hw_opta", 0) == 2) {
            z = true;
        }
        IS_VODAFONE = z;
    }

    public boolean isCustomizeProtect(Context context, String pkg) {
        if (IS_VODAFONE) {
            return true;
        }
        return false;
    }

    public boolean isDisabledPkgForProtected(Context aContext, String aPackageName) {
        return HwCustUtils.isPackageDisabled(aContext, aPackageName);
    }
}
