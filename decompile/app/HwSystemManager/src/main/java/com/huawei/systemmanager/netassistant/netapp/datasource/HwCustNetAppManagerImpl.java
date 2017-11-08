package com.huawei.systemmanager.netassistant.netapp.datasource;

import android.content.Context;
import com.huawei.systemmanager.util.HwCustUtils;

public class HwCustNetAppManagerImpl extends HwCustNetAppManager {
    public boolean isPackageDisabledForNetwork(Context aContext, String aPackageName) {
        return HwCustUtils.isPackageDisabled(aContext, aPackageName);
    }
}
