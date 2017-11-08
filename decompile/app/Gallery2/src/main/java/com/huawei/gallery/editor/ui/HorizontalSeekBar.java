package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class HorizontalSeekBar extends SeekBar {
    public HorizontalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (getAlpha() == 0.0f) {
            return false;
        }
        return super.onTouchEvent(event);
    }
}
