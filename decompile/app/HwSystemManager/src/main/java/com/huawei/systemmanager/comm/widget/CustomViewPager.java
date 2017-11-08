package com.huawei.systemmanager.comm.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomViewPager extends ViewPager {
    private boolean mScrollEnable = true;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollEnable(boolean enable) {
        this.mScrollEnable = enable;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return this.mScrollEnable ? super.onInterceptTouchEvent(ev) : false;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        return this.mScrollEnable ? super.onTouchEvent(ev) : false;
    }
}
