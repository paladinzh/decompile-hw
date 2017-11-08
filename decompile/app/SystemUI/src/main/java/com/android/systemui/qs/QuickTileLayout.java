package com.android.systemui.qs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class QuickTileLayout extends LinearLayout {
    public QuickTileLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(17);
    }

    public void addView(View child, int index, LayoutParams params) {
        LayoutParams params2 = new LinearLayout.LayoutParams(params.height, params.height);
        ((LinearLayout.LayoutParams) params2).weight = 1.0f;
        super.addView(child, index, params2);
    }
}
