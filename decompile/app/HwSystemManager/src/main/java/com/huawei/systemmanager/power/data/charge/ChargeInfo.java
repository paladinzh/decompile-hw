package com.huawei.systemmanager.power.data.charge;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemClock;
import com.huawei.systemmanager.power.comm.TimeConst;
import com.huawei.systemmanager.power.data.battery.BatteryInfo;
import com.huawei.systemmanager.util.HwLog;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ChargeInfo {
    private static double BATTERY_THRESHOLD_RATIO = 0.85d;
    private static int CHARGER_TYPE_FCP = 4;
    private static int CHARGER_TYPE_SCP = 0;
    private static int CHARGER_TYPE_STANDARD = 3;
    private static double CHARGE_BATTERY_CONSTANT_VOLTAGE = 470.0d;
    private static double CHARGE_BATTERY_PLUGGED_AC = 1000.0d;
    private static double CHARGE_BATTERY_PLUGGED_USB = 500.0d;
    private static String CONNECTED_TIME_AC_SET = "connected_time_ac_set";
    private static String CONNECTED_TIME_USB_SET = "connected_time_usb_set";
    private static double PLUGGED_FCB_CONSTANT_CURRENT = 2700.0d;
    private static double PLUGGED_OTHERS_CONSTANT_CURRENT = 2000.0d;
    private static double PLUGGED_STANDARD_CONSTANT_CURRENT = 1800.0d;
    private static String POWER_CHARGE_TIME = "power_charge_time";
    private static String POWER_LEVEL_CONNECTED_AC_SET = "power_level_connected_ac_set";
    private static String POWER_LEVEL_CONNECTED_USB_SET = "power_level_connected_usb_set";
    private static String POWER_LEVEL_LAST_CAPACITY_RM = "power_level_last_capacity_rm";
    private static double PULGGED_SCP_STEP_ONE_CONSTANT_CURRENT = 3400.0d;
    private static double PULGGED_SCP_STEP_TWO_CONSTANT_CURRENT = 1200.0d;
    private static final String TAG = ChargeInfo.class.getSimpleName();
    private static int VALIDNUM = 10;

    public static void doRecordsChargeInfo(int plugType, int chargeType, Context context) {
        SharedPreferences charge_info = context.getSharedPreferences("charge_info", 0);
        Editor editor = charge_info.edit();
        long nowTimes = SystemClock.elapsedRealtime();
        int capacityRm = BatteryInfo.getBatteryCapacityRmValue();
        if (chargeType == 1) {
            long chargeTime = charge_info.getLong(POWER_CHARGE_TIME, -1);
            HwLog.e(TAG, " doRecordsChargeInfo chargeTime =" + chargeTime + " plugType = " + plugType + " chargeType =" + chargeType);
            if (chargeTime != -1) {
                int lastCapacityRm = charge_info.getInt(POWER_LEVEL_LAST_CAPACITY_RM, -1);
                Set<String> setPower = new HashSet();
                Set<String> setTimes = new HashSet();
                long mTimes = nowTimes - chargeTime;
                if (mTimes <= 0) {
                    HwLog.i(TAG, " doRecordsChargeInfo charge times <= 0,so abandon the record.");
                    return;
                }
                int chargeBattery = capacityRm - lastCapacityRm;
                HwLog.e(TAG, " doRecordsChargeInfo lastCapacityRm =" + lastCapacityRm + " capacityRm = " + capacityRm + " mTimes = " + mTimes + " chargeBattery =" + chargeBattery);
                String tChargeBattery;
                String tmTimes;
                if (plugType == 1) {
                    setPower = charge_info.getStringSet(POWER_LEVEL_CONNECTED_AC_SET, setPower);
                    setTimes = charge_info.getStringSet(CONNECTED_TIME_AC_SET, setTimes);
                    if (setTimes.size() == setPower.size() && setPower.size() > VALIDNUM) {
                        setPower.remove(setPower.iterator().next());
                        setTimes.remove(setTimes.iterator().next());
                    }
                    HwLog.e(TAG, " doRecordsChargeInfo BATTERY_PLUGGED_AC chargeBattery =" + chargeBattery);
                    if (chargeBattery > 100) {
                        tChargeBattery = String.valueOf(chargeBattery);
                        tmTimes = String.valueOf(mTimes);
                        if (!setPower.contains(tChargeBattery) ? setTimes.contains(tmTimes) : true) {
                            HwLog.e(TAG, " doRecordsChargeInfo BATTERY_PLUGGED_AC there are some repeat value");
                        } else {
                            setPower.add(tChargeBattery);
                            editor.putStringSet(POWER_LEVEL_CONNECTED_AC_SET, setPower);
                            HwLog.e(TAG, " doRecordsChargeInfo BATTERY_PLUGGED_AC lastCapacityRm =" + lastCapacityRm + " chargeBattery = " + chargeBattery + " setPower =" + setPower.toString());
                            setTimes.add(tmTimes);
                            editor.putStringSet(CONNECTED_TIME_AC_SET, setTimes);
                            HwLog.e(TAG, " doRecordsChargeInfo BATTERY_PLUGGED_AC chargeTime =" + chargeTime + " mTimes = " + mTimes + " setTimes = " + setTimes.toString());
                        }
                    }
                } else if (plugType == 2 || plugType == 4) {
                    setPower = charge_info.getStringSet(POWER_LEVEL_CONNECTED_USB_SET, setPower);
                    setTimes = charge_info.getStringSet(CONNECTED_TIME_USB_SET, setTimes);
                    if (setTimes.size() == setPower.size() && setPower.size() > VALIDNUM) {
                        setPower.remove(setPower.iterator().next());
                        setTimes.remove(setTimes.iterator().next());
                    }
                    HwLog.e(TAG, " doRecordsChargeInfo BATTERY_PLUGGED_USB chargeBattery =" + chargeBattery);
                    if (chargeBattery > 100) {
                        tChargeBattery = String.valueOf(chargeBattery);
                        tmTimes = String.valueOf(mTimes);
                        if (!setPower.contains(tChargeBattery) ? setTimes.contains(tmTimes) : true) {
                            HwLog.e(TAG, " doRecordsChargeInfo BATTERY_PLUGGED_AC there are some repeat value");
                        } else {
                            setPower.add(tChargeBattery);
                            editor.putStringSet(POWER_LEVEL_CONNECTED_USB_SET, setPower);
                            HwLog.e(TAG, " doRecordsChargeInfo POWER_LEVEL_CONNECTED_USB_SET lastCapacityRm =" + lastCapacityRm + " chargeBattery = " + chargeBattery + " setPower =" + setPower.toString());
                            setTimes.add(tmTimes);
                            editor.putStringSet(CONNECTED_TIME_USB_SET, setTimes);
                            HwLog.e(TAG, " doRecordsChargeInfo POWER_LEVEL_CONNECTED_USB_SET chargeTime =" + chargeTime + " mTimes = " + mTimes + " setTimes =" + setTimes.toString());
                        }
                    }
                }
            } else {
                return;
            }
        } else if (chargeType == 0) {
            editor.putLong(POWER_CHARGE_TIME, nowTimes);
            editor.putInt(POWER_LEVEL_LAST_CAPACITY_RM, capacityRm);
            HwLog.e(TAG, " doRecordsChargeInfo POWER_LEVEL_CONNECTED_USB_SET POWER_CHARGE_TIME =" + nowTimes + " POWER_LEVEL_LAST_CAPACITY_RM = " + capacityRm + " chargeType =" + chargeType);
        }
        editor.commit();
    }

    public static HashMap<String, Integer> getTimeFullyChargedByHisic(Context context, int plugType) {
        HashMap<String, Integer> mChargedTime = new HashMap();
        if (plugType == 1 || plugType == 2) {
            double result = 0.0d;
            double batteryCapacity = (double) BatteryInfo.getBatteryCapacity();
            double batteryCapacityRm = batteryCapacity * (((double) BatteryInfo.getBatteryLevelValue()) / 100.0d);
            double rmValue = batteryCapacity - batteryCapacityRm;
            HwLog.e(TAG, " getTimeFullyChargedByHisic batteryCapacity =" + batteryCapacity + " , batteryCapacityRm= " + batteryCapacityRm + " ,rmValue= " + rmValue);
            if (plugType == 2) {
                result = (rmValue / CHARGE_BATTERY_PLUGGED_USB) * 60.0d;
                mChargedTime.put("threshold_time", Integer.valueOf(-1));
            } else if (plugType == 1) {
                boolean isScpCharge = BatteryInfo.getSCPChargedType() == CHARGER_TYPE_SCP;
                HwLog.i(TAG, "Curr PlugType Info, chargetype = " + BatteryInfo.getChargedTypeFromHisic() + " ,isScpCharge = " + isScpCharge);
                double thresholdValue = batteryCapacity * (1.0d - BATTERY_THRESHOLD_RATIO);
                if (rmValue > thresholdValue) {
                    double tempValue = rmValue - thresholdValue;
                    if (isScpCharge) {
                        result = ((tempValue / PULGGED_SCP_STEP_ONE_CONSTANT_CURRENT) * 60.0d) + ((thresholdValue / PULGGED_SCP_STEP_TWO_CONSTANT_CURRENT) * 60.0d);
                    } else {
                        int rmTime;
                        int chargeType = BatteryInfo.getChargedTypeFromHisic();
                        HwLog.i(TAG, "current plugType = " + chargeType);
                        if (chargeType == CHARGER_TYPE_FCP) {
                            result = ((tempValue / PLUGGED_FCB_CONSTANT_CURRENT) * 60.0d) + ((thresholdValue / CHARGE_BATTERY_CONSTANT_VOLTAGE) * 60.0d);
                            rmTime = (int) ((tempValue / PLUGGED_FCB_CONSTANT_CURRENT) * 60.0d);
                        } else if (chargeType == CHARGER_TYPE_STANDARD) {
                            result = ((tempValue / PLUGGED_STANDARD_CONSTANT_CURRENT) * 60.0d) + ((thresholdValue / CHARGE_BATTERY_CONSTANT_VOLTAGE) * 60.0d);
                            rmTime = (int) ((tempValue / PLUGGED_STANDARD_CONSTANT_CURRENT) * 60.0d);
                        } else {
                            result = ((tempValue / PLUGGED_OTHERS_CONSTANT_CURRENT) * 60.0d) + ((thresholdValue / CHARGE_BATTERY_CONSTANT_VOLTAGE) * 60.0d);
                            rmTime = (int) ((tempValue / PLUGGED_OTHERS_CONSTANT_CURRENT) * 60.0d);
                        }
                        mChargedTime.put("threshold_time", Integer.valueOf(rmTime));
                    }
                } else if (isScpCharge) {
                    result = (rmValue / PULGGED_SCP_STEP_TWO_CONSTANT_CURRENT) * 60.0d;
                    mChargedTime.put("threshold_time", Integer.valueOf(0));
                } else {
                    result = (rmValue / CHARGE_BATTERY_CONSTANT_VOLTAGE) * 60.0d;
                    mChargedTime.put("threshold_time", Integer.valueOf(0));
                }
            } else {
                HwLog.i(TAG, "Others plugType,plugType = " + plugType);
            }
            HwLog.i(TAG, "getTimeFullyChargedByHisic, result= " + result);
            mChargedTime.put("full_time", Integer.valueOf((int) result));
            return mChargedTime;
        }
        mChargedTime.put("full_time", Integer.valueOf(-1));
        mChargedTime.put("threshold_time", Integer.valueOf(-1));
        return mChargedTime;
    }

    public static int getTimeFullyCharged(Context context, int plugType) {
        if (plugType == 1 || plugType == 2) {
            int result = 5;
            SharedPreferences charge_info = context.getSharedPreferences("charge_info", 4);
            double batteryCapacity = (double) BatteryInfo.getBatteryCapacity();
            int batteryCapacityRm = BatteryInfo.getBatteryCapacityRmValue();
            int rmValue = ((int) batteryCapacity) - batteryCapacityRm;
            double battery = batteryCapacity - ((double) batteryCapacityRm);
            int capacity_ac = charge_info.getInt(POWER_LEVEL_LAST_CAPACITY_RM, 0);
            double avgPower = getAvgChargeValue(charge_info, plugType);
            int chargeValue = batteryCapacityRm - capacity_ac;
            if (!(capacity_ac == 0 || chargeValue >= 1 || avgPower == 0.0d)) {
                result = (int) ((battery / (200.0d + avgPower)) * 60.0d);
                HwLog.d(TAG, "getTimeFullyCharged result1 =" + result);
            }
            if (avgPower != 0.0d) {
                HwLog.d(TAG, "getTimeFullyCharged battery =" + battery + " avgPower = " + avgPower);
                result = (int) ((battery / (200.0d + avgPower)) * 60.0d);
            } else if (plugType == 1) {
                result = (int) ((battery / CHARGE_BATTERY_PLUGGED_AC) * 60.0d);
                HwLog.d(TAG, "getTimeFullyCharged result3 =" + result);
            } else if (plugType == 2 || plugType == 4) {
                result = (int) ((battery / CHARGE_BATTERY_PLUGGED_USB) * 60.0d);
                HwLog.d(TAG, "getTimeFullyCharged result4 =" + result);
            }
            if (rmValue != 0 && (result <= 0 || result >= TimeConst.TEN_HOURS)) {
                result = (int) ((((double) rmValue) / ((CHARGE_BATTERY_PLUGGED_AC + CHARGE_BATTERY_PLUGGED_USB) / 2.0d)) * 60.0d);
            } else if (rmValue == 0) {
                int batteryLevel = BatteryInfo.getBatteryLevelValue();
                if (batteryLevel != 100) {
                    result = (int) ((((((double) (100 - batteryLevel)) / 100.0d) * batteryCapacity) / CHARGE_BATTERY_PLUGGED_USB) * 60.0d);
                    if (result <= 5) {
                        result = 5;
                    }
                } else {
                    result = 0;
                }
            }
            HwLog.d(TAG, "getTimeFullyCharged result2 =" + result + " rmValue = " + rmValue);
            if (result <= 5) {
                result = 5;
            }
            return result;
        }
        HwLog.e(TAG, "getTimeFullyCharged plugType !=1 and plugType !=2");
        return -1;
    }

    private static double getAvgChargeValue(SharedPreferences chargePref, int plugType) {
        Set<String> setPower = new HashSet();
        Set<String> setTimes = new HashSet();
        if (plugType == 1) {
            setPower = chargePref.getStringSet(POWER_LEVEL_CONNECTED_AC_SET, setPower);
            setTimes = chargePref.getStringSet(CONNECTED_TIME_AC_SET, setTimes);
            HwLog.d(TAG, "getAvgChargeValue BATTERY_PLUGGED_AC setPower =" + setPower.toString() + " setTimes =" + setTimes.toString());
        } else if (plugType == 2 || plugType == 4) {
            setPower = chargePref.getStringSet(POWER_LEVEL_CONNECTED_USB_SET, setPower);
            setTimes = chargePref.getStringSet(CONNECTED_TIME_USB_SET, setTimes);
            HwLog.d(TAG, "getAvgChargeValue BATTERY_PLUGGED_USB setPower =" + setPower.toString() + " setTimes =" + setTimes.toString());
        }
        int size = setPower.size();
        if (size < VALIDNUM) {
            return 0.0d;
        }
        Object[] powerArray = setPower.toArray();
        Object[] timeArray = setTimes.toArray();
        double sumPower = 0.0d;
        double sumTime = 0.0d;
        for (int i = 0; i < size; i++) {
            sumPower += Double.parseDouble(powerArray[i].toString());
            sumTime += Double.parseDouble(timeArray[i].toString());
        }
        double tmpResult = sumPower / (sumTime / 3600000.0d);
        if (validSampleRecord(tmpResult, plugType)) {
            return formatDouble(tmpResult);
        }
        return 0.0d;
    }

    private static boolean validSampleRecord(double value, int plugType) {
        boolean z = true;
        boolean z2 = false;
        if (1 == plugType) {
            if (CHARGE_BATTERY_PLUGGED_AC <= value || value <= CHARGE_BATTERY_PLUGGED_AC / 2.0d) {
                z = false;
            }
            return z;
        } else if (2 != plugType) {
            return true;
        } else {
            if (CHARGE_BATTERY_PLUGGED_USB > value && value > CHARGE_BATTERY_PLUGGED_USB / 2.0d) {
                z2 = true;
            }
            return z2;
        }
    }

    private static double formatDouble(double param) {
        return new BigDecimal(param).setScale(2, 4).doubleValue();
    }
}
