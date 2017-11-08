package com.android.internal.policy;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemProperties;
import android.view.View;
import huawei.com.android.internal.widget.HwFragmentMenuItemView;

public class HwNavigationBarColorView extends View {
    private static final int ROTATION = (SystemProperties.getInt("ro.panel.hw_orientation", 0) / 90);
    private static final String TAG = "HwNavigationBarColorView";
    private static final int mLineHeightPx = 2;
    private int mLineColor;
    private int mOrientation;
    private Paint mPaint;

    public HwNavigationBarColorView(Context context) {
        super(context);
        initDrawLinePaint(context);
        initOrientation(context);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mOrientation == 1) {
            canvas.drawLine(0.0f, HwFragmentMenuItemView.ALPHA_NORMAL, (float) getWidth(), HwFragmentMenuItemView.ALPHA_NORMAL, this.mPaint);
        } else if (this.mOrientation != 2) {
        } else {
            if (isNavBarAtBottom()) {
                canvas.drawLine(0.0f, HwFragmentMenuItemView.ALPHA_NORMAL, (float) getWidth(), HwFragmentMenuItemView.ALPHA_NORMAL, this.mPaint);
                return;
            }
            canvas.drawLine(HwFragmentMenuItemView.ALPHA_NORMAL, 0.0f, HwFragmentMenuItemView.ALPHA_NORMAL, (float) getHeight(), this.mPaint);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        this.mOrientation = newConfig.orientation;
    }

    private void initDrawLinePaint(Context context) {
        this.mLineColor = context.getResources().getColor(33882234);
        this.mPaint = new Paint();
        this.mPaint.setStrokeWidth(2.0f);
        this.mPaint.setColor(this.mLineColor);
    }

    private void initOrientation(Context context) {
        this.mOrientation = context.getResources().getConfiguration().orientation;
    }

    private boolean isNavBarAtBottom() {
        boolean z = false;
        if (ROTATION == 0 || ROTATION == 2) {
            return false;
        }
        if (this.mOrientation == 2) {
            z = true;
        }
        return z;
    }
}
