package com.huawei.keyguard.view.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.huawei.keyguard.util.HwLog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MagazineSwitchViewPager extends ViewPager {
    private boolean isCanScroll = true;
    private Method mMethodSetCurrentItemInternal;

    public MagazineSwitchViewPager(Context context) {
        super(context);
    }

    public MagazineSwitchViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!this.isCanScroll) {
            return true;
        }
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!this.isCanScroll) {
            return true;
        }
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void setScanScroll(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }

    public void scrollTo(int x, int y) {
        if (this.isCanScroll) {
            super.scrollTo(x, y);
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw) {
            callSetCurrentItemInternal(getCurrentItem(), false, true);
        } else {
            super.onSizeChanged(w, h, oldw, oldh);
        }
    }

    public void callSetCurrentItemInternal(int position, boolean smoothScroll, boolean always) {
        try {
            if (this.mMethodSetCurrentItemInternal == null) {
                this.mMethodSetCurrentItemInternal = ViewPager.class.getDeclaredMethod("setCurrentItemInternal", new Class[]{Integer.TYPE, Boolean.TYPE, Boolean.TYPE});
                this.mMethodSetCurrentItemInternal.setAccessible(true);
            }
            if (this.mMethodSetCurrentItemInternal != null) {
                HwLog.d("MagazineSwitchViewPager", "invoke callSetCurrentItemInternal");
                this.mMethodSetCurrentItemInternal.invoke(this, new Object[]{Integer.valueOf(position), Boolean.valueOf(smoothScroll), Boolean.valueOf(always)});
            }
        } catch (NoSuchMethodException e) {
            HwLog.w("MagazineSwitchViewPager", "callSetCurrentItemInternal NoSuchMethodException", e);
        } catch (IllegalAccessException e2) {
            HwLog.w("MagazineSwitchViewPager", "callSetCurrentItemInternal IllegalAccessException", e2);
        } catch (InvocationTargetException e3) {
            HwLog.w("MagazineSwitchViewPager", "callSetCurrentItemInternal InvocationTargetException", e3);
        } catch (Exception e4) {
            HwLog.w("MagazineSwitchViewPager", "callSetCurrentItemInternal Exception", e4);
        }
    }
}
