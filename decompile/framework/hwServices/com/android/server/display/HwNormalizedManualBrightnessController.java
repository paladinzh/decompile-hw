package com.android.server.display;

import android.content.Context;
import android.graphics.PointF;
import android.hardware.SensorManager;
import android.util.Log;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.display.HwBrightnessXmlLoader.Data;
import com.android.server.display.HwLightSensorController.LightSensorCallbacks;
import com.android.server.display.ManualBrightnessController.ManualBrightnessCallbacks;
import com.android.server.lights.LightsManager;
import java.util.List;

public class HwNormalizedManualBrightnessController extends ManualBrightnessController implements LightSensorCallbacks {
    private static boolean DEBUG = false;
    private static final int DEFAULT = 0;
    private static final int INDOOR = 1;
    private static final int MAXDEFAULTBRIGHTNESS = 255;
    private static final int OUTDOOR = 2;
    private static String TAG = "HwNormalizedManualBrightnessController";
    private final Data mData = HwBrightnessXmlLoader.getData(getDeviceActualBrightnessLevel());
    private float mDefaultBrightness = 100.0f;
    private HwLightSensorController mLightSensorController = null;
    private boolean mLightSensorEnable;
    private int mManualAmbientLux;
    private boolean mManualModeEnable;
    private int mManualbrightness = -1;
    private int mManualbrightnessOut = -1;
    private boolean mNeedUpdateManualBrightness;
    private HwNormalizedManualBrightnessThresholdDetector mOutdoorDetector = null;
    private int mOutdoorScene;

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    private int getDeviceActualBrightnessLevel() {
        return ((LightsManager) LocalServices.getService(LightsManager.class)).getLight(0).getDeviceActualBrightnessLevel();
    }

    public HwNormalizedManualBrightnessController(ManualBrightnessCallbacks callbacks, Context context, SensorManager sensorManager) {
        super(callbacks);
        this.mLightSensorController = new HwLightSensorController(this, sensorManager, this.mData.lightSensorRateMills);
        this.mOutdoorDetector = new HwNormalizedManualBrightnessThresholdDetector(this.mData);
    }

    private static boolean wantScreenOn(int state) {
        switch (state) {
            case 2:
            case 3:
                return true;
            default:
                return false;
        }
    }

    public void updatePowerState(int state, boolean enable) {
        if (this.mManualModeEnable != enable) {
            if (DEBUG) {
                Slog.i(TAG, "HBM SensorEnable change " + this.mManualModeEnable + " -> " + enable);
            }
            this.mManualModeEnable = enable;
        }
        if (this.mManualModeEnable) {
            setLightSensorEnabled(wantScreenOn(state));
        } else {
            setLightSensorEnabled(this.mManualModeEnable);
        }
    }

    private void setLightSensorEnabled(boolean enable) {
        if (enable) {
            if (!this.mLightSensorEnable) {
                this.mLightSensorEnable = true;
                this.mLightSensorController.enableSensor();
                if (DEBUG) {
                    Slog.i(TAG, "HBM ManualMode sensor enable");
                }
            }
        } else if (this.mLightSensorEnable) {
            this.mLightSensorEnable = false;
            this.mLightSensorController.disableSensor();
            this.mOutdoorDetector.clearAmbientLightRingBuffer();
            if (DEBUG) {
                Slog.i(TAG, "HBM ManualMode sensor disenable");
            }
        }
    }

    public void updateManualBrightness(int brightness) {
        this.mManualbrightness = brightness;
        this.mManualbrightnessOut = brightness;
    }

    public int getManualBrightness() {
        if (!this.mData.manualMode) {
            this.mManualbrightnessOut = this.mManualbrightness;
            if (DEBUG) {
                Slog.i(TAG, "mManualbrightnessOut=" + this.mManualbrightnessOut + ",mData.manualMode=" + this.mData.manualMode);
            }
        } else if (this.mManualbrightnessOut >= this.mData.manualBrightnessMaxLimit) {
            float defaultBrightness = getDefaultBrightnessLevelNew(this.mData.defaultBrighnessLinePoints, (float) this.mManualAmbientLux);
            if (this.mOutdoorScene == 2) {
                this.mManualbrightnessOut = Math.max(Math.min(this.mManualbrightnessOut, this.mData.manualBrightnessMaxLimit), (int) defaultBrightness);
                if (DEBUG) {
                    Slog.i(TAG, "mManualbrightnessOut=" + this.mManualbrightnessOut + ",defaultBrightness=" + defaultBrightness + ",AutoLux=" + this.mManualAmbientLux);
                }
            } else {
                this.mManualbrightnessOut = Math.min(this.mManualbrightnessOut, this.mData.manualBrightnessMaxLimit);
                if (DEBUG) {
                    Slog.i(TAG, "mManualbrightnessOut1=" + this.mManualbrightnessOut + ",defaultBrightness=" + defaultBrightness + ",AutoLux=" + this.mManualAmbientLux);
                }
            }
        }
        return this.mManualbrightnessOut;
    }

    public int getMaxBrightnessForSeekbar() {
        if (!this.mData.manualMode) {
            this.mData.manualBrightnessMaxLimit = 255;
        }
        return this.mData.manualBrightnessMaxLimit;
    }

    public void processSensorData(long timeInMs, int lux) {
        this.mOutdoorDetector.handleLightSensorEvent(timeInMs, (float) lux);
        this.mOutdoorScene = this.mOutdoorDetector.getIndoorOutdoorFlagForHBM();
        this.mNeedUpdateManualBrightness = this.mOutdoorDetector.getLuxChangedFlagForHBM();
        this.mManualAmbientLux = (int) this.mOutdoorDetector.getAmbientLuxForHBM();
        if (this.mNeedUpdateManualBrightness) {
            this.mCallbacks.updateManualBrightnessForLux();
            this.mOutdoorDetector.setLuxChangedFlagForHBM();
            if (DEBUG) {
                Slog.i(TAG, "mManualAmbientLux =" + this.mManualAmbientLux + ",mNeedUpdateManualBrightness1=" + this.mNeedUpdateManualBrightness);
            }
        }
    }

    public float getDefaultBrightnessLevelNew(List<PointF> linePointsList, float lux) {
        List<PointF> linePointsListIn = linePointsList;
        int count = 0;
        float brightnessLevel = this.mDefaultBrightness;
        PointF temp1 = null;
        for (PointF temp : linePointsList) {
            if (count == 0) {
                temp1 = temp;
            }
            if (lux < temp.x) {
                PointF temp2 = temp;
                if (temp.x > temp1.x) {
                    return (((temp.y - temp1.y) / (temp.x - temp1.x)) * (lux - temp1.x)) + temp1.y;
                }
                brightnessLevel = this.mDefaultBrightness;
                if (!DEBUG) {
                    return brightnessLevel;
                }
                Slog.i(TAG, "DefaultBrighness_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                return brightnessLevel;
            }
            temp1 = temp;
            brightnessLevel = temp.y;
            count++;
        }
        return brightnessLevel;
    }
}
