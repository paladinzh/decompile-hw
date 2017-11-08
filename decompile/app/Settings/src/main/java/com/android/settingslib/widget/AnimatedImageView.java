package com.android.settingslib.widget;

import android.content.Context;
import android.graphics.drawable.AnimatedRotateDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class AnimatedImageView extends ImageView {
    private boolean mAnimating;
    private AnimatedRotateDrawable mDrawable;

    public AnimatedImageView(Context context) {
        super(context);
    }

    public AnimatedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void updateDrawable() {
        if (isShown() && this.mDrawable != null) {
            this.mDrawable.stop();
        }
        Drawable drawable = getDrawable();
        if (drawable instanceof AnimatedRotateDrawable) {
            this.mDrawable = (AnimatedRotateDrawable) drawable;
            this.mDrawable.setFramesCount(56);
            this.mDrawable.setFramesDuration(32);
            if (isShown() && this.mAnimating) {
                this.mDrawable.start();
                return;
            }
            return;
        }
        this.mDrawable = null;
    }

    private void updateAnimating() {
        if (this.mDrawable == null) {
            return;
        }
        if (isShown() && this.mAnimating) {
            this.mDrawable.start();
        } else {
            this.mDrawable.stop();
        }
    }

    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        updateDrawable();
    }

    public void setImageResource(int resid) {
        super.setImageResource(resid);
        updateDrawable();
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateAnimating();
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        updateAnimating();
    }

    public void setAnimating(boolean animating) {
        this.mAnimating = animating;
        updateAnimating();
    }

    protected void onVisibilityChanged(View changedView, int vis) {
        super.onVisibilityChanged(changedView, vis);
        updateAnimating();
    }
}
