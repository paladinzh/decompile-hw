package com.android.settings.accessibility;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

public class DividerAllowedBelowPreference extends Preference {
    public DividerAllowedBelowPreference(Context context) {
        super(context);
    }

    public DividerAllowedBelowPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DividerAllowedBelowPreference(Context context, AttributeSet attrs, int defStyleAttrs) {
        super(context, attrs, defStyleAttrs);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.setDividerAllowedBelow(true);
    }
}
