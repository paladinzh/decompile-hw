package com.android.systemui.time;

import android.content.Context;
import android.util.AttributeSet;
import com.android.systemui.time.TimeManager.TimeChangeCallback;
import com.android.systemui.tint.TintTextView;

public class HwStatusBarClock extends TintTextView implements TimeChangeCallback {
    public HwStatusBarClock(Context context) {
        super(context);
    }

    public HwStatusBarClock(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwStatusBarClock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void onTimeChange(long currentTimeMillis) {
        setText(TimeManager.getInstance().getChinaDateTime(getContext(), currentTimeMillis));
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
}
