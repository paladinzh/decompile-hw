package com.huawei.gallery.editor.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import com.huawei.gallery.editor.filters.FilterIllusionRepresentation;
import com.huawei.gallery.editor.ui.IllusionView.Listener;

public abstract class ShapeControl {
    protected float mDown1X;
    protected float mDown1Y;
    protected float mDown2X;
    protected float mDown2Y;
    protected float mDownX;
    protected float mDownY;
    protected Rect mImageBounds;
    protected Listener mListener;
    protected boolean mNeedCover = false;
    protected Paint mPaint = new Paint();
    protected int mTouchStyle = 0;

    public interface Line {
        float getPoint1X();

        float getPoint1Y();

        float getPoint2X();

        float getPoint2Y();
    }

    public interface Circle {
        float getPointX();

        float getPointY();

        float getRadius();
    }

    public abstract void actionDown(float f, float f2);

    public abstract void actionDown(float f, float f2, float f3, float f4);

    public abstract void actionMove(float f, float f2);

    public abstract void actionMove(float f, float f2, float f3, float f4);

    public abstract void actionUp(FilterIllusionRepresentation filterIllusionRepresentation, boolean z);

    public abstract void draw(Canvas canvas);

    public abstract void setScrImageInfo(Rect rect, Listener listener, FilterIllusionRepresentation filterIllusionRepresentation);

    protected boolean centerIsOutside(float x, float y) {
        boolean z = true;
        if (this.mImageBounds == null) {
            return true;
        }
        if (this.mImageBounds.contains((int) x, (int) y)) {
            z = false;
        }
        return z;
    }

    protected void initPoints(FilterIllusionRepresentation representation) {
    }

    protected void updatePoints(FilterIllusionRepresentation representation, float scale) {
    }

    public void actionUp(FilterIllusionRepresentation representation, float x, float y) {
    }

    public float getLength(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt((double) ((dx * dx) + (dy * dy)));
    }
}
