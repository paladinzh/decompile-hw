package com.android.contacts.hap.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.android.contacts.hap.CommonUtilMethods;

public class HapViewPager extends ViewPager {
    private boolean mDisableViewPagerScroll = false;
    private boolean mDisableViewPagerSlide = false;

    public HapViewPager(Context aContext) {
        super(aContext);
    }

    public HapViewPager(Context aContext, AttributeSet aAttrs) {
        super(aContext, aAttrs);
    }

    public void disableViewPagerScroll(boolean disable) {
        this.mDisableViewPagerScroll = disable;
    }

    public boolean canScrollHorizontally(int direction) {
        if (this.mDisableViewPagerScroll && CommonUtilMethods.calcIfNeedSplitScreen()) {
            return false;
        }
        return super.canScrollHorizontally(direction);
    }

    public boolean onTouchEvent(MotionEvent aEv) {
        try {
            if (isViewPagerSlideDisabled()) {
                return false;
            }
            return super.onTouchEvent(aEv);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean dispatchKeyEvent(KeyEvent aEvent) {
        try {
            return super.dispatchKeyEvent(aEvent);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return false;
        } catch (IllegalStateException ex2) {
            ex2.printStackTrace();
            return false;
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent aEv) {
        try {
            if (isViewPagerSlideDisabled()) {
                return false;
            }
            return super.onInterceptTouchEvent(aEv);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void disableViewPagerSlide(boolean disable) {
        this.mDisableViewPagerSlide = disable;
    }

    public boolean isViewPagerSlideDisabled() {
        return this.mDisableViewPagerSlide;
    }
}
