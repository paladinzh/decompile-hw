package com.android.systemui.statusbar.phone;

import android.util.Pools.SynchronizedPool;
import android.view.MotionEvent;
import android.view.VelocityTracker;

public class PlatformVelocityTracker implements VelocityTrackerInterface {
    private static final SynchronizedPool<PlatformVelocityTracker> sPool = new SynchronizedPool(2);
    private VelocityTracker mTracker;

    public static PlatformVelocityTracker obtain() {
        PlatformVelocityTracker tracker = (PlatformVelocityTracker) sPool.acquire();
        if (tracker == null) {
            tracker = new PlatformVelocityTracker();
        }
        tracker.setTracker(VelocityTracker.obtain());
        return tracker;
    }

    public void setTracker(VelocityTracker tracker) {
        this.mTracker = tracker;
    }

    public void addMovement(MotionEvent event) {
        this.mTracker.addMovement(event);
    }

    public void computeCurrentVelocity(int units) {
        this.mTracker.computeCurrentVelocity(units);
    }

    public float getXVelocity() {
        return this.mTracker.getXVelocity();
    }

    public float getYVelocity() {
        return this.mTracker.getYVelocity();
    }

    public void recycle() {
        this.mTracker.recycle();
        sPool.release(this);
    }
}
