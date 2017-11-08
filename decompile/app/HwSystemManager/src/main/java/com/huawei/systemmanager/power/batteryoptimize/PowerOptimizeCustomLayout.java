package com.huawei.systemmanager.power.batteryoptimize;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.RelativeLayout;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.util.HSMConst;

public class PowerOptimizeCustomLayout extends RelativeLayout {
    private Context mContext;
    private int mHeightOffset;
    private int mHeightWeight;
    private int mWidthWeight;

    public PowerOptimizeCustomLayout(Context context) {
        this(context, null);
    }

    public PowerOptimizeCustomLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PowerOptimizeCustomLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mWidthWeight = 1;
        this.mHeightWeight = 1;
        this.mHeightOffset = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomLayoutAttrs);
        this.mWidthWeight = a.getInteger(0, 1);
        this.mHeightWeight = a.getInteger(1, 1);
        this.mHeightOffset = a.getDimensionPixelSize(2, 0);
        this.mContext = context;
        a.recycle();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int adjustMeaureHeight;
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (HSMConst.isLand()) {
            adjustMeaureHeight = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.getMode(widthMeasureSpec));
        } else {
            int mHeight = ((this.mHeightWeight * SysCoreUtils.getScreenWidth(this.mContext)) / this.mWidthWeight) - this.mHeightOffset;
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            if (heightMode == 0) {
                heightMode = 1073741824;
            }
            adjustMeaureHeight = MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.getMode(heightMode));
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.getMode(widthMeasureSpec)), adjustMeaureHeight);
    }
}
