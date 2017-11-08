package com.android.settings.colortemper;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class SelectColorCircleLayout extends LinearLayout {
    public SelectColorCircleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectColorCircleLayout(Context context) {
        super(context);
    }

    public boolean onInterceptTouchEvent(MotionEvent p_event) {
        if (p_event.getAction() == 0 && getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.onInterceptTouchEvent(p_event);
    }
}
