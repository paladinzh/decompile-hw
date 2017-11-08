package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import com.huawei.keyguard.util.HwLog;

public class ScrimView extends View {
    private ValueAnimator mAlphaAnimator;
    private AnimatorUpdateListener mAlphaUpdateListener;
    private Runnable mChangeRunnable;
    private AnimatorListenerAdapter mClearAnimatorListener;
    private boolean mDebug;
    private boolean mDrawAsSrc;
    private Rect mExcludedRect;
    private boolean mHasExcludedArea;
    private boolean mIsEmpty;
    private final Paint mPaint;
    private int mScrimColor;
    private float mViewAlpha;

    public ScrimView(Context context) {
        this(context, null);
    }

    public ScrimView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrimView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ScrimView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mPaint = new Paint();
        this.mDebug = false;
        this.mIsEmpty = true;
        this.mViewAlpha = 1.0f;
        this.mExcludedRect = new Rect();
        this.mAlphaUpdateListener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                ScrimView.this.mViewAlpha = ((Float) animation.getAnimatedValue()).floatValue();
                ScrimView.this.invalidate();
            }
        };
        this.mClearAnimatorListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ScrimView.this.mAlphaAnimator = null;
            }
        };
    }

    protected void onDraw(Canvas canvas) {
        if (this.mDrawAsSrc || (!this.mIsEmpty && this.mViewAlpha > 0.0f)) {
            Mode mode = this.mDrawAsSrc ? Mode.SRC : Mode.SRC_OVER;
            int color = getScrimColorWithAlpha();
            if (this.mHasExcludedArea) {
                this.mPaint.setColor(color);
                if (this.mExcludedRect.top > 0) {
                    canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) this.mExcludedRect.top, this.mPaint);
                }
                if (this.mExcludedRect.left > 0) {
                    canvas.drawRect(0.0f, (float) this.mExcludedRect.top, (float) this.mExcludedRect.left, (float) this.mExcludedRect.bottom, this.mPaint);
                }
                if (this.mExcludedRect.right < getWidth()) {
                    canvas.drawRect((float) this.mExcludedRect.right, (float) this.mExcludedRect.top, (float) getWidth(), (float) this.mExcludedRect.bottom, this.mPaint);
                }
                if (this.mExcludedRect.bottom < getHeight()) {
                    canvas.drawRect(0.0f, (float) this.mExcludedRect.bottom, (float) getWidth(), (float) getHeight(), this.mPaint);
                    return;
                }
                return;
            }
            canvas.drawColor(color, mode);
        }
    }

    public int getScrimColorWithAlpha() {
        this.mScrimColor = 0;
        return Color.argb((int) (((float) Color.alpha(0)) * this.mViewAlpha), Color.red(0), Color.green(0), Color.blue(0));
    }

    public void setDrawAsSrc(boolean asSrc) {
        Mode mode;
        this.mDrawAsSrc = asSrc;
        Paint paint = this.mPaint;
        if (this.mDrawAsSrc) {
            mode = Mode.SRC;
        } else {
            mode = Mode.SRC_OVER;
        }
        paint.setXfermode(new PorterDuffXfermode(mode));
        invalidate();
    }

    public void setScrimColor(int color) {
        if (this.mDebug) {
            HwLog.d("SrimvView", "color is " + color);
        }
        if (this.mScrimColor != 0) {
            boolean z;
            if (Color.alpha(0) == 0) {
                z = true;
            } else {
                z = false;
            }
            this.mIsEmpty = z;
            this.mScrimColor = 0;
            invalidate();
            if (this.mChangeRunnable != null) {
                this.mChangeRunnable.run();
            }
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void animateViewAlpha(float alpha, long durationOut, Interpolator interpolator) {
        if (this.mAlphaAnimator != null) {
            this.mAlphaAnimator.cancel();
        }
        this.mAlphaAnimator = ValueAnimator.ofFloat(new float[]{this.mViewAlpha, alpha});
        this.mAlphaAnimator.addUpdateListener(this.mAlphaUpdateListener);
        this.mAlphaAnimator.addListener(this.mClearAnimatorListener);
        this.mAlphaAnimator.setInterpolator(interpolator);
        this.mAlphaAnimator.setDuration(durationOut);
        this.mAlphaAnimator.start();
    }

    public void setExcludedArea(Rect area) {
        boolean z = false;
        if (area == null) {
            this.mHasExcludedArea = false;
            invalidate();
            return;
        }
        int left = Math.max(area.left, 0);
        int top = Math.max(area.top, 0);
        int right = Math.min(area.right, getWidth());
        int bottom = Math.min(area.bottom, getHeight());
        this.mExcludedRect.set(left, top, right, bottom);
        if (left < right && top < bottom) {
            z = true;
        }
        this.mHasExcludedArea = z;
        invalidate();
    }

    public void setChangeRunnable(Runnable changeRunnable) {
        this.mChangeRunnable = changeRunnable;
    }
}
