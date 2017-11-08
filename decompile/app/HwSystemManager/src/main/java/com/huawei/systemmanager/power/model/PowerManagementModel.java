package com.huawei.systemmanager.power.model;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.Settings.System;
import com.android.internal.os.PowerProfile;
import com.google.android.collect.Maps;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.data.battery.BatteryInfo;
import com.huawei.systemmanager.power.data.devstatus.DevStatusUtil;
import com.huawei.systemmanager.power.data.profile.HwPowerProfile;
import com.huawei.systemmanager.power.data.stats.PowerStatsHelper;
import com.huawei.systemmanager.power.data.stats.UidAndPower;
import com.huawei.systemmanager.power.util.Conversion;
import com.huawei.systemmanager.power.util.SavingSettingUtil;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PowerManagementModel {
    public static final String BENEFIT_BT_KEY = "benefit_bt";
    public static final String BENEFIT_ONE_KEY_OPTIMIZE_KEY = "benefit_one_key_optimize";
    public static final String BENEFIT_POWER_APPS_KEY = "benefit_power_apps";
    public static final String BENEFIT_WLAN_KEY = "benefit_wlan";
    private static final double ONE_MINUTE = 1.0d;
    private static final int ONE_TIME = 1;
    private static final String PENDING_ALARM_APPLIST = "applist";
    private static final String PENDING_ALARM_ENABLE = "enable";
    private static final String PG_PENDING_ALARM_ACTION = "huawei.intent.action.PG_PENDING_ALARM_ACTION";
    private static final double SUPER_SMART_RATIO = 1.66d;
    private static final String TAG = "PowerManagementModel";
    private static final int ZERO_TIME = 0;
    private static PowerManagementModel mInstance = null;
    private Context mContext;
    private HwPowerProfile mHwPowerProfile;
    private boolean mIsOneKeyPowerOptimize = false;
    private double mLastAppWeight = 0.0d;
    private long mLastViewTime = 0;
    private PowerProfile mPowerProfile;
    private PowerStatsHelper mPowerStateHelper;

    private PowerManagementModel(Context context) {
        this.mContext = context;
        this.mHwPowerProfile = new HwPowerProfile(this.mContext);
        this.mPowerProfile = new PowerProfile(this.mContext);
    }

    public static synchronized PowerManagementModel getInstance(Context context) {
        PowerManagementModel powerManagementModel;
        synchronized (PowerManagementModel.class) {
            if (mInstance == null) {
                mInstance = new PowerManagementModel(context);
            }
            powerManagementModel = mInstance;
        }
        return powerManagementModel;
    }

    public PowerManagementModel load() {
        PowerManagementModel powerManagementModel;
        if (this.mPowerStateHelper == null) {
            this.mPowerStateHelper = PowerStatsHelper.newInstance(this.mContext, true);
        }
        try {
            PowerStatsHelper.reloadHelperStats();
        } catch (Exception e) {
            HwLog.e(TAG, "load catch Exception: " + e.getMessage());
            e.printStackTrace();
        }
        synchronized (PowerManagementModel.class) {
            powerManagementModel = mInstance;
        }
        return powerManagementModel;
    }

    public void setAlarmsPending(boolean pending) {
        ArrayList<String> appList = SavingSettingUtil.getWakeupPendingApp(this.mContext);
        HwLog.i(TAG, "setAlarmsPending appList = " + appList + "###pending = " + pending);
        if (!appList.isEmpty()) {
            Intent intent = new Intent(PG_PENDING_ALARM_ACTION);
            intent.setFlags(1073741824);
            intent.putExtra("enable", pending);
            intent.putExtra("applist", appList);
            this.mContext.sendBroadcast(intent, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
        }
    }

    public String[] getTimeForSuperMode(Context context, int level) {
        Map<String, Object> mode_times = getInstance(this.mContext).load().getTimeByCurrentBatteryLevel(context, level);
        int current_mode = System.getInt(context.getContentResolver(), ApplicationConstant.SMART_MODE_STATUS, 1);
        String current_mode_key = ApplicationConstant.SMART_MODE_KEY;
        if (current_mode == 0) {
            current_mode_key = ApplicationConstant.NORMAL_MODE_KEY;
        } else if (current_mode == 2) {
            current_mode_key = ApplicationConstant.ENDURANCE_MODE_KEY;
        }
        int current_time = (int) Double.parseDouble(mode_times.get(current_mode_key).toString());
        HwLog.i(TAG, "super_time: " + ((int) Double.parseDouble(mode_times.get(ApplicationConstant.SUPER_MODE_POWER_KEY).toString())) + "current_time: " + current_time);
        return new String[]{formatTime(current_time), formatTime(super_time - current_time)};
    }

    private String formatTime(int time) {
        if (time < 60) {
            return this.mContext.getResources().getQuantityString(R.plurals.power_time_min_array, time, new Object[]{Integer.valueOf(time)});
        }
        String string = this.mContext.getResources().getString(R.string.power_time_connect);
        Object[] objArr = new Object[2];
        objArr[0] = this.mContext.getResources().getQuantityString(R.plurals.power_time_hour_array, time / 60, new Object[]{Integer.valueOf(time / 60)});
        objArr[1] = this.mContext.getResources().getQuantityString(R.plurals.power_time_min_array, time % 60, new Object[]{Integer.valueOf(time % 60)});
        return String.format(string, objArr);
    }

    public Map<String, Object> getTimeByCurrentBatteryLevel(Context mContext, int batteryLevel) {
        long begin = System.currentTimeMillis();
        Map<String, Object> map = Maps.newHashMap();
        if (batteryLevel <= 0) {
            batteryLevel = BatteryInfo.getBatteryLevelValue();
            HwLog.e(TAG, " getTimeByCurrentBattery the batteryLevel <= 0 , then get the battery level from  BatteryInfo.getBatteryLevelValue() ");
        }
        double batteryCapacity = this.mPowerProfile.getBatteryCapacity();
        double realCapacity = (double) BatteryInfo.getRealCapacity((int) batteryCapacity);
        HwPowerProfile.setScreenTimeScaleByAirplaneMode();
        HwLog.i(TAG, "getTimeByCurrentBatteryLevel realCapacity=" + realCapacity + " ,batteryLevel= " + batteryLevel);
        double electricNoneBase = ((((((((0.0d + this.mHwPowerProfile.gpsPower()) + this.mHwPowerProfile.dataPower()) + this.mHwPowerProfile.getScreenTimeoutPower()) + this.mHwPowerProfile.getSoundWeightedForPower()) + this.mHwPowerProfile.getVibrateWeightedForPower()) + this.mHwPowerProfile.getAutoSyncForPower()) + this.mHwPowerProfile.getAutoRotateWeightedForPower()) + this.mHwPowerProfile.getTouchFeedbackWeightedForPower()) + this.mHwPowerProfile.futureScreenPower();
        double saveModeElectricNoneBase = ((((((0.0d + this.mHwPowerProfile.gpsPower()) + this.mHwPowerProfile.dataPower()) + this.mHwPowerProfile.getSoundWeightedForPower()) + this.mHwPowerProfile.getVibrateWeightedForPower()) + this.mHwPowerProfile.getAutoRotateWeightedForPower()) + this.mHwPowerProfile.futureScreenPower()) + this.mHwPowerProfile.getSaveModelScreenTimeoutPower();
        HwLog.i(TAG, "phase1 electricNoneBase= " + electricNoneBase + " saveModeElectricNoneBase= " + saveModeElectricNoneBase + " ,futureScreenPower= " + this.mHwPowerProfile.futureScreenPower() + " ,futureScreenPower=" + this.mHwPowerProfile.futureScreenPower());
        double wifiPower = 0.0d;
        if (DevStatusUtil.getWifiState(mContext)) {
            wifiPower = this.mPowerProfile.getAveragePower("wifi.on");
            if (wifiPower < 2.0d) {
                wifiPower = 2.0d;
            }
        }
        double bluetoothPower = 0.0d;
        if (DevStatusUtil.getBluetoothState()) {
            bluetoothPower = this.mPowerProfile.getAveragePower("bluetooth.on");
            if (bluetoothPower < 2.0d) {
                bluetoothPower = 2.0d;
            }
        }
        double appPowerWeight = Math.ceil(getBgAppPowerWeight(mContext, this.mPowerStateHelper.getPowerAppList(mContext, false)));
        this.mIsOneKeyPowerOptimize = false;
        double baseCurrentValue = ((electricNoneBase + wifiPower) + bluetoothPower) + appPowerWeight;
        double baseSaveCurrentValue = ((saveModeElectricNoneBase + wifiPower) + bluetoothPower) + appPowerWeight;
        HwLog.i(TAG, "phase2 baseCurrentValue= " + baseCurrentValue + " baseSaveCurrentValue= " + baseSaveCurrentValue + ",wifiPower= " + wifiPower + ",bluepower= " + bluetoothPower + " ,apppower= " + appPowerWeight);
        int dayType = RemainingTimeSceneHelper.isWeekend(System.currentTimeMillis()) ? 1 : 0;
        List<TimeSceneItem> mTimeSceneList = RemainingTimeSceneHelper.getTimeSceneList(dayType);
        HwLog.i(TAG, "phase3 timesceneList size= " + mTimeSceneList.size() + ",baseCurrentValue = " + baseCurrentValue + " ,type= " + dayType);
        double rmCurrTime = Math.ceil(Conversion.calculateRemainTime(batteryCapacity, batteryLevel, baseCurrentValue, mTimeSceneList));
        double rmSaveCurrTime = Math.ceil(Conversion.calculateRemainTime(batteryCapacity, batteryLevel, baseSaveCurrentValue, mTimeSceneList));
        double rmSuperSaveCurrTime = Math.ceil(Conversion.calculateRemainTime(batteryCapacity, batteryLevel, 0.0d, mTimeSceneList));
        double power_profit = SysCoreUtils.getPowerProfit();
        double amoled_profit = SysCoreUtils.getAmoledProfit();
        HwLog.i(TAG, "phase4 rmCurrTime = " + rmCurrTime + " ,rmSaveCurrTime= " + rmSaveCurrTime + " ,rmSuperSaveCurrTime= " + rmSuperSaveCurrTime + " ,power_profit= " + power_profit + " ,amoled_profit= " + amoled_profit);
        double smart_temp = 1.0d * ((rmCurrTime * power_profit) * amoled_profit);
        double save_temp = ((ApplicationConstant.SAVE_MODE_ITEM_RATIO * rmSaveCurrTime) * power_profit) * amoled_profit;
        double super_power_temp = ApplicationConstant.SUPER_SAVE_MODE_TIME_RATIO * rmSuperSaveCurrTime;
        HwLog.i(TAG, "save mode ratio = 1.067,super save ratio = 2.32");
        if (DevStatusUtil.getWifiState(mContext) && DevStatusUtil.isWlanStateOptimize(mContext)) {
            wifiPower = 0.0d;
        }
        if (DevStatusUtil.getBluetoothState() && DevStatusUtil.isBlueToothStateOptimize()) {
            bluetoothPower = 0.0d;
        }
        double tatalPower = (appPowerWeight + wifiPower) + bluetoothPower;
        int changeMode = PowerModeControl.getInstance(mContext).readSaveMode();
        double benefit = 0.0d;
        HwLog.i(TAG, "phase5 smart_temp = " + smart_temp + " ,save_temp= " + save_temp + " ,super_power_temp= " + super_power_temp + " ,tatalPower= " + tatalPower);
        if (changeMode == 4) {
            benefit = (((ApplicationConstant.SAVE_MODE_ITEM_RATIO * (Math.ceil(Conversion.calculateRemainTime(batteryCapacity, batteryLevel, baseSaveCurrentValue - tatalPower, mTimeSceneList)) * power_profit)) * amoled_profit) * power_profit) - save_temp;
        } else if (changeMode == 1) {
            benefit = (((1.0d * (Math.ceil(Conversion.calculateRemainTime(batteryCapacity, batteryLevel, baseCurrentValue - tatalPower, mTimeSceneList)) * power_profit)) * amoled_profit) * power_profit) - smart_temp;
        }
        if (benefit <= 0.0d) {
            map.put(BENEFIT_ONE_KEY_OPTIMIZE_KEY, Integer.valueOf(0));
            map.put(BENEFIT_POWER_APPS_KEY, Integer.valueOf(0));
            map.put(BENEFIT_WLAN_KEY, Integer.valueOf(0));
            map.put(BENEFIT_BT_KEY, Integer.valueOf(0));
        } else {
            int appBenefitResult = computeResult(appPowerWeight, tatalPower, benefit);
            int wifiBenefitResult = computeResult(wifiPower, tatalPower, benefit);
            int btBenefitResult = computeResult(bluetoothPower, tatalPower, benefit);
            HwLog.i(TAG, "phase6 benefit= " + benefit + " appBenefitResult=" + appBenefitResult + " ,wifiBenefitResult=" + wifiBenefitResult + " ,btBenefitResult=" + btBenefitResult);
            map.put(BENEFIT_ONE_KEY_OPTIMIZE_KEY, Double.valueOf(benefit));
            map.put(BENEFIT_POWER_APPS_KEY, Integer.valueOf(appBenefitResult));
            map.put(BENEFIT_WLAN_KEY, Integer.valueOf(wifiBenefitResult));
            map.put(BENEFIT_BT_KEY, Integer.valueOf(btBenefitResult));
        }
        String str = ApplicationConstant.SAVE_MODE_KEY;
        if (save_temp <= 5.0d) {
            save_temp = 5.0d;
        }
        map.put(str, Double.valueOf(save_temp));
        str = ApplicationConstant.SMART_MODE_KEY;
        if (smart_temp <= 5.0d) {
            smart_temp = 5.0d;
        }
        map.put(str, Double.valueOf(smart_temp));
        str = ApplicationConstant.SUPER_MODE_POWER_KEY;
        if (super_power_temp <= 5.0d) {
            super_power_temp = 5.0d;
        }
        map.put(str, Double.valueOf(super_power_temp));
        HwLog.i(TAG, "getTimeByCurrentBatteryLevel  during time(ms)= " + (System.currentTimeMillis() - begin));
        return map;
    }

    public Map<String, Object> getTimeByCurrentBattery(Context mContext, int batteryLevel) {
        Map<String, Object> map = Maps.newHashMap();
        if (batteryLevel <= 0) {
            batteryLevel = BatteryInfo.getBatteryLevelValue();
            HwLog.e(TAG, " getTimeByCurrentBattery the batteryLevel <= 0 , then get the battery level from  BatteryInfo.getBatteryLevelValue() ");
        }
        double realCapacity = (double) BatteryInfo.getRealCapacity((int) this.mPowerProfile.getBatteryCapacity());
        HwPowerProfile.setScreenTimeScaleByAirplaneMode();
        double superElectricNoneBase = ((((0.0d + this.mPowerProfile.getAveragePower("cpu.idle")) + this.mPowerProfile.getAveragePower("radio.on", 0)) + HwPowerProfile.SYSTEM_BASE_SMART_POWER) + this.mHwPowerProfile.getSoundWeightedForPower()) + this.mHwPowerProfile.futureSuperScreenPower();
        double electricNoneBase = ((((((((((0.0d + this.mHwPowerProfile.gpsPower()) + this.mHwPowerProfile.dataPower()) + this.mHwPowerProfile.futureScreenPower()) + this.mPowerProfile.getAveragePower("cpu.idle")) + this.mPowerProfile.getAveragePower("radio.on", 0)) + this.mHwPowerProfile.getScreenTimeoutWeightedForPower()) + this.mHwPowerProfile.getSoundWeightedForPower()) + this.mHwPowerProfile.getVibrateWeightedForPower()) + this.mHwPowerProfile.getAutoSyncForPower()) + this.mHwPowerProfile.getAutoRotateWeightedForPower()) + this.mHwPowerProfile.getTouchFeedbackWeightedForPower();
        HwLog.i(TAG, "superElectricNoneBase= " + superElectricNoneBase + ",electricNoneBase= " + electricNoneBase);
        double cpuPower = this.mHwPowerProfile.futureCpuPower();
        double base_normal = this.mHwPowerProfile.getAveragePower(HwPowerProfile.SYSTEM_BASE_NORMAL) + (ApplicationConstant.NORMAL_MODE_MULTIPLE * cpuPower);
        double base_smart = this.mHwPowerProfile.getAveragePower(HwPowerProfile.SYSTEM_BASE_SMART) + (ApplicationConstant.SMART_MODE_MULTIPLE * cpuPower);
        double base_super = this.mHwPowerProfile.getAveragePower(HwPowerProfile.SYSTEM_BASE_SUPER) + (1.0d * cpuPower);
        double base_super_power = this.mHwPowerProfile.getAveragePower(HwPowerProfile.SYSTEM_BASE_SUPER) + (1.0d * this.mHwPowerProfile.futureSuperCpuPower());
        HwLog.i(TAG, "phase1 base_normal= " + base_normal + ",base_smart= " + base_smart + ",base_super_power= " + base_super_power);
        double power_cpu_awake = this.mPowerProfile.getAveragePower("cpu.awake");
        base_normal = (HwPowerProfile.getScreenTimeScale() * base_normal) + ((1.0d - HwPowerProfile.getScreenTimeScale()) * power_cpu_awake);
        base_smart = (HwPowerProfile.getScreenTimeScale() * base_smart) + ((1.0d - HwPowerProfile.getScreenTimeScale()) * power_cpu_awake);
        base_super = (HwPowerProfile.getScreenTimeScale() * base_super) + ((1.0d - HwPowerProfile.getScreenTimeScale()) * power_cpu_awake);
        base_super_power = (((1.0d - this.mHwPowerProfile.getSuperScreenTimeScale()) - HwPowerProfile.WAKELOCK_SCALE) * base_super_power) + (HwPowerProfile.WAKELOCK_SCALE * power_cpu_awake);
        HwLog.i(TAG, "phase2 base_normal= " + base_normal + ",base_smart= " + base_smart + ",base_super_power= " + base_super_power);
        double wifiPower = 0.0d;
        if (DevStatusUtil.getWifiState(mContext)) {
            wifiPower = this.mPowerProfile.getAveragePower("wifi.on");
            if (wifiPower < 2.0d) {
                wifiPower = 2.0d;
            }
        }
        double bluetoothPower = 0.0d;
        if (DevStatusUtil.getBluetoothState()) {
            bluetoothPower = this.mPowerProfile.getAveragePower("bluetooth.on");
            if (bluetoothPower < 2.0d) {
                bluetoothPower = 2.0d;
            }
        }
        double appPowerWeight = getBgAppPowerWeight(mContext, this.mPowerStateHelper.getPowerAppList(mContext, false));
        this.mIsOneKeyPowerOptimize = false;
        double normal_change = ((electricNoneBase + wifiPower) + bluetoothPower) + appPowerWeight;
        base_normal += normal_change;
        base_smart += normal_change;
        base_super += normal_change;
        base_super_power += superElectricNoneBase;
        HwLog.i(TAG, "normal_change= " + normal_change + " ,wifiPower= " + wifiPower + " ,bluetoothPower= " + bluetoothPower + " ,appPowerWeight= " + appPowerWeight);
        HwLog.i(TAG, "phase3 base_normal= " + base_normal + ",base_smart= " + base_smart + ",base_super_power= " + base_super_power);
        double power_profit = SysCoreUtils.getPowerProfit();
        HwLog.i(TAG, "power_profit====" + power_profit);
        double temp_super_power = (Conversion.calculateTime(realCapacity, batteryLevel, base_super_power) * 60.0d) * power_profit;
        double temp_normal = Math.ceil(Conversion.calculateTime(realCapacity, batteryLevel, base_normal) * 60.0d) * power_profit;
        double temp_smart = Math.ceil(Conversion.calculateTime(realCapacity, batteryLevel, base_smart) * 60.0d) * power_profit;
        double temp_super = ((Conversion.calculateTime(realCapacity, batteryLevel, base_super) * 60.0d) * 1.2d) * power_profit;
        HwLog.i(TAG, " phase4 temp_normal= " + temp_normal + ",temp_smart= " + temp_smart + ",temp_super= " + temp_super + ",temp_super_power= " + temp_super_power);
        if (DevStatusUtil.getWifiState(mContext) && DevStatusUtil.isWlanStateOptimize(mContext)) {
            wifiPower = 0.0d;
        }
        if (DevStatusUtil.getBluetoothState() && DevStatusUtil.isBlueToothStateOptimize()) {
            bluetoothPower = 0.0d;
        }
        double tatalPower = (appPowerWeight + wifiPower) + bluetoothPower;
        double benefit = computeBenefit(Maps.newHashMap(), realCapacity, batteryLevel, tatalPower, base_normal, base_smart, base_super);
        HwLog.i(TAG, "getTimeByCurrentBattery benefit " + benefit);
        if (benefit <= 0.0d) {
            map.put(BENEFIT_ONE_KEY_OPTIMIZE_KEY, Integer.valueOf(0));
            map.put(BENEFIT_POWER_APPS_KEY, Integer.valueOf(0));
            map.put(BENEFIT_WLAN_KEY, Integer.valueOf(0));
            map.put(BENEFIT_BT_KEY, Integer.valueOf(0));
        } else {
            int appBenefitResult = computeResult(appPowerWeight, tatalPower, benefit);
            int wifiBenefitResult = computeResult(wifiPower, tatalPower, benefit);
            int btBenefitResult = computeResult(bluetoothPower, tatalPower, benefit);
            HwLog.i(TAG, "appBenefitResult=" + appBenefitResult + " ,wifiBenefitResult=" + wifiBenefitResult + " ,btBenefitResult=" + btBenefitResult);
            map.put(BENEFIT_ONE_KEY_OPTIMIZE_KEY, Double.valueOf(benefit));
            map.put(BENEFIT_POWER_APPS_KEY, Integer.valueOf(appBenefitResult));
            map.put(BENEFIT_WLAN_KEY, Integer.valueOf(wifiBenefitResult));
            map.put(BENEFIT_BT_KEY, Integer.valueOf(btBenefitResult));
        }
        long drainTime = getBatteryLatestAvgTimePerLevel(mContext, batteryLevel);
        HwLog.i(TAG, "getTimeByCurrentBattery drainTime " + drainTime);
        String str;
        if (drainTime > 0) {
            double base_drain_time = ((double) drainTime) / 60000.0d;
            double smart = (base_drain_time / 2.0d) + temp_smart;
            double normal = (base_drain_time / 2.0d) + temp_normal;
            double endurance = (((base_drain_time / temp_smart) * temp_super) + temp_super) / 2.0d;
            double super_mode = ((SUPER_SMART_RATIO * base_drain_time) + temp_super_power) / 2.0d;
            HwLog.i(TAG, "phase5 normal= " + normal + ",smart= " + smart + ",super_mode= " + super_mode);
            double d;
            if (super_mode < smart || super_mode < normal) {
                HwLog.i(TAG, "super_mode Time below smart or normal Time");
                str = ApplicationConstant.NORMAL_MODE_KEY;
                if (normal <= 5.0d) {
                    d = 5.0d;
                } else {
                    d = normal;
                }
                map.put(str, Double.valueOf(d));
                str = ApplicationConstant.SMART_MODE_KEY;
                if (smart <= 5.0d) {
                    d = 5.0d;
                } else {
                    d = smart;
                }
                map.put(str, Double.valueOf(d));
                str = ApplicationConstant.ENDURANCE_MODE_KEY;
                if (endurance <= 5.0d) {
                    d = 5.0d;
                } else {
                    d = endurance;
                }
                map.put(str, Double.valueOf(d));
                str = ApplicationConstant.SUPER_MODE_POWER_KEY;
                if (super_mode <= 5.0d) {
                    d = 5.0d;
                } else {
                    d = 1.0d + smart;
                }
                map.put(str, Double.valueOf(d));
            } else {
                str = ApplicationConstant.NORMAL_MODE_KEY;
                if (normal <= 5.0d) {
                    d = 5.0d;
                } else {
                    d = normal;
                }
                map.put(str, Double.valueOf(d));
                str = ApplicationConstant.SMART_MODE_KEY;
                if (smart <= 5.0d) {
                    d = 5.0d;
                } else {
                    d = smart;
                }
                map.put(str, Double.valueOf(d));
                str = ApplicationConstant.ENDURANCE_MODE_KEY;
                if (endurance <= 5.0d) {
                    d = 5.0d;
                } else {
                    d = endurance;
                }
                map.put(str, Double.valueOf(d));
                str = ApplicationConstant.SUPER_MODE_POWER_KEY;
                if (super_mode <= 5.0d) {
                    d = 5.0d;
                } else {
                    d = super_mode;
                }
                map.put(str, Double.valueOf(d));
            }
        } else {
            HwLog.i(TAG, "phase6 temp_normal= " + temp_normal + ",temp_smart= " + temp_smart + ",temp_super_power= " + temp_super_power);
            str = ApplicationConstant.NORMAL_MODE_KEY;
            if (temp_normal <= 5.0d) {
                temp_normal = 5.0d;
            }
            map.put(str, Double.valueOf(temp_normal));
            str = ApplicationConstant.SMART_MODE_KEY;
            if (temp_smart <= 5.0d) {
                temp_smart = 5.0d;
            }
            map.put(str, Double.valueOf(temp_smart));
            str = ApplicationConstant.ENDURANCE_MODE_KEY;
            if (temp_super <= 5.0d) {
                temp_super = 5.0d;
            }
            map.put(str, Double.valueOf(temp_super));
            str = ApplicationConstant.SUPER_MODE_POWER_KEY;
            if (temp_super_power <= 5.0d) {
                temp_super_power = 5.0d;
            }
            map.put(str, Double.valueOf(temp_super_power));
        }
        return map;
    }

    private double computeBenefit(Map<String, Object> map, double realCapacity, int batteryLevel, double PowerWeight, double base_normal, double base_smart, double base_super) {
        int changeMode = ChangeMode.getInstance(this.mContext).readSaveMode();
        double baseTime = 0.0d;
        double tempTime = 0.0d;
        if (changeMode == 0) {
            baseTime = Math.ceil(Conversion.calculateTime(realCapacity, batteryLevel, base_normal) * 60.0d);
            tempTime = Math.ceil(Conversion.calculateTime(realCapacity, batteryLevel, base_normal - PowerWeight) * 60.0d);
        } else if (changeMode == 1) {
            baseTime = Math.ceil(Conversion.calculateTime(realCapacity, batteryLevel, base_smart) * 60.0d);
            tempTime = Math.ceil(Conversion.calculateTime(realCapacity, batteryLevel, base_smart - PowerWeight) * 60.0d);
        } else if (changeMode == 2) {
            baseTime = Math.ceil(Conversion.calculateTime(realCapacity, batteryLevel, base_super) * 60.0d);
            tempTime = Math.ceil(Conversion.calculateTime(realCapacity, batteryLevel, base_super - PowerWeight) * 60.0d);
        }
        double benefit = tempTime - baseTime;
        map.put("tempTime", Double.valueOf(tempTime));
        return benefit;
    }

    private int computeResult(double power, double totalPower, double benefit) {
        double appBenefit = (power / totalPower) * benefit;
        if (appBenefit > 2.147483647E9d || power <= 0.0d) {
            return 0;
        }
        if (appBenefit < 1.0d) {
            appBenefit = 1.0d;
        }
        return (int) appBenefit;
    }

    private long getBatteryLatestAvgTimePerLevel(Context mContext, int batteryLevel) {
        long mPerLevel = this.mPowerStateHelper.computeTimePerLevel(mContext);
        if (mPerLevel <= 0 || batteryLevel <= 0) {
            return 0;
        }
        long drainTime = mPerLevel * ((long) batteryLevel);
        HwLog.d(TAG, "getBatteryLatestAvgTimePerLevel drainTime = " + drainTime);
        return drainTime;
    }

    public double getBgAppPowerWeight(Context mContext, List<UidAndPower> list) {
        try {
            if (list.size() == 0) {
                return 0.0d;
            }
            double sumAppPower = 0.0d;
            for (UidAndPower uap : list) {
                sumAppPower += uap.getPower() * 3600.0d;
            }
            double sumAppTime = ((double) this.mPowerStateHelper.computeBatteryRealtimeSinceUnplugged()) / 1000000.0d;
            if (sumAppTime / ((double) list.size()) < 1.0d) {
                HwLog.e(TAG, " getBgAppPowerWeight avgAppTime < 1s ");
                return 1.0d;
            }
            double appAvgElectricVlue = sumAppPower / sumAppTime;
            if (appAvgElectricVlue > 100.0d) {
                appAvgElectricVlue = 100.0d;
            }
            HwLog.i(TAG, " getBgAppPowerWeight  appAvgElectricVlue =" + appAvgElectricVlue + "sumAppPower =  " + sumAppPower + "list.size() = " + list.size() + "sumAppTime = " + sumAppTime);
            if (this.mLastViewTime == 0) {
                this.mLastAppWeight = appAvgElectricVlue;
                this.mLastViewTime = System.currentTimeMillis();
            } else if (list.size() > 0 && ((appAvgElectricVlue > 3.0d || this.mLastAppWeight > 3.0d) && Math.abs(appAvgElectricVlue - this.mLastAppWeight) < 3.0d && !this.mIsOneKeyPowerOptimize)) {
                appAvgElectricVlue = this.mLastAppWeight;
            }
            return appAvgElectricVlue;
        } catch (Exception e) {
            HwLog.e(TAG, "getBgAppPowerWeight catch exception: " + e.getMessage());
            e.printStackTrace();
            return 0.0d;
        }
    }

    public void setPowerModeState(int flag) {
        ChangeMode changeMode = ChangeMode.getInstance(this.mContext);
        if (flag == 0) {
            changeMode.wirteSaveMode(0, 3);
        } else if (flag == 1) {
            changeMode.wirteSaveMode(1, 2);
        } else if (flag == 2) {
            changeMode.wirteSaveMode(2, 1);
        }
    }

    public int getPowerModeState() {
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            return 2;
        }
        return ChangeMode.getInstance(this.mContext).readSaveMode();
    }

    public boolean isPowerModeOptimizeState() {
        int mPowerMode = getPowerModeState();
        if (mPowerMode == 2 || mPowerMode == 1) {
            return true;
        }
        return false;
    }

    public void doOneKeyPowerOptimize(List<UidAndPower> list, int[] items) {
        for (UidAndPower up : list) {
            String[] pkgs = this.mContext.getPackageManager().getPackagesForUid(up.getUid());
            if (!(pkgs == null || pkgs.length == 0)) {
                for (String pkg : pkgs) {
                    killAppsByPackageName(this.mContext, pkg);
                }
            }
        }
        if (items != null && items.length != 0) {
            for (int n : items) {
                if (n == 2) {
                    DevStatusUtil.setWifiState(this.mContext, false);
                } else if (n == 3) {
                    DevStatusUtil.setBluetoothState(false);
                }
            }
            this.mIsOneKeyPowerOptimize = true;
        }
    }

    public void killAppsByPackageName(Context context, String pkg) {
        ((ActivityManager) context.getSystemService("activity")).forceStopPackage(pkg);
        HwLog.d(TAG, "killAppsByPackageName Force stop package: " + pkg);
    }

    public void batchKillApps(List<String> pkgs) {
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        for (String pk : pkgs) {
            activityManager.forceStopPackage(pk);
            HwLog.d(TAG, "batchKillApps Force stop package: " + pk);
        }
    }
}
