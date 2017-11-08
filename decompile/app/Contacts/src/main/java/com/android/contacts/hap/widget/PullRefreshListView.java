package com.android.contacts.hap.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.android.contacts.widget.AutoScrollListView;

public class PullRefreshListView extends AutoScrollListView {
    public PullRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }
}
