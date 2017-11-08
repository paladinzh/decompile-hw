package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.R;

public class TrustDrawable extends Drawable {
    private int mAlpha;
    private final AnimatorUpdateListener mAlphaUpdateListener = new AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator animation) {
            TrustDrawable.this.mCurAlpha = ((Integer) animation.getAnimatedValue()).intValue();
            TrustDrawable.this.invalidateSelf();
        }
    };
    private boolean mAnimating;
    private int mCurAlpha;
    private Animator mCurAnimator;
    private float mCurInnerRadius;
    private final float mInnerRadiusEnter;
    private final float mInnerRadiusExit;
    private final float mInnerRadiusVisibleMax;
    private final float mInnerRadiusVisibleMin;
    private Paint mPaint;
    private final AnimatorUpdateListener mRadiusUpdateListener = new AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator animation) {
            TrustDrawable.this.mCurInnerRadius = ((Float) animation.getAnimatedValue()).floatValue();
            TrustDrawable.this.invalidateSelf();
        }
    };
    private int mState = -1;
    private final float mThickness;
    private boolean mTrustManaged;
    private final Animator mVisibleAnimator;

    private class StateUpdateAnimatorListener extends AnimatorListenerAdapter {
        boolean mCancelled;

        private StateUpdateAnimatorListener() {
        }

        public void onAnimationStart(Animator animation) {
            this.mCancelled = false;
        }

        public void onAnimationCancel(Animator animation) {
            this.mCancelled = true;
        }

        public void onAnimationEnd(Animator animation) {
            if (!this.mCancelled) {
                TrustDrawable.this.updateState(false);
            }
        }
    }

    public TrustDrawable(Context context) {
        Resources r = context.getResources();
        this.mInnerRadiusVisibleMin = r.getDimension(R.dimen.trust_circle_inner_radius_visible_min);
        this.mInnerRadiusVisibleMax = r.getDimension(R.dimen.trust_circle_inner_radius_visible_max);
        this.mInnerRadiusExit = r.getDimension(R.dimen.trust_circle_inner_radius_exit);
        this.mInnerRadiusEnter = r.getDimension(R.dimen.trust_circle_inner_radius_enter);
        this.mThickness = r.getDimension(R.dimen.trust_circle_thickness);
        this.mCurInnerRadius = this.mInnerRadiusEnter;
        this.mVisibleAnimator = makeVisibleAnimator();
        this.mPaint = new Paint();
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setColor(-1);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStrokeWidth(this.mThickness);
    }

    public void draw(Canvas canvas) {
        int newAlpha = (this.mCurAlpha * this.mAlpha) / 256;
        if (newAlpha != 0) {
            Rect r = getBounds();
            this.mPaint.setAlpha(newAlpha);
            canvas.drawCircle(r.exactCenterX(), r.exactCenterY(), this.mCurInnerRadius, this.mPaint);
        }
    }

    public void setAlpha(int alpha) {
        this.mAlpha = alpha;
    }

    public int getAlpha() {
        return this.mAlpha;
    }

    public void setColorFilter(ColorFilter colorFilter) {
        throw new UnsupportedOperationException("not implemented");
    }

    public int getOpacity() {
        return -3;
    }

    public void start() {
        if (!this.mAnimating) {
            this.mAnimating = true;
            updateState(true);
            invalidateSelf();
        }
    }

    public void stop() {
        if (this.mAnimating) {
            this.mAnimating = false;
            if (this.mCurAnimator != null) {
                this.mCurAnimator.cancel();
                this.mCurAnimator = null;
            }
            this.mState = -1;
            this.mCurAlpha = 0;
            this.mCurInnerRadius = this.mInnerRadiusEnter;
            invalidateSelf();
        }
    }

    public void setTrustManaged(boolean trustManaged) {
        if (trustManaged != this.mTrustManaged || this.mState == -1) {
            this.mTrustManaged = trustManaged;
            updateState(true);
        }
    }

    private void updateState(boolean allowTransientState) {
        if (this.mAnimating) {
            int nextState = this.mState;
            if (this.mState == -1) {
                nextState = this.mTrustManaged ? 1 : 0;
            } else if (this.mState == 0) {
                if (this.mTrustManaged) {
                    nextState = 1;
                }
            } else if (this.mState == 1) {
                if (!this.mTrustManaged) {
                    nextState = 3;
                }
            } else if (this.mState == 2) {
                if (!this.mTrustManaged) {
                    nextState = 3;
                }
            } else if (this.mState == 3 && this.mTrustManaged) {
                nextState = 1;
            }
            if (!allowTransientState) {
                if (nextState == 1) {
                    nextState = 2;
                }
                if (nextState == 3) {
                    nextState = 0;
                }
            }
            if (nextState != this.mState) {
                if (this.mCurAnimator != null) {
                    this.mCurAnimator.cancel();
                    this.mCurAnimator = null;
                }
                if (nextState == 0) {
                    this.mCurAlpha = 0;
                    this.mCurInnerRadius = this.mInnerRadiusEnter;
                } else if (nextState == 1) {
                    this.mCurAnimator = makeEnterAnimator(this.mCurInnerRadius, this.mCurAlpha);
                    if (this.mState == -1) {
                        this.mCurAnimator.setStartDelay(200);
                    }
                } else if (nextState == 2) {
                    this.mCurAlpha = 76;
                    this.mCurInnerRadius = this.mInnerRadiusVisibleMax;
                    this.mCurAnimator = this.mVisibleAnimator;
                } else if (nextState == 3) {
                    this.mCurAnimator = makeExitAnimator(this.mCurInnerRadius, this.mCurAlpha);
                }
                this.mState = nextState;
                if (this.mCurAnimator != null) {
                    this.mCurAnimator.start();
                }
                invalidateSelf();
            }
        }
    }

    private Animator makeVisibleAnimator() {
        return makeAnimators(this.mInnerRadiusVisibleMax, this.mInnerRadiusVisibleMin, 76, 38, 1000, Interpolators.ACCELERATE_DECELERATE, true, false);
    }

    private Animator makeEnterAnimator(float radius, int alpha) {
        return makeAnimators(radius, this.mInnerRadiusVisibleMax, alpha, 76, 500, Interpolators.LINEAR_OUT_SLOW_IN, false, true);
    }

    private Animator makeExitAnimator(float radius, int alpha) {
        return makeAnimators(radius, this.mInnerRadiusExit, alpha, 0, 500, Interpolators.FAST_OUT_SLOW_IN, false, true);
    }

    private Animator makeAnimators(float startRadius, float endRadius, int startAlpha, int endAlpha, long duration, Interpolator interpolator, boolean repeating, boolean stateUpdateListener) {
        long j = duration;
        ValueAnimator alphaAnimator = configureAnimator(ValueAnimator.ofInt(new int[]{startAlpha, endAlpha}), j, this.mAlphaUpdateListener, interpolator, repeating);
        j = duration;
        ValueAnimator sizeAnimator = configureAnimator(ValueAnimator.ofFloat(new float[]{startRadius, endRadius}), j, this.mRadiusUpdateListener, interpolator, repeating);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(new Animator[]{alphaAnimator, sizeAnimator});
        if (stateUpdateListener) {
            set.addListener(new StateUpdateAnimatorListener());
        }
        return set;
    }

    private ValueAnimator configureAnimator(ValueAnimator animator, long duration, AnimatorUpdateListener updateListener, Interpolator interpolator, boolean repeating) {
        animator.setDuration(duration);
        animator.addUpdateListener(updateListener);
        animator.setInterpolator(interpolator);
        if (repeating) {
            animator.setRepeatCount(-1);
            animator.setRepeatMode(2);
        }
        return animator;
    }
}
