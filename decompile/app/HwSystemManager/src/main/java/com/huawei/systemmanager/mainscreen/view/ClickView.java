package com.huawei.systemmanager.mainscreen.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.HsmMath;

public class ClickView extends ImageButton {
    private int centerX;
    private int centerY;
    private int mCircleRange;

    public ClickView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClickView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setImageResource(R.drawable.scan_bg);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.centerX = getWidth() / 2;
        this.centerY = getHeight() / 2;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean isOutRange = false;
        if (event.getAction() == 0) {
            if (Float.compare(HsmMath.dist(event.getX(), event.getY(), (float) this.centerX, (float) this.centerY), (float) this.mCircleRange) > 0) {
                isOutRange = true;
            }
            if (isOutRange) {
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    public void setScanEnd() {
        setImageResource(R.drawable.commont_scan_btn_selector);
        this.mCircleRange = getResources().getDimensionPixelSize(R.dimen.mainscreen_clickview_circle_y_position);
    }
}
