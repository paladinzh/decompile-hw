package com.android.systemui.settings;

import android.content.Context;
import android.os.IPowerManager;
import android.os.IPowerManager.Stub;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.util.Log;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;

class HwBrightnessUtils {
    public static final int INT_BRIGHTNESS_COVER_MODE = SystemProperties.getInt("ro.config.hw_cover_brightness", 60);
    private static final String TAG = HwBrightnessUtils.class.getSimpleName();
    private int mBrightnessInCoverMode;
    private Context mContext;
    private float mConvertFactor = 1.8f;
    private boolean mHighPrecisionSupported = false;
    private int mMaximumBrightness;
    private int mMinimumBrightness;
    private PowerManager mPowerManager;
    private IPowerManager mPowerManagerInterface;

    HwBrightnessUtils() {
    }

    public void init(Context context) {
        this.mContext = context;
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mPowerManagerInterface = Stub.asInterface(ServiceManager.getService("power"));
        this.mMinimumBrightness = this.mPowerManager.getMinimumScreenBrightnessSetting();
        this.mMaximumBrightness = this.mPowerManager.getMaximumScreenBrightnessSetting();
        this.mBrightnessInCoverMode = ((int) (((float) (this.mMaximumBrightness - this.mMinimumBrightness)) * (((float) INT_BRIGHTNESS_COVER_MODE) / 100.0f))) + this.mMinimumBrightness;
        initHighPrecisionSupported();
        initConvertFactor();
    }

    private void initConvertFactor() {
        if (this.mHighPrecisionSupported) {
            this.mConvertFactor = 1.8f;
            return;
        }
        this.mConvertFactor = Global.getFloat(this.mContext.getContentResolver(), "convert_brightnesss_factor", 0.0f);
        if (this.mConvertFactor <= 0.0f) {
            this.mConvertFactor = 2.7f;
        }
    }

    private void initHighPrecisionSupported() {
        try {
            Class<?> classz = Class.forName("android.os.PowerManager");
            this.mHighPrecisionSupported = ((Boolean) classz.getDeclaredMethod("isHighPrecision", null).invoke(this.mPowerManager, null)).booleanValue();
        } catch (RuntimeException ex) {
            Log.e(TAG, ": reflection exception: " + ex.getMessage());
        } catch (Exception ex2) {
            ex2.printStackTrace();
            Log.e(TAG, "other exception: " + ex2.getMessage());
        }
    }

    public boolean isHighPrecisionSupported() {
        return this.mHighPrecisionSupported;
    }

    public int convertSeekbarProgressToBrightness(int progress) {
        try {
            Class classz = Class.forName("android.os.IPowerManager");
            Object instance = this.mPowerManagerInterface;
            int brightness = ((Integer) classz.getDeclaredMethod("convertSeekbarProgressToBrightness", new Class[]{Integer.TYPE}).invoke(instance, new Object[]{Integer.valueOf(progress)})).intValue();
            Log.i(TAG, "APS brightness progress=" + progress + ",ConvertTobrightness=" + brightness);
            return brightness;
        } catch (RuntimeException e) {
            return convertSeekbarProgressToBrightnessDefault(progress);
        } catch (Exception e2) {
            return convertSeekbarProgressToBrightnessDefault(progress);
        }
    }

    private int convertSeekbarProgressToBrightnessDefault(int progress) {
        int brightness = Math.round((((float) Math.pow((double) (((float) progress) / 10000.0f), (double) this.mConvertFactor)) * ((float) (this.mMaximumBrightness - this.mMinimumBrightness))) + ((float) this.mMinimumBrightness));
        Log.i(TAG, "APS brightnessdefault progress=" + progress + ",ConvertTobrightness=" + brightness);
        return brightness;
    }

    public float convertBrightnessToSeekbarPercentage(float brightness) {
        try {
            Class classz = Class.forName("android.os.IPowerManager");
            Object instance = this.mPowerManagerInterface;
            float percentage = ((Float) classz.getDeclaredMethod("convertBrightnessToSeekbarPercentage", new Class[]{Float.TYPE}).invoke(instance, new Object[]{Float.valueOf(brightness)})).floatValue();
            Log.i(TAG, "APS brightness=" + brightness + ",ConvertToPercentage=" + percentage);
            return percentage;
        } catch (RuntimeException e) {
            return convertBrightnessToSeekbarPercentageDefault(brightness);
        } catch (Exception e2) {
            return convertBrightnessToSeekbarPercentageDefault(brightness);
        }
    }

    private float convertBrightnessToSeekbarPercentageDefault(float brightness) {
        float percentage = (float) Math.pow((double) ((brightness - ((float) this.mMinimumBrightness)) / ((float) (this.mMaximumBrightness - this.mMinimumBrightness))), (double) (1.0f / this.mConvertFactor));
        Log.i(TAG, "APS brightnessdefault brightness=" + brightness + ",ConvertToPercentage=" + percentage);
        return percentage;
    }

    public float convertAutoBrightnessADJToSeekbarPercentage(float adj) {
        return (1.0f + adj) / 2.0f;
    }

    public void setAutoBrightnessInHighPrecision(int brightness) {
        if (this.mHighPrecisionSupported && this.mPowerManagerInterface != null) {
            try {
                Class classz = Class.forName("android.os.IPowerManager");
                Object instance = this.mPowerManagerInterface;
                classz.getDeclaredMethod("setTemporaryScreenAutoBrightnessSettingOverride", new Class[]{Integer.TYPE}).invoke(instance, new Object[]{Integer.valueOf(brightness)});
                Log.d(TAG, "in high precision mode, setAutoBrightnessInHighPrecision brightness: " + brightness);
            } catch (RuntimeException ex) {
                Log.e(TAG, ": reflection exception: " + ex.getMessage());
            } catch (Exception ex2) {
                ex2.printStackTrace();
                Log.e(TAG, "other exception: " + ex2.getMessage());
            }
        }
    }

    public float converSeekBarProgressToAutoBrightnessADJ(int progress) {
        if (this.mHighPrecisionSupported) {
            return ((float) convertSeekbarProgressToBrightness(progress)) / ((float) this.mMaximumBrightness);
        }
        return ((((float) progress) * 2.0f) / 10000.0f) - 1.0f;
    }

    public static int getCurrentLevelOfIndicatorView(int progress) {
        if (progress < 0 || progress > 10000) {
            return 0;
        }
        return (progress * 100) / 10000;
    }

    public int getSeekBarProgress(boolean isAutoMode) {
        float percentage;
        int brightness = 100;
        if (!isAutoMode) {
            brightness = getScreenBrightnessFromDB();
            percentage = convertBrightnessToSeekbarPercentage((float) brightness);
        } else if (this.mHighPrecisionSupported) {
            brightness = getAutoBrightnessFromDB();
            percentage = convertBrightnessToSeekbarPercentage((float) brightness);
        } else {
            percentage = convertAutoBrightnessADJToSeekbarPercentage(getAutoBrightnessADJFromDB());
        }
        HwLog.i(TAG, " getSeekBarProgress isAutoMode:" + isAutoMode + " current brightness:" + brightness + " percentage:" + percentage);
        return Math.round(10000.0f * percentage);
    }

    public int getAutoBrightnessFromDB() {
        return System.getIntForUser(this.mContext.getContentResolver(), "screen_auto_brightness", 100, UserSwitchUtils.getCurrentUser());
    }

    public float getAutoBrightnessADJFromDB() {
        return System.getFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", 0.0f, UserSwitchUtils.getCurrentUser());
    }

    public int getScreenBrightnessFromDB() {
        return System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness", 100, UserSwitchUtils.getCurrentUser());
    }

    public void saveAutoBrightnessADJIntoDB(Float value) {
        System.putFloatForUser(this.mContext.getContentResolver(), "screen_auto_brightness_adj", value.floatValue(), UserSwitchUtils.getCurrentUser());
        HwLog.i(TAG, "saveAutoBrightnessADJIntoDB value = " + String.valueOf(value));
    }

    public void saveManualBrightnessIntoDB(int value) {
        System.putIntForUser(this.mContext.getContentResolver(), "screen_brightness", value, UserSwitchUtils.getCurrentUser());
        HwLog.i(TAG, "saveManualBrightnessIntoDB value = " + String.valueOf(value));
    }

    public void saveLastBrightnessProcessToDB(int value) {
        System.putIntForUser(this.mContext.getContentResolver(), "screen_brightness_process_last", value, UserSwitchUtils.getCurrentUser());
        HwLog.i(TAG, "saveLastBrightnessProcessToDB value = " + String.valueOf(value));
    }

    public int getLastBrightnessProcessFromDB() {
        try {
            return System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_process_last", UserSwitchUtils.getCurrentUser());
        } catch (SettingNotFoundException snfe) {
            HwLog.e(TAG, "getLastBrightnessProcessFromDB:: occur exception=" + snfe);
            return this.mMinimumBrightness;
        }
    }

    public void setBrightness(int progress, boolean isAutoMode) {
        HwLog.i(TAG, "setBrightness progress:" + progress + " isAutoMode:" + isAutoMode);
        if (!isAutoMode) {
            setBrightnessByPowerManager(convertSeekbarProgressToBrightness(progress));
        } else if (isHighPrecisionSupported()) {
            setAutoBrightnessInHighPrecision(convertSeekbarProgressToBrightness(progress));
        } else {
            setAutoBrightnessADJByPowerManager(converSeekBarProgressToAutoBrightnessADJ(progress));
        }
    }

    private void setAutoBrightnessADJByPowerManager(float valf) {
        try {
            if (this.mPowerManagerInterface != null) {
                this.mPowerManagerInterface.setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(valf);
            }
        } catch (RemoteException doe) {
            HwLog.e(TAG, "setAutoBrightnessADJByPowerManager occur exception=" + doe);
        } catch (NullPointerException e) {
            HwLog.e(TAG, "setAutoBrightnessADJByPowerManager occur exception=" + e);
        } catch (Exception e2) {
            HwLog.e(TAG, "setAutoBrightnessADJByPowerManager occur exception=" + e2);
        }
    }

    public void setBrightnessByPowerManager(int brightness) {
        try {
            if (this.mPowerManagerInterface != null && this.mPowerManagerInterface.isInteractive()) {
                this.mPowerManagerInterface.setTemporaryScreenBrightnessSettingOverride(brightness);
            }
        } catch (RemoteException doe) {
            HwLog.e(TAG, "setBrightnessByPowerManager occur exception=" + doe);
        } catch (NullPointerException e) {
            HwLog.e(TAG, "setBrightnessByPowerManager occur exception=" + e);
        } catch (Exception e2) {
            HwLog.e(TAG, "setBrightnessByPowerManager occur exception=" + e2);
        }
    }

    public void onBrightnessModeChange(boolean isAutoMode, int seekBarProgress) {
        HwLog.i(TAG, "setBrightnessModeChange isAutoMode:" + isAutoMode + " seekBarProgress:" + seekBarProgress);
        if (!isAutoMode) {
            saveManualBrightnessIntoDB(convertSeekbarProgressToBrightness(seekBarProgress));
        } else if (isHighPrecisionSupported()) {
            setAutoBrightnessInHighPrecision(-1);
            saveAutoBrightnessADJIntoDB(Float.valueOf(0.0f));
        } else {
            saveAutoBrightnessADJIntoDB(Float.valueOf(converSeekBarProgressToAutoBrightnessADJ(seekBarProgress)));
        }
    }

    public void saveAutoBrightnessMode(boolean isAutoMode) {
        System.putIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", isAutoMode ? 1 : 0, UserSwitchUtils.getCurrentUser());
        HwLog.i(TAG, "saveAutoBrightnessMode isAutoMode = " + String.valueOf(isAutoMode));
    }

    public void saveLastAutoBrightnessMode(boolean isAutoMode) {
        System.putIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode_last", isAutoMode ? 1 : 0, UserSwitchUtils.getCurrentUser());
        HwLog.i(TAG, "saveLastAutoBrightnessMode isAutoMode = " + String.valueOf(isAutoMode));
    }

    public boolean isAutoModeOn() {
        boolean z = true;
        try {
            if (System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", UserSwitchUtils.getCurrentUser()) != 1) {
                z = false;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

    public boolean isLastAutoModeOn() {
        boolean z = true;
        try {
            if (1 != System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode_last", UserSwitchUtils.getCurrentUser())) {
                z = false;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

    public void setLastStateValidable(boolean isValid) {
        System.putIntForUser(this.mContext.getContentResolver(), "systemui_brightness_last_valid", isValid ? 1 : 0, UserSwitchUtils.getCurrentUser());
        HwLog.i(TAG, "setLastStateValidable isValid = " + String.valueOf(isValid));
    }

    public boolean isLastStateValid() {
        boolean z = true;
        try {
            if (1 != System.getIntForUser(this.mContext.getContentResolver(), "systemui_brightness_last_valid", UserSwitchUtils.getCurrentUser())) {
                z = false;
            }
            return z;
        } catch (SettingNotFoundException snfe) {
            HwLog.e(TAG, "isLastStateValid:: occur exception=" + snfe);
            return false;
        }
    }

    public int getBrightnessInCoverMode() {
        return this.mBrightnessInCoverMode;
    }
}
