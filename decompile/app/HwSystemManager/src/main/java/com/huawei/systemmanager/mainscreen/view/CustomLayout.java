package com.huawei.systemmanager.mainscreen.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.RelativeLayout;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HSMConst;

public class CustomLayout extends RelativeLayout {
    private boolean mConsiderMax;
    private int mHeightOffset;
    private int mHeightWeight;
    private int mWidthWeight;

    public CustomLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mWidthWeight = 1;
        this.mHeightWeight = 1;
        this.mHeightOffset = 0;
        this.mConsiderMax = false;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomLayoutAttrs);
        this.mWidthWeight = a.getInteger(0, 1);
        this.mHeightWeight = a.getInteger(1, 1);
        this.mHeightOffset = a.getDimensionPixelSize(2, 0);
        this.mConsiderMax = a.getBoolean(3, false);
        a.recycle();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (HSMConst.isLand()) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getMode(widthMeasureSpec)), MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.getMode(heightMeasureSpec)));
            return;
        }
        int widthSize = HSMConst.getLongOrShortLength(this.mConsiderMax);
        super.onMeasure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.getMode(widthMeasureSpec)), MeasureSpec.makeMeasureSpec(((this.mHeightWeight * widthSize) / this.mWidthWeight) - this.mHeightOffset, MeasureSpec.getMode(heightMeasureSpec)));
    }
}
