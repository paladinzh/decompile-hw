package com.android.systemui.tuner;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.android.systemui.statusbar.phone.NavigationBarInflaterView;

public class PreviewNavInflater extends NavigationBarInflaterView {
    public PreviewNavInflater(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        TunerService.get(getContext()).removeTunable(this);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    public void onTuningChanged(String key, String newValue) {
        if (!"sysui_nav_bar".equals(key)) {
            super.onTuningChanged(key, newValue);
        } else if (isValidLayout(newValue)) {
            super.onTuningChanged(key, newValue);
        }
    }

    private boolean isValidLayout(String newValue) {
        boolean z = true;
        if (newValue == null) {
            return true;
        }
        int separatorCount = 0;
        int lastGravitySeparator = 0;
        int i = 0;
        while (i < newValue.length()) {
            if (newValue.charAt(i) == ";".charAt(0)) {
                if (i == 0 || i - lastGravitySeparator == 1) {
                    return false;
                }
                lastGravitySeparator = i;
                separatorCount++;
            }
            i++;
        }
        if (separatorCount != 2 || newValue.length() - lastGravitySeparator == 1) {
            z = false;
        }
        return z;
    }
}
