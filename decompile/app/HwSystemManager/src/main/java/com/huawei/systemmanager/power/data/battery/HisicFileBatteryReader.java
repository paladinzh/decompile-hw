package com.huawei.systemmanager.power.data.battery;

class HisicFileBatteryReader extends FileBatteryReader {
    private static final String AUTO_SCREEN_VALUE_PATH = "/sys/class/leds/lcd_backlight0/brightness";
    private static String CAPACITY_LEVEL_VALUE_PATH = "/sys/class/power_supply/Battery/capacity";
    private static String CAPACITY_RM_VALUE_PATH = "/sys/class/power_supply/Battery/capacity_rm";
    private static String FILEPATHCAPACITY = "/sys/class/power_supply/Battery/charge_full_design";

    HisicFileBatteryReader() {
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
