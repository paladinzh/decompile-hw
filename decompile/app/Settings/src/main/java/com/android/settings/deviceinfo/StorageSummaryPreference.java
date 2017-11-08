package com.android.settings.deviceinfo;

import android.graphics.Color;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.widget.ProgressBar;
import android.widget.TextView;

public class StorageSummaryPreference extends Preference {
    private int mPercent;

    public void onBindViewHolder(PreferenceViewHolder view) {
        ProgressBar progress = (ProgressBar) view.findViewById(16908301);
        if (this.mPercent != -1) {
            progress.setVisibility(0);
            progress.setProgress(this.mPercent);
        } else {
            progress.setVisibility(8);
        }
        ((TextView) view.findViewById(16908304)).setTextColor(Color.parseColor("#8a000000"));
        super.onBindViewHolder(view);
    }
}
