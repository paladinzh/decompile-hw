package com.huawei.systemmanager.power.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.GridView;

public class ScrollViewGridView extends GridView {
    public ScrollViewGridView(Context context) {
        super(context);
    }

    public ScrollViewGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollViewGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(536870911, Integer.MIN_VALUE));
    }
}
