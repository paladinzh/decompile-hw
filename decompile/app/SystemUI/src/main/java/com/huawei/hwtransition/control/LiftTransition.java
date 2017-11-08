package com.huawei.hwtransition.control;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import com.android.keyguard.R$bool;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.util.DoubleTapUtils;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.view.WallpaperPagerAdapter;
import fyusion.vislib.BuildConfig;

public class LiftTransition {
    private boolean bMultidirectionalUnlock = false;
    WallpaperPagerAdapter mAdapter;
    private ValueAnimator mAnimation;
    Context mAppContext;
    private float mDownY = 0.0f;
    private long mDuration_hint = 300;
    private long mDuration_lift = 300;
    private long mDuration_reset = 300;
    private float mDy = 0.0f;
    float mEndDy;
    GestureDetector mGestureDetector = new GestureDetector(this.mAppContext, new SimpleGestureListener());
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    LiftTransition.this.startLiftAnimation();
                    return;
                default:
                    HwLog.w("LiftTransition", "no supported message: " + msg.what);
                    return;
            }
        }
    };
    float mHeight;
    private boolean mHitModeEnable = true;
    private boolean mIsInAnimationING = false;
    private boolean mIsInLiftView = false;
    private boolean mIsTerminate = false;
    private float mLastAnimY = 0.0f;
    private float mLastDy = 0.0f;
    private int mLiftExtraHeight = 150;
    private TimeInterpolator mLiftInterPolator = new DecelerateInterpolator();
    LiftListener mLiftListener;
    private int mLiftMode;
    private int mLiftType;
    private PropertyValuesHolder mPvhY;
    private TimeInterpolator mResetInterpolator = new AccelerateInterpolator();
    int mStartX = 0;
    int mStartY = 0;
    float mThresholdDy;

    public interface LiftListener {
        void onLiftAnimationEnd();

        void setLift(float f);

        void setLift(float f, float f2);

        void setLiftMode(int i);
    }

    private class SimpleGestureListener extends SimpleOnGestureListener {
        private SimpleGestureListener() {
        }

        public boolean onSingleTapUp(MotionEvent e) {
            if (LiftTransition.this.mLastDy != 0.0f || !LiftTransition.this.mHitModeEnable) {
                return false;
            }
            HwLog.d("LiftTransition", "onSingleTapUp");
            LiftTransition.this.setLiftMode(1);
            LiftTransition.this.startLiftAnimation();
            return true;
        }

        public boolean onDoubleTap(MotionEvent event) {
            if (!DoubleTapUtils.readWakeupCheckValue(LiftTransition.this.mAppContext)) {
                return super.onDoubleTap(event);
            }
            if (!LiftTransition.this.mIsInLiftView) {
                DoubleTapUtils.offScreen(LiftTransition.this.mAppContext);
                HwLockScreenReporter.report(LiftTransition.this.mAppContext, 155, BuildConfig.FLAVOR);
            }
            return super.onDoubleTap(event);
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            HwLog.d("LiftTransition", "onFling velocityY = " + velocityY + ", " + velocityX);
            if (e1 == null || e2 == null) {
                return false;
            }
            int xMv = Math.abs((int) (e2.getX() - e1.getX()));
            int yMv = Math.abs((int) (e2.getY() - e1.getY()));
            float velocity = LiftTransition.this.mLiftType == 0 ? velocityX : velocityY;
            int distance = LiftTransition.this.mLiftType == 0 ? xMv : yMv;
            float v = (float) Math.sqrt((double) ((velocityX * velocityX) + (velocityY * velocityY)));
            if (LiftTransition.this.mLiftType == 2) {
                velocity = velocity > 0.0f ? v : -v;
                distance = (int) Math.sqrt((double) ((xMv * xMv) + (yMv * yMv)));
            }
            HwLog.d("LiftTransition", "onFling, distance :  " + distance + ";  velocity: " + velocity);
            if (Math.abs(velocity) < 1600.0f || distance < 200) {
                return false;
            }
            if (Math.abs(velocity) > 1600.0f) {
                float direction = LiftTransition.this.mLastDy == 0.0f ? LiftTransition.this.mEndDy : -LiftTransition.this.mEndDy;
                if (direction * velocity > 0.0f || LiftTransition.this.mLiftType == 2) {
                    HwLog.d("LiftTransition", "       direction = " + direction);
                    LiftTransition.this.setLiftMode(2);
                    LiftTransition.this.startLiftAnimation();
                    return true;
                }
            }
            return false;
        }
    }

    public LiftTransition(Context applicationContext, LiftListener liftListener) {
        this.mAppContext = applicationContext;
        this.mLiftListener = liftListener;
        setLiftMode(0);
        setLiftType(1);
        if (this.mAppContext != null) {
            this.bMultidirectionalUnlock = this.mAppContext.getResources().getBoolean(R$bool.config_multidirectionalUnlock);
        }
    }

    public void setLiftType(int type) {
        if (type >= 0 && type <= 2) {
            this.mLiftType = type;
        }
        HwLog.i("LiftTransition", "setLiftType = " + this.mLiftType);
    }

    public boolean isLifted() {
        return ((double) Math.abs(this.mLastDy - this.mEndDy)) < 1.0E-7d;
    }

    public void startAnimation(int liftMode) {
        setLiftMode(liftMode);
        startLiftAnimation();
    }

    public void setLiftRange(float maxDeltaY, float thresholdDeltaY) {
        if (maxDeltaY == 0.0f) {
            HwLog.e("LiftTransition", "setLiftRange delta y is zero");
            return;
        }
        this.mEndDy = maxDeltaY;
        this.mThresholdDy = Math.abs(thresholdDeltaY);
        if (this.mLastDy != 0.0f) {
            this.mLastDy = this.mEndDy;
        }
        HwLog.d("LiftTransition", "setLiftRange: " + this.mEndDy + ", threshold = " + this.mThresholdDy);
    }

    public void setHeight(float height, int liftViewType) {
        this.mHeight = height;
        if (liftViewType != 1) {
            this.mLiftExtraHeight = 0;
        } else {
            this.mLiftExtraHeight = 150;
        }
    }

    public boolean onTouchEvent(MotionEvent ev, boolean isInLiftView) {
        this.mIsInLiftView = isInLiftView;
        return onTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean z = false;
        if (ev.getPointerCount() > 1) {
            this.mIsTerminate = true;
        }
        float x = ev.getRawX();
        float y = ev.getRawY();
        MotionEvent event = MotionEvent.obtain(ev);
        event.setLocation(x, y);
        if (this.mGestureDetector.onTouchEvent(event) || this.mIsInAnimationING) {
            event.recycle();
            return true;
        }
        float val;
        switch (event.getAction()) {
            case 0:
                this.mStartX = (int) event.getX();
                this.mStartY = (int) event.getY();
                setLiftMode(0);
                cancelLiftAnimation();
                val = this.mLiftType == 0 ? x : y;
                this.mDownY = val - this.mLastAnimY;
                this.mDy = (val - this.mDownY) + this.mLastDy;
                this.mDy = checkRange(this.mDy);
                if (ev.getPointerCount() > 1) {
                    z = true;
                }
                this.mIsTerminate = z;
                break;
            case 1:
            case 3:
                this.mLastAnimY = 0.0f;
                int distance;
                if (this.bMultidirectionalUnlock) {
                    distance = caculateDistance((int) event.getX(), (int) event.getY());
                } else {
                    distance = (int) Math.abs(this.mDy - this.mLastDy);
                }
                if (this.mIsTerminate || ((float) distance) <= this.mThresholdDy) {
                    setLiftMode(0);
                } else {
                    setLiftMode(2);
                }
                startLiftAnimation();
                break;
            case 2:
                float deltaX = event.getX() - ((float) this.mStartX);
                float deltaY = event.getY() - ((float) this.mStartY);
                if (this.mLiftType == 2) {
                    val = deltaX > 0.0f ? (float) Math.sqrt((double) ((deltaX * deltaX) + (deltaY * deltaY))) : -((float) Math.sqrt((double) ((deltaX * deltaX) + (deltaY * deltaY))));
                    HwLog.d("LiftTransition", "val = " + val + ", mLastDy = " + this.mLastDy + ", mLastAnimY = " + this.mLastAnimY);
                    this.mDy = (this.mLastDy + val) + this.mLastAnimY;
                } else {
                    this.mDy = ((this.mLiftType == 0 ? x : y) - this.mDownY) + this.mLastDy;
                }
                HwLog.d("LiftTransition", "Lift check = " + this.mLiftType + ", mDy = " + this.mDy + " EndDy " + this.mEndDy + "  " + this.mLastDy);
                this.mDy = checkRangeWithDirection(this.mDy, deltaY);
                if (this.mLiftListener != null) {
                    HwLog.d("LiftTransition", "checkRange = " + this.mDy);
                    this.mLiftListener.setLift(deltaX, deltaY);
                    this.mLiftListener.setLift(this.mDy);
                    break;
                }
                break;
        }
        event.recycle();
        return true;
    }

    private float checkRange(float dy) {
        if (Math.abs(dy) > Math.abs(this.mEndDy) + ((float) this.mLiftExtraHeight)) {
            return this.mEndDy - ((float) this.mLiftExtraHeight);
        }
        if (this.mEndDy * dy < 0.0f) {
            return 0.0f;
        }
        return dy;
    }

    private float checkRangeWithDirection(float dy, float dyDirection) {
        if (Math.abs(dy) > Math.abs(this.mEndDy) + ((float) this.mLiftExtraHeight)) {
            if (0.0f > dyDirection) {
                return this.mEndDy - ((float) this.mLiftExtraHeight);
            }
            return 0.0f;
        } else if (this.mEndDy * dy < 0.0f) {
            return 0.0f;
        } else {
            if (Math.abs(dy) <= Math.abs(this.mEndDy) || 0.0f >= dyDirection) {
                return dy;
            }
            HwLog.d("LiftTransition", "checkRangeWithDirection:0");
            return 0.0f;
        }
    }

    private void cancelLiftAnimation() {
        if (this.mAnimation != null) {
            this.mAnimation.cancel();
        }
    }

    private void startLiftAnimation() {
        if (this.mLiftListener != null) {
            float y1;
            if (this.mAnimation == null) {
                this.mAdapter = WallpaperPagerAdapter.getInst(this.mAppContext, null);
                this.mPvhY = PropertyValuesHolder.ofFloat("Lift", new float[]{0.0f, 0.0f});
                this.mAnimation = ObjectAnimator.ofPropertyValuesHolder(this.mLiftListener, new PropertyValuesHolder[]{this.mPvhY});
                this.mAnimation.addListener(new AnimatorListener() {
                    public void onAnimationStart(Animator animation) {
                        HwLog.d("LiftTransition", "onAnimationStart");
                        if (LiftTransition.this.mLiftMode == 2 && LiftTransition.this.mAdapter != null && LiftTransition.this.mLastDy != 0.0f) {
                            LiftTransition.this.mAdapter.addImage2ListView();
                            WallpaperPagerAdapter wallpaperPagerAdapter = LiftTransition.this.mAdapter;
                            LiftTransition.this.mAdapter.getClass();
                            wallpaperPagerAdapter.loadPagerImageView(2);
                        }
                    }

                    public void onAnimationRepeat(Animator animation) {
                        HwLog.d("LiftTransition", "onAnimationRepeat");
                    }

                    public void onAnimationEnd(Animator animation) {
                        HwLog.d("LiftTransition", "onAnimationEnd" + LiftTransition.this.mLiftMode);
                        if (LiftTransition.this.mLiftMode == 1) {
                            LiftTransition.this.setLiftMode(0);
                            LiftTransition.this.mDy = Math.signum(LiftTransition.this.mEndDy) * 100.0f;
                            LiftTransition.this.mHandler.sendEmptyMessage(1);
                            return;
                        }
                        if (LiftTransition.this.mLiftMode == 2) {
                            LiftTransition.this.mIsInAnimationING = false;
                        }
                        LiftTransition.this.mLiftListener.onLiftAnimationEnd();
                    }

                    public void onAnimationCancel(Animator animation) {
                        LiftTransition.this.mLastAnimY = ((Float) LiftTransition.this.mAnimation.getAnimatedValue()).floatValue();
                        HwLog.d("LiftTransition", "onAnimationCancel currentY = " + LiftTransition.this.mLastAnimY);
                    }
                });
            }
            if (this.mLiftMode == 1) {
                y1 = 100.0f * Math.signum(this.mEndDy);
                this.mAnimation.setInterpolator(this.mLiftInterPolator);
                this.mAnimation.setDuration(this.mDuration_hint);
            } else if (this.mLiftMode == 2) {
                boolean isOverHeight;
                float mSignHeight = this.mHeight * Math.signum(this.mEndDy);
                if (Math.abs(this.mDy) > this.mHeight) {
                    isOverHeight = true;
                } else {
                    isOverHeight = false;
                }
                y1 = isOverHeight ? mSignHeight : this.mLastDy == 0.0f ? -this.mHeight : 0.0f;
                if (isOverHeight) {
                    this.mIsInAnimationING = true;
                }
                this.mAnimation.setInterpolator(this.mLiftInterPolator);
                this.mAnimation.setDuration(this.mDuration_lift);
                this.mLastDy = y1;
            } else {
                y1 = this.mLastDy;
                this.mAnimation.setInterpolator(this.mResetInterpolator);
                this.mAnimation.setDuration(y1 == 0.0f ? this.mDuration_reset : this.mDuration_lift);
            }
            HwLog.d("LiftTransition", "from " + this.mDy + ", to " + y1 + ", mLastDy = " + this.mLastDy);
            this.mPvhY.setFloatValues(new float[]{this.mDy, y1});
            this.mAnimation.start();
        }
    }

    private void setLiftMode(int mode) {
        HwLog.i("LiftTransition", "setLiftMode = " + mode);
        this.mLiftMode = mode;
        if (this.mLiftListener != null) {
            this.mLiftListener.setLiftMode(mode);
        }
    }

    public void reset() {
        this.mLastDy = 0.0f;
    }

    private int caculateDistance(int currentX, int currentY) {
        return (int) Math.sqrt((double) (((currentX - this.mStartX) * (currentX - this.mStartX)) + ((currentY - this.mStartY) * (currentY - this.mStartY))));
    }

    public void setMultiDirectional(boolean multi) {
        this.bMultidirectionalUnlock = multi;
    }

    public int getLiftMode() {
        return this.mLiftMode;
    }
}
