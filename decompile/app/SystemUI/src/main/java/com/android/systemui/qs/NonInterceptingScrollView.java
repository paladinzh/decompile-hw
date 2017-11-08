package com.android.systemui.qs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class NonInterceptingScrollView extends ScrollView {
    public NonInterceptingScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case 0:
                if (canScrollVertically(1)) {
                    requestDisallowInterceptTouchEvent(true);
                    break;
                }
                break;
        }
        return super.onTouchEvent(ev);
    }
}
