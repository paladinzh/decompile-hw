package com.huawei.gallery.anim;

import android.view.animation.Interpolator;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.TraceController;
import com.huawei.watermark.manager.parse.WMElement;

public abstract class Animation {
    private int mDelay = 0;
    private int mDuration;
    private boolean mFillAfter = false;
    private Interpolator mInterpolator;
    private boolean mIsAnimating = false;
    private AnimationListener mListener;
    private float mPreFrameElapse = GroundOverlayOptions.NO_DIMENSION;
    private boolean mReverse = false;
    protected float mReversePercent;
    private long mStartTime = -2;
    private float mTotoalMissElapse = 0.0f;

    public interface AnimationListener {
        void onAnimationEnd();
    }

    protected abstract void onCalculate(float f);

    public void setInterpolator(Interpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    public void start() {
        TraceController.traceBegin("Animation.start");
        this.mIsAnimating = true;
        if (!this.mReverse) {
            this.mStartTime = -1;
            this.mTotoalMissElapse = 0.0f;
            this.mPreFrameElapse = GroundOverlayOptions.NO_DIMENSION;
            TraceController.traceEnd();
        }
    }

    public void setStartTime(long time) {
        this.mStartTime = time;
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
        boolean more;
        TraceController.traceBegin("Animation.calculate");
        if (this.mStartTime == -1) {
            this.mStartTime = currentTimeMillis;
        }
        int elapse = (int) ((currentTimeMillis - this.mStartTime) - ((long) this.mDelay));
        if (elapse < 0) {
            elapse = 0;
        }
        if (this.mPreFrameElapse == GroundOverlayOptions.NO_DIMENSION) {
            this.mPreFrameElapse = (float) elapse;
        }
        if (((float) elapse) - this.mPreFrameElapse > 22.0f) {
            this.mTotoalMissElapse += (((float) elapse) - this.mPreFrameElapse) - 22.0f;
        }
        this.mPreFrameElapse = (float) elapse;
        elapse = (int) (((float) elapse) - this.mTotoalMissElapse);
        float x = ((float) elapse) / ((float) this.mDuration);
        if (this.mReverse) {
            x = (this.mReversePercent * 2.0f) - x;
            more = x > 0.0f;
        } else {
            this.mReversePercent = x;
            more = elapse < this.mDuration;
        }
        x = Utils.clamp(x, 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
        Interpolator i = this.mInterpolator;
        if (i != null) {
            x = i.getInterpolation(x);
        }
        onCalculate(x);
        if (!more) {
            if (!this.mFillAfter) {
                this.mStartTime = -2;
            }
            onAnimationEnd();
        }
        TraceController.traceEnd();
        return this.mIsAnimating;
    }

    protected void onAnimationEnd() {
        if (this.mIsAnimating) {
            TraceController.traceBegin("Animation.onAnimationEnd");
            this.mIsAnimating = false;
            if (this.mListener != null) {
                this.mListener.onAnimationEnd();
            }
            TraceController.traceEnd();
        }
    }
}
