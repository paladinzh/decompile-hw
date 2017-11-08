package com.android.gallery3d.anim;

public class FloatAnimation extends Animation {
    private float mCurrent;
    private final float mFrom;
    private final float mTo;

    public FloatAnimation(float from, float to, int duration) {
        this.mFrom = from;
        this.mTo = to;
        this.mCurrent = from;
        setDuration(duration);
    }

    protected void onCalculate(float progress) {
        this.mCurrent = this.mFrom + ((this.mTo - this.mFrom) * progress);
    }

    public float get() {
        return this.mCurrent;
    }

    public void reset() {
        this.mCurrent = this.mFrom;
    }
}
