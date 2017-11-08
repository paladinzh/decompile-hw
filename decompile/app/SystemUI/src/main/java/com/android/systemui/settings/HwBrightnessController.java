package com.android.systemui.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.System;
import com.android.systemui.settings.ToggleSlider.Listener;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;
import com.android.systemui.utils.UserSwitchUtils.UserSwitchedListener;
import com.android.systemui.utils.analyze.BDReporter;

public class HwBrightnessController implements Listener, UserSwitchedListener {
    private boolean mAutomatic;
    private final BrightnessObserver mBrightnessObserver;
    private final Context mContext;
    private final Handler mHandler;
    HwBrightnessUtils mHwBrightnessUtils;
    private boolean mIsObserveAutoBrightnessChange;
    private boolean mListening;
    private Handler mResetHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwLog.i("HwBrightnessController", "mResetHandler setAutoBrightnessInHighPrecision(-1)");
                    HwBrightnessController.this.mHwBrightnessUtils.setAutoBrightnessInHighPrecision(-1);
                    HwBrightnessController.this.mIsObserveAutoBrightnessChange = true;
                    return;
                default:
                    return;
            }
        }
    };
    private final ToggleSlider mToggleSlider;

    private class BrightnessObserver extends ContentObserver {
        private final Uri BRIGHTNESS_ADJ_URI = System.getUriFor("screen_auto_brightness_adj");
        private final Uri BRIGHTNESS_MODE_URI = System.getUriFor("screen_brightness_mode");
        private final Uri BRIGHTNESS_URI = System.getUriFor("screen_brightness");
        private final Uri HIGH_PRECISION_AUTO_BRIGHTNESS = System.getUriFor("screen_auto_brightness");

        public BrightnessObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            HwLog.i("HwBrightnessController", "onChange selfChange:" + selfChange + " uri.toString():" + (uri == null ? "null" : uri.toString()) + " mIsObserveAutoBrightnessChange:" + HwBrightnessController.this.mIsObserveAutoBrightnessChange);
            if (!selfChange) {
                if (this.BRIGHTNESS_MODE_URI.equals(uri)) {
                    HwBrightnessController.this.updateAutoCheckbox();
                    HwBrightnessController.this.updateSlider();
                } else if (this.BRIGHTNESS_URI.equals(uri)) {
                    HwBrightnessController.this.updateSlider();
                } else if (this.HIGH_PRECISION_AUTO_BRIGHTNESS.equals(uri)) {
                    if (HwBrightnessController.this.mAutomatic && HwBrightnessController.this.mIsObserveAutoBrightnessChange) {
                        HwBrightnessController.this.updateSlider();
                    }
                } else if (this.BRIGHTNESS_ADJ_URI.equals(uri) && !HwBrightnessController.this.mHwBrightnessUtils.isHighPrecisionSupported()) {
                    HwBrightnessController.this.updateSlider();
                }
            }
        }

        public void startObserving() {
            ContentResolver cr = HwBrightnessController.this.mContext.getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(this.BRIGHTNESS_MODE_URI, false, this, -1);
            cr.registerContentObserver(this.BRIGHTNESS_URI, false, this, -1);
            if (HwBrightnessController.this.mHwBrightnessUtils.isHighPrecisionSupported()) {
                cr.registerContentObserver(this.HIGH_PRECISION_AUTO_BRIGHTNESS, false, this, -1);
            } else {
                cr.registerContentObserver(this.BRIGHTNESS_ADJ_URI, false, this, -1);
            }
        }

        public void stopObserving() {
            HwBrightnessController.this.mContext.getContentResolver().unregisterContentObserver(this);
        }
    }

    public HwBrightnessController(Context context, ToggleSlider toggleSlider) {
        this.mToggleSlider = toggleSlider;
        this.mContext = context;
        this.mHandler = new Handler();
        this.mHwBrightnessUtils = new HwBrightnessUtils();
        this.mHwBrightnessUtils.init(context);
        this.mBrightnessObserver = new BrightnessObserver(this.mHandler);
    }

    public void onInit(ToggleSlider toggleSlider) {
        toggleSlider.setMax(10000);
        updateAutoCheckbox();
        this.mIsObserveAutoBrightnessChange = this.mAutomatic;
        updateSlider();
        HwLog.i("HwBrightnessController", "onInit mAutomatic:" + this.mAutomatic);
    }

    public void onChanged(ToggleSlider v, boolean tracking, boolean checked, int value, boolean stopTracking) {
        HwLog.i("HwBrightnessController", "onChanged tracking:" + tracking + " checked:" + checked + " value:" + value + " stopTracking:" + stopTracking + " mAutomatic:" + this.mAutomatic);
        if (this.mAutomatic != checked) {
            BDReporter.e(this.mContext, 27, "checked:" + checked);
            if (checked) {
                this.mHwBrightnessUtils.saveManualBrightnessIntoDB(this.mHwBrightnessUtils.convertSeekbarProgressToBrightness(value));
            }
            this.mHwBrightnessUtils.saveAutoBrightnessMode(checked);
            this.mAutomatic = checked;
            this.mIsObserveAutoBrightnessChange = checked;
            this.mHwBrightnessUtils.onBrightnessModeChange(this.mHwBrightnessUtils.isAutoModeOn(), value);
        } else if (tracking) {
            this.mIsObserveAutoBrightnessChange = false;
            if (this.mAutomatic && this.mHwBrightnessUtils.isHighPrecisionSupported() && this.mResetHandler.hasMessages(1)) {
                this.mResetHandler.removeMessages(1);
            }
            this.mHwBrightnessUtils.setBrightness(value, this.mAutomatic);
        } else {
            if (this.mAutomatic && this.mHwBrightnessUtils.isHighPrecisionSupported()) {
                this.mHwBrightnessUtils.saveAutoBrightnessADJIntoDB(Float.valueOf(this.mHwBrightnessUtils.converSeekBarProgressToAutoBrightnessADJ(value)));
                this.mResetHandler.sendMessageDelayed(this.mResetHandler.obtainMessage(1), 1000);
            } else if (this.mAutomatic) {
                this.mHwBrightnessUtils.saveAutoBrightnessADJIntoDB(Float.valueOf(this.mHwBrightnessUtils.converSeekBarProgressToAutoBrightnessADJ(value)));
            } else {
                int brightness = this.mHwBrightnessUtils.convertSeekbarProgressToBrightness(value);
                BDReporter.e(this.mContext, 43, "progress:" + brightness);
                this.mHwBrightnessUtils.saveManualBrightnessIntoDB(brightness);
            }
        }
    }

    public void registerCallbacks() {
        if (this.mListening) {
            HwLog.i("HwBrightnessController", "registerCallbacks mListening is true ,and return");
            return;
        }
        HwLog.i("HwBrightnessController", "registerCallbacks ");
        this.mBrightnessObserver.startObserving();
        this.mToggleSlider.setMax(10000);
        UserSwitchUtils.addListener(this);
        updateAutoCheckbox();
        updateSlider();
        this.mToggleSlider.setOnChangedListener(this);
        this.mListening = true;
    }

    public void unregisterCallbacks() {
        if (this.mListening) {
            this.mBrightnessObserver.stopObserving();
            this.mToggleSlider.setOnChangedListener(null);
            UserSwitchUtils.removeListener(this);
            this.mListening = false;
        }
    }

    public void onUserChanged() {
        HwLog.i("HwBrightnessController", "onUserChanged::user has changed!");
        updateAutoCheckbox();
        updateSlider();
    }

    public void updateSlider() {
        int seekBarProgress = this.mHwBrightnessUtils.getSeekBarProgress(this.mAutomatic);
        HwLog.i("HwBrightnessController", "updateSlider1 seekBarProgress:" + seekBarProgress);
        updateSlider(seekBarProgress);
    }

    private void updateSlider(int seekBarProgress) {
        HwLog.i("HwBrightnessController", "updateSlider2 seekBarProgress:" + seekBarProgress);
        this.mToggleSlider.setValue(seekBarProgress);
        HwBrightnessUtils hwBrightnessUtils = this.mHwBrightnessUtils;
        int level = HwBrightnessUtils.getCurrentLevelOfIndicatorView(seekBarProgress);
        if (this.mToggleSlider.mBrightnessIndicatorImageView != null) {
            this.mToggleSlider.mBrightnessIndicatorImageView.setImageLevel(level);
        }
    }

    public void updateAutoCheckbox() {
        this.mAutomatic = this.mHwBrightnessUtils.isAutoModeOn();
        HwLog.i("HwBrightnessController", "updateAutoCheckbox mAutomatic:" + this.mAutomatic);
        this.mIsObserveAutoBrightnessChange = this.mAutomatic;
        this.mToggleSlider.setOnChangedListener(null);
        this.mToggleSlider.setChecked(this.mAutomatic);
        this.mToggleSlider.setOnChangedListener(this);
    }

    public void onCoverModeChanged(boolean isInCoverMode) {
        if (this.mHwBrightnessUtils == null) {
            HwLog.e("HwBrightnessController", "onCoverModeChanged::mHwBrightnessUtils is null, isInCoverMode=" + isInCoverMode);
        } else if (HwBrightnessUtils.INT_BRIGHTNESS_COVER_MODE == 0) {
            HwLog.e("HwBrightnessController", "onCoverModeChanged::INT_BRIGHTNESS_COVER_MODE is set to 0 by product, isInCoverMode=" + isInCoverMode);
        } else {
            if (isInCoverMode) {
                if (this.mHwBrightnessUtils.isLastStateValid()) {
                    HwLog.w("HwBrightnessController", "onCoverModeChanged::last state is valid, donot reset it!");
                } else {
                    boolean isAutoMode = this.mHwBrightnessUtils.isAutoModeOn();
                    this.mHwBrightnessUtils.saveLastAutoBrightnessMode(isAutoMode);
                    int progress = this.mHwBrightnessUtils.getSeekBarProgress(isAutoMode);
                    this.mHwBrightnessUtils.saveLastBrightnessProcessToDB(progress);
                    this.mHwBrightnessUtils.setLastStateValidable(true);
                    this.mAutomatic = false;
                    this.mIsObserveAutoBrightnessChange = false;
                    int coverModeBrightness = this.mHwBrightnessUtils.getBrightnessInCoverMode();
                    int seekbarProgress = Math.round(this.mHwBrightnessUtils.convertBrightnessToSeekbarPercentage((float) coverModeBrightness) * 10000.0f);
                    this.mHwBrightnessUtils.saveAutoBrightnessMode(false);
                    updateSlider(seekbarProgress);
                    this.mHwBrightnessUtils.saveManualBrightnessIntoDB(coverModeBrightness);
                    this.mHwBrightnessUtils.setBrightnessByPowerManager(coverModeBrightness);
                    HwLog.i("HwBrightnessController", "onCoverModeChanged::enter cover mode with isAutoMode=" + isAutoMode + ", progress=" + progress + ", coverModeBrightness=" + coverModeBrightness + ", seekbarProgress=" + seekbarProgress);
                }
            } else if (this.mHwBrightnessUtils.isLastStateValid()) {
                this.mHwBrightnessUtils.setLastStateValidable(false);
                int brightnessProcess = this.mHwBrightnessUtils.getLastBrightnessProcessFromDB();
                this.mAutomatic = this.mHwBrightnessUtils.isLastAutoModeOn();
                HwLog.i("HwBrightnessController", "onCoverModeChanged::exit cover mode with isAutoMode=" + this.mAutomatic + ", progress=" + brightnessProcess);
                this.mIsObserveAutoBrightnessChange = this.mAutomatic;
                this.mHwBrightnessUtils.saveAutoBrightnessMode(this.mAutomatic);
                this.mHwBrightnessUtils.onBrightnessModeChange(this.mAutomatic, brightnessProcess);
                updateSlider();
            } else {
                HwLog.w("HwBrightnessController", "onCoverModeChanged::last state is invalid, donot reset it!");
            }
        }
    }
}
