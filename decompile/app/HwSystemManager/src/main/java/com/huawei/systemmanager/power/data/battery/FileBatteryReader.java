package com.huawei.systemmanager.power.data.battery;

abstract class FileBatteryReader {
    abstract int getBatteryLevelValue();

    abstract int getCapability();

    abstract int getCapabilityRm();

    abstract int getRealAutoScreenValue();

    FileBatteryReader() {
    }

    int getRealCapabilityRm() {
        int out = getCapabilityRm();
        if (-1 == out) {
            return BatteryConst.DEFAULT_BATTERY_CAPACITY;
        }
        return out;
    }

    int getRealBatteryLevelValue() {
        int out = getBatteryLevelValue();
        if (-1 == out) {
            return 100;
        }
        return out;
    }
}
