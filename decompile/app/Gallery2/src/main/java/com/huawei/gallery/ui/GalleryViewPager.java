package com.huawei.gallery.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.android.gallery3d.util.GalleryLog;

public class GalleryViewPager extends ViewPager {
    private ViewPageCallback mCallback;
    private int mCurrentItem;
    private boolean mInterceptTouchEvent = false;
    private int mRepeatCount = 0;

    public interface ViewPageCallback {
        boolean disableScroll();
    }

    public GalleryViewPager(Context context) {
        super(context);
    }

    public GalleryViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCallback(ViewPageCallback callback) {
        this.mCallback = callback;
    }

    private boolean canScroll() {
        return this.mCallback != null ? this.mCallback.disableScroll() : false;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean z = true;
        switch (ev.getAction() & 255) {
            case 0:
                this.mInterceptTouchEvent = canScroll();
                break;
            case 2:
                if (!(this.mInterceptTouchEvent || canScroll() || ev.getPointerCount() > 1)) {
                    z = false;
                }
                this.mInterceptTouchEvent = z;
                break;
        }
        if (this.mInterceptTouchEvent) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & 255) {
            case 0:
                this.mInterceptTouchEvent = canScroll();
                break;
            case 2:
                this.mInterceptTouchEvent = !this.mInterceptTouchEvent ? canScroll() : true;
                break;
        }
        if (this.mInterceptTouchEvent) {
            return true;
        }
        return super.onTouchEvent(ev);
    }

    public boolean executeKeyEvent(KeyEvent event) {
        if (canScroll()) {
            return false;
        }
        return super.executeKeyEvent(event);
    }

    public void setCurrentItem(int item) {
        if (this.mCurrentItem == item) {
            this.mRepeatCount++;
            if (this.mRepeatCount >= 5) {
                GalleryLog.e("GalleryViewPager", "current item doesn't changed. repeatCount: " + this.mRepeatCount);
            } else {
                GalleryLog.d("GalleryViewPager", "current item doesn't changed. repeatCount: " + this.mRepeatCount);
            }
            if (this.mRepeatCount >= 1) {
                return;
            }
        }
        this.mRepeatCount = 0;
        this.mCurrentItem = item;
        super.setCurrentItem(item);
    }
}
