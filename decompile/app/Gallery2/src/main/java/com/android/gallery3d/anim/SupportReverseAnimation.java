package com.android.gallery3d.anim;

import android.view.animation.Interpolator;
import com.android.gallery3d.common.Utils;
import com.huawei.watermark.manager.parse.WMElement;

public abstract class SupportReverseAnimation extends Animation {
    private int mElapseTime = 0;
    private boolean mNeedAutoReverse = true;
    private boolean mReverse = false;

    public boolean reverse() {
        if (this.mReverse) {
            return false;
        }
        if (this.mStartTime == -1) {
            forceStop();
            return false;
        }
        this.mReverse = true;
        super.start();
        return true;
    }

    public void start() {
        super.start();
        this.mReverse = false;
        this.mNeedAutoReverse = true;
        this.mElapseTime = 0;
    }

    public boolean calculate(long currentTimeMillis) {
        if (this.mStartTime == -2) {
            return false;
        }
        float x;
        if (this.mStartTime == -1) {
            this.mStartTime = currentTimeMillis;
        }
        int elapse = 0;
        if (this.mReverse) {
            elapse = this.mElapseTime - ((int) (currentTimeMillis - this.mStartTime));
            x = Utils.clamp(((float) elapse) / ((float) this.mDuration), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
        } else {
            this.mElapseTime = Utils.clamp((int) (currentTimeMillis - this.mStartTime), 0, this.mDuration);
            x = Utils.clamp(((float) this.mElapseTime) / ((float) this.mDuration), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
            if (this.mNeedAutoReverse && this.mElapseTime == this.mDuration) {
                elapse = this.mElapseTime;
                reverse();
            }
        }
        Interpolator i = this.mInterpolator;
        if (i != null) {
            x = i.getInterpolation(x);
        }
        onCalculate(x);
        if (elapse <= 0 && this.mReverse) {
            this.mStartTime = -2;
            onAnimationEnd();
        }
        return this.mIsAnimating;
    }
}
