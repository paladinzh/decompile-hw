package com.android.mms.attachment.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import com.google.android.gms.R;

public class CameraMaxFrameLayout extends FrameLayout {
    public CameraMaxFrameLayout(Context context) {
        super(context);
    }

    public CameraMaxFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraMaxFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightModel = MeasureSpec.getMode(heightMeasureSpec);
        if (getContext().getResources().getConfiguration().orientation == 1) {
            int maxPreview = ((getContext().getResources().getDisplayMetrics().widthPixels / 3) * 4) + (getContext().getResources().getDimensionPixelSize(R.dimen.camera_preview_layout_max_height_margin) * 2);
            if (heightSize > maxPreview) {
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxPreview, heightModel);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
