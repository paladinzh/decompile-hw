package com.huawei.systemmanager.spacecleanner.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Switch;
import com.huawei.systemmanager.util.HwLog;

public class EnsureSwitch extends Switch {
    private static final String TAG = "EnsureSwitch";

    public EnsureSwitch(Context context) {
        super(context);
    }

    public EnsureSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EnsureSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void toggle() {
        HwLog.i(TAG, "toggle do nothing");
    }
}
