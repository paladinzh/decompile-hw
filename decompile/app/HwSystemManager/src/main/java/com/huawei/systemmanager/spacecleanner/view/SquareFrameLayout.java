package com.huawei.systemmanager.spacecleanner.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;

public class SquareFrameLayout extends FrameLayout {
    private static int remainWidth;
    private static final int square_count = GlobalContext.getInteger(R.integer.photo_grid_column);
    private static int width;

    static {
        int screenWidth = Utility.getDisplayMetricsWidth();
        int middle_margin = GlobalContext.getDimensionPixelOffset(R.dimen.spacemanager_gallery_middle_margin);
        width = ((screenWidth + 0) + 0) - ((square_count - 1) * middle_margin);
        width /= square_count;
        remainWidth = (screenWidth - (width * square_count)) - ((square_count - 1) * middle_margin);
    }

    public static int getRemainWidth() {
        return remainWidth;
    }

    public static int getSquareWidth() {
        return width;
    }

    public SquareFrameLayout(Context context) {
        super(context);
    }

    public SquareFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (width > 0) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, 1073741824), MeasureSpec.makeMeasureSpec(width, 1073741824));
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
