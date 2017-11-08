package com.android.settings.smartcover;

import android.app.ActivityManager;
import android.os.Build.VERSION;
import android.os.SystemProperties;

public class Utils {
    public static final boolean IS_SHOW_SMART_COVER = SystemProperties.getBoolean("ro.config.show_smart_cover", false);

    public static boolean isMonkeyRunning() {
        return ActivityManager.isUserAMonkey();
    }

    public static boolean hasGingerbread() {
        return VERSION.SDK_INT >= 9;
    }

    public static boolean hasHoneycombMR1() {
        return VERSION.SDK_INT >= 12;
    }

    public static boolean hasFroyo() {
        return VERSION.SDK_INT >= 8;
    }
}
