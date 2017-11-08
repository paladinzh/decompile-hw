package com.android.systemui.recents.misc;

import android.os.Handler;
import android.view.ViewDebug.ExportedProperty;

public class DozeTrigger {
    @ExportedProperty(category = "recents")
    int mDozeDurationMilliseconds;
    Runnable mDozeRunnable = new Runnable() {
        public void run() {
            DozeTrigger.this.mIsDozing = false;
            DozeTrigger.this.mIsAsleep = true;
            DozeTrigger.this.mOnSleepRunnable.run();
        }
    };
    Handler mHandler = new Handler();
    @ExportedProperty(category = "recents")
    boolean mIsAsleep;
    @ExportedProperty(category = "recents")
    boolean mIsDozing;
    Runnable mOnSleepRunnable;

    public DozeTrigger(int dozeDurationMilliseconds, Runnable onSleepRunnable) {
        this.mDozeDurationMilliseconds = dozeDurationMilliseconds;
        this.mOnSleepRunnable = onSleepRunnable;
    }

    public void startDozing() {
        forcePoke();
        this.mIsAsleep = false;
    }

    public void stopDozing() {
        this.mHandler.removeCallbacks(this.mDozeRunnable);
        this.mIsDozing = false;
        this.mIsAsleep = false;
    }

    public void setDozeDuration(int duration) {
        this.mDozeDurationMilliseconds = duration;
    }

    public void poke() {
        if (this.mIsDozing) {
            forcePoke();
        }
    }

    void forcePoke() {
        this.mHandler.removeCallbacks(this.mDozeRunnable);
        this.mHandler.postDelayed(this.mDozeRunnable, (long) this.mDozeDurationMilliseconds);
        this.mIsDozing = true;
    }

    public boolean isDozing() {
        return this.mIsDozing;
    }

    public boolean isAsleep() {
        return this.mIsAsleep;
    }
}
