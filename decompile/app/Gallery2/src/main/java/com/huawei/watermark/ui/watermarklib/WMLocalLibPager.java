package com.huawei.watermark.ui.watermarklib;

import android.content.Context;
import android.util.AttributeSet;
import com.huawei.watermark.ui.baseview.viewpager.DirectionalViewPager;

public abstract class WMLocalLibPager extends DirectionalViewPager {
    private static final String TAG = ("CAMERA3WATERMARK_" + WMLocalLibPager.class.getSimpleName());

    public abstract void onOrientationChanged(int i);

    public abstract void setLayoutParams();

    public WMLocalLibPager(Context context) {
        super(context);
        setScrollerDuration(300);
        setLayoutParams();
    }

    public WMLocalLibPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScrollerDuration(300);
    }
}
