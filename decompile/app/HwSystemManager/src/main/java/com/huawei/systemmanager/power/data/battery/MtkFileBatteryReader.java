package com.huawei.systemmanager.power.data.battery;

class MtkFileBatteryReader extends FileBatteryReader {
    private static final String AUTO_SCREEN_VALUE_PATH = "/sys/class/leds/lcd-backlight/brightness";
    private static String CAPACITY_LEVEL_VALUE_PATH = "/sys/class/power_supply/battery/capacity";
    private static String CAPACITY_RM_VALUE_PATH = "/sys/class/power_supply/battery/capacity_rm";
    private static String FILEPATHCAPACITY = "/sys/class/power_supply/battery/capacity_fcc";

    MtkFileBatteryReader() {
    }

    int getCapability() {
        return DiskFileReader.readFileByInt(FILEPATHCAPACITY);
    }

    int getCapabilityRm() {
        return DiskFileReader.readFileByInt(CAPACITY_RM_VALUE_PATH);
    }

    int getBatteryLevelValue() {
        return DiskFileReader.readFileByInt(CAPACITY_LEVEL_VALUE_PATH);
    }

    int getRealAutoScreenValue() {
        return DiskFileReader.readFileByInt(AUTO_SCREEN_VALUE_PATH);
    }
}
