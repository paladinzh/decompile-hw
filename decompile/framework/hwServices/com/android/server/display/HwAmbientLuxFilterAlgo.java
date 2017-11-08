package com.android.server.display;

import android.graphics.PointF;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.server.display.HwBrightnessXmlLoader.Data;
import com.android.server.input.HwCircleAnimation;
import com.android.server.jankshield.TableJankBd;
import com.android.server.rms.iaware.cpu.CPUFeature;

public class HwAmbientLuxFilterAlgo {
    private static final int AMBIENT_LIGHT_HORIZON = 20000;
    private static final int AMBIENT_MAX_LUX = 40000;
    private static final int AMBIENT_SCENE_HORIZON = 80000;
    private static final boolean DEBUG;
    private static final int EXTRA_DELAY_TIME = 100;
    private static final long POWER_ON_BRIGHTENING_LIGHT_DEBOUNCE = 500;
    private static final long POWER_ON_DARKENING_LIGHT_DEBOUNCE = 1000;
    private static final int PROXIMITY_NEGATIVE = 0;
    private static final int PROXIMITY_POSITIVE = 1;
    private static final int PROXIMITY_UNKNOWN = -1;
    private static final String TAG = "HwAmbientLuxFilterAlgo";
    private boolean mAllowLabcUseProximity = false;
    private HwRingBuffer mAmbientLightRingBuffer;
    private HwRingBuffer mAmbientLightRingBufferFilter;
    private HwRingBuffer mAmbientLightRingBufferScene;
    protected float mAmbientLux;
    private float mAmbientLuxNewMax;
    private float mAmbientLuxNewMin;
    public boolean mAutoBrightnessIntervened = false;
    private float mBrightenDeltaLuxMax;
    private float mBrightenDeltaLuxMin;
    private boolean mCoverState = false;
    private float mDarkenDeltaLuxMax;
    private float mDarkenDeltaLuxMin;
    private final Data mData;
    private boolean mFirstAmbientLux = true;
    private boolean mIsCoverModeFastResponseFlag = false;
    private boolean mIsclosed = false;
    private float mLastCloseScreenLux = 0.0f;
    private float mLastObservedLux;
    private final int mLightSensorRate;
    private final Object mLock = new Object();
    private float mLuxBufferAvg = 0.0f;
    private float mLuxBufferAvgMax = 0.0f;
    private float mLuxBufferAvgMin = 0.0f;
    private boolean mNeedToSendProximityDebounceMsg = false;
    private boolean mNeedToUpdateBrightness;
    public long mNextTransitionTime = -1;
    protected long mNormBrighenDebounceTime;
    protected long mNormBrighenDebounceTimeForSmallThr;
    protected long mNormDarkenDebounceTime;
    protected long mNormDarkenDebounceTimeForSmallThr;
    private int mPendingProximity = -1;
    private long mPendingProximityDebounceTime = -1;
    private boolean mPowerStatus = false;
    private long mPrintLogTime = 0;
    private int mProximity = -1;
    private int mProximityNegativeDebounceTime = 3000;
    private int mProximityPositiveDebounceTime = CPUFeature.MSG_THERMAL_LIMITED;
    private boolean mProximityPositiveStatus;
    private int mResponseDurationPoints;
    private float mSceneAmbientLuxMax;
    private float mSceneAmbientLuxMin;
    private float mSceneAmbientLuxWeight;
    private float mStability = 0.0f;
    private float mStabilityBrightenConstant = 101.0f;
    private float mStabilityBrightenConstantForSmallThr;
    private float mStabilityDarkenConstant = 101.0f;
    private float mStabilityDarkenConstantForSmallThr;
    private float mStabilityForSmallThr = 0.0f;
    private float mlastFilterLux;

    public interface Callbacks {
        void updateBrightness();
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    public HwAmbientLuxFilterAlgo(int lightSensorRate, int deviceActualBrightnessLevel) {
        this.mLightSensorRate = lightSensorRate;
        this.mNeedToUpdateBrightness = false;
        this.mAmbientLightRingBuffer = new HwRingBuffer(50);
        this.mAmbientLightRingBufferFilter = new HwRingBuffer(50);
        this.mAmbientLightRingBufferScene = new HwRingBuffer(250);
        this.mData = HwBrightnessXmlLoader.getData(deviceActualBrightnessLevel);
    }

    public void isFirstAmbientLux(boolean isFirst) {
        this.mFirstAmbientLux = isFirst;
    }

    public void handleLightSensorEvent(long time, float lux) {
        synchronized (this.mLock) {
            if (lux > 40000.0f) {
                if (DEBUG) {
                    Slog.i(TAG, "lux >= max, lux=" + lux);
                }
                lux = 40000.0f;
            }
            try {
                applyLightSensorMeasurement(time, lux);
                updateAmbientLux(time);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    private void applyLightSensorMeasurement(long time, float lux) {
        this.mAmbientLightRingBuffer.prune(time - TableJankBd.recordMAXCOUNT);
        this.mAmbientLightRingBuffer.push(time, lux);
        this.mLastObservedLux = lux;
    }

    public float getCurrentAmbientLux() {
        return this.mAmbientLux;
    }

    private void setAmbientLux(float lux) {
        this.mAmbientLux = (float) Math.round(lux);
        if (this.mAmbientLux < 10.0f) {
            this.mStabilityBrightenConstantForSmallThr = 0.5f;
            this.mStabilityDarkenConstantForSmallThr = 0.5f;
        } else if (this.mAmbientLux < 10.0f || this.mAmbientLux >= 50.0f) {
            this.mStabilityBrightenConstantForSmallThr = 5.0f;
            this.mStabilityDarkenConstantForSmallThr = 5.0f;
        } else {
            this.mStabilityBrightenConstantForSmallThr = 3.0f;
            this.mStabilityDarkenConstantForSmallThr = 3.0f;
        }
        updatepara(this.mAmbientLightRingBuffer, this.mAmbientLux);
        this.mResponseDurationPoints = 0;
    }

    public void updateAmbientLux() {
        synchronized (this.mLock) {
            long time = SystemClock.uptimeMillis();
            try {
                this.mAmbientLightRingBuffer.push(time, this.mLastObservedLux);
                this.mAmbientLightRingBuffer.prune(time - TableJankBd.recordMAXCOUNT);
                if (DEBUG) {
                    Slog.d(TAG, "updateAmbientLux:time=" + time + ",mLastObservedLux=" + this.mLastObservedLux);
                }
                updateAmbientLux(time);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    private void updateAmbientLux(long time) {
        float filterLux = prefilterAmbientLux(time, this.mData.preMethodNum);
        float lastestLux = getOrigLastAmbientLux(time);
        updateBuffer(time, filterLux, AMBIENT_LIGHT_HORIZON);
        updateBufferForScene(time, filterLux, AMBIENT_SCENE_HORIZON);
        this.mlastFilterLux = getFilterLastAmbientLux(time);
        float ambientLux = postfilterAmbientLux(time, this.mData.postMethodNum);
        if (this.mFirstAmbientLux) {
            if (this.mCoverState) {
                this.mCoverState = false;
                if (this.mPowerStatus) {
                    ambientLux = this.mLastCloseScreenLux;
                    this.mAmbientLightRingBuffer.putLux(0, ambientLux);
                    this.mAmbientLightRingBufferFilter.putLux(0, ambientLux);
                    if (DEBUG) {
                        Slog.i(TAG, "LabcCoverMode1 ambientLux=" + ambientLux + ",mCoverState=" + this.mCoverState + ",mPowerStatus=" + this.mPowerStatus);
                    }
                } else {
                    if (this.mData.lastCloseScreenEnable) {
                        ambientLux = this.mLastCloseScreenLux;
                    } else {
                        ambientLux = this.mData.coverModeFirstLux;
                    }
                    this.mAmbientLightRingBuffer.putLux(0, ambientLux);
                    this.mAmbientLightRingBufferFilter.putLux(0, ambientLux);
                    if (DEBUG) {
                        Slog.i(TAG, "LabcCoverMode ambientLux=" + ambientLux + ",mCoverState=" + this.mCoverState + ",mPowerStatus=" + this.mPowerStatus);
                    }
                }
            }
            setAmbientLux(ambientLux);
            this.mFirstAmbientLux = false;
            if (DEBUG) {
                Slog.d(TAG, "updateAmbientLux: Initializing: mAmbientLightRingBuffer=" + this.mAmbientLightRingBuffer + ", mAmbientLux=" + this.mAmbientLux + ",mLastCloseScreenLux=" + this.mLastCloseScreenLux + ",mAmbientLightRingBufferFilter=" + this.mAmbientLightRingBufferFilter);
            }
            this.mNeedToUpdateBrightness = true;
        }
        updateNewAmbientLuxFromScene(this.mAmbientLightRingBufferScene);
        updatepara(this.mAmbientLightRingBuffer, this.mAmbientLux);
        long nextBrightenTransition = nextAmbientLightBrighteningTransition(time);
        long nextDarkenTransition = nextAmbientLightDarkeningTransition(time);
        boolean needToBrighten = decideToBrighten(ambientLux);
        boolean needToBrightenNew = decideToBrighten(lastestLux);
        boolean needToDarken = decideToDarken(ambientLux);
        boolean needToDarkenNew = decideToDarken(lastestLux);
        long nextBrightenTransitionForSmallThr = nextAmbientLightBrighteningTransitionForSmallThr(time);
        long nextDarkenTransitionForSmallThr = nextAmbientLightDarkeningTransitionForSmallThr(time);
        boolean needToBrightenForSmallThr = decideToBrightenForSmallThr(ambientLux);
        boolean needToDarkenForSmallThr = decideToDarkenForSmallThr(ambientLux);
        needToBrightenForSmallThr = needToBrightenForSmallThr && nextBrightenTransitionForSmallThr <= time;
        needToDarkenForSmallThr = needToDarkenForSmallThr && nextDarkenTransitionForSmallThr <= time;
        if (needToBrighten && needToBrightenNew && nextBrightenTransition <= time) {
            int needToBrighten2 = 1;
        } else {
            needToBrighten = needToBrightenForSmallThr;
        }
        if (needToDarken && needToDarkenNew && nextDarkenTransition <= time) {
            int needToDarken2 = 1;
        } else {
            needToDarken = needToDarkenForSmallThr;
        }
        float brightenLux = this.mAmbientLux + this.mBrightenDeltaLuxMax;
        float darkenLux = this.mAmbientLux - this.mDarkenDeltaLuxMax;
        if (DEBUG && time - this.mPrintLogTime > 2000) {
            Slog.d(TAG, "t=" + time + ",lx=" + this.mAmbientLightRingBuffer.toString(6) + ",mLx=" + this.mAmbientLux + ",s=" + this.mStability + ",ss=" + this.mStabilityForSmallThr + ",AuIntervened=" + this.mAutoBrightnessIntervened + ",mlastFilterLux=" + this.mlastFilterLux + ",mProximityState=" + this.mProximityPositiveStatus + ",bLux=" + brightenLux + ",dLux=" + darkenLux + ",mDt=" + this.mNormDarkenDebounceTime + ",mBt=" + this.mNormBrighenDebounceTime + ",mMax=" + this.mAmbientLuxNewMax + ",mMin=" + this.mAmbientLuxNewMin + ",mu=" + this.mSceneAmbientLuxWeight + ",sMax=" + this.mSceneAmbientLuxMax + ",sMin=" + this.mSceneAmbientLuxMin);
            this.mPrintLogTime = time;
        }
        if ((needToBrighten2 | needToDarken2) != 0) {
            setAmbientLux(ambientLux);
            if (DEBUG) {
                Slog.d(TAG, "updateAmbientLux: " + (needToBrighten2 != 0 ? "Brightened" : "Darkened") + ", mAmbientLightRingBuffer=" + this.mAmbientLightRingBuffer.toString(6) + ", mAmbientLux=" + this.mAmbientLux + ",s=" + this.mStability + ",ss=" + this.mStabilityForSmallThr + ",needBs=" + needToBrightenForSmallThr + ",needDs=" + needToDarkenForSmallThr + ", mAmbientLightRingBufferF=" + this.mAmbientLightRingBufferFilter.toString(6));
                Slog.d(TAG, "PreMethodNum=" + this.mData.preMethodNum + ",PreMeanFilterNum=" + this.mData.preMeanFilterNum + ",mData.preMeanFilterNoFilterNum=" + this.mData.preMeanFilterNoFilterNum + ",PostMethodNum=" + this.mData.postMethodNum);
            }
            if (DEBUG && this.mIsCoverModeFastResponseFlag) {
                Slog.i(TAG, "CoverModeBResponseTime=" + this.mData.coverModeBrightenResponseTime + ",CoverModeDResponseTime=" + this.mData.coverModeDarkenResponseTime);
            }
            if (DEBUG && this.mPowerStatus) {
                Slog.i(TAG, "PowerOnBT=500,PowerOnDT=1000");
            }
            this.mNeedToUpdateBrightness = true;
            nextBrightenTransition = nextAmbientLightBrighteningTransition(time);
            nextDarkenTransition = nextAmbientLightDarkeningTransition(time);
        }
        this.mNextTransitionTime = Math.min(nextDarkenTransition, nextBrightenTransition);
        this.mNextTransitionTime = this.mNextTransitionTime > time ? (this.mNextTransitionTime + ((long) this.mLightSensorRate)) + 100 : (((long) this.mLightSensorRate) + time) + 100;
        if (DEBUG && time - this.mPrintLogTime > 2000) {
            Slog.d(TAG, "updateAmbientLux: Scheduling ambient lux update for " + this.mNextTransitionTime + TimeUtils.formatUptime(this.mNextTransitionTime));
        }
    }

    private void updateNewAmbientLuxFromScene(HwRingBuffer hwBuffer) {
        int N = hwBuffer.size();
        this.mAmbientLuxNewMax = this.mAmbientLux;
        this.mAmbientLuxNewMin = this.mAmbientLux;
        this.mSceneAmbientLuxMax = this.mAmbientLux;
        this.mSceneAmbientLuxMin = this.mAmbientLux;
        if (this.mResponseDurationPoints == Integer.MAX_VALUE) {
            this.mResponseDurationPoints = Integer.MAX_VALUE;
        } else {
            this.mResponseDurationPoints++;
        }
        if (N != 0 && N >= this.mData.sceneGapPoints && this.mResponseDurationPoints - this.mData.sceneMinPoints >= this.mData.sceneGapPoints && this.mData.sceneMaxPoints >= this.mData.sceneMinPoints && this.mData.sceneMaxPoints + this.mData.sceneGapPoints <= 228) {
            updateSceneBufferAmbientLuxMaxMinAvg(hwBuffer, this.mResponseDurationPoints < this.mData.sceneMaxPoints + this.mData.sceneGapPoints ? N - this.mResponseDurationPoints : (N - this.mData.sceneMaxPoints) - this.mData.sceneGapPoints, N - this.mData.sceneGapPoints);
            this.mSceneAmbientLuxWeight = ((float) this.mData.sceneGapPoints) / ((float) this.mResponseDurationPoints);
            if (this.mAmbientLux > this.mSceneAmbientLuxMax) {
                this.mAmbientLuxNewMax = (this.mSceneAmbientLuxWeight * this.mAmbientLux) + ((HwCircleAnimation.SMALL_ALPHA - this.mSceneAmbientLuxWeight) * this.mSceneAmbientLuxMax);
            }
            if (this.mAmbientLux > this.mSceneAmbientLuxMin) {
                this.mAmbientLuxNewMin = (this.mSceneAmbientLuxWeight * this.mAmbientLux) + ((HwCircleAnimation.SMALL_ALPHA - this.mSceneAmbientLuxWeight) * this.mSceneAmbientLuxMin);
            }
            correctAmbientLux();
        }
    }

    private void updateSceneBufferAmbientLuxMaxMinAvg(HwRingBuffer buffer, int start, int end) {
        int N = buffer.size();
        if (N == 0 || end < start || start > N - 1 || end < 0) {
            Slog.e(TAG, "error buffer lux,end=" + end + ",start=" + end);
            return;
        }
        float luxSum = 0.0f;
        float luxMin = buffer.getLux(start);
        float luxMax = buffer.getLux(start);
        for (int i = start; i <= end; i++) {
            float lux = buffer.getLux(i);
            if (lux > luxMax) {
                luxMax = lux;
            }
            if (lux < luxMin) {
                luxMin = lux;
            }
            luxSum += lux;
        }
        float luxMean = (float) Math.round(luxSum / ((float) ((end - start) + 1)));
        this.mSceneAmbientLuxMax = (this.mData.sceneAmbientLuxMaxWeight * luxMean) + ((HwCircleAnimation.SMALL_ALPHA - this.mData.sceneAmbientLuxMaxWeight) * luxMax);
        this.mSceneAmbientLuxMin = (this.mData.sceneAmbientLuxMinWeight * luxMean) + ((HwCircleAnimation.SMALL_ALPHA - this.mData.sceneAmbientLuxMinWeight) * luxMin);
    }

    private void correctAmbientLux() {
        float ambientLuxDarkenDelta = calculateDarkenThresholdDelta(this.mAmbientLux);
        float ambientLuxNewMaxBrightenDelta = calculateBrightenThresholdDelta(this.mAmbientLuxNewMax);
        float ambientLuxNewMinBrightenDelta = calculateBrightenThresholdDelta(this.mAmbientLuxNewMin);
        if (this.mAmbientLux - ambientLuxDarkenDelta > this.mAmbientLuxNewMax - 1.0E-5f) {
            Slog.i(TAG, " Reset mAmbientLuxNewMax:mAmbientLux" + this.mAmbientLux + ", ambientLuxDarkenDelta=" + ambientLuxDarkenDelta + ", mAmbientLuxNewMax=" + this.mAmbientLuxNewMax);
            this.mAmbientLuxNewMax = this.mAmbientLux;
        }
        if (this.mAmbientLux > (this.mAmbientLuxNewMax + ambientLuxNewMaxBrightenDelta) - 1.0E-5f) {
            Slog.i(TAG, " Reset mAmbientLuxNewMax:mAmbientLux" + this.mAmbientLux + ", ambientLuxNewMaxBrightenDelta=" + ambientLuxNewMaxBrightenDelta + ", mAmbientLuxNewMax=" + this.mAmbientLuxNewMax);
            this.mAmbientLuxNewMax = this.mAmbientLux;
        }
        if (this.mAmbientLux - ambientLuxDarkenDelta > this.mAmbientLuxNewMin - 1.0E-5f) {
            Slog.i(TAG, " Reset mAmbientLuxNewMin:mAmbientLux" + this.mAmbientLux + ", ambientLuxDarkenDelta=" + ambientLuxDarkenDelta + ", mAmbientLuxNewMin=" + this.mAmbientLuxNewMin);
            this.mAmbientLuxNewMin = this.mAmbientLux;
        }
        if (this.mAmbientLux > (this.mAmbientLuxNewMin + ambientLuxNewMinBrightenDelta) - 1.0E-5f) {
            Slog.i(TAG, " Reset mAmbientLuxNewMin:mAmbientLux" + this.mAmbientLux + ", ambientLuxNewMinBrightenDelta=" + ambientLuxNewMinBrightenDelta + ", mAmbientLuxNewMin=" + this.mAmbientLuxNewMin);
            this.mAmbientLuxNewMin = this.mAmbientLux;
        }
    }

    public boolean needToUpdateBrightness() {
        return this.mNeedToUpdateBrightness;
    }

    public boolean brightnessUpdated() {
        this.mNeedToUpdateBrightness = false;
        return false;
    }

    public boolean needToSendUpdateAmbientLuxMsg() {
        return this.mNextTransitionTime > 0;
    }

    public long getSendUpdateAmbientLuxMsgTime() {
        return this.mNextTransitionTime;
    }

    private long getNextAmbientLightBrighteningTime(long earliedtime) {
        if (this.mIsCoverModeFastResponseFlag) {
            return this.mData.coverModeBrightenResponseTime + earliedtime;
        }
        if (this.mPowerStatus) {
            return 500 + earliedtime;
        }
        return this.mNormBrighenDebounceTime + earliedtime;
    }

    private long getNextAmbientLightDarkeningTime(long earliedtime) {
        if (this.mIsCoverModeFastResponseFlag) {
            return this.mData.coverModeDarkenResponseTime + earliedtime;
        }
        if (this.mPowerStatus) {
            return 1000 + earliedtime;
        }
        return this.mNormDarkenDebounceTime + earliedtime;
    }

    public void setPowerStatus(boolean powerStatus) {
        this.mPowerStatus = powerStatus;
    }

    public void clear() {
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.d(TAG, "clear buffer data and algo flags");
            }
            this.mLastCloseScreenLux = this.mAmbientLux;
            if (DEBUG) {
                Slog.d(TAG, "LabcCoverMode clear: mLastCloseScreenLux=" + this.mLastCloseScreenLux);
            }
            this.mIsCoverModeFastResponseFlag = false;
            this.mAutoBrightnessIntervened = false;
            this.mAmbientLightRingBuffer.clear();
            this.mAmbientLightRingBufferFilter.clear();
            this.mAmbientLightRingBufferScene.clear();
        }
    }

    private void updateBuffer(long time, float ambientLux, int horizon) {
        this.mAmbientLightRingBufferFilter.push(time, ambientLux);
        this.mAmbientLightRingBufferFilter.prune(time - ((long) horizon));
    }

    private void updateBufferForScene(long time, float ambientLux, int horizon) {
        this.mAmbientLightRingBufferScene.push(time, ambientLux);
        this.mAmbientLightRingBufferScene.prune(time - ((long) horizon));
    }

    private void updatepara(HwRingBuffer buffer, float lux) {
        float stability = calculateStability(buffer);
        float stabilityForSmallThr = calculateStabilityForSmallThr(buffer);
        if (stability > 100.0f) {
            this.mStability = 100.0f;
        } else if (stability < ((float) this.mData.stabilityConstant)) {
            this.mStability = (float) this.mData.stabilityConstant;
        } else {
            this.mStability = stability;
        }
        if (stabilityForSmallThr > 100.0f) {
            this.mStabilityForSmallThr = 100.0f;
        } else {
            this.mStabilityForSmallThr = stabilityForSmallThr;
        }
        float mLux = (float) Math.round(lux);
        if (mLux >= this.mData.brightTimeDelayLuxThreshold || this.mlastFilterLux >= this.mData.brightTimeDelayLuxThreshold || !this.mData.brightTimeDelayEnable) {
            this.mNormBrighenDebounceTime = (long) (((float) this.mData.brighenDebounceTime) * (((this.mData.brightenDebounceTimeParaBig * (this.mStability - ((float) this.mData.stabilityConstant))) / 100.0f) + HwCircleAnimation.SMALL_ALPHA));
        } else {
            this.mNormBrighenDebounceTime = (long) this.mData.brightTimeDelay;
        }
        if (mLux >= this.mData.darkTimeDelayLuxThreshold || !this.mData.darkTimeDelayEnable) {
            this.mNormDarkenDebounceTime = (long) (((float) this.mData.darkenDebounceTime) * (((this.mData.darkenDebounceTimeParaBig * (this.mStability - ((float) this.mData.stabilityConstant))) / 100.0f) + HwCircleAnimation.SMALL_ALPHA));
        } else {
            float ambientLuxDarkenDelta = calculateDarkenThresholdDelta(this.mAmbientLux);
            float currentAmbientLux = buffer.getLux(buffer.size() - 1);
            float luxNormalizedFactor = ((this.mData.darkTimeDelayBeta2 * (this.mAmbientLux - currentAmbientLux)) + (this.mData.darkTimeDelayBeta1 * ((this.mAmbientLux - currentAmbientLux) - ambientLuxDarkenDelta))) + HwCircleAnimation.SMALL_ALPHA;
            if (luxNormalizedFactor < 0.001f) {
                this.mNormDarkenDebounceTime = (long) (((float) this.mData.darkTimeDelay) + this.mData.darkTimeDelayBeta0);
            } else {
                this.mNormDarkenDebounceTime = ((long) this.mData.darkTimeDelay) + ((long) ((this.mData.darkTimeDelayBeta0 * ((this.mData.darkTimeDelayBeta2 * HwCircleAnimation.SMALL_ALPHA) + HwCircleAnimation.SMALL_ALPHA)) / luxNormalizedFactor));
            }
        }
        this.mNormBrighenDebounceTimeForSmallThr = (long) this.mData.brighenDebounceTimeForSmallThr;
        this.mNormDarkenDebounceTimeForSmallThr = (long) this.mData.darkenDebounceTimeForSmallThr;
        setDarkenThresholdNew(this.mAmbientLuxNewMin);
        setBrightenThresholdNew(this.mAmbientLuxNewMax);
    }

    private void setBrightenThresholdNew(float amLux) {
        this.mBrightenDeltaLuxMax = calculateBrightenThresholdDelta(amLux);
        this.mBrightenDeltaLuxMin = this.mBrightenDeltaLuxMax * this.mData.ratioForBrightnenSmallThr;
        this.mBrightenDeltaLuxMax *= ((this.mData.brightenDeltaLuxPara * (this.mStability - ((float) this.mData.stabilityConstant))) / 100.0f) + HwCircleAnimation.SMALL_ALPHA;
    }

    private void setDarkenThresholdNew(float amLux) {
        this.mDarkenDeltaLuxMax = calculateDarkenThresholdDelta(amLux);
        if (this.mAmbientLux < 10.0f) {
            this.mDarkenDeltaLuxMin = this.mDarkenDeltaLuxMax;
        } else {
            this.mDarkenDeltaLuxMin = this.mDarkenDeltaLuxMax * this.mData.ratioForDarkenSmallThr;
        }
        this.mDarkenDeltaLuxMax *= ((this.mData.darkenDeltaLuxPara * (this.mStability - ((float) this.mData.stabilityConstant))) / 100.0f) + HwCircleAnimation.SMALL_ALPHA;
    }

    private float calculateBrightenThresholdDelta(float amLux) {
        float brightenThreshold = 0.0f;
        int count = 0;
        PointF temp1 = null;
        for (PointF temp : this.mData.brightenlinePoints) {
            if (count == 0) {
                temp1 = temp;
            }
            if (amLux < temp.x) {
                PointF temp2 = temp;
                if (temp.x > temp1.x) {
                    return (((temp.y - temp1.y) / (temp.x - temp1.x)) * (amLux - temp1.x)) + temp1.y;
                }
                if (!DEBUG) {
                    return HwCircleAnimation.SMALL_ALPHA;
                }
                Slog.i(TAG, "Brighten_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                return HwCircleAnimation.SMALL_ALPHA;
            }
            temp1 = temp;
            brightenThreshold = temp.y;
            count++;
        }
        return brightenThreshold;
    }

    private float calculateDarkenThresholdDelta(float amLux) {
        float darkenThreshold = 0.0f;
        int count = 0;
        PointF temp1 = null;
        for (PointF temp : this.mData.darkenlinePoints) {
            if (count == 0) {
                temp1 = temp;
            }
            if (amLux < temp.x) {
                PointF temp2 = temp;
                if (temp.x > temp1.x) {
                    return Math.max((((temp.y - temp1.y) / (temp.x - temp1.x)) * (amLux - temp1.x)) + temp1.y, HwCircleAnimation.SMALL_ALPHA);
                }
                if (!DEBUG) {
                    return HwCircleAnimation.SMALL_ALPHA;
                }
                Slog.i(TAG, "Darken_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                return HwCircleAnimation.SMALL_ALPHA;
            }
            temp1 = temp;
            darkenThreshold = temp.y;
            count++;
        }
        return darkenThreshold;
    }

    private float prefilterAmbientLux(long now, int filterMethodNum) {
        if (filterMethodNum == 1) {
            return prefilterMeanFilter(now);
        }
        if (filterMethodNum == 2) {
            return prefilterWeightedMeanFilter(now);
        }
        return prefilterNoFilter(now);
    }

    private float prefilterNoFilter(long now) {
        int N = this.mAmbientLightRingBuffer.size();
        if (N != 0) {
            return this.mAmbientLightRingBuffer.getLux(N - 1);
        }
        Slog.e(TAG, "prefilterNoFilter: No ambient light readings available, return 0");
        return 0.0f;
    }

    private float prefilterMeanFilter(long now) {
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "prefilterMeanFilter: No ambient light readings available, return 0");
            return 0.0f;
        } else if (this.mData.preMeanFilterNum <= 0 || this.mData.preMeanFilterNoFilterNum < this.mData.preMeanFilterNum) {
            Slog.e(TAG, "prefilterMeanFilter: ErrorPara, return 0, MeanFilterNum=" + this.mData.preMeanFilterNum + ",MeanFilterNoFilterNum=" + this.mData.preMeanFilterNoFilterNum);
            return 0.0f;
        } else if (N <= this.mData.preMeanFilterNoFilterNum) {
            return this.mAmbientLightRingBuffer.getLux(N - 1);
        } else {
            float sum = 0.0f;
            for (int i = N - 1; i >= N - this.mData.preMeanFilterNum; i--) {
                sum += this.mAmbientLightRingBuffer.getLux(i);
            }
            return (float) Math.round(sum / ((float) this.mData.preMeanFilterNum));
        }
    }

    private float prefilterWeightedMeanFilter(long now) {
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "prefilterWeightedMeanFilter: No ambient light readings available, return 0");
            return 0.0f;
        } else if (this.mData.preWeightedMeanFilterNum <= 0 || this.mData.preWeightedMeanFilterNoFilterNum < this.mData.preWeightedMeanFilterNum) {
            Slog.e(TAG, "prefilterWeightedMeanFilter: ErrorPara, return 0, WeightedMeanFilterNum=" + this.mData.preWeightedMeanFilterNum + ",WeightedMeanFilterNoFilterNum=" + this.mData.preWeightedMeanFilterNoFilterNum);
            return 0.0f;
        } else {
            float tempLux = this.mAmbientLightRingBuffer.getLux(N - 1);
            if (N <= this.mData.preWeightedMeanFilterNoFilterNum) {
                return tempLux;
            }
            int i;
            float maxLux = 0.0f;
            float sum = 0.0f;
            float totalWeight = 0.0f;
            for (i = N - 1; i >= N - this.mData.preWeightedMeanFilterMaxFuncLuxNum; i--) {
                tempLux = this.mAmbientLightRingBuffer.getLux(i);
                if (tempLux >= maxLux) {
                    maxLux = tempLux;
                }
            }
            for (i = N - 1; i >= N - this.mData.preWeightedMeanFilterNum; i--) {
                float weight;
                if (this.mAmbientLightRingBuffer.getLux(i) != 0.0f || maxLux > this.mData.preWeightedMeanFilterLuxTh) {
                    weight = HwCircleAnimation.SMALL_ALPHA;
                } else {
                    weight = this.mData.preWeightedMeanFilterAlpha * HwCircleAnimation.SMALL_ALPHA;
                }
                totalWeight += weight;
                sum += this.mAmbientLightRingBuffer.getLux(i) * weight;
            }
            return (float) Math.round(sum / totalWeight);
        }
    }

    private float getOrigLastAmbientLux(long now) {
        int N = this.mAmbientLightRingBuffer.size();
        if (N != 0) {
            return this.mAmbientLightRingBuffer.getLux(N - 1);
        }
        Slog.e(TAG, "OrigAmbient: No ambient light readings available, return 0");
        return 0.0f;
    }

    private float getFilterLastAmbientLux(long now) {
        int N = this.mAmbientLightRingBufferFilter.size();
        if (N != 0) {
            return this.mAmbientLightRingBufferFilter.getLux(N - 1);
        }
        Slog.e(TAG, "FilterLastAmbient: No ambient light readings available, return 0");
        return 0.0f;
    }

    private float postfilterAmbientLux(long now, int filterMethodNum) {
        if (filterMethodNum == 1) {
            return postfilterMeanFilter(now);
        }
        if (filterMethodNum == 2) {
            return postfilterMaxMinAvgFilter(now);
        }
        return postfilterNoFilter(now);
    }

    private float postfilterNoFilter(long now) {
        int N = this.mAmbientLightRingBufferFilter.size();
        if (N != 0) {
            return this.mAmbientLightRingBufferFilter.getLux(N - 1);
        }
        Slog.e(TAG, "postfilterNoFilter: No ambient light readings available, return 0");
        return 0.0f;
    }

    private float postfilterMeanFilter(long now) {
        int N = this.mAmbientLightRingBufferFilter.size();
        if (N == 0) {
            Slog.e(TAG, "prefilterMeanFilter: No ambient light readings available, return 0");
            return 0.0f;
        } else if (this.mData.postMeanFilterNum <= 0 || this.mData.postMeanFilterNoFilterNum < this.mData.postMeanFilterNum) {
            Slog.e(TAG, "postfilterMeanFilter: ErrorPara, return 0, MeanFilterNum=" + this.mData.postMeanFilterNum + ",MeanFilterNoFilterNum=" + this.mData.postMeanFilterNum);
            return 0.0f;
        } else if (N <= this.mData.postMeanFilterNoFilterNum) {
            return this.mAmbientLightRingBufferFilter.getLux(N - 1);
        } else {
            float sum = 0.0f;
            for (int i = N - 1; i >= N - this.mData.postMeanFilterNum; i--) {
                sum += this.mAmbientLightRingBufferFilter.getLux(i);
            }
            return (float) Math.round(sum / ((float) this.mData.postMeanFilterNum));
        }
    }

    private float postfilterMaxMinAvgFilter(long now) {
        int N = this.mAmbientLightRingBufferFilter.size();
        if (N == 0) {
            Slog.e(TAG, "postfilterMaxMinAvgFilter: No ambient light readings available, return 0");
            return 0.0f;
        } else if (this.mData.postMaxMinAvgFilterNum <= 0 || this.mData.postMaxMinAvgFilterNoFilterNum < this.mData.postMaxMinAvgFilterNum) {
            Slog.e(TAG, "postfilterMaxMinAvgFilter: ErrorPara, return 0, PostMaxMinAvgFilterNoFilterNum=" + this.mData.postMaxMinAvgFilterNoFilterNum + ",PostMaxMinAvgFilterNum=" + this.mData.postMaxMinAvgFilterNum);
            return 0.0f;
        } else if (N <= this.mData.postMaxMinAvgFilterNoFilterNum) {
            return this.mAmbientLightRingBufferFilter.getLux(N - 1);
        } else {
            float sum = this.mAmbientLightRingBufferFilter.getLux(N - 1);
            float luxMin = this.mAmbientLightRingBufferFilter.getLux(N - 1);
            float luxMax = this.mAmbientLightRingBufferFilter.getLux(N - 1);
            for (int i = N - 2; i >= N - this.mData.postMaxMinAvgFilterNum; i--) {
                if (luxMin > this.mAmbientLightRingBufferFilter.getLux(i)) {
                    luxMin = this.mAmbientLightRingBufferFilter.getLux(i);
                }
                if (luxMax < this.mAmbientLightRingBufferFilter.getLux(i)) {
                    luxMax = this.mAmbientLightRingBufferFilter.getLux(i);
                }
                sum += this.mAmbientLightRingBufferFilter.getLux(i);
            }
            return ((sum - luxMin) - luxMax) / 3.0f;
        }
    }

    private long nextAmbientLightBrighteningTransition(long time) {
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBufferFilter.size() - 1; i >= 0; i--) {
            boolean BrightenChange;
            if (this.mAmbientLightRingBufferFilter.getLux(i) - this.mAmbientLuxNewMax > this.mBrightenDeltaLuxMax) {
                BrightenChange = true;
            } else {
                BrightenChange = false;
            }
            if (!BrightenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return getNextAmbientLightBrighteningTime(earliestValidTime);
    }

    private long nextAmbientLightBrighteningTransitionForSmallThr(long time) {
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBufferFilter.size() - 1; i >= 0; i--) {
            boolean BrightenChange;
            if (this.mAmbientLightRingBufferFilter.getLux(i) - this.mAmbientLux > this.mBrightenDeltaLuxMin) {
                BrightenChange = true;
            } else {
                BrightenChange = false;
            }
            if (!BrightenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return earliestValidTime + this.mNormBrighenDebounceTimeForSmallThr;
    }

    private long nextAmbientLightDarkeningTransition(long time) {
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBufferFilter.size() - 1; i >= 0; i--) {
            boolean DarkenChange;
            if (this.mAmbientLuxNewMin - this.mAmbientLightRingBufferFilter.getLux(i) >= this.mDarkenDeltaLuxMax) {
                DarkenChange = true;
            } else {
                DarkenChange = false;
            }
            if (!DarkenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return getNextAmbientLightDarkeningTime(earliestValidTime);
    }

    private long nextAmbientLightDarkeningTransitionForSmallThr(long time) {
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBufferFilter.size() - 1; i >= 0; i--) {
            boolean DarkenChange;
            if (this.mAmbientLux - this.mAmbientLightRingBufferFilter.getLux(i) >= this.mDarkenDeltaLuxMin) {
                DarkenChange = true;
            } else {
                DarkenChange = false;
            }
            if (!DarkenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return earliestValidTime + this.mNormDarkenDebounceTimeForSmallThr;
    }

    private boolean decideToBrighten(float ambientLux) {
        boolean needToBrighten;
        if (ambientLux - this.mAmbientLuxNewMax < this.mBrightenDeltaLuxMax || this.mStability >= this.mStabilityBrightenConstant) {
            needToBrighten = false;
        } else {
            needToBrighten = true;
        }
        if (!needToBrighten || this.mAutoBrightnessIntervened) {
            return false;
        }
        return true;
    }

    private boolean decideToBrightenForSmallThr(float ambientLux) {
        boolean needToBrighten;
        if (ambientLux - this.mAmbientLux < this.mBrightenDeltaLuxMin || this.mStabilityForSmallThr >= this.mStabilityBrightenConstantForSmallThr) {
            needToBrighten = false;
        } else {
            needToBrighten = true;
        }
        if (!needToBrighten || this.mAutoBrightnessIntervened) {
            return false;
        }
        return true;
    }

    private boolean decideToDarken(float ambientLux) {
        boolean needToDarken;
        if (this.mAmbientLuxNewMin - ambientLux < this.mDarkenDeltaLuxMax || this.mStability > this.mStabilityDarkenConstant) {
            needToDarken = false;
        } else {
            needToDarken = true;
        }
        if (!needToDarken || this.mAutoBrightnessIntervened || this.mProximityPositiveStatus) {
            return false;
        }
        return true;
    }

    private boolean decideToDarkenForSmallThr(float ambientLux) {
        boolean needToDarken;
        if (this.mAmbientLux - ambientLux < this.mDarkenDeltaLuxMin || this.mStabilityForSmallThr > this.mStabilityDarkenConstantForSmallThr) {
            needToDarken = false;
        } else {
            needToDarken = true;
        }
        if (!needToDarken || this.mAutoBrightnessIntervened || this.mProximityPositiveStatus) {
            return false;
        }
        return true;
    }

    private float calculateStability(HwRingBuffer buffer) {
        int N = buffer.size();
        if (N <= 1) {
            return 0.0f;
        }
        int index1;
        int index2;
        float tmp;
        float Stability1;
        float Stability2;
        float Stability;
        float currentLux = buffer.getLux(N - 1);
        calculateAvg(buffer);
        float luxT1 = currentLux;
        float luxT2 = currentLux;
        int T1 = 0;
        int T2 = 0;
        int index = 0;
        float luxT1Min = currentLux;
        float luxT2Min = currentLux;
        int indexMin = 0;
        float luxT1Max = currentLux;
        float luxT2Max = currentLux;
        int indexMax = 0;
        int j = 0;
        while (j < N - 1) {
            Object obj;
            float lux1 = buffer.getLux((N - 1) - j);
            float lux2 = buffer.getLux(((N - 1) - j) - 1);
            if (this.mLuxBufferAvg > lux1 || this.mLuxBufferAvg < lux2) {
                if (this.mLuxBufferAvg >= lux1 && this.mLuxBufferAvg <= lux2) {
                }
                if (this.mLuxBufferAvgMin > lux1 || this.mLuxBufferAvgMin < lux2) {
                    if (this.mLuxBufferAvgMin >= lux1 && this.mLuxBufferAvgMin <= lux2) {
                    }
                    if (this.mLuxBufferAvgMax > lux1 || this.mLuxBufferAvgMax < lux2) {
                        if (this.mLuxBufferAvgMax >= lux1 && this.mLuxBufferAvgMax <= lux2) {
                        }
                        if (!(index == 0 || (indexMin == 0 && indexMax == 0))) {
                            if (index <= indexMin || index < indexMax) {
                                if (index >= indexMin && index <= indexMax) {
                                    break;
                                }
                            }
                            break;
                        }
                        j++;
                    }
                    obj = (Math.abs(this.mLuxBufferAvgMax - lux1) < 1.0E-7f || Math.abs(this.mLuxBufferAvgMax - lux2) >= 1.0E-7f) ? null : 1;
                    if (obj == null) {
                        luxT1Max = lux1;
                        luxT2Max = lux2;
                        indexMax = j;
                    }
                    if (index <= indexMin) {
                    }
                    break;
                }
                obj = (Math.abs(this.mLuxBufferAvgMin - lux1) < 1.0E-7f || Math.abs(this.mLuxBufferAvgMin - lux2) >= 1.0E-7f) ? null : 1;
                if (obj == null) {
                    luxT1Min = lux1;
                    luxT2Min = lux2;
                    indexMin = j;
                }
                if (Math.abs(this.mLuxBufferAvgMax - lux1) < 1.0E-7f) {
                }
                if (obj == null) {
                    luxT1Max = lux1;
                    luxT2Max = lux2;
                    indexMax = j;
                }
                if (index <= indexMin) {
                }
                break;
            }
            obj = (Math.abs(this.mLuxBufferAvg - lux1) >= 1.0E-7f || Math.abs(this.mLuxBufferAvg - lux2) >= 1.0E-7f) ? null : 1;
            if (obj == null) {
                luxT1 = lux1;
                luxT2 = lux2;
                T1 = (N - 1) - j;
                T2 = ((N - 1) - j) - 1;
                index = j;
            }
            if (Math.abs(this.mLuxBufferAvgMin - lux1) < 1.0E-7f) {
            }
            if (obj == null) {
                luxT1Min = lux1;
                luxT2Min = lux2;
                indexMin = j;
            }
            if (Math.abs(this.mLuxBufferAvgMax - lux1) < 1.0E-7f) {
            }
            if (obj == null) {
                luxT1Max = lux1;
                luxT2Max = lux2;
                indexMax = j;
            }
            if (index <= indexMin) {
            }
        }
        if (indexMax <= indexMin) {
            index1 = indexMax;
            index2 = indexMin;
        } else {
            index1 = indexMin;
            index2 = indexMax;
        }
        int k1 = (N - 1) - index1;
        while (k1 <= N - 1 && k1 != N - 1) {
            float luxk1 = buffer.getLux(k1);
            float luxk2 = buffer.getLux(k1 + 1);
            if (indexMax > indexMin) {
                if (luxk1 <= luxk2) {
                    break;
                }
                T1 = k1 + 1;
            } else if (luxk1 >= luxk2) {
                break;
            } else {
                T1 = k1 + 1;
            }
            k1++;
        }
        int k3 = (N - 1) - index2;
        while (k3 >= 0 && k3 != 0) {
            float luxk3 = buffer.getLux(k3);
            float luxk4 = buffer.getLux(k3 - 1);
            if (indexMax > indexMin) {
                if (luxk3 >= luxk4) {
                    break;
                }
                T2 = k3 - 1;
            } else if (luxk3 <= luxk4) {
                break;
            } else {
                T2 = k3 - 1;
            }
            k3--;
        }
        int t1 = (N - 1) - T1;
        int t2 = T2;
        float s1 = calculateStabilityFactor(buffer, T1, N - 1);
        float avg1 = calcluateAvg(buffer, T1, N - 1);
        float s2 = calculateStabilityFactor(buffer, 0, T2);
        float deltaAvg = Math.abs(avg1 - calcluateAvg(buffer, 0, T2));
        float k = 0.0f;
        if (T1 != T2) {
            k = Math.abs((buffer.getLux(T1) - buffer.getLux(T2)) / ((float) (T1 - T2)));
        }
        if (k < 10.0f / (5.0f + k)) {
            tmp = k;
        } else {
            tmp = 10.0f / (5.0f + k);
        }
        if (tmp > 20.0f / (10.0f + deltaAvg)) {
            tmp = 20.0f / (10.0f + deltaAvg);
        }
        if (t1 > this.mData.stabilityTime1) {
            Stability1 = s1;
        } else {
            float a1 = (float) Math.exp((double) (t1 - this.mData.stabilityTime1));
            float b1 = (float) (this.mData.stabilityTime1 - t1);
            float s3 = tmp;
            Stability1 = ((a1 * s1) + (b1 * tmp)) / (a1 + b1);
        }
        if (t2 > this.mData.stabilityTime2) {
            Stability2 = s2;
        } else {
            float a2 = (float) Math.exp((double) (t2 - this.mData.stabilityTime2));
            float b2 = (float) (this.mData.stabilityTime2 - t2);
            float s4 = tmp;
            Stability2 = ((a2 * s2) + (b2 * tmp)) / (a2 + b2);
        }
        if (t1 > this.mData.stabilityTime1) {
            Stability = Stability1;
        } else {
            float a = (float) Math.exp((double) (t1 - this.mData.stabilityTime1));
            float b = (float) (this.mData.stabilityTime1 - t1);
            Stability = ((a * Stability1) + (b * Stability2)) / (a + b);
        }
        return Stability;
    }

    private void calculateAvg(HwRingBuffer buffer) {
        int N = buffer.size();
        if (N != 0) {
            float currentLux = buffer.getLux(N - 1);
            float luxBufferSum = 0.0f;
            float luxBufferMin = currentLux;
            float luxBufferMax = currentLux;
            for (int i = N - 1; i >= 0; i--) {
                float lux = buffer.getLux(i);
                if (lux > luxBufferMax) {
                    luxBufferMax = lux;
                }
                if (lux < luxBufferMin) {
                    luxBufferMin = lux;
                }
                luxBufferSum += lux;
            }
            this.mLuxBufferAvg = luxBufferSum / ((float) N);
            this.mLuxBufferAvgMax = (this.mLuxBufferAvg + luxBufferMax) / 2.0f;
            this.mLuxBufferAvgMin = (this.mLuxBufferAvg + luxBufferMin) / 2.0f;
        }
    }

    private float calculateStabilityForSmallThr(HwRingBuffer buffer) {
        int N = buffer.size();
        if (N <= 1) {
            return 0.0f;
        }
        if (N <= 15) {
            return calculateStabilityFactor(buffer, 0, N - 1);
        }
        return calculateStabilityFactor(buffer, 0, 14);
    }

    private float calcluateAvg(HwRingBuffer buffer, int start, int end) {
        float sum = 0.0f;
        for (int i = start; i <= end; i++) {
            sum += buffer.getLux(i);
        }
        if (end < start) {
            return 0.0f;
        }
        return sum / ((float) ((end - start) + 1));
    }

    private float calculateStabilityFactor(HwRingBuffer buffer, int start, int end) {
        int size = (end - start) + 1;
        float sum = 0.0f;
        float sigma = 0.0f;
        if (size <= 1) {
            return 0.0f;
        }
        int i;
        for (i = start; i <= end; i++) {
            sum += buffer.getLux(i);
        }
        float avg = sum / ((float) size);
        for (i = start; i <= end; i++) {
            sigma += (buffer.getLux(i) - avg) * (buffer.getLux(i) - avg);
        }
        float ss = sigma / ((float) (size - 1));
        if (avg == 0.0f) {
            return 0.0f;
        }
        return ss / avg;
    }

    public boolean reportValueWhenSensorOnChange() {
        return this.mData.reportValueWhenSensorOnChange;
    }

    public int getProximityState() {
        return this.mProximity;
    }

    public boolean needToUseProximity() {
        return this.mAllowLabcUseProximity;
    }

    public boolean needToSendProximityDebounceMsg() {
        return this.mNeedToSendProximityDebounceMsg;
    }

    public long getPendingProximityDebounceTime() {
        return this.mPendingProximityDebounceTime;
    }

    public void setCoverModeStatus(boolean isclosed) {
        if (!isclosed && this.mIsclosed) {
            this.mCoverState = true;
        }
        this.mIsclosed = isclosed;
    }

    public void setCoverModeFastResponseFlag(boolean isFast) {
        this.mIsCoverModeFastResponseFlag = isFast;
        if (DEBUG) {
            Slog.i(TAG, "LabcCoverMode mIsCoverModeFastResponseFlag=" + this.mIsCoverModeFastResponseFlag);
        }
    }

    public boolean getLastCloseScreenEnable() {
        return !this.mData.lastCloseScreenEnable;
    }

    private void setProximityState(boolean proximityPositive) {
        this.mProximityPositiveStatus = proximityPositive;
        if (!this.mProximityPositiveStatus) {
            this.mNeedToUpdateBrightness = true;
            if (DEBUG) {
                Slog.i(TAG, "Proximity sets brightness");
            }
        }
    }

    private void clearPendingProximityDebounceTime() {
        if (this.mPendingProximityDebounceTime >= 0) {
            this.mPendingProximityDebounceTime = -1;
        }
    }

    public void handleProximitySensorEvent(long time, boolean positive) {
        if (this.mPendingProximity == 0 && !positive) {
            return;
        }
        if (this.mPendingProximity != 1 || !positive) {
            if (positive) {
                this.mPendingProximity = 1;
                this.mPendingProximityDebounceTime = ((long) this.mProximityPositiveDebounceTime) + time;
            } else {
                this.mPendingProximity = 0;
                this.mPendingProximityDebounceTime = ((long) this.mProximityNegativeDebounceTime) + time;
            }
            debounceProximitySensor();
        }
    }

    public void debounceProximitySensor() {
        this.mNeedToSendProximityDebounceMsg = false;
        if (this.mPendingProximity != -1 && this.mPendingProximityDebounceTime >= 0) {
            if (this.mPendingProximityDebounceTime <= SystemClock.uptimeMillis()) {
                this.mProximity = this.mPendingProximity;
                if (this.mProximity == 1) {
                    setProximityState(true);
                } else if (this.mProximity == 0) {
                    setProximityState(false);
                }
                if (DEBUG) {
                    Slog.d(TAG, "debounceProximitySensor:mProximity=" + this.mProximity);
                }
                clearPendingProximityDebounceTime();
                return;
            }
            this.mNeedToSendProximityDebounceMsg = true;
        }
    }

    public int getpowerOnFastResponseLuxNum() {
        return this.mData.powerOnFastResponseLuxNum;
    }
}
