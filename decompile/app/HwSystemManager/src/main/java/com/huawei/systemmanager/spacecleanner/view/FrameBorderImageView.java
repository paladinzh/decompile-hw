package com.huawei.systemmanager.spacecleanner.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.huawei.systemmanager.util.HwLog;

public class FrameBorderImageView extends ImageView {
    private static final String TAG = "FrameBorderImageView";
    private boolean hasBorder = false;

    public FrameBorderImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setHasBorder(boolean hasBorder) {
        this.hasBorder = hasBorder;
        HwLog.d(TAG, "has border " + this.hasBorder);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        HwLog.d(TAG, "has border");
    }
}
