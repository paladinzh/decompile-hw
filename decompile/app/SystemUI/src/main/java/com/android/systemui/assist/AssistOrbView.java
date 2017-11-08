package com.android.systemui.assist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.Interpolators;
import com.android.systemui.R;

public class AssistOrbView extends FrameLayout {
    private final Paint mBackgroundPaint;
    private final int mBaseMargin;
    private float mCircleAnimationEndValue;
    private ValueAnimator mCircleAnimator;
    private final int mCircleMinSize;
    private final Rect mCircleRect;
    private float mCircleSize;
    private AnimatorUpdateListener mCircleUpdateListener;
    private AnimatorListenerAdapter mClearAnimatorListener;
    private boolean mClipToOutline;
    private ImageView mLogo;
    private final int mMaxElevation;
    private float mOffset;
    private ValueAnimator mOffsetAnimator;
    private AnimatorUpdateListener mOffsetUpdateListener;
    private float mOutlineAlpha;
    private final Interpolator mOvershootInterpolator;
    private final int mStaticOffset;
    private final Rect mStaticRect;

    public AssistOrbView(Context context) {
        this(context, null);
    }

    public AssistOrbView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AssistOrbView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AssistOrbView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mBackgroundPaint = new Paint();
        this.mCircleRect = new Rect();
        this.mStaticRect = new Rect();
        this.mOvershootInterpolator = new OvershootInterpolator();
        this.mCircleUpdateListener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                AssistOrbView.this.applyCircleSize(((Float) animation.getAnimatedValue()).floatValue());
                AssistOrbView.this.updateElevation();
            }
        };
        this.mClearAnimatorListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                AssistOrbView.this.mCircleAnimator = null;
            }
        };
        this.mOffsetUpdateListener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                AssistOrbView.this.mOffset = ((Float) animation.getAnimatedValue()).floatValue();
                AssistOrbView.this.updateLayout();
            }
        };
        setOutlineProvider(new ViewOutlineProvider() {
            public void getOutline(View view, Outline outline) {
                if (AssistOrbView.this.mCircleSize > 0.0f) {
                    outline.setOval(AssistOrbView.this.mCircleRect);
                } else {
                    outline.setEmpty();
                }
                outline.setAlpha(AssistOrbView.this.mOutlineAlpha);
            }
        });
        setWillNotDraw(false);
        this.mCircleMinSize = context.getResources().getDimensionPixelSize(R.dimen.assist_orb_size);
        this.mBaseMargin = context.getResources().getDimensionPixelSize(R.dimen.assist_orb_base_margin);
        this.mStaticOffset = context.getResources().getDimensionPixelSize(R.dimen.assist_orb_travel_distance);
        this.mMaxElevation = context.getResources().getDimensionPixelSize(R.dimen.assist_orb_elevation);
        this.mBackgroundPaint.setAntiAlias(true);
        this.mBackgroundPaint.setColor(getResources().getColor(R.color.assist_orb_color));
    }

    public ImageView getLogo() {
        return this.mLogo;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawCircle((float) this.mCircleRect.centerX(), (float) this.mCircleRect.centerY(), this.mCircleSize / 2.0f, this.mBackgroundPaint);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mLogo = (ImageView) findViewById(R.id.search_logo);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.mLogo.layout(0, 0, this.mLogo.getMeasuredWidth(), this.mLogo.getMeasuredHeight());
        if (changed) {
            updateCircleRect(this.mStaticRect, (float) this.mStaticOffset, true);
        }
    }

    public void animateCircleSize(float circleSize, long duration, long startDelay, Interpolator interpolator) {
        if (circleSize != this.mCircleAnimationEndValue) {
            if (this.mCircleAnimator != null) {
                this.mCircleAnimator.cancel();
            }
            this.mCircleAnimator = ValueAnimator.ofFloat(new float[]{this.mCircleSize, circleSize});
            this.mCircleAnimator.addUpdateListener(this.mCircleUpdateListener);
            this.mCircleAnimator.addListener(this.mClearAnimatorListener);
            this.mCircleAnimator.setInterpolator(interpolator);
            this.mCircleAnimator.setDuration(duration);
            this.mCircleAnimator.setStartDelay(startDelay);
            this.mCircleAnimator.start();
            this.mCircleAnimationEndValue = circleSize;
        }
    }

    private void applyCircleSize(float circleSize) {
        this.mCircleSize = circleSize;
        updateLayout();
    }

    private void updateElevation() {
        setElevation((1.0f - Math.max((((float) this.mStaticOffset) - this.mOffset) / ((float) this.mStaticOffset), 0.0f)) * ((float) this.mMaxElevation));
    }

    private void animateOffset(float offset, long duration, long startDelay, Interpolator interpolator) {
        if (this.mOffsetAnimator != null) {
            this.mOffsetAnimator.removeAllListeners();
            this.mOffsetAnimator.cancel();
        }
        this.mOffsetAnimator = ValueAnimator.ofFloat(new float[]{this.mOffset, offset});
        this.mOffsetAnimator.addUpdateListener(this.mOffsetUpdateListener);
        this.mOffsetAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                AssistOrbView.this.mOffsetAnimator = null;
            }
        });
        this.mOffsetAnimator.setInterpolator(interpolator);
        this.mOffsetAnimator.setStartDelay(startDelay);
        this.mOffsetAnimator.setDuration(duration);
        this.mOffsetAnimator.start();
    }

    private void updateLayout() {
        updateCircleRect();
        updateLogo();
        invalidateOutline();
        invalidate();
        updateClipping();
    }

    private void updateClipping() {
        boolean clip = this.mCircleSize < ((float) this.mCircleMinSize);
        if (clip != this.mClipToOutline) {
            setClipToOutline(clip);
            this.mClipToOutline = clip;
        }
    }

    private void updateLogo() {
        float translationX = (((float) (this.mCircleRect.left + this.mCircleRect.right)) / 2.0f) - (((float) this.mLogo.getWidth()) / 2.0f);
        float t = (((float) this.mStaticOffset) - this.mOffset) / ((float) this.mStaticOffset);
        float translationY = (((((float) (this.mCircleRect.top + this.mCircleRect.bottom)) / 2.0f) - (((float) this.mLogo.getHeight()) / 2.0f)) - (((float) this.mCircleMinSize) / 7.0f)) + ((((float) this.mStaticOffset) * t) * 0.1f);
        this.mLogo.setImageAlpha((int) (255.0f * Math.max(((1.0f - t) - 0.5f) * 2.0f, 0.0f)));
        this.mLogo.setTranslationX(translationX);
        this.mLogo.setTranslationY(translationY);
    }

    private void updateCircleRect() {
        updateCircleRect(this.mCircleRect, this.mOffset, false);
    }

    private void updateCircleRect(Rect rect, float offset, boolean useStaticSize) {
        float circleSize = useStaticSize ? (float) this.mCircleMinSize : this.mCircleSize;
        int left = ((int) (((float) getWidth()) - circleSize)) / 2;
        int top = (int) (((((float) getHeight()) - (circleSize / 2.0f)) - ((float) this.mBaseMargin)) - offset);
        rect.set(left, top, (int) (((float) left) + circleSize), (int) (((float) top) + circleSize));
    }

    public void startExitAnimation(long delay) {
        animateCircleSize(0.0f, 200, delay, Interpolators.FAST_OUT_LINEAR_IN);
        animateOffset(0.0f, 200, delay, Interpolators.FAST_OUT_LINEAR_IN);
    }

    public void startEnterAnimation() {
        applyCircleSize(0.0f);
        post(new Runnable() {
            public void run() {
                AssistOrbView.this.animateCircleSize((float) AssistOrbView.this.mCircleMinSize, 300, 0, AssistOrbView.this.mOvershootInterpolator);
                AssistOrbView.this.animateOffset((float) AssistOrbView.this.mStaticOffset, 400, 0, Interpolators.LINEAR_OUT_SLOW_IN);
            }
        });
    }

    public void reset() {
        this.mClipToOutline = false;
        this.mBackgroundPaint.setAlpha(255);
        this.mOutlineAlpha = 1.0f;
    }

    public boolean hasOverlappingRendering() {
        return false;
    }
}
