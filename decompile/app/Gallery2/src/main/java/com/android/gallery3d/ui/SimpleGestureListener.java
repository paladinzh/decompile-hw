package com.android.gallery3d.ui;

import android.view.MotionEvent;
import com.android.gallery3d.ui.GestureRecognizer.Listener;

public class SimpleGestureListener implements Listener {
    public boolean onSingleTapUp(float x, float y) {
        return false;
    }

    public boolean onDoubleTap(float x, float y) {
        return false;
    }

    public boolean onScroll(float dx, float dy, float totalX, float totalY) {
        return false;
    }

    public boolean onFling(float velocityX, float velocityY) {
        return false;
    }

    public boolean onScaleBegin(float focusX, float focusY) {
        return false;
    }

    public boolean onScale(float focusX, float focusY, float scale) {
        return false;
    }

    public void onScaleEnd() {
    }

    public void onLongPress(MotionEvent e) {
    }

    public void onDown(float x, float y) {
    }

    public void onUp() {
    }
}
