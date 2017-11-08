package com.android.deskclock.alarmclock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.RelativeLayout;

public class CoverView extends RelativeLayout {
    private KeyEventListener mKeyEventListener;

    public interface KeyEventListener {
        boolean dispatchKeyEvent(KeyEvent keyEvent);
    }

    public CoverView(Context context) {
        this(context, null);
    }

    public CoverView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (this.mKeyEventListener != null) {
            return this.mKeyEventListener.dispatchKeyEvent(event);
        }
        return super.dispatchKeyEvent(event);
    }

    public void setKeyEventListener(KeyEventListener keyEventListener) {
        this.mKeyEventListener = keyEventListener;
    }
}
