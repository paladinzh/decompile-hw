package com.android.systemui.statusbar.phone;

import android.util.Pools.SynchronizedPool;
import android.view.MotionEvent;
import java.util.ArrayDeque;
import java.util.Iterator;

public class NoisyVelocityTracker implements VelocityTrackerInterface {
    private static final SynchronizedPool<NoisyVelocityTracker> sNoisyPool = new SynchronizedPool(2);
    private final int MAX_EVENTS = 8;
    private ArrayDeque<MotionEventCopy> mEventBuf = new ArrayDeque(8);
    private float mVX;
    private float mVY = 0.0f;

    private static class MotionEventCopy {
        long t;
        float x;
        float y;

        public MotionEventCopy(float x2, float y2, long eventTime) {
            this.x = x2;
            this.y = y2;
            this.t = eventTime;
        }
    }

    public static NoisyVelocityTracker obtain() {
        NoisyVelocityTracker instance = (NoisyVelocityTracker) sNoisyPool.acquire();
        return instance != null ? instance : new NoisyVelocityTracker();
    }

    private NoisyVelocityTracker() {
    }

    public void addMovement(MotionEvent event) {
        if (this.mEventBuf.size() == 8) {
            this.mEventBuf.remove();
        }
        this.mEventBuf.add(new MotionEventCopy(event.getX(), event.getY(), event.getEventTime()));
    }

    public void computeCurrentVelocity(int units) {
        this.mVY = 0.0f;
        this.mVX = 0.0f;
        MotionEventCopy last = null;
        int i = 0;
        float totalweight = 0.0f;
        float weight = 10.0f;
        Iterator<MotionEventCopy> iter = this.mEventBuf.iterator();
        while (iter.hasNext()) {
            MotionEventCopy event = (MotionEventCopy) iter.next();
            if (last != null) {
                float dt = ((float) (event.t - last.t)) / ((float) units);
                float dx = event.x - last.x;
                float dy = event.y - last.y;
                if (event.t != last.t) {
                    this.mVX += (weight * dx) / dt;
                    this.mVY += (weight * dy) / dt;
                    totalweight += weight;
                    weight *= 0.75f;
                }
            }
            last = event;
            i++;
        }
        if (totalweight > 0.0f) {
            this.mVX /= totalweight;
            this.mVY /= totalweight;
            return;
        }
        this.mVY = 0.0f;
        this.mVX = 0.0f;
    }

    public float getXVelocity() {
        if (Float.isNaN(this.mVX) || Float.isInfinite(this.mVX)) {
            this.mVX = 0.0f;
        }
        return this.mVX;
    }

    public float getYVelocity() {
        if (Float.isNaN(this.mVY) || Float.isInfinite(this.mVX)) {
            this.mVY = 0.0f;
        }
        return this.mVY;
    }

    public void recycle() {
        this.mEventBuf.clear();
        sNoisyPool.release(this);
    }
}
