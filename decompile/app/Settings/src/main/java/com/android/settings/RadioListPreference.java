package com.android.settings;

import android.content.Context;
import android.support.v7.preference.CheckBoxPreference;
import android.util.AttributeSet;

public class RadioListPreference extends CheckBoxPreference {
    public RadioListPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWidgetLayoutResource(2130969001);
    }

    public RadioListPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842895);
    }
}
