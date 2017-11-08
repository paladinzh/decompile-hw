package com.huawei.harassmentinterception.strategy;

import com.huawei.systemmanager.util.HwLog;

public class HotlineInterceptionConfigs {
    public static final String NUMBER_START = "9";
    public static final int SIZE_MAX = 9;
    public static final int SIZE_MINI = 5;
    private static final String TAG = "HotlineInterceptionConfigs";
    private static boolean enable = true;

    public static boolean isHotlineNumberWithoutAreaCodeFuzzyMatchEnable() {
        HwLog.i(TAG, "enabled ? = " + enable);
        return enable;
    }
}
