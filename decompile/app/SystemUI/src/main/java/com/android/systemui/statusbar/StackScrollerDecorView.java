package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;

public abstract class StackScrollerDecorView extends ExpandableView {
    private boolean mAnimating;
    protected View mContent;
    private boolean mIsVisible;

    protected abstract View findContentView();

    public StackScrollerDecorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mContent = findContentView();
        setInvisible();
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setOutlineProvider(null);
    }

    public boolean isTransparent() {
        return true;
    }

    public void performVisibilityAnimation(boolean nowVisible) {
        animateText(nowVisible, null);
    }

    public void performVisibilityAnimation(boolean nowVisible, Runnable onFinishedRunnable) {
        animateText(nowVisible, onFinishedRunnable);
    }

    public boolean isVisible() {
        return !this.mIsVisible ? this.mAnimating : true;
    }

    private void animateText(boolean nowVisible, final Runnable onFinishedRunnable) {
        if (nowVisible != this.mIsVisible) {
            Interpolator interpolator;
            float endValue = nowVisible ? 1.0f : 0.0f;
            if (nowVisible) {
                interpolator = Interpolators.ALPHA_IN;
            } else {
                interpolator = Interpolators.ALPHA_OUT;
            }
            this.mAnimating = true;
            this.mContent.animate().alpha(endValue).setInterpolator(interpolator).setDuration(260).withEndAction(new Runnable() {
                public void run() {
                    StackScrollerDecorView.this.mAnimating = false;
                    if (onFinishedRunnable != null) {
                        onFinishedRunnable.run();
                    }
                }
            });
            this.mIsVisible = nowVisible;
        } else if (onFinishedRunnable != null) {
            onFinishedRunnable.run();
        }
    }

    public void setInvisible() {
        this.mContent.setAlpha(0.0f);
        this.mIsVisible = false;
    }

    public void performRemoveAnimation(long duration, float translationDirection, Runnable onFinishedRunnable) {
        performVisibilityAnimation(false);
    }

    public void performAddAnimation(long delay, long duration) {
        performVisibilityAnimation(true);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void cancelAnimation() {
        this.mContent.animate().cancel();
    }
}
