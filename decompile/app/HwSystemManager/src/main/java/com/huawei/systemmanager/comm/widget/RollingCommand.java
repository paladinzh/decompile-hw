package com.huawei.systemmanager.comm.widget;

import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import com.huawei.systemmanager.comm.misc.Utility;

public class RollingCommand implements Runnable {
    private static final Interpolator DEFAULT_INTERPOLATOR = new DecelerateInterpolator();
    private int mCurrentNumber;
    private long mDuration;
    private final Handler mHandler;
    private Interpolator mInterpolator;
    private int mStartCount;
    private long mStartTime;
    private int mTargetCount;

    public RollingCommand() {
        this.mInterpolator = DEFAULT_INTERPOLATOR;
        this.mHandler = new Handler();
    }

    public RollingCommand(Handler handler, String type) {
        this.mInterpolator = new DecelerateInterpolator();
        this.mHandler = handler;
    }

    public void setInterpolator(Interpolator interpolator) {
        if (interpolator != null) {
            this.mInterpolator = interpolator;
        }
    }

    public void setNewTarget(int target) {
        this.mTargetCount = target;
    }

    public void setNewTarget(int target, long duration) {
        setNewTarget(this.mCurrentNumber, target, duration);
    }

    public void setNewTarget(int current, int target, long duration) {
        setNewTarget(current, target, duration, DEFAULT_INTERPOLATOR);
    }

    public void setNewTarget(int current, int target, long duration, Interpolator interpolator) {
        this.mHandler.removeCallbacks(this);
        this.mStartCount = current;
        this.mCurrentNumber = this.mStartCount;
        this.mTargetCount = target;
        this.mDuration = duration;
        this.mStartTime = SystemClock.elapsedRealtime();
        this.mInterpolator = interpolator;
        this.mHandler.post(this);
    }

    public void run() {
        boolean endFlag = true;
        float passedFactor = Utility.ALPHA_MAX;
        if (this.mDuration > 0) {
            long currentTime = SystemClock.elapsedRealtime();
            passedFactor = ((float) (((currentTime - this.mStartTime) * 100) / this.mDuration)) / 100.0f;
            if (currentTime < this.mStartTime + this.mDuration) {
                endFlag = false;
            }
        }
        if (passedFactor < 0.0f || passedFactor > Utility.ALPHA_MAX) {
            passedFactor = Utility.ALPHA_MAX;
        }
        if (endFlag) {
            this.mCurrentNumber = this.mTargetCount;
            onNumberUpate(this.mCurrentNumber);
            this.mHandler.removeCallbacks(this);
            onRollingComplate();
            return;
        }
        int count = ((int) (((float) (this.mTargetCount - this.mStartCount)) * this.mInterpolator.getInterpolation(passedFactor))) + this.mStartCount;
        this.mCurrentNumber = count;
        onNumberUpate(count);
        this.mHandler.post(this);
    }

    public void stop() {
        this.mTargetCount = 0;
        this.mStartCount = 0;
        this.mDuration = 0;
        this.mCurrentNumber = 0;
        this.mHandler.removeCallbacksAndMessages(null);
    }

    protected void onNumberUpate(int count) {
    }

    protected void onRollingComplate() {
    }
}
