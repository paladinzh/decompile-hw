package com.huawei.systemmanager.spacecleanner.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.RelativeLayout;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;

public class AdjustRelativeLayout extends RelativeLayout {
    private final int mAjudstHeigh;

    public AdjustRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdjustRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mAjudstHeigh = getContext().getResources().getDimensionPixelSize(R.dimen.space_cleaner_upperview_height) - getContext().getResources().getDimensionPixelSize(R.dimen.space_cleaner_panel_expaned_margin_top);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (Utility.isSupportOrientation()) {
            if (!(getContext().getResources().getConfiguration().orientation == 2)) {
                heightSize -= this.mAjudstHeigh;
            }
        } else {
            heightSize -= this.mAjudstHeigh;
        }
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(heightSize, heightMode));
    }
}
