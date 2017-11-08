package com.android.settings;

import android.content.Context;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceViewHolder;

public class AccessiblePreferenceCategory extends PreferenceCategory {
    private String mContentDescription;

    public AccessiblePreferenceCategory(Context context) {
        super(context);
    }

    public void setContentDescription(String contentDescription) {
        this.mContentDescription = contentDescription;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        view.itemView.setContentDescription(this.mContentDescription);
    }
}
