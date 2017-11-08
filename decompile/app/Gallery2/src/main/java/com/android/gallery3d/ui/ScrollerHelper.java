package com.android.gallery3d.ui;

import android.content.Context;
import android.os.SystemProperties;
import android.view.ViewConfiguration;
import com.android.gallery3d.common.OverScroller;
import com.android.gallery3d.common.Utils;
import com.huawei.gallery.animation.CubicBezierInterpolator;

public class ScrollerHelper {
    private static final float DENSITY_SCALE = (((float) SystemProperties.getInt("hw.lcd.density.scale", 1000)) / 1000.0f);
    private int mOverflingDistance;
    private boolean mOverflingEnabled;
    private OverScroller mScroller;

    public ScrollerHelper(Context context) {
        this.mScroller = new OverScroller(context, DENSITY_SCALE);
        this.mScroller.setSpringBackInterpolator(new CubicBezierInterpolator(0.2f, 0.65f, 0.28f, 0.97f));
        this.mOverflingDistance = ViewConfiguration.get(context).getScaledOverflingDistance();
    }

    public void setOverfling(boolean enabled) {
        this.mOverflingEnabled = enabled;
    }

    public boolean advanceAnimation(long currentTimeMillis) {
        return this.mScroller.computeScrollOffset();
    }

    public boolean isFinished() {
        return this.mScroller.isFinished();
    }

    public void forceFinished() {
        this.mScroller.forceFinished(true);
    }

    public int getPosition() {
        return this.mScroller.getCurrX();
    }

    public float getCurrVelocity() {
        return this.mScroller.getCurrVelocity();
    }

    public void setPosition(int position) {
        this.mScroller.startScroll(position, 0, 0, 0, 0);
        this.mScroller.abortAnimation();
    }

    public void fling(int velocity, int min, int max) {
        int i;
        int currX = getPosition();
        OverScroller overScroller = this.mScroller;
        if (this.mOverflingEnabled) {
            i = this.mOverflingDistance;
        } else {
            i = 0;
        }
        overScroller.fling(currX, 0, velocity, 0, min, max, 0, 0, i, 0);
    }

    public int startScroll(int distance, int min, int max, int overshot) {
        int finalPosition;
        boolean pass = true;
        int currPosition = this.mScroller.getCurrX();
        if (this.mScroller.isFinished()) {
            finalPosition = currPosition;
        } else {
            finalPosition = this.mScroller.getFinalX();
        }
        if (currPosition >= max && max != min) {
            pass = false;
        }
        if (!pass) {
            distance = Utils.getElasticInterpolation(distance, currPosition - max, 250);
        }
        int i = finalPosition + distance;
        if (pass) {
            overshot = 0;
        }
        int newPosition = Utils.clamp(i, min, max + overshot);
        if (newPosition != currPosition) {
            this.mScroller.startScroll(currPosition, 0, newPosition - currPosition, 0, 0);
        }
        return (finalPosition + distance) - newPosition;
    }

    public void release(int max) {
        int currPosition = this.mScroller.getCurrX();
        if (currPosition > max) {
            this.mScroller.springBack(currPosition, 0, 0, max, 0, 0);
        }
    }
}
