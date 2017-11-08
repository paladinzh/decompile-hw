package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class ObservableScrollView extends ScrollView {
    private boolean mBlockFlinging;
    private boolean mHandlingTouchEvent;
    private int mLastOverscrollAmount;
    private float mLastX;
    private float mLastY;
    private Listener mListener;
    private boolean mTouchCancelled;
    private boolean mTouchEnabled = true;

    public interface Listener {
        void onOverscrolled(float f, float f2, int i);

        void onScrollChanged();
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private int getMaxScrollY() {
        if (getChildCount() > 0) {
            return Math.max(0, getChildAt(0).getHeight() - ((getHeight() - this.mPaddingBottom) - this.mPaddingTop));
        }
        return 0;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        this.mHandlingTouchEvent = true;
        this.mLastX = ev.getX();
        this.mLastY = ev.getY();
        boolean result = super.onTouchEvent(ev);
        this.mHandlingTouchEvent = false;
        return result;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        this.mHandlingTouchEvent = true;
        this.mLastX = ev.getX();
        this.mLastY = ev.getY();
        boolean result = super.onInterceptTouchEvent(ev);
        this.mHandlingTouchEvent = false;
        return result;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 0) {
            if (this.mTouchEnabled) {
                this.mTouchCancelled = false;
            } else {
                this.mTouchCancelled = true;
                return false;
            }
        } else if (this.mTouchCancelled) {
            return false;
        } else {
            if (!this.mTouchEnabled) {
                MotionEvent cancel = MotionEvent.obtain(ev);
                cancel.setAction(3);
                super.dispatchTouchEvent(cancel);
                cancel.recycle();
                this.mTouchCancelled = true;
                return false;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (this.mListener != null) {
            this.mListener.onScrollChanged();
        }
    }

    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        this.mLastOverscrollAmount = Math.max(0, (scrollY + deltaY) - getMaxScrollY());
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    public void fling(int velocityY) {
        if (!this.mBlockFlinging) {
            super.fling(velocityY);
        }
    }

    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (this.mListener != null && this.mLastOverscrollAmount > 0) {
            this.mListener.onOverscrolled(this.mLastX, this.mLastY, this.mLastOverscrollAmount);
        }
    }
}
