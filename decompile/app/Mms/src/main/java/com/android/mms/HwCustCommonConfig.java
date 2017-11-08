package com.android.mms;

import com.huawei.rcs.util.RcsFeatureEnabler;

public class HwCustCommonConfig {
    private static boolean mIsRcsOn = RcsFeatureEnabler.getInstance().isRcsEnabled();

    private HwCustCommonConfig() {
    }

    public static boolean isRCSSwitchOn() {
        return mIsRcsOn;
    }
}
