package com.huawei.rcs.util;

import android.os.SystemProperties;

public class HwRcsFeatureEnabler {
    private static final String HW_RCS_PRODUCT_ENABLED = "ro.config.hw_rcs_product";
    private static final String HW_RCS_VENDOR_ENABLED = "ro.config.hw_rcs_vendor";
    private static boolean mRcsProductEnabled = SystemProperties.getBoolean(HW_RCS_PRODUCT_ENABLED, false);
    private static boolean mRcsVendorEnabled = SystemProperties.getBoolean(HW_RCS_VENDOR_ENABLED, false);

    private HwRcsFeatureEnabler() {
    }

    public static boolean isRcsEnabled() {
        return mRcsProductEnabled ? mRcsVendorEnabled : false;
    }
}
