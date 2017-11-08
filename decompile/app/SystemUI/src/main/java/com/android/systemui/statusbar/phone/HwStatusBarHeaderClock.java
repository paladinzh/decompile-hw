package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.systemui.time.TimeManager;
import com.android.systemui.time.TimeManager.TimeChangeCallback;

public class HwStatusBarHeaderClock extends TextView implements TimeChangeCallback {
    public HwStatusBarHeaderClock(Context context) {
        super(context);
    }

    public HwStatusBarHeaderClock(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwStatusBarHeaderClock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HwStatusBarHeaderClock(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setText(TimeManager.getInstance().getChinaDateTime(getContext(), System.currentTimeMillis()));
        TimeManager.getInstance().registerTimeChangeCallback(this);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        TimeManager.getInstance().unRegisterTimeChangeCallback(this);
    }

    public void onTimeChange(long currentTimeMillis) {
        setText(TimeManager.getInstance().getChinaDateTime(getContext(), currentTimeMillis));
    }
}
