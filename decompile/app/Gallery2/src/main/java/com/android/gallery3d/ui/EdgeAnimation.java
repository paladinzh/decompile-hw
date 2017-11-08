package com.android.gallery3d.ui;

import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.common.Utils;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Paper */
class EdgeAnimation {
    private long mDuration;
    private final Interpolator mInterpolator = new DecelerateInterpolator();
    private long mStartTime;
    private int mState = 0;
    private float mValue;
    private float mValueFinish;
    private float mValueStart;

    private void startAnimation(float start, float finish, long duration, int newState) {
        this.mValueStart = start;
        this.mValueFinish = finish;
        this.mDuration = duration;
        this.mStartTime = now();
        this.mState = newState;
    }

    public void onPull(float deltaDistance) {
        if (this.mState != 2) {
            this.mValue = Utils.clamp(this.mValue + deltaDistance, (float) GroundOverlayOptions.NO_DIMENSION, (float) WMElement.CAMERASIZEVALUE1B1);
            this.mState = 1;
        }
    }

    public void onRelease() {
        if (this.mState != 0 && this.mState != 2) {
            startAnimation(this.mValue, 0.0f, 500, 3);
        }
    }

    public void onAbsorb(float velocity) {
        startAnimation(this.mValue, Utils.clamp(this.mValue + (0.1f * velocity), (float) GroundOverlayOptions.NO_DIMENSION, (float) WMElement.CAMERASIZEVALUE1B1), 200, 2);
    }

    public boolean update() {
        if (this.mState == 0) {
            return false;
        }
        if (this.mState == 1) {
            return true;
        }
        float t = Utils.clamp(((float) (now() - this.mStartTime)) / ((float) this.mDuration), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
        this.mValue = this.mValueStart + ((this.mValueFinish - this.mValueStart) * (this.mState == 2 ? t : this.mInterpolator.getInterpolation(t)));
        if (t >= WMElement.CAMERASIZEVALUE1B1) {
            switch (this.mState) {
                case 2:
                    startAnimation(this.mValue, 0.0f, 500, 3);
                    break;
                case 3:
                    this.mState = 0;
                    break;
            }
        }
        return true;
    }

    public float getValue() {
        return this.mValue;
    }

    private long now() {
        return AnimationTime.get();
    }
}
