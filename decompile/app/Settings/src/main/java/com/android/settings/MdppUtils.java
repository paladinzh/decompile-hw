package com.android.settings;

import android.os.SystemProperties;

public final class MdppUtils {
    private static final boolean mIsCcModeDisabled = SystemProperties.get("persist.sys.cc_mode", "0").equals("0");

    public static boolean isCcModeDisabled() {
        return mIsCcModeDisabled;
    }
}
