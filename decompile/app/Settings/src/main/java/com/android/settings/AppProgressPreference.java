package com.android.settings;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class AppProgressPreference extends TintablePreference {
    private int mProgress;

    public AppProgressPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(2130968871);
        setWidgetLayoutResource(2130969257);
    }

    public void setProgress(int amount) {
        this.mProgress = amount;
        notifyChanged();
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        ((ProgressBar) view.findViewById(16908301)).setProgress(this.mProgress);
    }
}
