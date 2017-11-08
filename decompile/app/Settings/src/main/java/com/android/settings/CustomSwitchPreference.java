package com.android.settings;

import android.content.Context;
import android.support.v14.preference.SwitchPreference;
import android.util.AttributeSet;

public class CustomSwitchPreference extends SwitchPreference {
    public CustomSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSwitchPreference(Context context) {
        super(context, null);
    }
}
