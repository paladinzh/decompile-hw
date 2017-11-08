package com.huawei.systemmanager.antivirus.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.ListView;

public class AntiVirusScrollListView extends ListView {
    public AntiVirusScrollListView(Context context) {
        super(context);
    }

    public AntiVirusScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AntiVirusScrollListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(536870911, Integer.MIN_VALUE));
    }
}
