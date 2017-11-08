package com.android.huawei.coverscreen;

import huawei.cust.HwCustUtils;

public class HwCustCoverViewManager {
    private static HwCustCoverViewManager mHwCustCoverViewManager = null;

    public static synchronized HwCustCoverViewManager getDefault() {
        HwCustCoverViewManager hwCustCoverViewManager;
        synchronized (HwCustCoverViewManager.class) {
            if (mHwCustCoverViewManager == null) {
                mHwCustCoverViewManager = (HwCustCoverViewManager) HwCustUtils.createObj(HwCustCoverViewManager.class, new Object[0]);
            }
            hwCustCoverViewManager = mHwCustCoverViewManager;
        }
        return hwCustCoverViewManager;
    }
}
