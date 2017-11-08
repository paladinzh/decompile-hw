package com.android.settings;

import android.content.Context;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

public class DividedCategory extends PreferenceCategory {
    public DividedCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.setDividerAllowedAbove(true);
    }
}
