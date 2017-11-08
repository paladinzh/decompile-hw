package com.huawei.mms.util;

import com.android.mms.HwCustMmsConfigImpl;

public class HwCustHwSpecialUtilsImpl extends HwCustHwSpecialUtils {
    private static final String TAG = "HwCustHwSpecialUtilsImpl";

    public boolean isNotLimitToRoamingState() {
        return HwCustMmsConfigImpl.isNotLimitToRoamingState();
    }
}
