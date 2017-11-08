package com.huawei.systemmanager.power.data.profile;

import android.content.Context;
import android.provider.Settings.System;
import com.android.internal.os.PowerProfile;
import com.huawei.netassistant.analyse.TrafficNotifyAfterLocked;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.power.comm.SharedPrefKeyConst;
import com.huawei.systemmanager.power.data.devstatus.DevStatusUtil;
import com.huawei.systemmanager.power.model.RemainingTimeSceneHelper;
import com.huawei.systemmanager.power.model.UsageStatusHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashMap;

public class HwPowerProfile {
    private static final String DB_SCREEN_AUTO_BRIGHTNESS = "screen_auto_brightness";
    public static final double SCREEN_ON_IDLE_SCALE = 0.5d;
    public static final double SCREEN_SLIDE_SCALE = 0.5d;
    private static double SCREEN_TIME_SCALE = SCREEN_TIME_SCALE_DEFAULT_VALUE;
    public static final double SCREEN_TIME_SCALE_DEFAULT_VALUE = 0.2358d;
    private static final double SUPER_SCREEN_TIME_SCALE = 0.079d;
    public static final String SYSTEM_BASE_NORMAL = "system.base.normal";
    public static final double SYSTEM_BASE_NORMAL_POWER = 30.0d;
    public static final String SYSTEM_BASE_SMART = "system.base.smart";
    public static final double SYSTEM_BASE_SMART_POWER = 15.0d;
    public static final String SYSTEM_BASE_SUPER = "system.base.super";
    public static final double SYSTEM_BASE_SUPER_POWER = 7.0d;
    public static final double WAKELOCK_SCALE = 0.2d;
    static final HashMap<String, Object> sPowerMap = new HashMap();
    private Context mContext;
    private PowerProfile mPowerProfile;

    public HwPowerProfile(Context context) {
        if (sPowerMap.size() == 0) {
            sPowerMap.put(SYSTEM_BASE_NORMAL, Double.valueOf(SYSTEM_BASE_NORMAL_POWER));
            sPowerMap.put(SYSTEM_BASE_SMART, Double.valueOf(SYSTEM_BASE_SMART_POWER));
            sPowerMap.put(SYSTEM_BASE_SUPER, Double.valueOf(SYSTEM_BASE_SUPER_POWER));
        }
        this.mContext = context;
        this.mPowerProfile = new PowerProfile(this.mContext);
    }

    public double getAveragePower(String type) {
        if (!sPowerMap.containsKey(type)) {
            return 0.0d;
        }
        Object data = sPowerMap.get(type);
        if (data instanceof Double[]) {
            return ((Double[]) data)[0].doubleValue();
        }
        return ((Double) sPowerMap.get(type)).doubleValue();
    }

    public double futureScreenPower() {
        return (SCREEN_TIME_SCALE * currentScreenPower()) * 0.5d;
    }

    private int getBrightnessFromDB() {
        return System.getInt(this.mContext.getContentResolver(), "screen_brightness", 44);
    }

    private int getAutoBrightnessFromDB() {
        return System.getInt(this.mContext.getContentResolver(), DB_SCREEN_AUTO_BRIGHTNESS, 44);
    }

    public double currentScreenPower() {
        int nowBrightness;
        if (DevStatusUtil.getScreenAutoBrightnessState(this.mContext)) {
            nowBrightness = getAutoBrightnessFromDB();
            if (nowBrightness == -1) {
                nowBrightness = 44;
            }
        } else {
            nowBrightness = getBrightnessFromDB();
        }
        double result = this.mPowerProfile.getAveragePower("screen.on") + ((((double) (nowBrightness + 0)) / ((double) 255)) * this.mPowerProfile.getAveragePower("screen.full"));
        HwLog.i("HwPowerManagerActivity", "brightniess:" + nowBrightness + ", current screen power:" + result);
        return result;
    }

    public double futureCpuPower() {
        return SCREEN_TIME_SCALE * ((this.mPowerProfile.getAveragePower("cpu.active", 0) * 0.5d) + (0.5d * this.mPowerProfile.getAveragePower("cpu.active", 3)));
    }

    public double gpsPower() {
        return (double) (DevStatusUtil.getGpsState(this.mContext) ? 1 : 0);
    }

    public double dataPower() {
        return (double) (DevStatusUtil.getMobileDataState(this.mContext) ? 10 : 0);
    }

    public double getScreenTimeoutWeightedForPower() {
        long timeout = DevStatusUtil.getScreenTimeoutState(this.mContext);
        if (timeout <= 15000) {
            return SYSTEM_BASE_SMART_POWER;
        }
        if (timeout <= 30000) {
            return RemainingTimeSceneHelper.SLEEP_CURRENT_VALUE;
        }
        if (timeout <= 60000) {
            return 25.0d;
        }
        if (timeout <= 120000) {
            return SYSTEM_BASE_NORMAL_POWER;
        }
        if (timeout <= TrafficNotifyAfterLocked.SCREEN_LOCK_NO_CHEK_DELAY) {
            return 50.0d;
        }
        if (timeout <= 600000) {
            return 80.0d;
        }
        if (timeout <= 1800000) {
            return 120.0d;
        }
        return 150.0d;
    }

    public double getScreenTimeoutPower() {
        double power;
        long timeout = DevStatusUtil.getScreenTimeoutState(this.mContext);
        if (timeout <= 15000) {
            power = SYSTEM_BASE_SMART_POWER;
        } else if (timeout <= 30000) {
            power = RemainingTimeSceneHelper.SLEEP_CURRENT_VALUE;
        } else if (timeout <= 60000) {
            power = 25.0d;
        } else if (timeout <= 120000) {
            power = SYSTEM_BASE_NORMAL_POWER;
        } else if (timeout <= TrafficNotifyAfterLocked.SCREEN_LOCK_NO_CHEK_DELAY) {
            power = 50.0d;
        } else if (timeout <= 600000) {
            power = 80.0d;
        } else if (timeout <= 1800000) {
            power = 120.0d;
        } else {
            power = 150.0d;
        }
        return power - SYSTEM_BASE_SMART_POWER;
    }

    public double getSaveModelScreenTimeoutPower() {
        double power;
        if (DevStatusUtil.getScreenTimeoutState(this.mContext) <= 15000) {
            power = SYSTEM_BASE_SMART_POWER;
        } else {
            power = RemainingTimeSceneHelper.SLEEP_CURRENT_VALUE;
        }
        return power - SYSTEM_BASE_SMART_POWER;
    }

    public double getSoundWeightedForPower() {
        return (double) (DevStatusUtil.getSoundState(this.mContext) ? 2 : 0);
    }

    public double getAutoSyncForPower() {
        return (double) (DevStatusUtil.getAutoSyncState() ? 5 : 0);
    }

    public double getAutoRotateWeightedForPower() {
        return (double) (DevStatusUtil.getAutoRotateState(this.mContext) ? 2 : 0);
    }

    public double getTouchFeedbackWeightedForPower() {
        return (double) (DevStatusUtil.getTouchFeedbackState(this.mContext) ? 5 : 0);
    }

    public double getVibrateWeightedForPower() {
        return (double) (DevStatusUtil.getVibrateState(this.mContext) ? 2 : 0);
    }

    public static void setScreenTimeScaleByAirplaneMode() {
        if (DevStatusUtil.getAirplaneModeState(GlobalContext.getContext())) {
            SCREEN_TIME_SCALE = 0.1d;
        }
    }

    public static double getScreenTimeScale() {
        return Double.parseDouble(SharePrefWrapper.getPrefValue(GlobalContext.getContext(), SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, UsageStatusHelper.isWeekend() ? SharedPrefKeyConst.BASE_SCREEN_STATUS_RATIO_WEEKEND_KEY : SharedPrefKeyConst.BASE_SCREEN_STATUS_RATIO_WORKDAY_KEY, String.valueOf(SCREEN_TIME_SCALE)));
    }

    public static void setScreenTimeScale(double scale) {
        HwLog.i("HwPowerProfile", "change ScreenRatio to " + scale);
        SCREEN_TIME_SCALE = scale;
    }

    public double getSuperScreenTimeScale() {
        return SUPER_SCREEN_TIME_SCALE;
    }

    public double futureSuperCpuPower() {
        return SUPER_SCREEN_TIME_SCALE * ((this.mPowerProfile.getAveragePower("cpu.active", 0) * 0.5d) + (this.mPowerProfile.getAveragePower("cpu.active", 3) * 0.5d));
    }

    public double futureSuperScreenPower() {
        return SUPER_SCREEN_TIME_SCALE * (this.mPowerProfile.getAveragePower("screen.on") + ((((double) 77) / ((double) 255)) * this.mPowerProfile.getAveragePower("screen.full")));
    }
}
