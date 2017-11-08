package com.huawei.notificationmanager.util;

import android.content.Context;
import com.huawei.systemmanager.util.HwCustUtils;

public class HwCustHelperImpl extends HwCustHelper {
    public boolean isPackageDisabledForNoticationCenter(Context aContext, String aPackageName) {
        return HwCustUtils.isPackageDisabled(aContext, aPackageName);
    }
}
