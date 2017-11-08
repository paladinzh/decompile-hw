package com.android.contacts.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;

public final class FullHeightLinearLayout extends LinearLayout {
    public FullHeightLinearLayout(Context context) {
        super(context);
    }

    public FullHeightLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullHeightLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(heightMeasureSpec) == Integer.MIN_VALUE) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), 1073741824);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
