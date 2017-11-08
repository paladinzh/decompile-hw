package com.android.mms.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import com.huawei.mms.ui.EmuiListView_V3;

public class SimMessageListView extends EmuiListView_V3 {
    public SimMessageListView(Context context) {
        super(context);
    }

    public SimMessageListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        if (mode == Integer.MIN_VALUE) {
            mode = 1073741824;
        }
        super.onMeasure(widthMeasureSpec, MeasureSpec.getSize(heightMeasureSpec) | mode);
    }

    public boolean isInvalideItemId(long itemId) {
        return -1 == itemId;
    }
}
