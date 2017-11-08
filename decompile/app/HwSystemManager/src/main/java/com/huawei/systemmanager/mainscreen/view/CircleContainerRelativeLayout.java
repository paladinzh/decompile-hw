package com.huawei.systemmanager.mainscreen.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import com.huawei.systemmanager.comm.misc.Utility;

public class CircleContainerRelativeLayout extends RelativeLayout {
    private final int mScreenWith;

    public CircleContainerRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleContainerRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mScreenWith = Utility.getScreenSmallWidth(context);
    }

    public LayoutParams getLayoutParams() {
        LayoutParams params = super.getLayoutParams();
        if (params instanceof RelativeLayout.LayoutParams) {
            ((RelativeLayout.LayoutParams) params).topMargin = (this.mScreenWith - params.height) / 2;
        }
        return params;
    }
}
