package com.huawei.systemmanager.spacecleanner.setting;

import com.huawei.systemmanager.util.HwLog;

public class OnlyWifiUpdateSetting extends SpaceSwitchSetting {
    private static final String TAG = OnlyWifiUpdateSetting.class.getSimpleName();

    public OnlyWifiUpdateSetting(String key) {
        super(key);
    }

    public void doSwitchOn() {
        HwLog.d(TAG, "wifi only switch on");
    }

    public void doSwitchOff() {
        HwLog.d(TAG, "wifi only switch off");
    }

    public void doAction() {
        HwLog.d(TAG, "wifi only update setting need do nothing");
    }
}
