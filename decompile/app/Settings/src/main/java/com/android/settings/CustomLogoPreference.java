package com.android.settings;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;

public class CustomLogoPreference extends Preference {
    private Context mContext;

    public CustomLogoPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }

    public CustomLogoPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public CustomLogoPreference(Context context) {
        super(context);
        this.mContext = context;
    }
}
