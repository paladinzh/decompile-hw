package com.huawei.netassistant.ui.view;

import android.graphics.RectF;

public class TrafficLineChartData implements ChartData {
    private RectF mClickArea;
    private String mDate;
    private int mId;
    private int mPointRadius;
    private String mSize;
    private float mX;
    private float mY;

    public TrafficLineChartData(int id, float x, float y, String size, int radius, RectF rect, String date) {
        this.mId = id;
        this.mX = x;
        this.mY = y;
        this.mSize = size;
        this.mPointRadius = radius;
        this.mClickArea = rect;
        this.mDate = date;
    }

    public int getId() {
        return this.mId;
    }

    public float getX() {
        return this.mX;
    }

    public void updateX(float x) {
        this.mX = x;
    }

    public void updateY(float y) {
        this.mY = y;
    }

    public float getY() {
        return this.mY;
    }

    public String getText() {
        return this.mSize;
    }

    public int getPointRadius() {
        return this.mPointRadius;
    }

    public RectF getClickArea() {
        return this.mClickArea;
    }

    public void updateClickArea(RectF rectf) {
        this.mClickArea = rectf;
    }

    public void setPointRadius(int radius) {
        this.mPointRadius = radius;
    }

    public String getDate() {
        return this.mDate;
    }
}
