package com.android.settings;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import com.android.settingslib.RestrictedPreference;

public class CustomDividerPreference extends RestrictedPreference {
    private boolean mAllowAbove = true;
    private boolean mAllowBelow = true;

    public CustomDividerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomDividerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomDividerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomDividerPreference(Context context) {
        super(context);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        view.setDividerAllowedAbove(this.mAllowAbove);
        view.setDividerAllowedBelow(this.mAllowBelow);
    }

    public void setAllowAbove(boolean allowAbove) {
        this.mAllowAbove = allowAbove;
        notifyChanged();
    }

    public void setAllowBelow(boolean allowBelow) {
        this.mAllowBelow = allowBelow;
        notifyChanged();
    }
}
