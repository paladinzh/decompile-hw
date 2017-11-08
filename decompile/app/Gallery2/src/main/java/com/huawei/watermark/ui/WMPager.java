package com.huawei.watermark.ui;

import android.content.Context;
import android.view.MotionEvent;
import com.huawei.watermark.decoratorclass.WMLog;
import com.huawei.watermark.ui.baseview.viewpager.DirectionalViewPager;
import com.huawei.watermark.ui.baseview.viewpager.WMBasePagerAdapter;

public class WMPager extends DirectionalViewPager {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMPager.class.getSimpleName());
    private boolean mBeReverse = false;
    private boolean mIsDisableScroll;
    private boolean mUseTouchEventDelegate = false;

    public WMPager(Context context) {
        super(context);
    }

    public void onOrientationChanged(int ori) {
        if (ori == 0 || ori == 180) {
            setOrientation(0);
        } else {
            setOrientation(1);
        }
        boolean tempReverse = this.mBeReverse;
        if (ori == 0 || ori == 270) {
            this.mBeReverse = false;
        } else {
            this.mBeReverse = true;
        }
        if (tempReverse != this.mBeReverse) {
            refresh();
        }
    }

    public void refresh() {
        if (getAdapter() != null) {
            ((WMPagerAdapter) getAdapter()).setBeReverse(this.mBeReverse);
            setCurrentItemWhenBeReverseChanged(getCurrentItem());
        }
    }

    public void setAdapter(WMBasePagerAdapter adapter) {
        if (adapter != null && (adapter instanceof WMPagerAdapter)) {
            ((WMPagerAdapter) adapter).setBeReverse(this.mBeReverse);
            super.setAdapter(adapter);
        }
    }

    public void setCurrentItemWhenBeReverseChanged(int item) {
        super.setCurrentItem(consItemIfNeedReverse(item, true), false);
    }

    public void setCurrentItem(int item) {
        super.setCurrentItem(consItemIfNeedReverse(item, this.mBeReverse), false);
    }

    public int getCurrentItemIfNeedReverse() {
        return consItemIfNeedReverse(super.getCurrentItem(), this.mBeReverse);
    }

    public int consItemIfNeedReverse(int item, boolean reverse) {
        if (getAdapter() == null || getAdapter().getCount() == 0) {
            return item;
        }
        if (reverse) {
            item = (getAdapter().getCount() - 1) - item;
        }
        if (item < 0 || item >= getAdapter().getCount()) {
            item = 0;
        }
        return item;
    }

    public void setUseTouchEventDelegateStatus(boolean use) {
        this.mUseTouchEventDelegate = use;
    }

    public boolean superDispatchTouchEvent(MotionEvent ev) {
        if (!this.mUseTouchEventDelegate || needIgnoreGesture(ev)) {
            MotionEvent cancelEvent = MotionEvent.obtain(ev);
            cancelEvent.setAction(3);
            super.dispatchTouchEvent(cancelEvent);
            cancelEvent.recycle();
            return false;
        }
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            WMLog.e(TAG, "superDispatchTouchEvent got a exception", e);
            return false;
        }
    }

    private boolean needIgnoreGesture(MotionEvent ev) {
        if (ev.getPointerCount() > 1) {
            return true;
        }
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        return true;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.mUseTouchEventDelegate) {
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }

    public void enableScroll() {
        this.mIsDisableScroll = false;
    }

    public void disableScroll() {
        this.mIsDisableScroll = true;
    }
}
