package com.huawei.netassistant.ui.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NoScrollViewPager extends ViewPager {
    private boolean isCanScroll = false;

    public NoScrollViewPager(Context context) {
        super(context);
    }

    public NoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScanScroll(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }

    public boolean onTouchEvent(MotionEvent arg0) {
        if (this.isCanScroll) {
            return super.onTouchEvent(arg0);
        }
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (this.isCanScroll) {
            return super.onInterceptTouchEvent(arg0);
        }
        return false;
    }
}
