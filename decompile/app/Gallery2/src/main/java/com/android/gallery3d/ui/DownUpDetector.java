package com.android.gallery3d.ui;

import android.view.MotionEvent;

public class DownUpDetector {
    private DownUpListener mListener;
    private boolean mStillDown;

    public interface DownUpListener {
        void onDown(MotionEvent motionEvent);

        void onUp(MotionEvent motionEvent);
    }

    public DownUpDetector(DownUpListener listener) {
        this.mListener = listener;
    }

    private void setState(boolean down, MotionEvent e) {
        if (down != this.mStillDown) {
            this.mStillDown = down;
            if (down) {
                this.mListener.onDown(e);
            } else {
                this.mListener.onUp(e);
            }
        }
    }

    public void onTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & 255) {
            case 0:
                setState(true, ev);
                return;
            case 1:
            case 3:
            case 5:
                setState(false, ev);
                return;
            default:
                return;
        }
    }
}
