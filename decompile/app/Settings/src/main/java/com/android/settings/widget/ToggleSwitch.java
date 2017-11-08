package com.android.settings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Switch;

public class ToggleSwitch extends Switch {
    private OnBeforeCheckedChangeListener mOnBeforeListener;

    public interface OnBeforeCheckedChangeListener {
        boolean onBeforeCheckedChanged(ToggleSwitch toggleSwitch, boolean z);
    }

    public ToggleSwitch(Context context) {
        super(context);
    }

    public ToggleSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ToggleSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ToggleSwitch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setOnBeforeCheckedChangeListener(OnBeforeCheckedChangeListener listener) {
        this.mOnBeforeListener = listener;
    }

    public void setChecked(boolean checked) {
        if (this.mOnBeforeListener == null || !this.mOnBeforeListener.onBeforeCheckedChanged(this, checked)) {
            super.setChecked(checked);
        }
    }

    public void setCheckedInternal(boolean checked) {
        super.setChecked(checked);
    }
}
