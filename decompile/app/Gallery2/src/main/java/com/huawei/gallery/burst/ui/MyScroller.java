package com.huawei.gallery.burst.ui;

import android.content.Context;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.amap.api.maps.model.WeightedLatLng;
import com.autonavi.amap.mapcore.MapConfig;
import com.huawei.watermark.manager.parse.WMElement;

public class MyScroller {
    private static float DECELERATION_RATE = ((float) (Math.log(0.78d) / Math.log(0.9d)));
    private static final float[] SPLINE_POSITION = new float[101];
    private static final float[] SPLINE_TIME = new float[101];
    private static float sViscousFluidNormalize;
    private static float sViscousFluidScale = 8.0f;
    private float mCurrVelocity;
    private int mCurrentX;
    private int mCurrentY;
    private float mDeceleration;
    private float mDeltaX;
    private float mDeltaY;
    private int mDistance;
    private int mDuration;
    private float mDurationReciprocal;
    private int mFinalX;
    private int mFinalY;
    private boolean mFinished;
    private float mFlingFriction;
    private boolean mFlywheel;
    private Interpolator mInterpolator;
    private int mMaxX;
    private int mMaxY;
    private int mMinX;
    private int mMinY;
    private int mMode;
    private float mPhysicalCoeff;
    private final float mPpi;
    private long mStartTime;
    private int mStartX;
    private int mStartY;
    private float mVelocity;

    static {
        float x_min = 0.0f;
        float y_min = 0.0f;
        for (int i = 0; i < 100; i++) {
            float x;
            float coef;
            float y;
            float alpha = ((float) i) / 100.0f;
            float x_max = WMElement.CAMERASIZEVALUE1B1;
            while (true) {
                x = x_min + ((x_max - x_min) / 2.0f);
                coef = (MapConfig.MIN_ZOOM * x) * (WMElement.CAMERASIZEVALUE1B1 - x);
                float tx = ((((WMElement.CAMERASIZEVALUE1B1 - x) * 0.175f) + (0.35000002f * x)) * coef) + ((x * x) * x);
                if (((double) Math.abs(tx - alpha)) < 1.0E-5d) {
                    break;
                } else if (tx > alpha) {
                    x_max = x;
                } else {
                    x_min = x;
                }
            }
            SPLINE_POSITION[i] = ((((WMElement.CAMERASIZEVALUE1B1 - x) * 0.5f) + x) * coef) + ((x * x) * x);
            float y_max = WMElement.CAMERASIZEVALUE1B1;
            while (true) {
                y = y_min + ((y_max - y_min) / 2.0f);
                coef = (MapConfig.MIN_ZOOM * y) * (WMElement.CAMERASIZEVALUE1B1 - y);
                float dy = ((((WMElement.CAMERASIZEVALUE1B1 - y) * 0.5f) + y) * coef) + ((y * y) * y);
                if (((double) Math.abs(dy - alpha)) < 1.0E-5d) {
                    break;
                } else if (dy > alpha) {
                    y_max = y;
                } else {
                    y_min = y;
                }
            }
            SPLINE_TIME[i] = ((((WMElement.CAMERASIZEVALUE1B1 - y) * 0.175f) + (0.35000002f * y)) * coef) + ((y * y) * y);
        }
        float[] fArr = SPLINE_POSITION;
        SPLINE_TIME[100] = WMElement.CAMERASIZEVALUE1B1;
        fArr[100] = WMElement.CAMERASIZEVALUE1B1;
        sViscousFluidNormalize = WMElement.CAMERASIZEVALUE1B1;
        sViscousFluidNormalize = WMElement.CAMERASIZEVALUE1B1 / viscousFluid(WMElement.CAMERASIZEVALUE1B1);
    }

    public MyScroller(Context context, Interpolator interpolator) {
        this(context, interpolator, context.getApplicationInfo().targetSdkVersion >= 11);
    }

    public MyScroller(Context context, Interpolator interpolator, boolean flywheel) {
        this.mFlingFriction = ViewConfiguration.getScrollFriction();
        this.mFinished = true;
        this.mInterpolator = interpolator;
        this.mPpi = context.getResources().getDisplayMetrics().density * 160.0f;
        this.mDeceleration = computeDeceleration(ViewConfiguration.getScrollFriction());
        this.mFlywheel = flywheel;
        this.mPhysicalCoeff = computeDeceleration(0.84f);
    }

    private float computeDeceleration(float fraction) {
        return (this.mPpi * 386.0878f) * fraction;
    }

    public final boolean isFinished() {
        return this.mFinished;
    }

    public final void forceFinished(boolean finished) {
        this.mFinished = finished;
    }

    public final int getCurrX() {
        return this.mCurrentX;
    }

    public float getCurrVelocity() {
        return this.mMode == 1 ? this.mCurrVelocity : this.mVelocity - ((this.mDeceleration * ((float) timePassed())) / 2000.0f);
    }

    public boolean computeScrollOffset() {
        if (this.mFinished) {
            return false;
        }
        int timePassed = (int) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime);
        if (timePassed < this.mDuration) {
            switch (this.mMode) {
                case 0:
                    float x = ((float) timePassed) * this.mDurationReciprocal;
                    if (this.mInterpolator == null) {
                        x = viscousFluid(x);
                    } else {
                        x = this.mInterpolator.getInterpolation(x);
                    }
                    this.mCurrentX = this.mStartX + Math.round(this.mDeltaX * x);
                    this.mCurrentY = this.mStartY + Math.round(this.mDeltaY * x);
                    break;
                case 1:
                    float t = ((float) timePassed) / ((float) this.mDuration);
                    int idx = (int) (100.0f * t);
                    float distanceCoef = WMElement.CAMERASIZEVALUE1B1;
                    float velocityCoef = 0.0f;
                    if (idx < 100) {
                        float t_inf = ((float) idx) / 100.0f;
                        float t_sup = ((float) (idx + 1)) / 100.0f;
                        float d_inf = SPLINE_POSITION[idx];
                        velocityCoef = (SPLINE_POSITION[idx + 1] - d_inf) / (t_sup - t_inf);
                        distanceCoef = d_inf + ((t - t_inf) * velocityCoef);
                    }
                    this.mCurrVelocity = ((((float) this.mDistance) * velocityCoef) / ((float) this.mDuration)) * 1000.0f;
                    this.mCurrentX = this.mStartX + Math.round(((float) (this.mFinalX - this.mStartX)) * distanceCoef);
                    this.mCurrentX = Math.min(this.mCurrentX, this.mMaxX);
                    this.mCurrentX = Math.max(this.mCurrentX, this.mMinX);
                    this.mCurrentY = this.mStartY + Math.round(((float) (this.mFinalY - this.mStartY)) * distanceCoef);
                    this.mCurrentY = Math.min(this.mCurrentY, this.mMaxY);
                    this.mCurrentY = Math.max(this.mCurrentY, this.mMinY);
                    if (this.mCurrentX == this.mFinalX && this.mCurrentY == this.mFinalY) {
                        this.mFinished = true;
                        break;
                    }
            }
        }
        this.mCurrentX = this.mFinalX;
        this.mCurrentY = this.mFinalY;
        this.mFinished = true;
        return true;
    }

    public void startScroll(int startX, int startY, int deltaX, int deltaY, int duration) {
        this.mMode = 0;
        this.mFinished = false;
        this.mDuration = duration;
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mStartX = startX;
        this.mStartY = startY;
        this.mFinalX = startX + deltaX;
        this.mFinalY = startY + deltaY;
        this.mDeltaX = (float) deltaX;
        this.mDeltaY = (float) deltaY;
        this.mDurationReciprocal = WMElement.CAMERASIZEVALUE1B1 / ((float) this.mDuration);
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int offset, int singleWidth) {
        if (this.mFlywheel && !this.mFinished) {
            float oldVel = getCurrVelocity();
            float dx = (float) (this.mFinalX - this.mStartX);
            float dy = (float) (this.mFinalY - this.mStartY);
            float hyp = (float) Math.sqrt((double) ((dx * dx) + (dy * dy)));
            float oldVelocityX = (dx / hyp) * oldVel;
            float oldVelocityY = (dy / hyp) * oldVel;
            if (Math.signum((float) velocityX) == Math.signum(oldVelocityX) && Math.signum((float) velocityY) == Math.signum(oldVelocityY)) {
                velocityX = (int) (((float) velocityX) + oldVelocityX);
                velocityY = (int) (((float) velocityY) + oldVelocityY);
            }
        }
        this.mMode = 1;
        this.mFinished = false;
        float velocity = (float) Math.sqrt((double) ((velocityX * velocityX) + (velocityY * velocityY)));
        this.mVelocity = velocity;
        this.mDuration = getSplineFlingDuration(velocity);
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mStartX = startX;
        this.mStartY = startY;
        float coeffX = velocity == 0.0f ? WMElement.CAMERASIZEVALUE1B1 : ((float) velocityX) / velocity;
        float coeffY = velocity == 0.0f ? WMElement.CAMERASIZEVALUE1B1 : ((float) velocityY) / velocity;
        double totalDistance = getSplineFlingDistance(velocity);
        this.mDistance = (int) (((double) Math.signum(velocity)) * totalDistance);
        this.mMinX = minX;
        this.mMaxX = maxX;
        this.mMinY = minY;
        this.mMaxY = maxY;
        this.mFinalX = (((((((int) Math.round(((double) coeffX) * totalDistance)) + offset) + singleWidth) / singleWidth) * singleWidth) + offset) + startX;
        if (Math.abs(this.mFinalX - startX) < singleWidth && velocityX < 0) {
            this.mFinalX += (velocityX / Math.abs(velocityX)) * singleWidth;
        }
        if (Math.abs(this.mFinalX - startX) / this.mDuration > 2 && this.mDistance < 500) {
            this.mDuration = Math.abs(this.mFinalX - startX);
        }
        this.mFinalX = Math.min(this.mFinalX, this.mMaxX);
        this.mFinalX = Math.max(this.mFinalX, this.mMinX);
        this.mFinalY = ((int) Math.round(((double) coeffY) * totalDistance)) + startY;
        this.mFinalY = Math.min(this.mFinalY, this.mMaxY);
        this.mFinalY = Math.max(this.mFinalY, this.mMinY);
    }

    private double getSplineDeceleration(float velocity) {
        return Math.log((double) ((Math.abs(velocity) * 0.35f) / (this.mFlingFriction * this.mPhysicalCoeff)));
    }

    private int getSplineFlingDuration(float velocity) {
        return (int) (Math.exp(getSplineDeceleration(velocity) / (((double) DECELERATION_RATE) - WeightedLatLng.DEFAULT_INTENSITY)) * 1000.0d);
    }

    private double getSplineFlingDistance(float velocity) {
        return ((double) (this.mFlingFriction * this.mPhysicalCoeff)) * Math.exp((((double) DECELERATION_RATE) / (((double) DECELERATION_RATE) - WeightedLatLng.DEFAULT_INTENSITY)) * getSplineDeceleration(velocity));
    }

    static float viscousFluid(float posX) {
        posX *= sViscousFluidScale;
        if (posX < WMElement.CAMERASIZEVALUE1B1) {
            posX -= WMElement.CAMERASIZEVALUE1B1 - ((float) Math.exp((double) (-posX)));
        } else {
            posX = 0.36787945f + (0.63212055f * (WMElement.CAMERASIZEVALUE1B1 - ((float) Math.exp((double) (WMElement.CAMERASIZEVALUE1B1 - posX)))));
        }
        return posX * sViscousFluidNormalize;
    }

    public int timePassed() {
        return (int) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime);
    }
}
