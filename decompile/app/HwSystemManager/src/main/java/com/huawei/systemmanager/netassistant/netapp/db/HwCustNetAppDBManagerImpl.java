package com.huawei.systemmanager.netassistant.netapp.db;

import android.content.Context;
import com.huawei.systemmanager.util.HwCustUtils;

public class HwCustNetAppDBManagerImpl extends HwCustNetAppDBManager {
    public boolean isPackageDisabledForNetwork(Context aContext, String aPackageName) {
        return HwCustUtils.isPackageDisabled(aContext, aPackageName);
    }
}
