package com.android.gallery3d.ui;

import android.content.Context;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import com.android.gallery3d.ui.DownUpDetector.DownUpListener;
import com.android.gallery3d.util.GalleryLog;

public class GestureRecognizer {
    private final DownUpDetector mDownUpDetector = new DownUpDetector(new MyDownUpListener());
    private final GestureDetector mGestureDetector;
    private final Listener mListener;
    private final ScaleGestureDetector mScaleDetector;

    public interface Listener {
        boolean onDoubleTap(float f, float f2);

        void onDown(float f, float f2);

        boolean onFling(float f, float f2);

        void onLongPress(MotionEvent motionEvent);

        boolean onScale(float f, float f2, float f3);

        boolean onScaleBegin(float f, float f2);

        void onScaleEnd();

        boolean onScroll(float f, float f2, float f3, float f4);

        boolean onSingleTapUp(float f, float f2);

        void onUp();
    }

    private class MyDownUpListener implements DownUpListener {
        private MyDownUpListener() {
        }

        public void onDown(MotionEvent e) {
            GestureRecognizer.this.mListener.onDown(e.getX(), e.getY());
        }

        public void onUp(MotionEvent e) {
            GestureRecognizer.this.mListener.onUp();
        }
    }

    private class MyGestureListener extends SimpleOnGestureListener {
        private MyGestureListener() {
        }

        public boolean onSingleTapUp(MotionEvent e) {
            return GestureRecognizer.this.mListener.onSingleTapUp(e.getX(), e.getY());
        }

        public boolean onDoubleTap(MotionEvent e) {
            return GestureRecognizer.this.mListener.onDoubleTap(e.getX(), e.getY());
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
            if (e1 != null) {
                return GestureRecognizer.this.mListener.onScroll(dx, dy, e2.getX() - e1.getX(), e2.getY() - e1.getY());
            }
            GalleryLog.w("GestureRecognizer", "onScroll MotionEvent e2 X=" + e2.getX() + ", Y=" + e2.getY() + ", dx=" + dx + ", dy=" + dy);
            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return GestureRecognizer.this.mListener.onFling(velocityX, velocityY);
        }

        public void onLongPress(MotionEvent e) {
            GestureRecognizer.this.mListener.onLongPress(e);
        }
    }

    private class MyScaleListener extends SimpleOnScaleGestureListener {
        private MyScaleListener() {
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return GestureRecognizer.this.mListener.onScaleBegin(detector.getFocusX(), detector.getFocusY());
        }

        public boolean onScale(ScaleGestureDetector detector) {
            return GestureRecognizer.this.mListener.onScale(detector.getFocusX(), detector.getFocusY(), detector.getScaleFactor());
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            GestureRecognizer.this.mListener.onScaleEnd();
        }
    }

    public GestureRecognizer(Context context, Listener listener) {
        this.mListener = listener;
        this.mGestureDetector = new GestureDetector(context, new MyGestureListener(), null, true);
        this.mScaleDetector = new ScaleGestureDetector(context, new MyScaleListener());
    }

    public void onTouchEvent(MotionEvent event) {
        this.mGestureDetector.onTouchEvent(event);
        this.mScaleDetector.onTouchEvent(event);
        this.mDownUpDetector.onTouchEvent(event);
    }

    public void cancelScale() {
        long now = SystemClock.uptimeMillis();
        MotionEvent cancelEvent = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, 0);
        this.mScaleDetector.onTouchEvent(cancelEvent);
        cancelEvent.recycle();
    }
}
