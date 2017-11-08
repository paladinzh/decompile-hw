package com.huawei.mms.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import com.android.mms.ui.MessageUtils;

public class HwComposeBottomEditView extends LinearLayout {
    private int mActionBarShowHeight = -1;
    private int mAttchmentDraftViewHeight = -1;
    private int mBottomGroupHeight = -1;
    private int mComposeLayoutMarginTop = -1;
    private ScrollableCallback mScrollableCallback;

    public interface ScrollableCallback {
        boolean isScrollable();
    }

    public HwComposeBottomEditView(Context context) {
        super(context);
    }

    public HwComposeBottomEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwComposeBottomEditView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        super.measureChild(child, parentWidthMeasureSpec, parentHeightMeasureSpec);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (-1 == this.mActionBarShowHeight || -1 == this.mComposeLayoutMarginTop) {
            super.onLayout(changed, l, t, r, b);
        } else if (t < this.mActionBarShowHeight + this.mComposeLayoutMarginTop) {
            super.onLayout(changed, l, this.mActionBarShowHeight + this.mComposeLayoutMarginTop, r, b);
        } else {
            super.onLayout(changed, l, t, r, b);
        }
    }

    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        super.measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

    public void setActionBarHeight(int height) {
        this.mActionBarShowHeight = height;
    }

    public void setBottomGroupHeight(int height) {
        this.mBottomGroupHeight = height;
    }

    public void setmAttchmentDraftViewHeight(int height) {
        this.mAttchmentDraftViewHeight = height;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (-1 == this.mBottomGroupHeight || -1 == this.mActionBarShowHeight || -1 == this.mComposeLayoutMarginTop || -1 == this.mAttchmentDraftViewHeight || MessageUtils.getIsMediaPanelInScrollingStatus() || isScrollableMode()) {
            setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int limitHeight = ((this.mBottomGroupHeight - this.mActionBarShowHeight) - this.mComposeLayoutMarginTop) - this.mAttchmentDraftViewHeight;
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);
        int actualHeight;
        if (Integer.MIN_VALUE == specMode) {
            actualHeight = specSize > limitHeight ? limitHeight : specSize;
            setMeasuredDimension(getMeasuredWidth(), actualHeight);
            super.onMeasure(widthMeasureSpec, actualHeight - Integer.MIN_VALUE);
        } else if (1073741824 == specMode) {
            actualHeight = specSize > limitHeight ? limitHeight : specSize;
            setMeasuredDimension(getMeasuredWidth(), actualHeight);
            super.onMeasure(widthMeasureSpec, actualHeight + 1073741824);
        } else {
            setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setComposeLayoutMarginTop(int top) {
        this.mComposeLayoutMarginTop = top;
    }

    public void setScrollableCallback(ScrollableCallback scrollCallback) {
        this.mScrollableCallback = scrollCallback;
    }

    private boolean isScrollableMode() {
        if (this.mScrollableCallback == null) {
            return false;
        }
        return this.mScrollableCallback.isScrollable();
    }
}
