package com.android.systemui.recents.views;

/* compiled from: TaskStackLayoutAlgorithm */
class Range {
    float max;
    float min;
    float origin;
    final float relativeMax;
    final float relativeMin;

    public Range(float relMin, float relMax) {
        this.relativeMin = relMin;
        this.min = relMin;
        this.relativeMax = relMax;
        this.max = relMax;
    }

    public void offset(float x) {
        this.origin = x;
        this.min = this.relativeMin + x;
        this.max = this.relativeMax + x;
    }

    public float getNormalizedX(float x) {
        if (x < this.origin) {
            return (((x - this.origin) * 0.5f) / (-this.relativeMin)) + 0.5f;
        }
        return (((x - this.origin) * 0.5f) / this.relativeMax) + 0.5f;
    }

    public float getAbsoluteX(float normX) {
        if (normX < 0.5f) {
            return ((normX - 0.5f) / 0.5f) * (-this.relativeMin);
        }
        return ((normX - 0.5f) / 0.5f) * this.relativeMax;
    }

    public boolean isInRange(float absX) {
        return ((double) absX) >= Math.floor((double) this.min) && ((double) absX) <= Math.ceil((double) this.max);
    }
}
