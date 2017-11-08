package com.huawei.rcs.util;

import android.os.SystemProperties;

public class RcsFeatureEnabler {
    private static RcsFeatureEnabler sInstance;
    private boolean mRcsProductEnabled;
    private boolean mRcsVendorEnabled;

    public static synchronized RcsFeatureEnabler getInstance() {
        RcsFeatureEnabler rcsFeatureEnabler;
        synchronized (RcsFeatureEnabler.class) {
            if (sInstance == null) {
                sInstance = new RcsFeatureEnabler();
            }
            rcsFeatureEnabler = sInstance;
        }
        return rcsFeatureEnabler;
    }

    private RcsFeatureEnabler() {
        this.mRcsProductEnabled = false;
        this.mRcsVendorEnabled = false;
        this.mRcsProductEnabled = SystemProperties.getBoolean("ro.config.hw_rcs_product", false);
        this.mRcsVendorEnabled = SystemProperties.getBoolean("ro.config.hw_rcs_vendor", false);
        MLog.d("RcsFeatureEnabler", "rcsProductEnabled: " + this.mRcsProductEnabled + " rcsVendorEnabled: " + this.mRcsVendorEnabled);
    }

    public boolean isRcsEnabled() {
        if ("true".equals(RcsXmlParser.getValueByNameFromXml("huawei_rcs_enabler")) && this.mRcsProductEnabled) {
            return this.mRcsVendorEnabled;
        }
        return false;
    }

    public boolean isRcsPropertiesConfigOn() {
        return this.mRcsProductEnabled ? this.mRcsVendorEnabled : false;
    }
}
