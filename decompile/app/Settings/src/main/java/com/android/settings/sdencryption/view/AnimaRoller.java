package com.android.settings.sdencryption.view;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

public class AnimaRoller {
    public static final Interpolator ACCELER_DECELER_POLATRO = new AccelerateDecelerateInterpolator();
    private float mCurrentFactor;
    private float mCurrentPoint;
    private int mDuration;
    private float mEndPonit;
    private boolean mFinished;
    private Interpolator mInterpolator = ACCELER_DECELER_POLATRO;
    private float mStartPonit;
    private long mStartTime;

    public void startRoll(float startPonit, float endPonit, int duration) {
        if (duration <= 0) {
            finish();
            return;
        }
        this.mStartPonit = startPonit;
        this.mEndPonit = endPonit;
        this.mDuration = duration;
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mFinished = false;
    }

    public void continueRoll(float endPoint, int duration) {
        computeRoll();
        startRoll(this.mCurrentPoint, endPoint, duration);
    }

    public boolean computeRoll() {
        long elapsedTime = AnimationUtils.currentAnimationTimeMillis() - this.mStartTime;
        if (elapsedTime < ((long) this.mDuration)) {
            float q = this.mInterpolator.getInterpolation(((float) elapsedTime) / ((float) this.mDuration));
            this.mCurrentPoint = this.mStartPonit + ((this.mEndPonit - this.mStartPonit) * q);
            this.mCurrentFactor = q;
            return true;
        }
        this.mCurrentPoint = this.mEndPonit;
        this.mCurrentFactor = 1.0f;
        this.mFinished = true;
        return false;
    }

    public float value() {
        return this.mCurrentPoint;
    }

    public void finish() {
        this.mCurrentPoint = this.mEndPonit;
        this.mFinished = true;
    }
}
