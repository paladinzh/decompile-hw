package com.android.mms.attachment.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import com.google.android.gms.R;

public class AttachMaxFrameLayout extends FrameLayout {
    public AttachMaxFrameLayout(Context context) {
        super(context);
    }

    public AttachMaxFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AttachMaxFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightModel = MeasureSpec.getMode(heightMeasureSpec);
        int maxHeight = getContext().getResources().getDimensionPixelSize(R.dimen.attachment_preview_max_height);
        if (heightSize > maxHeight) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, heightModel);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
