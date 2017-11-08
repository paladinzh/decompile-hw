package com.android.systemui.recents;

import android.os.SystemProperties;

public class Constants {
    public static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", "default"));
}
