package com.huawei.gallery.share;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import com.fyusion.sdk.viewer.internal.request.target.Target;

class HwWrapContentViewPager extends ViewPager {
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxChildHeight = 0;
        int size = getChildCount();
        int height = MeasureSpec.getSize(heightMeasureSpec);
        for (int i = 0; i < size; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, Target.SIZE_ORIGINAL));
                int childHeight = child.getMeasuredHeight();
                if (childHeight > maxChildHeight) {
                    maxChildHeight = childHeight;
                }
            }
        }
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(maxChildHeight, 1073741824));
    }

    public HwWrapContentViewPager(Context context) {
        super(context);
    }

    public HwWrapContentViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
