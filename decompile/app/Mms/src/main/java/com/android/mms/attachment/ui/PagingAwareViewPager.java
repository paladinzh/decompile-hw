package com.android.mms.attachment.ui;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.android.mms.ui.MessageUtils;

public class PagingAwareViewPager extends ViewPager {
    private boolean mPagingEnabled = true;

    public PagingAwareViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(getRtlPosition(item), smoothScroll);
    }

    public void setCurrentItem(int item) {
        super.setCurrentItem(getRtlPosition(item));
    }

    public int getCurrentItem() {
        return getRtlPosition(super.getCurrentItem());
    }

    protected int getRtlPosition(int position) {
        PagerAdapter adapter = getAdapter();
        if (adapter == null || !MessageUtils.isNeedLayoutRtl()) {
            return position;
        }
        return (adapter.getCount() - 1) - position;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mPagingEnabled) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.mPagingEnabled) {
            return super.onInterceptTouchEvent(event);
        }
        return false;
    }

    public void setPagingEnabled(boolean enabled) {
        this.mPagingEnabled = enabled;
    }

    public boolean canScrollHorizontally(int direction) {
        if (this.mPagingEnabled) {
            return super.canScrollHorizontally(direction);
        }
        return false;
    }
}
