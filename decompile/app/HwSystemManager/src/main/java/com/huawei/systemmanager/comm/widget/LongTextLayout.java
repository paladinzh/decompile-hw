package com.huawei.systemmanager.comm.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class LongTextLayout extends RelativeLayout {
    public LongTextLayout(Context context) {
        super(context);
    }

    public LongTextLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildrenWidth(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void measureChildrenWidth(int widthMeasureSpec, int heightMeasureSpec) {
        if (getChildCount() == 2) {
            View leftView = getChildAt(0);
            View rightView = getChildAt(1);
            int width = MeasureSpec.getSize(widthMeasureSpec);
            leftView.measure(MeasureSpec.makeMeasureSpec(width, Integer.MIN_VALUE), heightMeasureSpec);
            rightView.measure(MeasureSpec.makeMeasureSpec(width, Integer.MIN_VALUE), heightMeasureSpec);
            int leftWidth = leftView.getMeasuredWidth();
            int rightWidth = rightView.getMeasuredWidth();
            LayoutParams leftLayoutParams = (LayoutParams) leftView.getLayoutParams();
            LayoutParams rightLayoutParams = (LayoutParams) rightView.getLayoutParams();
            int paddingStart = getPaddingStart();
            int paddingEnd = getPaddingEnd();
            int leftMargin = leftLayoutParams.getMarginStart() + leftLayoutParams.getMarginEnd();
            int rightMargin = rightLayoutParams.getMarginStart() + rightLayoutParams.getMarginEnd();
            if (((((paddingStart + leftWidth) + leftMargin) + rightWidth) + rightMargin) + paddingEnd > width) {
                rightLayoutParams.width = ((width / 3) - paddingEnd) - rightMargin;
                rightView.setLayoutParams(rightLayoutParams);
                leftLayoutParams.width = (((width * 2) / 3) - paddingStart) - leftMargin;
                leftView.setLayoutParams(leftLayoutParams);
            } else {
                rightLayoutParams.width = -2;
                rightView.setLayoutParams(rightLayoutParams);
                leftLayoutParams.width = -2;
                leftView.setLayoutParams(leftLayoutParams);
            }
        }
    }
}
