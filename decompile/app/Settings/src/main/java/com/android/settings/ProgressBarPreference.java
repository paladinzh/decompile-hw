package com.android.settings;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.widget.ProgressBar;

public class ProgressBarPreference extends Preference {
    private int mPercent = -1;

    public ProgressBarPreference(Context context) {
        super(context);
        setLayoutResource(2130969041);
    }

    public void setPercent(int percent) {
        this.mPercent = percent;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        ProgressBar progress = (ProgressBar) view.findViewById(16908301);
        if (this.mPercent != -1) {
            progress.setMax(100);
            progress.setVisibility(0);
            progress.setProgress(this.mPercent);
        } else {
            progress.setVisibility(8);
        }
        super.onBindViewHolder(view);
    }
}
