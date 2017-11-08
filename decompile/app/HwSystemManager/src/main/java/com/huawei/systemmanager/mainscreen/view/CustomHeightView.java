package com.huawei.systemmanager.mainscreen.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import com.huawei.systemmanager.comm.misc.Utility;

public class CustomHeightView extends View {
    private final int mScreenWith;

    public CustomHeightView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomHeightView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mScreenWith = Utility.getScreenSmallWidth(context);
    }

    public LayoutParams getLayoutParams() {
        LayoutParams params = super.getLayoutParams();
        if (params instanceof RelativeLayout.LayoutParams) {
            ((RelativeLayout.LayoutParams) params).topMargin = this.mScreenWith;
        }
        return params;
    }
}
