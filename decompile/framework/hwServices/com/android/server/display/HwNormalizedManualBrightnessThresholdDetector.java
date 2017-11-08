package com.android.server.display;

import android.graphics.PointF;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwBrightnessXmlLoader.Data;
import com.android.server.input.HwCircleAnimation;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;

public class HwNormalizedManualBrightnessThresholdDetector {
    private static final int AMBIENT_LIGHT_HORIZON = 10000;
    private static final int AMBIENT_MAX_LUX = 40000;
    private static boolean DEBUG = false;
    private static final int INDOOR_UI = 1;
    private static final int OUTDOOR_UI = 2;
    private static String TAG = "HwNormalizedManualBrightnessThresholdDetector";
    protected boolean mAmChangeFlagForHBM = false;
    protected HwRingBuffer mAmbientLightRingBuffer;
    protected HwRingBuffer mAmbientLightRingBufferFilter;
    protected float mAmbientLux;
    private float mBrightenDeltaLuxMax = 0.0f;
    private float mDarkenDeltaLuxMax = 0.0f;
    private final Data mData;
    private int mLastInOutState = 1;

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    public HwNormalizedManualBrightnessThresholdDetector(Data data) {
        this.mData = data;
        this.mAmbientLightRingBuffer = new HwRingBuffer(50);
        this.mAmbientLightRingBufferFilter = new HwRingBuffer(50);
    }

    public void clearAmbientLightRingBuffer() {
        this.mAmbientLightRingBuffer.clear();
        this.mAmbientLightRingBufferFilter.clear();
    }

    public void handleLightSensorEvent(long time, float lux) {
        if (lux > 40000.0f) {
            lux = 40000.0f;
        }
        applyLightSensorMeasurement(time, lux);
        updateAmbientLux(time);
    }

    private void applyLightSensorMeasurement(long time, float lux) {
        this.mAmbientLightRingBuffer.prune(time - MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
        this.mAmbientLightRingBuffer.push(time, lux);
    }

    private float calculateAmbientLuxForNewPolicy(long now) {
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            if (DEBUG) {
                Slog.v(TAG, "calculateAmbientLux: No ambient light readings available");
            }
            return -1.0f;
        } else if (N < 5) {
            return this.mAmbientLightRingBuffer.getLux(N - 1);
        } else {
            float sum = this.mAmbientLightRingBuffer.getLux(N - 1);
            float luxMin = this.mAmbientLightRingBuffer.getLux(N - 1);
            float luxMax = this.mAmbientLightRingBuffer.getLux(N - 1);
            for (int i = N - 2; i >= (N - 1) - 4; i--) {
                if (luxMin > this.mAmbientLightRingBuffer.getLux(i)) {
                    luxMin = this.mAmbientLightRingBuffer.getLux(i);
                }
                if (luxMax < this.mAmbientLightRingBuffer.getLux(i)) {
                    luxMax = this.mAmbientLightRingBuffer.getLux(i);
                }
                sum += this.mAmbientLightRingBuffer.getLux(i);
            }
            return ((sum - luxMin) - luxMax) / 3.0f;
        }
    }

    private long nextAmbientLightBrighteningTransitionForNewPolicy(long time) {
        int N = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mData.manualBrighenDebounceTime;
        if (1 == N) {
            debounceTime = 0;
        }
        long earliestValidTime = time;
        for (int i = N - 1; i >= 0; i--) {
            boolean BrightenChange;
            if (this.mAmbientLightRingBufferFilter.getLux(i) - this.mAmbientLux > this.mBrightenDeltaLuxMax) {
                BrightenChange = true;
            } else {
                BrightenChange = false;
            }
            if (!BrightenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return earliestValidTime + debounceTime;
    }

    private long nextAmbientLightDarkeningTransitionForNewPolicy(long time) {
        int N = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mData.manualDarkenDebounceTime;
        if (1 == N) {
            debounceTime = 0;
        }
        long earliestValidTime = time;
        for (int i = N - 1; i >= 0; i--) {
            boolean DarkenChange;
            if (this.mAmbientLux - this.mAmbientLightRingBufferFilter.getLux(i) >= this.mDarkenDeltaLuxMax) {
                DarkenChange = true;
            } else {
                DarkenChange = false;
            }
            if (!DarkenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return earliestValidTime + debounceTime;
    }

    private long nextAmbientLightDarkeningTransitionForInOut(long time) {
        int N = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mData.manualBrighenDebounceTime;
        if (1 == N) {
            debounceTime = 0;
        }
        long earliestValidTime = time;
        int i = N - 1;
        while (i >= 0) {
            boolean DarkenChange;
            if (this.mAmbientLightRingBufferFilter.getLux(i) <= ((float) this.mData.inDoorThreshold) || this.mAmbientLightRingBufferFilter.getLux(i) >= ((float) this.mData.outDoorThreshold)) {
                DarkenChange = false;
            } else {
                DarkenChange = true;
            }
            if (!DarkenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
            i--;
        }
        return earliestValidTime + debounceTime;
    }

    private void setBrightenThresholdNew() {
        int count = 0;
        PointF temp1 = null;
        for (PointF temp : this.mData.manualBrightenlinePoints) {
            if (count == 0) {
                temp1 = temp;
            }
            if (this.mAmbientLux < temp.x) {
                PointF temp2 = temp;
                if (temp.x <= temp1.x) {
                    this.mBrightenDeltaLuxMax = HwCircleAnimation.SMALL_ALPHA;
                    if (DEBUG) {
                        Slog.i(TAG, "Brighten_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                        return;
                    }
                    return;
                }
                this.mBrightenDeltaLuxMax = (((temp.y - temp1.y) / (temp.x - temp1.x)) * (this.mAmbientLux - temp1.x)) + temp1.y;
                return;
            }
            temp1 = temp;
            this.mBrightenDeltaLuxMax = temp.y;
            count++;
        }
    }

    private void setDarkenThresholdNew() {
        int count = 0;
        PointF temp1 = null;
        for (PointF temp : this.mData.manualDarkenlinePoints) {
            if (count == 0) {
                temp1 = temp;
            }
            if (this.mAmbientLux < temp.x) {
                PointF temp2 = temp;
                if (temp.x <= temp1.x) {
                    this.mDarkenDeltaLuxMax = HwCircleAnimation.SMALL_ALPHA;
                    if (DEBUG) {
                        Slog.i(TAG, "Darken_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                        return;
                    }
                    return;
                }
                this.mDarkenDeltaLuxMax = Math.max((((temp.y - temp1.y) / (temp.x - temp1.x)) * (this.mAmbientLux - temp1.x)) + temp1.y, HwCircleAnimation.SMALL_ALPHA);
                return;
            }
            temp1 = temp;
            this.mDarkenDeltaLuxMax = temp.y;
            count++;
        }
    }

    private void updateAmbientLux(long time) {
        float value = calculateAmbientLuxForNewPolicy(time);
        if (this.mAmbientLightRingBuffer.size() == 1) {
            Slog.i(TAG, "fist sensor lux and filteredlux=" + value + ",time=" + time);
        }
        this.mAmbientLightRingBufferFilter.push(time, value);
        this.mAmbientLightRingBufferFilter.prune(time - MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
        long nextBrightenTransition = nextAmbientLightBrighteningTransitionForNewPolicy(time);
        long nextDarkenTransition = nextAmbientLightDarkeningTransitionForNewPolicy(time);
        long nextInOutTransition = nextAmbientLightDarkeningTransitionForInOut(time);
        this.mAmChangeFlagForHBM = false;
        boolean updateFlag = nextBrightenTransition <= time || nextDarkenTransition <= time || ((nextBrightenTransition <= time && nextInOutTransition <= time) || (nextDarkenTransition <= time && nextInOutTransition <= time));
        if (nextBrightenTransition > time && nextDarkenTransition > time) {
            if (!updateFlag) {
                return;
            }
        }
        if (DEBUG) {
            Slog.i(TAG, "update_Flag=" + updateFlag + ",filteredlux=" + value + ",time=" + time + ",nextBTime=" + nextBrightenTransition + ",nextDTime=" + nextDarkenTransition + ",nextInOutTime=" + nextInOutTransition);
        }
        updateParaForHBM(value);
    }

    private void updateParaForHBM(float lux) {
        float mAmbientLuxTmp = lux;
        if (lux >= ((float) this.mData.outDoorThreshold)) {
            this.mAmbientLux = lux;
            this.mLastInOutState = 2;
            this.mAmChangeFlagForHBM = true;
        }
        if (lux < ((float) this.mData.inDoorThreshold)) {
            this.mAmbientLux = lux;
            this.mLastInOutState = 1;
            this.mAmChangeFlagForHBM = true;
        }
        if (lux < ((float) this.mData.outDoorThreshold) && lux >= ((float) this.mData.inDoorThreshold)) {
            this.mAmbientLux = lux;
            if (this.mAmbientLightRingBufferFilter.size() == 1) {
                this.mLastInOutState = 1;
            }
            this.mAmChangeFlagForHBM = true;
        }
        setBrightenThresholdNew();
        setDarkenThresholdNew();
        if (DEBUG) {
            Slog.i(TAG, "update_lux =" + this.mAmbientLux + ",IN_OUT_DoorFlag =" + this.mLastInOutState + ",mBrightenDeltaLuxMax=" + this.mBrightenDeltaLuxMax + ",mDarkenDeltaLuxMax=" + this.mDarkenDeltaLuxMax);
        }
    }

    public float getAmbientLuxForHBM() {
        return this.mAmbientLux;
    }

    public boolean getLuxChangedFlagForHBM() {
        return this.mAmChangeFlagForHBM;
    }

    public void setLuxChangedFlagForHBM() {
        this.mAmChangeFlagForHBM = false;
    }

    public int getIndoorOutdoorFlagForHBM() {
        return this.mLastInOutState;
    }
}
