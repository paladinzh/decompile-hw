package com.android.contacts.hap.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import com.android.contacts.hap.util.SingleHandModeManager;
import com.google.android.gms.R;

public class DialpadLinearLayout extends LinearLayout {
    private int mEdge = 0;

    public DialpadLinearLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mEdge = context.getResources().getDimensionPixelSize(R.dimen.dialpad_huawei_container_touch_edge);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (processTouch(ev)) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (processTouch(ev)) {
            return true;
        }
        return super.onTouchEvent(ev);
    }

    private boolean processTouch(MotionEvent ev) {
        float actX = ev.getX();
        return ev.getAction() == 0 && ((actX < ((float) this.mEdge) && SingleHandModeManager.getInstance(getContext().getApplicationContext()).getCurrentHandMode() != 2) || (actX > ((float) (getMeasuredWidth() - this.mEdge)) && SingleHandModeManager.getInstance(getContext().getApplicationContext()).getCurrentHandMode() != 1));
    }
}
