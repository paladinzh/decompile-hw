package com.android.contacts.dialpad;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.ViewSwitcher;

public class DialpadViewSwitcher extends ViewSwitcher {
    public DialpadViewSwitcher(Context context) {
        super(context);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
}
