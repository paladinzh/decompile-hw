package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.DisplayListCanvas;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import com.android.systemui.Interpolators;
import com.android.systemui.R;

public class KeyguardAffordanceView extends ImageView {
    private ValueAnimator mAlphaAnimator;
    private AnimatorListenerAdapter mAlphaEndListener;
    private int mCenterX;
    private int mCenterY;
    private ValueAnimator mCircleAnimator;
    private int mCircleColor;
    private AnimatorListenerAdapter mCircleEndListener;
    private final Paint mCirclePaint;
    private float mCircleRadius;
    private float mCircleStartRadius;
    private AnimatorListenerAdapter mClipEndListener;
    private final ArgbEvaluator mColorInterpolator;
    private boolean mFinishing;
    private final FlingAnimationUtils mFlingAnimationUtils;
    private CanvasProperty<Float> mHwCenterX;
    private CanvasProperty<Float> mHwCenterY;
    private CanvasProperty<Paint> mHwCirclePaint;
    private CanvasProperty<Float> mHwCircleRadius;
    private float mImageScale;
    private final int mInverseColor;
    private boolean mLaunchingAffordance;
    private float mMaxCircleSize;
    private final int mMinBackgroundRadius;
    private final int mNormalColor;
    private Animator mPreviewClipper;
    private View mPreviewView;
    private float mRestingAlpha;
    private ValueAnimator mScaleAnimator;
    private AnimatorListenerAdapter mScaleEndListener;
    private boolean mSupportHardware;
    private int[] mTempPoint;

    public KeyguardAffordanceView(Context context) {
        this(context, null);
    }

    public KeyguardAffordanceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardAffordanceView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public KeyguardAffordanceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mTempPoint = new int[2];
        this.mImageScale = 1.0f;
        this.mRestingAlpha = 0.5f;
        this.mClipEndListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                KeyguardAffordanceView.this.mPreviewClipper = null;
            }
        };
        this.mCircleEndListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                KeyguardAffordanceView.this.mCircleAnimator = null;
            }
        };
        this.mScaleEndListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                KeyguardAffordanceView.this.mScaleAnimator = null;
            }
        };
        this.mAlphaEndListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                KeyguardAffordanceView.this.mAlphaAnimator = null;
            }
        };
        this.mCirclePaint = new Paint();
        this.mCirclePaint.setAntiAlias(true);
        this.mCircleColor = -1;
        this.mCirclePaint.setColor(this.mCircleColor);
        this.mNormalColor = -1;
        this.mInverseColor = -16777216;
        this.mMinBackgroundRadius = this.mContext.getResources().getDimensionPixelSize(R.dimen.keyguard_affordance_min_background_radius);
        this.mColorInterpolator = new ArgbEvaluator();
        this.mFlingAnimationUtils = new FlingAnimationUtils(this.mContext, 0.3f);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.mCenterX = getWidth() / 2;
        this.mCenterY = getHeight() / 2;
        this.mMaxCircleSize = getMaxCircleSize();
    }

    protected void onDraw(Canvas canvas) {
        this.mSupportHardware = false;
        drawBackgroundCircle(canvas);
        canvas.save();
        canvas.scale(this.mImageScale, this.mImageScale, (float) (getWidth() / 2), (float) (getHeight() / 2));
        super.onDraw(canvas);
        canvas.restore();
    }

    private void drawBackgroundCircle(Canvas canvas) {
        if (this.mCircleRadius <= 0.0f && !this.mFinishing) {
            return;
        }
        if (this.mFinishing && this.mSupportHardware) {
            ((DisplayListCanvas) canvas).drawCircle(this.mHwCenterX, this.mHwCenterY, this.mHwCircleRadius, this.mHwCirclePaint);
            return;
        }
        updateCircleColor();
        canvas.drawCircle((float) this.mCenterX, (float) this.mCenterY, this.mCircleRadius, this.mCirclePaint);
    }

    private void updateCircleColor() {
        float fraction = 0.5f + (Math.max(0.0f, Math.min(1.0f, (this.mCircleRadius - ((float) this.mMinBackgroundRadius)) / (((float) this.mMinBackgroundRadius) * 0.5f))) * 0.5f);
        if (this.mPreviewView != null && this.mPreviewView.getVisibility() == 0) {
            fraction *= 1.0f - (Math.max(0.0f, this.mCircleRadius - this.mCircleStartRadius) / (this.mMaxCircleSize - this.mCircleStartRadius));
        }
        this.mCirclePaint.setColor(Color.argb((int) (((float) Color.alpha(this.mCircleColor)) * fraction), Color.red(this.mCircleColor), Color.green(this.mCircleColor), Color.blue(this.mCircleColor)));
    }

    private float getMaxCircleSize() {
        getLocationInWindow(this.mTempPoint);
        float width = (float) (this.mTempPoint[0] + this.mCenterX);
        return (float) Math.hypot((double) Math.max(((float) getRootView().getWidth()) - width, width), (double) ((float) (this.mTempPoint[1] + this.mCenterY)));
    }

    private void cancelAnimator(Animator animator) {
        if (animator != null) {
            animator.cancel();
        }
    }

    public void setImageScale(float imageScale, boolean animate, long duration, Interpolator interpolator) {
        cancelAnimator(this.mScaleAnimator);
        if (animate) {
            ValueAnimator animator = ValueAnimator.ofFloat(new float[]{this.mImageScale, imageScale});
            this.mScaleAnimator = animator;
            animator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    KeyguardAffordanceView.this.mImageScale = ((Float) animation.getAnimatedValue()).floatValue();
                    KeyguardAffordanceView.this.invalidate();
                }
            });
            animator.addListener(this.mScaleEndListener);
            if (interpolator == null) {
                if (imageScale == 0.0f) {
                    interpolator = Interpolators.FAST_OUT_LINEAR_IN;
                } else {
                    interpolator = Interpolators.LINEAR_OUT_SLOW_IN;
                }
            }
            animator.setInterpolator(interpolator);
            if (duration == -1) {
                duration = (long) (200.0f * Math.min(1.0f, Math.abs(this.mImageScale - imageScale) / 0.19999999f));
            }
            animator.setDuration(duration);
            animator.start();
            return;
        }
        this.mImageScale = imageScale;
        invalidate();
    }

    public void setRestingAlpha(float alpha) {
        this.mRestingAlpha = alpha;
        setImageAlpha(alpha, false);
    }

    public float getRestingAlpha() {
        return this.mRestingAlpha;
    }

    public void setImageAlpha(float alpha, boolean animate) {
        setImageAlpha(alpha, animate, -1, null, null);
    }

    public void setImageAlpha(float alpha, boolean animate, long duration, Interpolator interpolator, Runnable runnable) {
        cancelAnimator(this.mAlphaAnimator);
        if (this.mLaunchingAffordance) {
            alpha = 0.0f;
        }
        int endAlpha = (int) (alpha * 255.0f);
        final Drawable background = getBackground();
        if (animate) {
            ValueAnimator animator = ValueAnimator.ofInt(new int[]{getImageAlpha(), endAlpha});
            this.mAlphaAnimator = animator;
            animator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    int alpha = ((Integer) animation.getAnimatedValue()).intValue();
                    if (background != null) {
                        background.mutate().setAlpha(alpha);
                    }
                    KeyguardAffordanceView.this.setImageAlpha(alpha);
                }
            });
            animator.addListener(this.mAlphaEndListener);
            if (interpolator == null) {
                if (alpha == 0.0f) {
                    interpolator = Interpolators.FAST_OUT_LINEAR_IN;
                } else {
                    interpolator = Interpolators.LINEAR_OUT_SLOW_IN;
                }
            }
            animator.setInterpolator(interpolator);
            if (duration == -1) {
                duration = (long) (200.0f * Math.min(1.0f, ((float) Math.abs(currentAlpha - endAlpha)) / 255.0f));
            }
            animator.setDuration(duration);
            if (runnable != null) {
                animator.addListener(getEndListener(runnable));
            }
            animator.start();
            return;
        }
        if (background != null) {
            background.mutate().setAlpha(endAlpha);
        }
        setImageAlpha(endAlpha);
    }

    private AnimatorListener getEndListener(final Runnable runnable) {
        return new AnimatorListenerAdapter() {
            boolean mCancelled;

            public void onAnimationCancel(Animator animation) {
                this.mCancelled = true;
            }

            public void onAnimationEnd(Animator animation) {
                if (!this.mCancelled) {
                    runnable.run();
                }
            }
        };
    }

    public boolean performClick() {
        if (isClickable()) {
            return super.performClick();
        }
        return false;
    }

    public void setLaunchingAffordance(boolean launchingAffordance) {
        this.mLaunchingAffordance = launchingAffordance;
    }
}
