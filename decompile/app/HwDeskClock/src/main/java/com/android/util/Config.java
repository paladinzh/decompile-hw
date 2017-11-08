package com.android.util;

import android.graphics.Color;
import android.os.SystemProperties;

public final class Config {
    public static final int UNSELECT_COLOR = Color.rgb(247, 247, 247);
    private static int exit_count = 0;
    private static int mCurrentTab;
    private static boolean mTouch_mode = false;
    private static boolean misVibratePause = true;

    public static int clockTabIndex() {
        return mCurrentTab;
    }

    public static void setmCurrentTab(int mCurrentTab) {
        mCurrentTab = mCurrentTab;
    }

    public static void updateCurrentTab(int currentTab) {
        mCurrentTab = currentTab;
    }

    public static boolean getTouch_mode() {
        return mTouch_mode;
    }

    public static int getExit_count() {
        return exit_count;
    }

    public static void doSetExit_count(int exit_count) {
        exit_count = exit_count;
    }

    public static void updateVibratePause(boolean state) {
        misVibratePause = state;
    }

    public static boolean istablet() {
        return "tablet".equals(SystemProperties.get("ro.build.characteristics"));
    }
}
