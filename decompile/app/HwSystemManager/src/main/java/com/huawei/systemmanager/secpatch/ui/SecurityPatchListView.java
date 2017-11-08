package com.huawei.systemmanager.secpatch.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.ListView;

public class SecurityPatchListView extends ListView {
    public SecurityPatchListView(Context context) {
        super(context);
    }

    public SecurityPatchListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SecurityPatchListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(536870911, Integer.MIN_VALUE));
    }
}
