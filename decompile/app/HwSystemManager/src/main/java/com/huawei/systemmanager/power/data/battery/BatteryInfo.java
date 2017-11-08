package com.huawei.systemmanager.power.data.battery;

import android.os.SystemProperties;
import com.huawei.systemmanager.util.HwLog;

public class BatteryInfo {
    private static final String BOARD_PLATFORM_TAG = "ro.board.platform";
    private static final String CHARGER_TYPE = "/sys/class/hw_power/charger/charge_data/chargerType";
    private static final String CHIP_PLATFORM_TAG = "ro.config.hw_ChipPlatform";
    private static final String PLATFORM_MTK = "MTK_Platform";
    private static final String PLATFORM_QUALCOMM = "msm";
    private static final String SCP_CHARGER_TYPE = "/sys/class/hw_power/charger/direct_charger/adaptor_detect";
    private static final String TAG = "BatteryInfo";
    private static int mBatteryCapacity = 0;
    private static FileBatteryReader mReader = null;

    public static int getAutoScreenValue() {
        return getFileReader().getRealAutoScreenValue();
    }

    public static int getBatteryCapacity() {
        return getRealCapacity(BatteryConst.DEFAULT_BATTERY_CAPACITY);
    }

    public static int getBatteryCapacityRmValue() {
        return getFileReader().getRealCapabilityRm();
    }

    public static int getBatteryLevelValue() {
        return getFileReader().getRealBatteryLevelValue();
    }

    public static int getRealCapacity(int def) {
        if (mBatteryCapacity != 0 && -1 != mBatteryCapacity) {
            return mBatteryCapacity;
        }
        mBatteryCapacity = getFileReader().getCapability();
        if (-1 == mBatteryCapacity) {
            return def;
        }
        return mBatteryCapacity;
    }

    private static FileBatteryReader getFileReader() {
        if (mReader != null) {
            return mReader;
        }
        if (SystemProperties.get(BOARD_PLATFORM_TAG, "").startsWith(PLATFORM_QUALCOMM)) {
            mReader = new QualcommFileBatteryReader();
        } else {
            String chipPlatform = SystemProperties.get(CHIP_PLATFORM_TAG, "");
            HwLog.i(TAG, " chipPlatform = " + chipPlatform);
            if (chipPlatform.equals(PLATFORM_MTK)) {
                mReader = new MtkFileBatteryReader();
            } else {
                mReader = new HisicFileBatteryReader();
            }
        }
        return mReader;
    }

    public static boolean isHisic() {
        if (SystemProperties.get(BOARD_PLATFORM_TAG, "").startsWith(PLATFORM_QUALCOMM)) {
            return false;
        }
        String chipPlatform = SystemProperties.get(CHIP_PLATFORM_TAG, "");
        HwLog.i(TAG, " chipPlatform = " + chipPlatform);
        if (chipPlatform.equals(PLATFORM_MTK)) {
            return false;
        }
        return true;
    }

    public static int getChargedTypeFromHisic() {
        return DiskFileReader.readFileByInt(CHARGER_TYPE);
    }

    public static int getSCPChargedType() {
        return DiskFileReader.readFileByInt(SCP_CHARGER_TYPE);
    }
}
