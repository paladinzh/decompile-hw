package com.android.settings.pressure;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.RadioButton;
import android.widget.TextView;

public class AppItemPreference extends Preference {
    private String mClassName;
    private boolean mIsChecked;
    private String mPackageName;
    private RadioButton mRadioButton;
    private ResolveInfo mResolveInfo;

    public AppItemPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(2130968636);
    }

    public AppItemPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppItemPreference(Context context) {
        this(context, null);
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    public String getClassName() {
        return this.mClassName;
    }

    public void setClassName(String className) {
        this.mClassName = className;
    }

    public void setChecked(boolean isChecked) {
        this.mIsChecked = isChecked;
    }

    public void setResolveInfo(ResolveInfo resolveInfo) {
        this.mResolveInfo = resolveInfo;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mRadioButton = (RadioButton) view.findViewById(2131886996);
        if (this.mRadioButton != null) {
            this.mRadioButton.setChecked(this.mIsChecked);
        }
        TextView title = (TextView) view.findViewById(16908310);
        if (title != null) {
            title.setTextColor(-16777216);
        }
    }
}
