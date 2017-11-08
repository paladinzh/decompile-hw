package com.huawei.systemmanager.spacecleanner.setting;

import com.huawei.systemmanager.spacecleanner.statistics.SpaceStatsUtils;
import com.huawei.systemmanager.util.HwLog;

public class NotCommonlyUsedSetting extends SpaceSwitchSetting {
    public static final String TAG = "NotCommonlyUsedSetting";

    public NotCommonlyUsedSetting(String key) {
        super(key);
    }

    public void doSwitchOn() {
        HwLog.i(TAG, "NotCommonlyUsedSetting is switch on!");
        SpaceStatsUtils.reportFileAnalysisSwitchOp(1);
    }

    public void doSwitchOff() {
        HwLog.i(TAG, "NotCommonlyUsedSetting is switch off!");
        SpaceStatsUtils.reportFileAnalysisSwitchOp(0);
    }

    public void doAction() {
    }

    public void doCheck() {
    }
}
