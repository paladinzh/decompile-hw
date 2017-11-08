package com.huawei.gallery.burst.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class BurstImageThumbGallery extends MyGallery {
    private MyGestureListener mListener;

    public interface MyGestureListener {
        boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2);

        boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2);
    }

    public BurstImageThumbGallery(Context context) {
        super(context);
    }

    public BurstImageThumbGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnFlingListener(MyGestureListener listener) {
        this.mListener = listener;
    }

    public boolean superFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return super.onFling(e1, e2, velocityX, velocityY);
    }

    public boolean superScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return this.mListener.onFling(e1, e2, velocityX, velocityY);
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return this.mListener.onScroll(e1, e2, distanceX, distanceY);
    }

    protected void onFinishedMovement() {
        super.onFinishedMovement();
        int dX = getSelectedViewOffset();
        if (dX != 0) {
            trackMotionScroll(dX);
        }
    }
}
