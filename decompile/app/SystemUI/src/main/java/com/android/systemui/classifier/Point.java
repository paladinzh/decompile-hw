package com.android.systemui.classifier;

public class Point {
    public long timeOffsetNano;
    public float x;
    public float y;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
        this.timeOffsetNano = 0;
    }

    public Point(float x, float y, long timeOffsetNano) {
        this.x = x;
        this.y = y;
        this.timeOffsetNano = timeOffsetNano;
    }

    public float dist(Point a) {
        return (float) Math.hypot((double) (a.x - this.x), (double) (a.y - this.y));
    }
}
