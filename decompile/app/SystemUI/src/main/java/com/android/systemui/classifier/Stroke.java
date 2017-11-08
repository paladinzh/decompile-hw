package com.android.systemui.classifier;

import java.util.ArrayList;

public class Stroke {
    private final float NANOS_TO_SECONDS = 1.0E9f;
    private final float mDpi;
    private long mEndTimeNano;
    private float mLength;
    private ArrayList<Point> mPoints = new ArrayList();
    private long mStartTimeNano;

    public Stroke(long eventTimeNano, float dpi) {
        this.mDpi = dpi;
        this.mEndTimeNano = eventTimeNano;
        this.mStartTimeNano = eventTimeNano;
    }

    public void addPoint(float x, float y, long eventTimeNano) {
        this.mEndTimeNano = eventTimeNano;
        Point point = new Point(x / this.mDpi, y / this.mDpi, eventTimeNano - this.mStartTimeNano);
        if (!this.mPoints.isEmpty()) {
            this.mLength = ((Point) this.mPoints.get(this.mPoints.size() - 1)).dist(point) + this.mLength;
        }
        this.mPoints.add(point);
    }

    public int getCount() {
        return this.mPoints.size();
    }

    public float getTotalLength() {
        return this.mLength;
    }

    public float getEndPointLength() {
        return ((Point) this.mPoints.get(0)).dist((Point) this.mPoints.get(this.mPoints.size() - 1));
    }

    public long getDurationNanos() {
        return this.mEndTimeNano - this.mStartTimeNano;
    }

    public float getDurationSeconds() {
        return ((float) getDurationNanos()) / 1.0E9f;
    }

    public ArrayList<Point> getPoints() {
        return this.mPoints;
    }
}
