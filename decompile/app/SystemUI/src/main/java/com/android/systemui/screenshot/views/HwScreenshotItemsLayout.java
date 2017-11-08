package com.android.systemui.screenshot.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class HwScreenshotItemsLayout extends LinearLayout {
    public HwScreenshotItemsLayout(Context context) {
        this(context, null);
    }

    public HwScreenshotItemsLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwScreenshotItemsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwScreenshotItemsLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredHeight = 0;
        int N = getChildCount();
        int i = 0;
        while (i < N) {
            if (getChildAt(i) != null && measuredHeight < getChildAt(i).getMeasuredHeight()) {
                measuredHeight = getChildAt(i).getMeasuredHeight();
            }
            i++;
        }
        super.onMeasure(widthMeasureSpec, measuredHeight + (getPaddingTop() + getPaddingBottom()));
    }
}
