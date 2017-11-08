package com.huawei.watermark.ui.element;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import com.android.gallery3d.R;
import com.huawei.watermark.manager.parse.WMElement;
import com.huawei.watermark.wmutil.WMAnimationUtil;

public class WMRotateLinearLayout2 extends LinearLayout implements Rotatable {
    private long mAnimationEndTime = 0;
    private long mAnimationStartTime = 0;
    private boolean mClockwise = false;
    private int mCurrentDegree = 0;
    private Interpolator mInterpolator;
    private Runnable mRotateRunnable = new Runnable() {
        public void run() {
            if (WMRotateLinearLayout2.this.mCurrentDegree != WMRotateLinearLayout2.this.mTargetDegree) {
                long time = AnimationUtils.currentAnimationTimeMillis();
                if (time < WMRotateLinearLayout2.this.mAnimationEndTime) {
                    long timeDuration = WMRotateLinearLayout2.this.mAnimationEndTime - WMRotateLinearLayout2.this.mAnimationStartTime;
                    float deltaTime = ((float) timeDuration) * WMRotateLinearLayout2.this.mInterpolator.getInterpolation(((((float) (time - WMRotateLinearLayout2.this.mAnimationStartTime)) * WMElement.CAMERASIZEVALUE1B1) / ((float) timeDuration)) * WMElement.CAMERASIZEVALUE1B1);
                    int -get6 = WMRotateLinearLayout2.this.mStartDegree;
                    if (!WMRotateLinearLayout2.this.mClockwise) {
                        deltaTime = -deltaTime;
                    }
                    int degree = -get6 + ((int) ((214.28572f * deltaTime) / 1000.0f));
                    WMRotateLinearLayout2.this.mCurrentDegree = degree >= 0 ? degree % 360 : (degree % 360) + 360;
                } else {
                    WMRotateLinearLayout2.this.mCurrentDegree = WMRotateLinearLayout2.this.mTargetDegree;
                }
                WMRotateLinearLayout2.this.setRotation((float) (-WMRotateLinearLayout2.this.mCurrentDegree));
                WMRotateLinearLayout2.this.post(WMRotateLinearLayout2.this.mRotateRunnable);
            }
        }
    };
    private int mStartDegree = 0;
    private int mTargetDegree = 0;

    public WMRotateLinearLayout2(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mInterpolator = WMAnimationUtil.getInterpolator(context, R.anim.cubic_bezier_interpolator_type_a);
    }

    public void setCurrentDegree(int currentDegree) {
        this.mCurrentDegree = currentDegree;
    }

    public void setTargetDegree(int targetDegree) {
        this.mTargetDegree = targetDegree;
    }

    public void setOrientation(int degree, boolean animation) {
        boolean z = false;
        if (degree != -1) {
            degree = degree >= 0 ? degree % 360 : (degree % 360) + 360;
            if (degree != this.mTargetDegree) {
                this.mTargetDegree = degree;
                this.mStartDegree = this.mCurrentDegree;
                this.mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();
                int diff = this.mTargetDegree - this.mCurrentDegree;
                if (diff < 0) {
                    diff += 360;
                }
                if (diff > 180) {
                    diff -= 360;
                }
                if (diff >= 0) {
                    z = true;
                }
                this.mClockwise = z;
                if (animation) {
                    this.mAnimationEndTime = this.mAnimationStartTime + ((long) ((((float) Math.abs(diff)) * 1000.0f) / 214.28572f));
                } else {
                    this.mAnimationEndTime = 0;
                }
                post(this.mRotateRunnable);
            }
        }
    }
}
