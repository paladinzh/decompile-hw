package com.android.deskclock.alarmclock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class BollView extends CallPanelView {
    public BollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean handleTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        }
        return super.onTouchEvent(event);
    }
}
