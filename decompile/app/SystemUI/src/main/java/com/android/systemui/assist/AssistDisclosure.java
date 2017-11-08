package com.android.systemui.assist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AnimationUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.R;

public class AssistDisclosure {
    private final Context mContext;
    private final Handler mHandler;
    private Runnable mShowRunnable = new Runnable() {
        public void run() {
            AssistDisclosure.this.show();
        }
    };
    private AssistDisclosureView mView;
    private boolean mViewAdded;
    private final WindowManager mWm;

    private class AssistDisclosureView extends View implements AnimatorUpdateListener {
        private int mAlpha = 0;
        private final ValueAnimator mAlphaInAnimator;
        private final ValueAnimator mAlphaOutAnimator;
        private final AnimatorSet mAnimator;
        private final Paint mPaint = new Paint();
        private final Paint mShadowPaint = new Paint();
        private float mShadowThickness;
        private float mThickness;
        private final ValueAnimator mTracingAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f}).setDuration(600);
        private float mTracingProgress = 0.0f;

        public AssistDisclosureView(Context context) {
            super(context);
            this.mTracingAnimator.addUpdateListener(this);
            this.mTracingAnimator.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, R.interpolator.assist_disclosure_trace));
            this.mAlphaInAnimator = ValueAnimator.ofInt(new int[]{0, 255}).setDuration(450);
            this.mAlphaInAnimator.addUpdateListener(this);
            this.mAlphaInAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            this.mAlphaOutAnimator = ValueAnimator.ofInt(new int[]{255, 0}).setDuration(400);
            this.mAlphaOutAnimator.addUpdateListener(this);
            this.mAlphaOutAnimator.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
            this.mAnimator = new AnimatorSet();
            this.mAnimator.play(this.mAlphaInAnimator).with(this.mTracingAnimator);
            this.mAnimator.play(this.mAlphaInAnimator).before(this.mAlphaOutAnimator);
            this.mAnimator.addListener(new AnimatorListenerAdapter() {
                boolean mCancelled;

                public void onAnimationStart(Animator animation) {
                    this.mCancelled = false;
                }

                public void onAnimationCancel(Animator animation) {
                    this.mCancelled = true;
                }

                public void onAnimationEnd(Animator animation) {
                    if (!this.mCancelled) {
                        AssistDisclosure.this.hide();
                    }
                }
            });
            PorterDuffXfermode srcMode = new PorterDuffXfermode(Mode.SRC);
            this.mPaint.setColor(-1);
            this.mPaint.setXfermode(srcMode);
            this.mShadowPaint.setColor(-12303292);
            this.mShadowPaint.setXfermode(srcMode);
            this.mThickness = getResources().getDimension(R.dimen.assist_disclosure_thickness);
            this.mShadowThickness = getResources().getDimension(R.dimen.assist_disclosure_shadow_thickness);
        }

        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            startAnimation();
            sendAccessibilityEvent(16777216);
        }

        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            this.mAnimator.cancel();
            this.mTracingProgress = 0.0f;
            this.mAlpha = 0;
        }

        private void startAnimation() {
            this.mAnimator.cancel();
            this.mAnimator.start();
        }

        protected void onDraw(Canvas canvas) {
            this.mPaint.setAlpha(this.mAlpha);
            this.mShadowPaint.setAlpha(this.mAlpha / 4);
            drawGeometry(canvas, this.mShadowPaint, this.mShadowThickness);
            drawGeometry(canvas, this.mPaint, 0.0f);
        }

        private void drawGeometry(Canvas canvas, Paint paint, float padding) {
            int width = getWidth();
            int height = getHeight();
            float thickness = this.mThickness;
            float pixelProgress = this.mTracingProgress * (((float) (width + height)) - (2.0f * thickness));
            float bottomProgress = Math.min(pixelProgress, ((float) width) / 2.0f);
            if (bottomProgress > 0.0f) {
                drawBeam(canvas, (((float) width) / 2.0f) - bottomProgress, ((float) height) - thickness, (((float) width) / 2.0f) + bottomProgress, (float) height, paint, padding);
            }
            float sideProgress = Math.min(pixelProgress - bottomProgress, ((float) height) - thickness);
            if (sideProgress > 0.0f) {
                drawBeam(canvas, 0.0f, (((float) height) - thickness) - sideProgress, thickness, ((float) height) - thickness, paint, padding);
                Canvas canvas2 = canvas;
                drawBeam(canvas2, ((float) width) - thickness, (((float) height) - thickness) - sideProgress, (float) width, ((float) height) - thickness, paint, padding);
            }
            float topProgress = Math.min((pixelProgress - bottomProgress) - sideProgress, ((float) (width / 2)) - thickness);
            if (sideProgress > 0.0f && topProgress > 0.0f) {
                drawBeam(canvas, thickness, 0.0f, thickness + topProgress, thickness, paint, padding);
                drawBeam(canvas, (((float) width) - thickness) - topProgress, 0.0f, ((float) width) - thickness, thickness, paint, padding);
            }
        }

        private void drawBeam(Canvas canvas, float left, float top, float right, float bottom, Paint paint, float padding) {
            canvas.drawRect(left - padding, top - padding, right + padding, bottom + padding, paint);
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            if (animation == this.mAlphaOutAnimator) {
                this.mAlpha = ((Integer) this.mAlphaOutAnimator.getAnimatedValue()).intValue();
            } else if (animation == this.mAlphaInAnimator) {
                this.mAlpha = ((Integer) this.mAlphaInAnimator.getAnimatedValue()).intValue();
            } else if (animation == this.mTracingAnimator) {
                this.mTracingProgress = ((Float) this.mTracingAnimator.getAnimatedValue()).floatValue();
            }
            invalidate();
        }
    }

    public AssistDisclosure(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mWm = (WindowManager) this.mContext.getSystemService(WindowManager.class);
    }

    public void postShow() {
        this.mHandler.removeCallbacks(this.mShowRunnable);
        this.mHandler.post(this.mShowRunnable);
    }

    private void show() {
        if (this.mView == null) {
            this.mView = new AssistDisclosureView(this.mContext);
        }
        if (!this.mViewAdded) {
            LayoutParams lp = new LayoutParams(2015, 17302792, -3);
            lp.setTitle("AssistDisclosure");
            this.mWm.addView(this.mView, lp);
            this.mViewAdded = true;
        }
    }

    private void hide() {
        if (this.mViewAdded) {
            this.mWm.removeView(this.mView);
            this.mViewAdded = false;
        }
    }
}
