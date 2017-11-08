package com.android.settings.applications;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;

public class SpacePreference extends Preference {
    private int mHeight;

    public SpacePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842894);
    }

    public SpacePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SpacePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutResource(2130969144);
        this.mHeight = context.obtainStyledAttributes(attrs, new int[]{16842997}, defStyleAttr, defStyleRes).getDimensionPixelSize(0, 0);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        view.itemView.setLayoutParams(new LayoutParams(-1, this.mHeight));
    }
}
