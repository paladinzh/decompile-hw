package cn.com.xy.sms.sdk.ui.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class DuoquScrollView extends ScrollView {
    private GestureDetector mGestureDetector;

    class YScrollDetector extends SimpleOnGestureListener {
        YScrollDetector() {
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return Math.abs(distanceY) > Math.abs(distanceX);
        }
    }

    public DuoquScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mGestureDetector = new GestureDetector(context, new YScrollDetector());
        fullScroll(33);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (super.onInterceptTouchEvent(ev)) {
            return this.mGestureDetector.onTouchEvent(ev);
        }
        return false;
    }
}
