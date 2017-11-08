package com.android.server.display;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.HwNormalizedSpline;
import android.util.Log;
import android.util.Slog;
import android.util.Spline;
import com.android.server.LocalServices;
import com.android.server.display.AutomaticBrightnessController.Callbacks;
import com.android.server.lights.LightsManager;

public class HwNormalizedAutomaticBrightnessController extends AutomaticBrightnessController {
    private static final int BRIGHTNESS_FOR_PROXIMITY_POSITIVE = -2;
    private static final int BRIGHTNESS_FOR_SENSOR_NOT_READY_WHEN_WAKEUP = -1;
    private static final boolean DEBUG;
    private static final int MSG_CoverMode_DEBOUNCED = 4;
    private static final int MSG_PROXIMITY_SENSOR_DEBOUNCED = 3;
    private static final int MSG_REPORT_PROXIMITY_STATE = 2;
    private static final int MSG_UPDATE_AMBIENT_LUX = 1;
    private static final long POWER_ON_LUX_ABANDON_COUNT_MAX = 3;
    private static final int POWER_ON_LUX_COUNT_MAX = 8;
    private static final int PROXIMITY_NEGATIVE = 0;
    private static final int PROXIMITY_POSITIVE = 1;
    private static final int PROXIMITY_UNKNOWN = -1;
    private static String TAG = "HwNormalizedAutomaticBrightnessController";
    private static final int TIME_DELAYED_USING_PROXIMITY_STATE = 500;
    private static final int TIME_PRINT_SENSOR_VALUE_INTERVAL = 4000;
    private static final int TIME_SENSOR_REPORT_NONE_VALUE = 400;
    private static int mDeviceActualBrightnessLevel;
    private static int mDeviceActualBrightnessNit;
    private static int mDeviceStandardBrightnessNit;
    private static HwNormalizedSpline mHwNormalizedScreenAutoBrightnessSpline;
    private static final Object mLock = new Object();
    private boolean mAllowLabcUseProximity;
    private final HandlerThread mAutoBrightnessProcessThread;
    private int mCoverModeFastResponseTimeDelay;
    private boolean mCoverStateFast;
    private boolean mCurrentUserChanging;
    private int mCurrentUserId;
    private HwRingBuffer mHwAmbientLightRingBuffer;
    private HwRingBuffer mHwAmbientLightRingBufferTrace;
    private HwAmbientLuxFilterAlgo mHwAmbientLuxFilterAlgo;
    private HwEyeProtectionControllerImpl mHwEyeProtectionController;
    private int mHwLastReportedSensorValue;
    private long mHwLastReportedSensorValueTime;
    private int mHwLastSensorValue;
    private final HwNormalizedAutomaticBrightnessHandler mHwNormalizedAutomaticBrightnessHandler;
    private long mHwPrintLogTime;
    private int mHwRateMillis;
    private boolean mHwReportValueWhenSensorOnChange;
    private boolean mIsclosed;
    private boolean mPolicyChangeFromDim;
    private int mPowerOnLuxAbandonCount;
    private int mPowerOnLuxCount;
    private int mPowerPolicy;
    private boolean mPowerStatus;
    private int mProximity;
    private boolean mProximityPositive;
    private long mProximityReportTime;
    private final Sensor mProximitySensor;
    private boolean mProximitySensorEnabled;
    private final SensorEventListener mProximitySensorListener;

    private final class HwNormalizedAutomaticBrightnessHandler extends Handler {
        public HwNormalizedAutomaticBrightnessHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwNormalizedAutomaticBrightnessController.this.handleUpdateAmbientLuxMsg();
                    return;
                case 2:
                    HwNormalizedAutomaticBrightnessController.this.handleProximitySensorEvent();
                    return;
                case 3:
                    HwNormalizedAutomaticBrightnessController.this.debounceProximitySensor();
                    return;
                case 4:
                    HwNormalizedAutomaticBrightnessController.this.setCoverModeFastResponseFlag();
                    return;
                default:
                    return;
            }
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
        mDeviceActualBrightnessLevel = 0;
        mDeviceActualBrightnessNit = 0;
        mDeviceStandardBrightnessNit = 0;
        mDeviceActualBrightnessLevel = getDeviceActualBrightnessLevel();
        mDeviceActualBrightnessNit = getDeviceActualBrightnessNit();
        mDeviceStandardBrightnessNit = getDeviceStandardBrightnessNit();
        if (DEBUG) {
            Slog.i(TAG, "DeviceActualLevel=" + mDeviceActualBrightnessLevel + ",DeviceActualBrightnessNit=" + mDeviceActualBrightnessNit + ",DeviceStandardBrightnessNit=" + mDeviceStandardBrightnessNit);
        }
    }

    private static int getDeviceActualBrightnessLevel() {
        try {
            return ((LightsManager) LocalServices.getService(LightsManager.class)).getLight(0).getDeviceActualBrightnessLevel();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static int getDeviceActualBrightnessNit() {
        try {
            return ((LightsManager) LocalServices.getService(LightsManager.class)).getLight(0).getDeviceActualBrightnessNit();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static int getDeviceStandardBrightnessNit() {
        try {
            return ((LightsManager) LocalServices.getService(LightsManager.class)).getLight(0).getDeviceStandardBrightnessNit();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static HwNormalizedSpline createHwNormalizedAutoBrightnessSpline(Context context) {
        try {
            mHwNormalizedScreenAutoBrightnessSpline = HwNormalizedSpline.createHwNormalizedSpline(context, mDeviceActualBrightnessLevel, mDeviceActualBrightnessNit, mDeviceStandardBrightnessNit);
            return mHwNormalizedScreenAutoBrightnessSpline;
        } catch (IllegalArgumentException ex) {
            Slog.e(TAG, "Could not create auto-brightness spline.", ex);
            return null;
        }
    }

    public HwNormalizedAutomaticBrightnessController(Callbacks callbacks, Looper looper, SensorManager sensorManager, Spline autoBrightnessSpline, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, int lightSensorRate, long brighteningLightDebounceConfig, long darkeningLightDebounceConfig, boolean resetAmbientLuxAfterWarmUpConfig, int ambientLightHorizon, float autoBrightnessAdjustmentMaxGamma, Context context) {
        super(callbacks, looper, sensorManager, createHwNormalizedAutoBrightnessSpline(context), lightSensorWarmUpTime, brightnessMin, brightnessMax, dozeScaleFactor, lightSensorRate, brighteningLightDebounceConfig, darkeningLightDebounceConfig, resetAmbientLuxAfterWarmUpConfig, ambientLightHorizon, autoBrightnessAdjustmentMaxGamma);
        this.mPowerStatus = false;
        this.mPowerOnLuxCount = 0;
        this.mPowerOnLuxAbandonCount = 0;
        this.mCurrentUserId = 0;
        this.mCurrentUserChanging = false;
        this.mHwRateMillis = 300;
        this.mHwPrintLogTime = -1;
        this.mHwLastSensorValue = -1;
        this.mHwLastReportedSensorValue = -1;
        this.mHwLastReportedSensorValueTime = -1;
        this.mHwReportValueWhenSensorOnChange = true;
        this.mHwAmbientLightRingBuffer = new HwRingBuffer(10);
        this.mHwAmbientLightRingBufferTrace = new HwRingBuffer(50);
        this.mPowerPolicy = 0;
        this.mPolicyChangeFromDim = false;
        this.mProximity = -1;
        this.mCoverModeFastResponseTimeDelay = 2500;
        this.mCoverStateFast = false;
        this.mIsclosed = false;
        this.mProximitySensorListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                boolean z = false;
                if (HwNormalizedAutomaticBrightnessController.this.mProximitySensorEnabled) {
                    float distance = event.values[0];
                    HwNormalizedAutomaticBrightnessController.this.mProximityReportTime = SystemClock.uptimeMillis();
                    HwNormalizedAutomaticBrightnessController hwNormalizedAutomaticBrightnessController = HwNormalizedAutomaticBrightnessController.this;
                    if (distance >= 0.0f && distance < 5.0f) {
                        z = true;
                    }
                    hwNormalizedAutomaticBrightnessController.mProximityPositive = z;
                    if (HwNormalizedAutomaticBrightnessController.DEBUG) {
                        Slog.d(HwNormalizedAutomaticBrightnessController.TAG, "mProximitySensorListener: time = " + HwNormalizedAutomaticBrightnessController.this.mProximityReportTime + "; distance = " + distance);
                    }
                    if (!HwNormalizedAutomaticBrightnessController.this.mWakeupFromSleep && HwNormalizedAutomaticBrightnessController.this.mProximityReportTime - HwNormalizedAutomaticBrightnessController.this.mLightSensorEnableTime > 500) {
                        HwNormalizedAutomaticBrightnessController.this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessage(2);
                    }
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mHwAmbientLuxFilterAlgo = new HwAmbientLuxFilterAlgo(lightSensorRate, mDeviceActualBrightnessLevel);
        this.mAutoBrightnessProcessThread = new HandlerThread(TAG);
        this.mAutoBrightnessProcessThread.start();
        this.mHwNormalizedAutomaticBrightnessHandler = new HwNormalizedAutomaticBrightnessHandler(this.mAutoBrightnessProcessThread.getLooper());
        this.mHwReportValueWhenSensorOnChange = this.mHwAmbientLuxFilterAlgo.reportValueWhenSensorOnChange();
        this.mProximitySensor = sensorManager.getDefaultSensor(8);
        this.mAllowLabcUseProximity = this.mHwAmbientLuxFilterAlgo.needToUseProximity();
        if (SystemProperties.getInt("ro.config.hw_eyes_protection", 7) != 0) {
            this.mHwEyeProtectionController = new HwEyeProtectionControllerImpl(context, this);
        }
    }

    public HwNormalizedAutomaticBrightnessController(Callbacks callbacks, Looper looper, SensorManager sensorManager, Spline autoBrightnessSpline, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, Context context) {
        this(callbacks, looper, sensorManager, createHwNormalizedAutoBrightnessSpline(context), lightSensorWarmUpTime, brightnessMin, brightnessMax, dozeScaleFactor, 0, 0, 0, false, 10000, 3.0f, context);
        if (SystemProperties.getInt("ro.config.hw_eyes_protection", 7) != 0) {
            this.mHwEyeProtectionController = new HwEyeProtectionControllerImpl(context, this);
        }
    }

    public void configure(boolean enable, float adjustment, boolean dozing) {
        configure(enable, adjustment, dozing, false, false);
    }

    public void configure(boolean enable, float adjustment, boolean dozing, boolean userInitiatedChange, boolean useTwilight) {
        boolean z = false;
        if (this.mLightSensorEnabled && !enable) {
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(1);
            this.mHwAmbientLuxFilterAlgo.clear();
            if (!this.mHwReportValueWhenSensorOnChange) {
                clearSensorData();
            }
        }
        if (!enable) {
            this.mHwAmbientLuxFilterAlgo.setPowerStatus(false);
        }
        super.configure(enable, adjustment, dozing, userInitiatedChange, useTwilight);
        if (this.mLightSensorEnabled && -1 == this.mHwPrintLogTime) {
            this.mHwPrintLogTime = this.mLightSensorEnableTime;
        }
        if (this.mAllowLabcUseProximity && enable && !dozing) {
            z = true;
        }
        setProximitySensorEnabled(z);
    }

    public int getAutomaticScreenBrightness() {
        if (this.mWakeupFromSleep && SystemClock.uptimeMillis() - this.mLightSensorEnableTime < 200) {
            if (DEBUG) {
                Slog.d(TAG, "mWakeupFromSleep= " + this.mWakeupFromSleep + ",currentTime=" + SystemClock.uptimeMillis() + ",mLightSensorEnableTime=" + this.mLightSensorEnableTime);
            }
            return -1;
        } else if (needToSetBrightnessBaseProximity()) {
            return -2;
        } else {
            return super.getAutomaticScreenBrightness();
        }
    }

    private int getSensorData() {
        synchronized (this.mHwAmbientLightRingBuffer) {
            long time = SystemClock.uptimeMillis();
            int N = this.mHwAmbientLightRingBuffer.size();
            int i;
            if (N > 0) {
                int sum = 0;
                for (int i2 = N - 1; i2 >= 0; i2--) {
                    sum = (int) (((float) sum) + this.mHwAmbientLightRingBuffer.getLux(i2));
                }
                int average = sum / N;
                if (average >= 0) {
                    this.mHwLastSensorValue = average;
                }
                this.mHwAmbientLightRingBuffer.clear();
                if (time - this.mHwPrintLogTime > 4000) {
                    Slog.d("lux trace:", this.mHwAmbientLightRingBufferTrace.toString(this.mHwAmbientLightRingBufferTrace.size()));
                    this.mHwAmbientLightRingBufferTrace.clear();
                    this.mHwPrintLogTime = time;
                }
                i = this.mHwLastSensorValue;
                return i;
            } else if (time - this.mHwLastReportedSensorValueTime < 400) {
                i = this.mHwLastSensorValue;
                return i;
            } else {
                i = this.mHwLastReportedSensorValue;
                return i;
            }
        }
    }

    private void clearSensorData() {
        synchronized (this.mHwAmbientLightRingBuffer) {
            this.mHwAmbientLightRingBuffer.clear();
            Slog.d("lux trace:", this.mHwAmbientLightRingBufferTrace.toString(this.mHwAmbientLightRingBufferTrace.size()));
            this.mHwAmbientLightRingBufferTrace.clear();
            this.mHwLastReportedSensorValueTime = -1;
            this.mHwLastReportedSensorValue = -1;
            this.mHwLastSensorValue = -1;
            this.mHwPrintLogTime = -1;
        }
    }

    private void reportLightSensorEventToAlgo(long time, float lux) {
        this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(1);
        if (!this.mAmbientLuxValid) {
            this.mWakeupFromSleep = false;
            this.mAmbientLuxValid = true;
            this.mHwAmbientLuxFilterAlgo.isFirstAmbientLux(true);
            if (DEBUG) {
                Slog.d(TAG, "mAmbientLuxValid=" + this.mAmbientLuxValid + ",mWakeupFromSleep= " + this.mWakeupFromSleep);
            }
        }
        this.mHwAmbientLuxFilterAlgo.handleLightSensorEvent(time, lux);
        this.mAmbientLux = this.mHwAmbientLuxFilterAlgo.getCurrentAmbientLux();
        if (this.mHwAmbientLuxFilterAlgo.needToUpdateBrightness()) {
            if (DEBUG) {
                Slog.d(TAG, "need to update brightness: mAmbientLux=" + this.mAmbientLux);
            }
            this.mHwAmbientLuxFilterAlgo.brightnessUpdated();
            updateAutoBrightness(true);
        }
        if (!this.mHwReportValueWhenSensorOnChange) {
            this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessageDelayed(1, (long) this.mHwRateMillis);
        }
    }

    protected void handleLightSensorEvent(long time, float lux) {
        if (mHwNormalizedScreenAutoBrightnessSpline.getCalibrationTestEable()) {
            this.mSetbrightnessImmediateEnable = true;
            getLightSensorFromDB();
            return;
        }
        this.mSetbrightnessImmediateEnable = false;
        if (!this.mAmbientLuxValid || this.mHwReportValueWhenSensorOnChange) {
            reportLightSensorEventToAlgo(time, lux);
            if (!this.mHwReportValueWhenSensorOnChange) {
                synchronized (mLock) {
                    this.mHwLastReportedSensorValue = (int) lux;
                    this.mHwLastReportedSensorValueTime = time;
                }
                return;
            }
            return;
        }
        synchronized (this.mHwAmbientLightRingBuffer) {
            this.mHwAmbientLightRingBuffer.push(time, lux);
            this.mHwAmbientLightRingBufferTrace.push(time, lux);
            this.mHwLastReportedSensorValue = (int) lux;
            this.mHwLastReportedSensorValueTime = time;
        }
    }

    protected void getLightSensorFromDB() {
        this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(1);
        this.mAmbientLuxValid = true;
        float ambientLux = mHwNormalizedScreenAutoBrightnessSpline.getAmbientValueFromDB();
        if (((int) (ambientLux * 10.0f)) != ((int) (this.mAmbientLux * 10.0f))) {
            this.mAmbientLux = ambientLux;
            if (DEBUG) {
                Slog.d(TAG, "setAmbientLuxDB=" + this.mAmbientLux);
            }
            updateAutoBrightness(true);
        }
    }

    public void setPowerStatus(boolean powerStatus) {
        if (DEBUG && this.mPowerStatus != powerStatus) {
            Slog.d(TAG, "set power status:mPowerStatus=" + this.mPowerStatus + ",powerStatus=" + powerStatus);
        }
        this.mPowerStatus = powerStatus;
        this.mWakeupFromSleep = powerStatus;
        this.mHwAmbientLuxFilterAlgo.setPowerStatus(powerStatus);
        if (!this.mPowerStatus) {
            this.mPowerOnLuxAbandonCount = 0;
            this.mPowerOnLuxCount = 0;
        }
        if (this.mHwEyeProtectionController != null) {
            this.mHwEyeProtectionController.onScreenStateChanged(powerStatus);
        }
    }

    protected boolean interceptHandleLightSensorEvent(long time, float lux) {
        if (SystemClock.uptimeMillis() < ((long) this.mLightSensorWarmUpTimeConfig) + this.mLightSensorEnableTime) {
            Slog.i(TAG, "sensor not ready yet at time " + time);
            return true;
        } else if (this.mCurrentUserChanging) {
            return true;
        } else {
            if (this.mPowerStatus) {
                this.mPowerOnLuxAbandonCount++;
                this.mPowerOnLuxCount++;
                if (this.mPowerOnLuxCount > getpowerOnFastResponseLuxNum()) {
                    if (DEBUG) {
                        Slog.d(TAG, "set power status:false,powerOnFastResponseLuxNum=" + getpowerOnFastResponseLuxNum());
                    }
                    this.mPowerStatus = false;
                    this.mHwAmbientLuxFilterAlgo.setPowerStatus(false);
                }
                if (this.mLightSensorEnableElapsedTimeNanos - time > 350000000) {
                    if (DEBUG) {
                        Slog.d(TAG, "abandon handleLightSensorEvent:" + lux);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    public void updateAutoBrightnessAdjustFactor(float adjustFactor) {
        float lux = this.mHwAmbientLuxFilterAlgo.getCurrentAmbientLux();
        if (DEBUG) {
            Slog.i(TAG, "AdjustPositionBrightness=" + ((int) (adjustFactor * 255.0f)) + ",lux=" + lux);
        }
        mHwNormalizedScreenAutoBrightnessSpline.updateLevelWithLux(adjustFactor * 255.0f, lux);
    }

    protected boolean setScreenAutoBrightnessAdjustment(float adjustment) {
        return false;
    }

    public void saveOffsetAlgorithmParas() {
    }

    private void handleUpdateAmbientLuxMsg() {
        reportLightSensorEventToAlgo(SystemClock.uptimeMillis(), (float) getSensorData());
    }

    protected void updateBrightnessIfNoAmbientLuxReported() {
        if (this.mWakeupFromSleep) {
            this.mWakeupFromSleep = false;
            this.mCallbacks.updateBrightness();
            this.mFirstAutoBrightness = false;
            this.mUpdateAutoBrightnessCount++;
            if (DEBUG) {
                Slog.d(TAG, "sensor doesn't report lux in 200ms");
            }
        }
    }

    public void updateCurrentUserId(int userId) {
        if (userId != this.mCurrentUserId) {
            if (DEBUG) {
                Slog.d(TAG, "user change from  " + this.mCurrentUserId + " into " + userId);
            }
            this.mCurrentUserId = userId;
            this.mCurrentUserChanging = true;
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(1);
            this.mAmbientLuxValid = false;
            this.mHwAmbientLuxFilterAlgo.clear();
            mHwNormalizedScreenAutoBrightnessSpline.updateCurrentUserId(userId);
            this.mCurrentUserChanging = false;
        }
    }

    private void setProximitySensorEnabled(boolean enable) {
        if (enable) {
            if (!this.mProximitySensorEnabled) {
                this.mProximitySensorEnabled = true;
                getSensorManager().registerListener(this.mProximitySensorListener, this.mProximitySensor, 3, this.mHwNormalizedAutomaticBrightnessHandler);
                if (DEBUG) {
                    Slog.d(TAG, "open proximity sensor");
                }
            }
        } else if (this.mProximitySensorEnabled) {
            this.mProximitySensorEnabled = false;
            this.mProximity = -1;
            getSensorManager().unregisterListener(this.mProximitySensorListener);
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(3);
            this.mCallbacks.updateProximityState(false);
            if (DEBUG) {
                Slog.d(TAG, "close proximity sensor");
            }
        }
    }

    private void processProximityState() {
        int proximity = this.mHwAmbientLuxFilterAlgo.getProximityState();
        if (this.mProximity != proximity) {
            if (DEBUG) {
                Slog.d(TAG, "mProximity=" + this.mProximity + ",proximity=" + proximity);
            }
            if (this.mProximity == 1 && proximity == 0) {
                this.mFirstBrightnessAfterProximityNegative = true;
            }
            this.mProximity = proximity;
            if (this.mProximity != -1) {
                if (this.mProximity == 1) {
                    this.mCallbacks.updateProximityState(true);
                } else if (this.mProximity == 0) {
                    this.mCallbacks.updateProximityState(false);
                }
            }
        }
    }

    private void handleProximitySensorEvent() {
        this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(3);
        this.mHwAmbientLuxFilterAlgo.handleProximitySensorEvent(this.mProximityReportTime, this.mProximityPositive);
        processProximityState();
        if (this.mHwAmbientLuxFilterAlgo.needToSendProximityDebounceMsg()) {
            this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessageAtTime(3, this.mHwAmbientLuxFilterAlgo.getPendingProximityDebounceTime());
        }
    }

    private void debounceProximitySensor() {
        if (DEBUG) {
            Slog.d(TAG, "process MSG_PROXIMITY_SENSOR_DEBOUNCED");
        }
        this.mHwAmbientLuxFilterAlgo.debounceProximitySensor();
        processProximityState();
    }

    public void updatePowerPolicy(int policy) {
        if (this.mPowerPolicy != 2 || policy == 2) {
            this.mPolicyChangeFromDim = false;
        } else {
            this.mPolicyChangeFromDim = true;
        }
        this.mPowerPolicy = policy;
    }

    private boolean needToSetBrightnessBaseProximity() {
        boolean needToSet = (this.mProximity != 1 || this.mBrightnessEnlarge || this.mUpdateAutoBrightnessCount <= 1 || this.mPowerPolicy == 2 || this.mPolicyChangeFromDim) ? false : true;
        if (DEBUG && needToSet) {
            Slog.d(TAG, "mProximity= " + this.mProximity + ",mBrightnessEnlarge=" + this.mBrightnessEnlarge + ",mUpdateAutoBrightnessCount=" + this.mUpdateAutoBrightnessCount + ",mPowerPolicy=" + this.mPowerPolicy + ",mPolicyChangeFromDim=" + this.mPolicyChangeFromDim);
        }
        return needToSet;
    }

    public void setSplineEyeProtectionControlFlag(boolean flag) {
        if (mHwNormalizedScreenAutoBrightnessSpline != null) {
            mHwNormalizedScreenAutoBrightnessSpline.setEyeProtectionControlFlag(flag);
        }
    }

    public boolean getPowerStatus() {
        return this.mPowerStatus;
    }

    public void setCoverModeStatus(boolean isclosed) {
        if (isclosed) {
            this.mHwNormalizedAutomaticBrightnessHandler.removeMessages(4);
        }
        if (!isclosed && this.mIsclosed) {
            this.mCoverStateFast = true;
            this.mHwAmbientLuxFilterAlgo.setCoverModeFastResponseFlag(this.mCoverStateFast);
            this.mHwNormalizedAutomaticBrightnessHandler.sendEmptyMessageDelayed(4, (long) this.mCoverModeFastResponseTimeDelay);
        }
        this.mIsclosed = isclosed;
        this.mHwAmbientLuxFilterAlgo.setCoverModeStatus(isclosed);
    }

    public void setCoverModeFastResponseFlag() {
        this.mCoverStateFast = false;
        this.mHwAmbientLuxFilterAlgo.setCoverModeFastResponseFlag(this.mCoverStateFast);
        mHwNormalizedScreenAutoBrightnessSpline.setNoOffsetEnable(this.mCoverStateFast);
        if (DEBUG) {
            Slog.i(TAG, "LabcCoverMode FastResponseFlag =" + this.mCoverStateFast);
        }
    }

    public boolean getCoverModeFastResponseFlag() {
        return this.mCoverStateFast;
    }

    public int getpowerOnFastResponseLuxNum() {
        if (this.mHwAmbientLuxFilterAlgo == null) {
            return 8;
        }
        return this.mHwAmbientLuxFilterAlgo.getpowerOnFastResponseLuxNum();
    }
}
