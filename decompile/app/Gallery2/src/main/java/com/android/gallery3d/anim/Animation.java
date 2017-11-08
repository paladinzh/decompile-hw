package com.android.gallery3d.anim;

import android.view.animation.Interpolator;
import com.android.gallery3d.common.Utils;
import com.huawei.watermark.manager.parse.WMElement;

public abstract class Animation {
    private int mDelay = 0;
    protected int mDuration;
    protected Interpolator mInterpolator;
    protected boolean mIsAnimating = false;
    private AnimationListener mListener;
    protected long mStartTime = -2;

    public interface AnimationListener {
        void onAnimationEnd();
    }

    protected abstract void onCalculate(float f);

    public void setAnimationListener(AnimationListener listener) {
        this.mListener = listener;
    }

    public void setInterpolator(Interpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    public void setDelay(int delay) {
        this.mDelay = delay;
    }

    public void start() {
        this.mIsAnimating = true;
        this.mStartTime = -1;
    }

    public boolean isActive() {
        return this.mStartTime != -2;
    }

    public boolean isAnimating() {
        return this.mIsAnimating;
    }

    public void forceStop() {
        this.mStartTime = -2;
        onAnimationEnd();
    }

    public boolean calculate(long currentTimeMillis) {
        if (this.mStartTime == -2) {
            return false;
        }
        if (this.mStartTime == -1) {
            this.mStartTime = currentTimeMillis;
        }
        int elapse = (int) ((currentTimeMillis - this.mStartTime) - ((long) this.mDelay));
        float x = Utils.clamp(((float) elapse) / ((float) this.mDuration), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
        Interpolator i = this.mInterpolator;
        if (i != null) {
            x = i.getInterpolation(x);
        }
        onCalculate(x);
        if (elapse >= this.mDuration) {
            this.mStartTime = -2;
            onAnimationEnd();
        }
        return this.mIsAnimating;
    }

    protected void onAnimationEnd() {
        if (this.mIsAnimating) {
            this.mIsAnimating = false;
            if (this.mListener != null) {
                this.mListener.onAnimationEnd();
            }
        }
    }
}
