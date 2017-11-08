package com.android.gallery3d.common;

import android.content.Context;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.amap.api.maps.model.WeightedLatLng;
import com.android.gallery3d.util.GalleryLog;
import com.autonavi.amap.mapcore.MapConfig;
import com.huawei.watermark.manager.parse.WMElement;

public class OverScroller {
    private final boolean mFlywheel;
    private Interpolator mInterpolator;
    private int mMode;
    private final SplineOverScroller mScrollerX;
    private final SplineOverScroller mScrollerY;

    static class SplineOverScroller {
        private static float DECELERATION_RATE = ((float) (Math.log(0.78d) / Math.log(0.9d)));
        private static float PHYSICAL_COEF;
        private static final float[] SPLINE_POSITION = new float[101];
        private static final float[] SPLINE_TIME = new float[101];
        private float mCurrVelocity;
        private int mCurrentPosition;
        private float mDeceleration;
        private int mDuration;
        private int mFinal;
        private boolean mFinished = true;
        private float mFlingFriction = ViewConfiguration.getScrollFriction();
        private int mOver;
        private float mPhysicalCoeff;
        private int mSplineDistance;
        private int mSplineDuration;
        private Interpolator mSpringBackInterpolator;
        private int mStart;
        private long mStartTime;
        private int mState = 0;
        private int mVelocity;

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
                    float tx = ((((WMElement.CAMERASIZEVALUE1B1 - x) * 0.2f) + (0.25f * x)) * coef) + ((x * x) * x);
                    if (((double) Math.abs(tx - alpha)) < 1.0E-5d) {
                        break;
                    } else if (tx > alpha) {
                        x_max = x;
                    } else {
                        x_min = x;
                    }
                }
                SPLINE_POSITION[i] = ((((WMElement.CAMERASIZEVALUE1B1 - x) * 0.8f) + x) * coef) + ((x * x) * x);
                float y_max = WMElement.CAMERASIZEVALUE1B1;
                while (true) {
                    y = y_min + ((y_max - y_min) / 2.0f);
                    coef = (MapConfig.MIN_ZOOM * y) * (WMElement.CAMERASIZEVALUE1B1 - y);
                    float dy = ((((WMElement.CAMERASIZEVALUE1B1 - y) * 0.8f) + y) * coef) + ((y * y) * y);
                    if (((double) Math.abs(dy - alpha)) < 1.0E-5d) {
                        break;
                    } else if (dy > alpha) {
                        y_max = y;
                    } else {
                        y_min = y;
                    }
                }
                SPLINE_TIME[i] = ((((WMElement.CAMERASIZEVALUE1B1 - y) * 0.2f) + (0.25f * y)) * coef) + ((y * y) * y);
            }
            float[] fArr = SPLINE_POSITION;
            SPLINE_TIME[100] = WMElement.CAMERASIZEVALUE1B1;
            fArr[100] = WMElement.CAMERASIZEVALUE1B1;
        }

        static void initFromContext(Context context) {
            PHYSICAL_COEF = (386.0878f * (context.getResources().getDisplayMetrics().density * 160.0f)) * 0.84f;
        }

        void setSpringBackInterpolator(Interpolator i) {
            this.mSpringBackInterpolator = i;
        }

        SplineOverScroller(float densityScale) {
            this.mPhysicalCoeff = PHYSICAL_COEF * densityScale;
        }

        void updateScroll(float q) {
            this.mCurrentPosition = this.mStart + Math.round(((float) (this.mFinal - this.mStart)) * q);
        }

        private static float getDeceleration(int velocity) {
            return velocity > 0 ? -2000.0f : 2000.0f;
        }

        private void adjustDuration(int start, int oldFinal, int newFinal) {
            float x = Math.abs(((float) (newFinal - start)) / ((float) (oldFinal - start)));
            int index = (int) (100.0f * x);
            if (index < 100) {
                float x_inf = ((float) index) / 100.0f;
                float x_sup = ((float) (index + 1)) / 100.0f;
                float t_inf = SPLINE_TIME[index];
                this.mDuration = (int) (((float) this.mDuration) * (t_inf + (((x - x_inf) / (x_sup - x_inf)) * (SPLINE_TIME[index + 1] - t_inf))));
            }
        }

        void startScroll(int start, int distance, int duration) {
            this.mFinished = false;
            this.mStart = start;
            this.mFinal = start + distance;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mDuration = duration;
            this.mDeceleration = 0.0f;
            this.mVelocity = 0;
        }

        void finish() {
            this.mCurrentPosition = this.mFinal;
            this.mFinished = true;
        }

        boolean springback(int start, int min, int max) {
            this.mFinished = true;
            this.mFinal = start;
            this.mStart = start;
            this.mVelocity = 0;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mDuration = 0;
            if (start < min) {
                startSpringback(start, min, 0);
            } else if (start > max) {
                startSpringback(start, max, 0);
            }
            if (this.mFinished) {
                return false;
            }
            return true;
        }

        private void startSpringback(int start, int end, int velocity) {
            this.mFinished = false;
            this.mState = 1;
            this.mStart = start;
            this.mFinal = end;
            int delta = start - end;
            this.mDeceleration = getDeceleration(delta);
            this.mVelocity = -delta;
            this.mOver = Math.abs(delta);
            this.mDuration = (int) (Math.sqrt((((double) delta) * -2.0d) / ((double) this.mDeceleration)) * 1000.0d);
        }

        void fling(int start, int velocity, int min, int max, int over) {
            this.mOver = over;
            this.mFinished = false;
            this.mVelocity = velocity;
            this.mCurrVelocity = (float) velocity;
            this.mSplineDuration = 0;
            this.mDuration = 0;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mStart = start;
            this.mCurrentPosition = start;
            if (start > max || start < min) {
                startAfterEdge(start, min, max, velocity);
                return;
            }
            this.mState = 0;
            double totalDistance = 0.0d;
            if (velocity != 0) {
                int splineFlingDuration = getSplineFlingDuration(getSplineFlingDuration(velocity), velocity, (double) DECELERATION_RATE, this.mFlingFriction, this.mPhysicalCoeff);
                this.mSplineDuration = splineFlingDuration;
                this.mDuration = splineFlingDuration;
                totalDistance = getSplineFlingDistance(getSplineFlingDistance(velocity), velocity, (double) DECELERATION_RATE, this.mFlingFriction, this.mPhysicalCoeff);
            }
            this.mSplineDistance = (int) (((double) Math.signum((float) velocity)) * totalDistance);
            this.mFinal = this.mSplineDistance + start;
            if (this.mFinal < min) {
                adjustDuration(this.mStart, this.mFinal, min);
                this.mFinal = min;
            }
            if (this.mFinal > max) {
                adjustDuration(this.mStart, this.mFinal, max);
                this.mFinal = max;
            }
        }

        private double getSplineDeceleration(int velocity) {
            return Math.log((double) ((((float) Math.abs(velocity)) * 0.25f) / (this.mFlingFriction * PHYSICAL_COEF)));
        }

        private double getSplineFlingDistance(int velocity) {
            return ((double) (this.mFlingFriction * PHYSICAL_COEF)) * Math.exp((((double) DECELERATION_RATE) / (((double) DECELERATION_RATE) - WeightedLatLng.DEFAULT_INTENSITY)) * getSplineDeceleration(velocity));
        }

        private int getSplineFlingDuration(int velocity) {
            return (int) (Math.exp(getSplineDeceleration(velocity) / (((double) DECELERATION_RATE) - WeightedLatLng.DEFAULT_INTENSITY)) * 1000.0d);
        }

        private void fitOnBounceCurve(int start, int end, int velocity) {
            float totalDuration = (float) Math.sqrt((((double) (((((float) (velocity * velocity)) / 2.0f) / Math.abs(this.mDeceleration)) + ((float) Math.abs(end - start)))) * 2.0d) / ((double) Math.abs(this.mDeceleration)));
            this.mStartTime -= (long) ((int) ((totalDuration - (((float) (-velocity)) / this.mDeceleration)) * 1000.0f));
            this.mStart = end;
            this.mVelocity = (int) ((-this.mDeceleration) * totalDuration);
        }

        private void startBounceAfterEdge(int start, int end, int velocity) {
            int i;
            if (velocity == 0) {
                i = start - end;
            } else {
                i = velocity;
            }
            this.mDeceleration = getDeceleration(i);
            fitOnBounceCurve(start, end, velocity);
            onEdgeReached();
        }

        private void startAfterEdge(int start, int min, int max, int velocity) {
            if (start <= min || start >= max) {
                int edge;
                boolean positive = start > max;
                if (positive) {
                    edge = max;
                } else {
                    edge = min;
                }
                int overDistance = start - edge;
                if (overDistance * velocity >= 0) {
                    startBounceAfterEdge(start, edge, velocity);
                } else {
                    if (getSplineFlingDistance(getSplineFlingDistance(velocity), velocity, (double) DECELERATION_RATE, this.mFlingFriction, this.mPhysicalCoeff) > ((double) Math.abs(overDistance))) {
                        fling(start, velocity, positive ? min : start, positive ? start : max, this.mOver);
                    } else {
                        startSpringback(start, edge, velocity);
                    }
                }
                return;
            }
            GalleryLog.e("OverScroller", "startAfterEdge called from a valid position");
            this.mFinished = true;
        }

        private void onEdgeReached() {
            float distance = ((float) this.mVelocity) * (((float) this.mVelocity) / (Math.abs(this.mDeceleration) * 2.0f));
            float sign = Math.signum((float) this.mVelocity);
            if (distance > ((float) this.mOver)) {
                this.mDeceleration = (((-sign) * ((float) this.mVelocity)) * ((float) this.mVelocity)) / (((float) this.mOver) * 2.0f);
                distance = (float) this.mOver;
            }
            this.mOver = (int) distance;
            this.mState = 2;
            int i = this.mStart;
            if (this.mVelocity <= 0) {
                distance = -distance;
            }
            this.mFinal = i + ((int) distance);
            this.mDuration = -((int) ((((float) this.mVelocity) * 1000.0f) / this.mDeceleration));
        }

        boolean continueWhenFinished() {
            switch (this.mState) {
                case 0:
                    if (this.mDuration < this.mSplineDuration) {
                        this.mStart = this.mFinal;
                        this.mVelocity = (int) this.mCurrVelocity;
                        this.mDeceleration = getDeceleration(this.mVelocity);
                        this.mStartTime += (long) this.mDuration;
                        onEdgeReached();
                        break;
                    }
                    return false;
                case 1:
                    return false;
                case 2:
                    this.mStartTime += (long) this.mDuration;
                    startSpringback(this.mFinal, this.mStart, 0);
                    break;
            }
            update();
            return true;
        }

        boolean update() {
            long currentTime = AnimationUtils.currentAnimationTimeMillis() - this.mStartTime;
            if (currentTime > ((long) this.mDuration)) {
                return false;
            }
            double distance = 0.0d;
            float t;
            switch (this.mState) {
                case 0:
                    t = ((float) currentTime) / ((float) this.mSplineDuration);
                    int index = (int) (100.0f * t);
                    float distanceCoef = WMElement.CAMERASIZEVALUE1B1;
                    float velocityCoef = 0.0f;
                    if (index < 100) {
                        float t_inf = ((float) index) / 100.0f;
                        float t_sup = ((float) (index + 1)) / 100.0f;
                        float d_inf = SPLINE_POSITION[index];
                        velocityCoef = (SPLINE_POSITION[index + 1] - d_inf) / (t_sup - t_inf);
                        distanceCoef = d_inf + ((t - t_inf) * velocityCoef);
                    }
                    distance = (double) (((float) this.mSplineDistance) * distanceCoef);
                    this.mCurrVelocity = ((((float) this.mSplineDistance) * velocityCoef) / ((float) this.mSplineDuration)) * 1000.0f;
                    break;
                case 1:
                    float progress = ((float) currentTime) / ((float) this.mDuration);
                    if (this.mSpringBackInterpolator == null) {
                        t = progress;
                    } else {
                        t = this.mSpringBackInterpolator.getInterpolation(progress);
                    }
                    float t2 = t * t;
                    float sign = Math.signum((float) this.mVelocity);
                    distance = (double) ((((float) this.mOver) * sign) * ((MapConfig.MIN_ZOOM * t2) - ((2.0f * t) * t2)));
                    this.mCurrVelocity = ((((float) this.mOver) * sign) * 6.0f) * ((-t) + t2);
                    break;
                case 2:
                    t = ((float) currentTime) / 1000.0f;
                    this.mCurrVelocity = ((float) this.mVelocity) + (this.mDeceleration * t);
                    distance = (double) ((((float) this.mVelocity) * t) + (((this.mDeceleration * t) * t) / 2.0f));
                    break;
            }
            this.mCurrentPosition = this.mStart + ((int) Math.round(distance));
            return true;
        }

        private float getSplineFlingDurationModify(int velocity) {
            if (Math.abs(velocity) < 5000) {
                return 2.2f;
            }
            return WMElement.CAMERASIZEVALUE1B1;
        }

        private float getSplineFlingDistanceModify(int velocity) {
            if (Math.abs(velocity) < 5000) {
                return 1.8f;
            }
            return WMElement.CAMERASIZEVALUE1B1;
        }

        public double getSplineFlingDistance(double orignDistance, int velocity, double decelerationRate, float flingFriction, float physicalCoeff) {
            if (Math.abs(velocity) > 24000) {
                velocity = (int) (Math.signum((float) velocity) * 24000.0f);
            }
            if (flingFriction * physicalCoeff == 0.0f) {
                return orignDistance;
            }
            return ((double) ((flingFriction * getSplineFlingDistanceModify(velocity)) * physicalCoeff)) * Math.exp((decelerationRate / (decelerationRate - WeightedLatLng.DEFAULT_INTENSITY)) * Math.log((double) ((((float) Math.abs(velocity)) * 0.3f) / (flingFriction * physicalCoeff))));
        }

        public int getSplineFlingDuration(int orignDurtion, int velocity, double decelerationRate, float flingFriction, float physicalCoeff) {
            if (Math.abs(velocity) > 24000) {
                velocity = (int) (Math.signum((float) velocity) * 24000.0f);
            }
            if (flingFriction * physicalCoeff == 0.0f) {
                return orignDurtion;
            }
            return (int) ((((double) getSplineFlingDurationModify(velocity)) * 1000.0d) * Math.exp(Math.log((double) ((((float) Math.abs(velocity)) * 0.9f) / (flingFriction * physicalCoeff))) / (decelerationRate - WeightedLatLng.DEFAULT_INTENSITY)));
        }
    }

    public OverScroller(Context context, float densityScale) {
        this(context, null, densityScale);
    }

    public OverScroller(Context context, Interpolator interpolator, float densityScale) {
        this(context, interpolator, true, densityScale);
    }

    public OverScroller(Context context, Interpolator interpolator, boolean flywheel, float densityScale) {
        this.mInterpolator = interpolator;
        this.mFlywheel = flywheel;
        SplineOverScroller.initFromContext(context);
        this.mScrollerX = new SplineOverScroller(densityScale);
        this.mScrollerY = new SplineOverScroller(densityScale);
    }

    public void setSpringBackInterpolator(Interpolator interpolator) {
        this.mScrollerX.setSpringBackInterpolator(interpolator);
        this.mScrollerY.setSpringBackInterpolator(interpolator);
    }

    public final boolean isFinished() {
        return this.mScrollerX.mFinished ? this.mScrollerY.mFinished : false;
    }

    public final void forceFinished(boolean finished) {
        this.mScrollerX.mFinished = this.mScrollerY.mFinished = finished;
    }

    public final int getCurrX() {
        return this.mScrollerX.mCurrentPosition;
    }

    public float getCurrVelocity() {
        return (float) Math.hypot((double) this.mScrollerX.mCurrVelocity, (double) this.mScrollerY.mCurrVelocity);
    }

    public final int getFinalX() {
        return this.mScrollerX.mFinal;
    }

    public boolean computeScrollOffset() {
        if (isFinished()) {
            return false;
        }
        switch (this.mMode) {
            case 0:
                long elapsedTime = AnimationUtils.currentAnimationTimeMillis() - this.mScrollerX.mStartTime;
                int duration = this.mScrollerX.mDuration;
                if (elapsedTime >= ((long) duration)) {
                    abortAnimation();
                    break;
                }
                float q = ((float) elapsedTime) / ((float) duration);
                if (this.mInterpolator == null) {
                    q = Scroller.viscousFluid(q);
                } else {
                    q = this.mInterpolator.getInterpolation(q);
                }
                this.mScrollerX.updateScroll(q);
                this.mScrollerY.updateScroll(q);
                break;
            case 1:
                if (!(this.mScrollerX.mFinished || this.mScrollerX.update() || this.mScrollerX.continueWhenFinished())) {
                    this.mScrollerX.finish();
                }
                if (!(this.mScrollerY.mFinished || this.mScrollerY.update() || this.mScrollerY.continueWhenFinished())) {
                    this.mScrollerY.finish();
                    break;
                }
        }
        return true;
    }

    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        this.mMode = 0;
        this.mScrollerX.startScroll(startX, dx, duration);
        this.mScrollerY.startScroll(startY, dy, duration);
    }

    public boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY) {
        this.mMode = 1;
        return !this.mScrollerX.springback(startX, minX, maxX) ? this.mScrollerY.springback(startY, minY, maxY) : true;
    }

    public void fling(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY, int overX, int overY) {
        if (this.mFlywheel && !isFinished()) {
            float oldVelocityX = this.mScrollerX.mCurrVelocity;
            float oldVelocityY = this.mScrollerY.mCurrVelocity;
            if (Math.signum((float) velocityX) == Math.signum(oldVelocityX) && Math.signum((float) velocityY) == Math.signum(oldVelocityY)) {
                velocityX = (int) (((float) velocityX) + oldVelocityX);
                velocityY = (int) (((float) velocityY) + oldVelocityY);
            }
        }
        this.mMode = 1;
        this.mScrollerX.fling(startX, velocityX, minX, maxX, overX);
        this.mScrollerY.fling(startY, velocityY, minY, maxY, overY);
    }

    public void abortAnimation() {
        this.mScrollerX.finish();
        this.mScrollerY.finish();
    }
}
