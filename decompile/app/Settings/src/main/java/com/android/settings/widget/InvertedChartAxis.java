package com.android.settings.widget;

import android.content.res.Resources;
import android.text.SpannableStringBuilder;

public class InvertedChartAxis implements ChartAxis {
    private float mSize;
    private final ChartAxis mWrapped;

    public InvertedChartAxis(ChartAxis wrapped) {
        this.mWrapped = wrapped;
    }

    public boolean setBounds(long min, long max) {
        return this.mWrapped.setBounds(min, max);
    }

    public boolean setSize(float size) {
        this.mSize = size;
        return this.mWrapped.setSize(size);
    }

    public float convertToPoint(long value) {
        return this.mSize - this.mWrapped.convertToPoint(value);
    }

    public long convertToValue(float point) {
        return this.mWrapped.convertToValue(this.mSize - point);
    }

    public long buildLabel(Resources res, SpannableStringBuilder builder, long value) {
        return this.mWrapped.buildLabel(res, builder, value);
    }

    public float[] getTickPoints() {
        float[] points = this.mWrapped.getTickPoints();
        for (int i = 0; i < points.length; i++) {
            points[i] = this.mSize - points[i];
        }
        return points;
    }

    public int shouldAdjustAxis(long value) {
        return this.mWrapped.shouldAdjustAxis(value);
    }
}
