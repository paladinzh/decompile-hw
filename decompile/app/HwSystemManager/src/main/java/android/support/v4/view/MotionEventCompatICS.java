package android.support.v4.view;

import android.view.MotionEvent;

class MotionEventCompatICS {
    MotionEventCompatICS() {
    }

    public static int getButtonState(MotionEvent event) {
        return event.getButtonState();
    }
}
