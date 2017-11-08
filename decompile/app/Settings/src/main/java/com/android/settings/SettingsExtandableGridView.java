package com.android.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.GridView;

public class SettingsExtandableGridView extends GridView {
    public SettingsExtandableGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingsExtandableGridView(Context context) {
        super(context);
    }

    public SettingsExtandableGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(536870911, Integer.MIN_VALUE));
    }
}
