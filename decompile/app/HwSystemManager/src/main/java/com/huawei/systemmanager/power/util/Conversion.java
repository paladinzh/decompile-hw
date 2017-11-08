package com.huawei.systemmanager.power.util;

import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.model.RemainingTimeSceneHelper;
import com.huawei.systemmanager.power.model.TimeSceneItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.Calendar;
import java.util.List;

public class Conversion {
    private static final String TAG = Conversion.class.getSimpleName();

    public static double calculateTime(double batteryCapacity, int rawlevel, double currentPower) {
        double ratio;
        if (rawlevel >= 3) {
            ratio = ((double) (rawlevel - 2)) / 100.0d;
        } else {
            ratio = 0.0d;
        }
        return (batteryCapacity / currentPower) * ratio;
    }

    public static double calculateRemainTime(double batteryCapacity, int rawlevel, double baseValue, List<TimeSceneItem> mList) {
        if (mList.size() == 0) {
            return 0.0d;
        }
        double ratio;
        int currNumber = RemainingTimeSceneHelper.getNumberSceneByTimeStamp(System.currentTimeMillis());
        if (rawlevel >= 3) {
            ratio = ((double) (rawlevel - 2)) / 100.0d;
        } else {
            ratio = 0.0d;
        }
        double rmBattery = (batteryCapacity * ratio) * 60.0d;
        HwLog.i(TAG, "calculateRemainTime rmBattery= " + rmBattery + " ,currNumber= " + currNumber + " baseValue=" + baseValue);
        int first = 10 - (Calendar.getInstance().get(12) % 10);
        double tempBase = baseValue;
        if (((TimeSceneItem) mList.get(currNumber)).getCurrentValue() <= RemainingTimeSceneHelper.SLEEP_CURRENT_VALUE) {
            tempBase = 0.0d;
        }
        rmBattery -= (((TimeSceneItem) mList.get(currNumber)).getCurrentValue() + tempBase) * ((double) first);
        double remainTime = 0.0d + ((double) first);
        HwLog.i(TAG, "calculateRemainTime begin: currNumber= " + currNumber + " ,remainTime= " + remainTime + " ,rmBattery= " + rmBattery);
        while (rmBattery > 0.0d) {
            currNumber = (currNumber + 1) % RemainingTimeSceneHelper.TIME_SCENE_NUM_ONE_DAY;
            tempBase = baseValue;
            if (((TimeSceneItem) mList.get(currNumber)).getCurrentValue() <= RemainingTimeSceneHelper.SLEEP_CURRENT_VALUE) {
                tempBase = 0.0d;
            }
            double sceneCap = (((TimeSceneItem) mList.get(currNumber)).getCurrentValue() + tempBase) * 10.0d;
            if (rmBattery < sceneCap) {
                remainTime += (rmBattery / sceneCap) * 10.0d;
                HwLog.i(TAG, "calculateRemainTime end: remainTime= " + remainTime + " ,rmBattery= " + 0.0d + " ,currNumber= " + currNumber);
                break;
            }
            rmBattery -= sceneCap;
            remainTime += 10.0d;
        }
        return remainTime;
    }

    public static String toShortName(String original) {
        if (original == null) {
            return null;
        }
        if (original.contains(".") && original.contains(ApplicationConstant.COM)) {
            return original.substring(original.lastIndexOf(".") + 1);
        }
        return original;
    }

    public static int invertedCompare(double left, double right) {
        if (left < right) {
            return 1;
        }
        if (left > right) {
            return -1;
        }
        return 0;
    }

    public static String formatPower(double power) {
        return String.format("%.2f", new Object[]{Double.valueOf(power)});
    }
}
