package com.huawei.netassistant.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.huawei.systemmanager.R;

public class MyTextView extends TextView {
    private static final int TYPE_FIRST = 1;
    private static final int TYPE_LAST = 3;
    private static final int TYPE_MIDDLE = 2;
    private int indatePointType;
    private int indatePointX;
    private int indatePointY;
    private int xWidth;

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LayoutParams getLayoutParams() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) super.getLayoutParams();
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (this.indatePointType == 1) {
            params.leftMargin = this.indatePointX;
            params.rightMargin = (this.xWidth - this.indatePointX) - width;
        } else if (this.indatePointType == 2) {
            params.leftMargin = this.indatePointX - (width / 2);
            params.rightMargin = (this.xWidth - this.indatePointX) - (width / 2);
        } else if (this.indatePointType == 3) {
            params.leftMargin = this.indatePointX - width;
            params.rightMargin = this.xWidth - this.indatePointX;
        }
        params.topMargin = this.indatePointY - height;
        return params;
    }

    public void setXY(int w, int x, int y, int type) {
        this.xWidth = w;
        this.indatePointX = x;
        this.indatePointY = y;
        this.indatePointY += getResources().getDimensionPixelSize(R.dimen.net_text_near_to_point);
        this.indatePointType = type;
        setVisibility(0);
        requestLayout();
    }

    public void udpateXWidth(int w, int x) {
        this.xWidth = w;
        this.indatePointX = x;
        requestLayout();
    }
}
