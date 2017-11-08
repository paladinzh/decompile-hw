package com.android.settings;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.IPowerManager.Stub;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.widget.SeekBar;
import com.android.settings.BrightnessSeekBarPreference.Callback;

public class BrightnessSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private CustomSwitchPreference mAutoAdjustPreference;
    private ContentObserver mAutoBrightnessAdjObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            if (!BrightnessSettings.this.mHighPrecisionSupported) {
                Log.d("BrightnessFragment", "onBrightnessADJ changed called. current mode is auto mode.");
                BrightnessSettings.this.onBrightnessChanged();
            }
        }
    };
    private ContentObserver mAutoBrightnessObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            if (BrightnessSettings.this.mHighPrecisionSupported && BrightnessSettings.this.isAutoMode() && BrightnessSettings.this.mObserveAutoBrightnessChange) {
                Log.d("BrightnessFragment", "onBrightness changed called. mAutoBrightnessObserver. current mode is auto mode.");
                BrightnessSettings.this.onBrightnessChanged();
            }
        }
    };
    private ContentObserver mBrightnessModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            BrightnessSettings.this.onBrightnessModeChanged();
        }
    };
    private ContentObserver mBrightnessObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            if (BrightnessSettings.this.mObserveMannualBrightnessChange && !BrightnessSettings.this.isAutoMode()) {
                Log.d("BrightnessFragment", "onBrightness changed called. current mode is manual mode.");
                BrightnessSettings.this.onBrightnessChanged();
            }
        }
    };
    Callback mCallback = new Callback() {
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.d("BrightnessFragment", "onStartTrackingTouch called.");
            if (BrightnessSettings.this.mHighPrecisionSupported && BrightnessSettings.this.isAutoMode()) {
                if (BrightnessSettings.this.mHandler != null && BrightnessSettings.this.mHandler.hasMessages(1)) {
                    BrightnessSettings.this.mHandler.removeMessages(1);
                }
                BrightnessSettings.this.mObserveAutoBrightnessChange = false;
            }
            BrightnessSettings.this.mObserveMannualBrightnessChange = false;
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
            BrightnessSettings.this.setBrightness(progress);
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            boolean isAutoMode = BrightnessSettings.this.isAutoMode();
            Log.d("BrightnessFragment", "onStopTrackingTouch called.");
            if (BrightnessSettings.this.mHighPrecisionSupported && isAutoMode) {
                BrightnessSettings.this.saveAutoBrightnessADJIntoDB(Float.valueOf(BrightnessSettings.this.converSeekBarProgressToAutoBrightnessADJ(seekBar.getProgress())));
                if (BrightnessSettings.this.mHandler != null) {
                    BrightnessSettings.this.mHandler.sendMessageDelayed(BrightnessSettings.this.mHandler.obtainMessage(1), 1000);
                }
            } else if (isAutoMode) {
                BrightnessSettings.this.saveAutoBrightnessADJIntoDB(Float.valueOf(BrightnessSettings.this.converSeekBarProgressToAutoBrightnessADJ(seekBar.getProgress())));
            } else {
                BrightnessSettings.this.mObserveMannualBrightnessChange = true;
            }
            BrightnessSettings.this.saveBrightnessIntoDB(BrightnessSettings.this.convertSeekbarProgressToBrightness(seekBar.getProgress()));
            ItemUseStat.getInstance().handleClick(BrightnessSettings.this.getActivity(), 2, "seekbar_preference", seekBar.getProgress());
        }
    };
    private double mCovertFactor;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    BrightnessSettings.this.setAutoBrightnessInHighPrecision(-1);
                    BrightnessSettings.this.mObserveAutoBrightnessChange = true;
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mHighPrecisionSupported = false;
    private int mMaximumBrightness;
    private int mMinimumBrightness;
    private boolean mObserveAutoBrightnessChange = false;
    private boolean mObserveMannualBrightnessChange = false;
    private PowerManager mPowerManager;
    private IPowerManager mPowerManagerInterface;
    private BrightnessSeekBarPreference mSeekBarPreference;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230749);
        this.mPowerManager = (PowerManager) getSystemService("power");
        this.mMinimumBrightness = this.mPowerManager.getMinimumScreenBrightnessSetting();
        this.mMaximumBrightness = this.mPowerManager.getMaximumScreenBrightnessSetting();
        this.mPowerManagerInterface = Stub.asInterface(ServiceManager.getService("power"));
        this.mSeekBarPreference = (BrightnessSeekBarPreference) findPreference("seekbar_preference");
        this.mSeekBarPreference.setCallback(this.mCallback);
        this.mAutoAdjustPreference = (CustomSwitchPreference) findPreference("auto_adjust_preference");
        if (!Utils.isSupportAutoBrightness(getActivity())) {
            removePreference("auto_adjust_preference");
        }
        this.mAutoAdjustPreference.setOnPreferenceChangeListener(this);
        this.mHighPrecisionSupported = isHighPrecisionSupported();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onResume() {
        super.onResume();
        this.mCovertFactor = (double) getConvertFactor();
        setProgress();
        this.mObserveAutoBrightnessChange = isAutoMode();
        this.mObserveMannualBrightnessChange = !this.mObserveAutoBrightnessChange;
        this.mAutoAdjustPreference.setChecked(this.mObserveAutoBrightnessChange);
        registerBrightnessObservers();
    }

    public void onPause() {
        super.onPause();
        unRegisterBrightnessObservers();
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference != this.mAutoAdjustPreference) {
            return false;
        }
        onBrightnessModeChanged(((Boolean) value).booleanValue());
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), this.mAutoAdjustPreference, value);
        return true;
    }

    private int getSeekBarProgress() {
        float percentage;
        if (!isAutoMode()) {
            percentage = convertBrightnessToSeekbarPercentage((float) getBrightnessFromDB());
        } else if (this.mHighPrecisionSupported) {
            int brightness = getAutoBrightnessFromDB();
            percentage = convertBrightnessToSeekbarPercentage((float) brightness);
            Log.d("BrightnessFragment", "current brightness in auto high precision mode is: " + brightness);
        } else {
            percentage = convertAutoBrightnessADJToSeekbarPercentage(getAutoBrightnessADJFromDB());
        }
        Log.d("BrightnessFragment", "current precentage is: " + percentage);
        return Math.round(10000.0f * percentage);
    }

    private void onBrightnessChanged() {
        setProgress();
    }

    private void onBrightnessModeChanged() {
        boolean autoModeOn = isAutoMode();
        this.mObserveAutoBrightnessChange = autoModeOn;
        this.mObserveMannualBrightnessChange = !autoModeOn;
        if (autoModeOn != this.mAutoAdjustPreference.isChecked()) {
            this.mAutoAdjustPreference.setChecked(autoModeOn);
            setProgress();
        }
    }

    private void onBrightnessModeChanged(boolean enable) {
        saveAutoBrightnessMode(enable);
        if (!enable) {
            this.mObserveMannualBrightnessChange = true;
            setBrightness(this.mSeekBarPreference.getProgress());
            saveBrightnessIntoDB(convertSeekbarProgressToBrightness(this.mSeekBarPreference.getProgress()));
            if (this.mHighPrecisionSupported) {
                setAutoBrightnessInHighPrecision(-1);
                saveAutoBrightnessADJIntoDB(Float.valueOf(0.0f));
            }
        } else if (this.mHighPrecisionSupported) {
            this.mObserveAutoBrightnessChange = true;
            setAutoBrightnessInHighPrecision(-1);
            saveAutoBrightnessADJIntoDB(Float.valueOf(0.0f));
        } else {
            this.mObserveAutoBrightnessChange = false;
            setBrightness(this.mSeekBarPreference.getProgress());
            saveAutoBrightnessADJIntoDB(Float.valueOf(converSeekBarProgressToAutoBrightnessADJ(this.mSeekBarPreference.getProgress())));
        }
    }

    private void setProgress() {
        this.mSeekBarPreference.setProgress(getSeekBarProgress());
    }

    private void setBrightness(int progress) {
        Log.d("BrightnessFragment", "user settings progress is: " + progress);
        if (!isAutoMode()) {
            int brightness = convertSeekbarProgressToBrightness(progress);
            try {
                if (this.mPowerManagerInterface != null) {
                    this.mPowerManagerInterface.setTemporaryScreenBrightnessSettingOverride(brightness);
                }
            } catch (RemoteException ex) {
                Log.e("BrightnessFragment", ex.getMessage());
            }
        } else if (this.mHighPrecisionSupported) {
            setAutoBrightnessInHighPrecision(convertSeekbarProgressToBrightness(progress));
        } else {
            float valf = converSeekBarProgressToAutoBrightnessADJ(progress);
            try {
                if (this.mPowerManagerInterface != null) {
                    this.mPowerManagerInterface.setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(valf);
                }
            } catch (RemoteException ex2) {
                Log.e("BrightnessFragment", ex2.getMessage());
            }
        }
    }

    private boolean isAutoMode() {
        boolean z = true;
        try {
            if (1 != System.getInt(getContentResolver(), "screen_brightness_mode")) {
                z = false;
            }
            return z;
        } catch (SettingNotFoundException e) {
            Log.e("BrightnessFragment", e.getMessage());
            return false;
        }
    }

    private void saveAutoBrightnessMode(boolean enable) {
        int i;
        ContentResolver contentResolver = getContentResolver();
        String str = "screen_brightness_mode";
        if (enable) {
            i = 1;
        } else {
            i = 0;
        }
        System.putInt(contentResolver, str, i);
    }

    private float getConvertFactor() {
        float convertFactor;
        if (this.mHighPrecisionSupported) {
            convertFactor = 1.8f;
        } else {
            convertFactor = Global.getFloat(getContentResolver(), "convert_brightnesss_factor", 0.0f);
            if (convertFactor <= 0.0f) {
                convertFactor = 2.7f;
            }
        }
        Log.d("BrightnessFragment", "Conver factor is: " + convertFactor);
        return convertFactor;
    }

    private int getBrightnessFromDB() {
        return System.getInt(getContentResolver(), "screen_brightness", 100);
    }

    private int getAutoBrightnessFromDB() {
        return System.getInt(getContentResolver(), "screen_auto_brightness", 100);
    }

    private float getAutoBrightnessADJFromDB() {
        return System.getFloat(getContentResolver(), "screen_auto_brightness_adj", 0.0f);
    }

    private void saveBrightnessIntoDB(int brightness) {
        System.putInt(getContentResolver(), "screen_brightness", brightness);
    }

    private void saveAutoBrightnessADJIntoDB(Float value) {
        System.putFloat(getContentResolver(), "screen_auto_brightness_adj", value.floatValue());
    }

    private void registerBrightnessObservers() {
        getContentResolver().registerContentObserver(System.getUriFor("screen_brightness_mode"), true, this.mBrightnessModeObserver);
        getContentResolver().registerContentObserver(System.getUriFor("screen_brightness"), true, this.mBrightnessObserver);
        if (this.mHighPrecisionSupported) {
            getContentResolver().registerContentObserver(System.getUriFor("screen_auto_brightness"), true, this.mAutoBrightnessObserver);
        } else {
            getContentResolver().registerContentObserver(System.getUriFor("screen_auto_brightness_adj"), true, this.mAutoBrightnessAdjObserver);
        }
    }

    private void unRegisterBrightnessObservers() {
        getContentResolver().unregisterContentObserver(this.mBrightnessModeObserver);
        getContentResolver().unregisterContentObserver(this.mBrightnessObserver);
        if (this.mHighPrecisionSupported) {
            getContentResolver().unregisterContentObserver(this.mAutoBrightnessObserver);
        } else {
            getContentResolver().unregisterContentObserver(this.mAutoBrightnessAdjObserver);
        }
    }

    private int convertSeekbarProgressToBrightness(int progress) {
        try {
            int brightness = ((Integer) Class.forName("android.os.IPowerManager").getDeclaredMethod("convertSeekbarProgressToBrightness", new Class[]{Integer.TYPE}).invoke(this.mPowerManagerInterface, new Object[]{Integer.valueOf(progress)})).intValue();
            Log.i("BrightnessFragment", "APS brightness progress=" + progress + ",ConvertTobrightness=" + brightness);
            return brightness;
        } catch (RuntimeException e) {
            return convertSeekbarProgressToBrightnessDefault(progress);
        } catch (Exception e2) {
            return convertSeekbarProgressToBrightnessDefault(progress);
        }
    }

    private int convertSeekbarProgressToBrightnessDefault(int progress) {
        int brightness = Math.round((((float) Math.pow((double) (((float) progress) / 10000.0f), this.mCovertFactor)) * ((float) (this.mMaximumBrightness - this.mMinimumBrightness))) + ((float) this.mMinimumBrightness));
        Log.i("BrightnessFragment", "APS brightnessdefault progress=" + progress + ",ConvertTobrightness=" + brightness);
        return brightness;
    }

    private float convertBrightnessToSeekbarPercentage(float brightness) {
        try {
            float percentage = ((Float) Class.forName("android.os.IPowerManager").getDeclaredMethod("convertBrightnessToSeekbarPercentage", new Class[]{Float.TYPE}).invoke(this.mPowerManagerInterface, new Object[]{Float.valueOf(brightness)})).floatValue();
            Log.i("BrightnessFragment", "APS brightness=" + brightness + ",ConvertToPercentage=" + percentage);
            return percentage;
        } catch (RuntimeException e) {
            return convertBrightnessToSeekbarPercentageDefault(brightness);
        } catch (Exception e2) {
            return convertBrightnessToSeekbarPercentageDefault(brightness);
        }
    }

    private float convertBrightnessToSeekbarPercentageDefault(float brightness) {
        float percentage = (float) Math.pow((double) ((brightness - ((float) this.mMinimumBrightness)) / ((float) (this.mMaximumBrightness - this.mMinimumBrightness))), 1.0d / this.mCovertFactor);
        Log.i("BrightnessFragment", "APS brightnessdefault brightness=" + brightness + ",ConvertToPercentage=" + percentage);
        return percentage;
    }

    private float converSeekBarProgressToAutoBrightnessADJ(int progress) {
        if (this.mHighPrecisionSupported) {
            return ((float) convertSeekbarProgressToBrightness(progress)) / ((float) this.mMaximumBrightness);
        }
        return ((((float) progress) * 2.0f) / 10000.0f) - 1.0f;
    }

    private float convertAutoBrightnessADJToSeekbarPercentage(float adj) {
        return (1.0f + adj) / 2.0f;
    }

    private boolean isHighPrecisionSupported() {
        boolean z = false;
        try {
            Class classz = Class.forName("android.os.PowerManager");
            z = ((Boolean) classz.getDeclaredMethod("isHighPrecision", null).invoke(this.mPowerManager, null)).booleanValue();
            Log.d("BrightnessFragment", " isHighPrecision supported: " + z);
            return z;
        } catch (RuntimeException ex) {
            Log.e("BrightnessFragment", ": reflection exception: " + ex.getMessage());
            return z;
        } catch (Exception ex2) {
            ex2.printStackTrace();
            Log.e("BrightnessFragment", "other exception: " + ex2.getMessage());
            return z;
        }
    }

    private void setAutoBrightnessInHighPrecision(int brightness) {
        if (this.mHighPrecisionSupported && this.mPowerManagerInterface != null) {
            try {
                Class classz = Class.forName("android.os.IPowerManager");
                Object instance = this.mPowerManagerInterface;
                classz.getDeclaredMethod("setTemporaryScreenAutoBrightnessSettingOverride", new Class[]{Integer.TYPE}).invoke(instance, new Object[]{Integer.valueOf(brightness)});
                Log.d("BrightnessFragment", " in high precision mode, set auto brightness: " + brightness);
            } catch (RuntimeException ex) {
                Log.e("BrightnessFragment", ": reflection exception: " + ex.getMessage());
            } catch (Exception ex2) {
                ex2.printStackTrace();
                Log.e("BrightnessFragment", "other exception: " + ex2.getMessage());
            }
        }
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
