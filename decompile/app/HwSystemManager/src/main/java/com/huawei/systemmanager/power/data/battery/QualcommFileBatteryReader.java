package com.huawei.systemmanager.power.data.battery;

class QualcommFileBatteryReader extends FileBatteryReader {
    private static final String AUTO_SCREEN_VALUE_PATH = "/sys/class/leds/lcd-backlight/brightness";
    private static String CAPACITY_LEVEL_VALUE_PATH = "/sys/class/power_supply/battery/capacity";
    private static String FILEPATHCAPACITY = "/sys/class/power_supply/battery/charge_full_design";

    QualcommFileBatteryReader() {
    }

    int getRealCapabilityRm() {
        int batteryFull = getCapability();
        if (-1 == batteryFull) {
            batteryFull = BatteryConst.DEFAULT_BATTERY_CAPACITY;
        }
        return (batteryFull * getRealBatteryLevelValue()) / 100;
    }

    int getCapability() {
        int capability = DiskFileReader.readFileByInt(FILEPATHCAPACITY);
        if (-1 != capability) {
            return capability / 1000;
        }
        return capability;
    }

    int getCapabilityRm() {
        return 0;
    }

    int getBatteryLevelValue() {
        return DiskFileReader.readFileByInt(CAPACITY_LEVEL_VALUE_PATH);
    }

    int getRealAutoScreenValue() {
        return DiskFileReader.readFileByInt(AUTO_SCREEN_VALUE_PATH);
    }
}
