package com.android.settings.fingerprint;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

public class FingerprintLocationAnimationView extends View implements FingerprintFindSensorAnimation {
    private ValueAnimator mAlphaAnimator;
    private final Paint mDotPaint = new Paint();
    private final int mDotRadius = getResources().getDimensionPixelSize(2131558692);
    private final Interpolator mFastOutSlowInInterpolator;
    private final float mFractionCenterX = getResources().getFraction(2131951616, 1, 1);
    private final float mFractionCenterY = getResources().getFraction(2131951617, 1, 1);
    private final Interpolator mLinearOutSlowInInterpolator;
    private final int mMaxPulseRadius = getResources().getDimensionPixelSize(2131558693);
    private final Paint mPulsePaint = new Paint();
    private float mPulseRadius;
    private ValueAnimator mRadiusAnimator;
    private final Runnable mStartPhaseRunnable = new Runnable() {
        public void run() {
            FingerprintLocationAnimationView.this.startPhase();
        }
    };

    public FingerprintLocationAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(16843829, typedValue, true);
        int color = getResources().getColor(typedValue.resourceId, null);
        this.mDotPaint.setAntiAlias(true);
        this.mPulsePaint.setAntiAlias(true);
        this.mDotPaint.setColor(color);
        this.mPulsePaint.setColor(color);
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563662);
        this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563662);
    }

    protected void onDraw(Canvas canvas) {
        drawPulse(canvas);
        drawDot(canvas);
    }

    private void drawDot(Canvas canvas) {
        canvas.drawCircle(getCenterX(), getCenterY(), (float) this.mDotRadius, this.mDotPaint);
    }

    private void drawPulse(Canvas canvas) {
        canvas.drawCircle(getCenterX(), getCenterY(), this.mPulseRadius, this.mPulsePaint);
    }

    private float getCenterX() {
        return ((float) getWidth()) * this.mFractionCenterX;
    }

    private float getCenterY() {
        return ((float) getHeight()) * this.mFractionCenterY;
    }

    public void startAnimation() {
        startPhase();
    }

    public void stopAnimation() {
        removeCallbacks(this.mStartPhaseRunnable);
        if (this.mRadiusAnimator != null) {
            this.mRadiusAnimator.cancel();
        }
        if (this.mAlphaAnimator != null) {
            this.mAlphaAnimator.cancel();
        }
    }

    public void pauseAnimation() {
        stopAnimation();
    }

    private void startPhase() {
        startRadiusAnimation();
        startAlphaAnimation();
    }

    private void startRadiusAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(new float[]{0.0f, (float) this.mMaxPulseRadius});
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                FingerprintLocationAnimationView.this.mPulseRadius = ((Float) animation.getAnimatedValue()).floatValue();
                FingerprintLocationAnimationView.this.invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            boolean mCancelled;

            public void onAnimationCancel(Animator animation) {
                this.mCancelled = true;
            }

            public void onAnimationEnd(Animator animation) {
                FingerprintLocationAnimationView.this.mRadiusAnimator = null;
                if (!this.mCancelled) {
                    FingerprintLocationAnimationView.this.postDelayed(FingerprintLocationAnimationView.this.mStartPhaseRunnable, 1000);
                }
            }
        });
        animator.setDuration(1000);
        animator.setInterpolator(this.mLinearOutSlowInInterpolator);
        animator.start();
        this.mRadiusAnimator = animator;
    }

    private void startAlphaAnimation() {
        this.mPulsePaint.setAlpha(38);
        ValueAnimator animator = ValueAnimator.ofFloat(new float[]{0.15f, 0.0f});
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                FingerprintLocationAnimationView.this.mPulsePaint.setAlpha((int) (((Float) animation.getAnimatedValue()).floatValue() * 255.0f));
                FingerprintLocationAnimationView.this.invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                FingerprintLocationAnimationView.this.mAlphaAnimator = null;
            }
        });
        animator.setDuration(750);
        animator.setInterpolator(this.mFastOutSlowInInterpolator);
        animator.setStartDelay(250);
        animator.start();
        this.mAlphaAnimator = animator;
    }
}
