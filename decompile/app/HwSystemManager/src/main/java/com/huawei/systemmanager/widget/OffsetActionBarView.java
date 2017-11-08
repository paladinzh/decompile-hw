package com.huawei.systemmanager.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import com.huawei.systemmanager.util.HSMConst;

public class OffsetActionBarView extends View {
    public OffsetActionBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public OffsetActionBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OffsetActionBarView(Context context) {
        super(context);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = 0;
        if (HSMConst.isLand()) {
            height = HSMConst.getActionBarHeight();
        }
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, 1073741824));
    }
}
