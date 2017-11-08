package com.huawei.systemmanager.comm.widget;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;

public class WrappingSwitchPreference extends SwitchPreference {
    public WrappingSwitchPreference(Context context) {
        super(context);
    }

    public WrappingSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onClick() {
        super.onClick();
    }
}
