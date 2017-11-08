package com.android.mms.attachment.ui.mediapicker.camerafocus;

import android.graphics.Canvas;
import android.view.MotionEvent;

public abstract class OverlayRenderer implements Renderer {
    protected int mBottom;
    protected int mLeft;
    protected RenderOverlay mOverlay;
    protected int mRight;
    protected int mTop;
    protected boolean mVisible;

    public abstract void onDraw(Canvas canvas);

    public void setVisible(boolean vis) {
        this.mVisible = vis;
        update();
    }

    public boolean isVisible() {
        return this.mVisible;
    }

    public boolean handlesTouch() {
        return false;
    }

    public boolean onTouchEvent(MotionEvent evt) {
        return false;
    }

    public void draw(Canvas canvas) {
        if (this.mVisible) {
            onDraw(canvas);
        }
    }

    public void setOverlay(RenderOverlay overlay) {
        this.mOverlay = overlay;
    }

    public void layout(int left, int top, int right, int bottom) {
        this.mLeft = left;
        this.mRight = right;
        this.mTop = top;
        this.mBottom = bottom;
    }

    public int getWidth() {
        return this.mRight - this.mLeft;
    }

    public int getHeight() {
        return this.mBottom - this.mTop;
    }

    protected void update() {
        if (this.mOverlay != null) {
            this.mOverlay.update();
        }
    }
}
