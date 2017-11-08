package com.android.settings;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;

public class ProgressCategory extends ProgressCategoryBase {
    private int mEmptyTextRes;
    private boolean mNoDeviceFoundAdded;
    private Preference mNoDeviceFoundPreference;
    private boolean mProgress;

    public ProgressCategory(Context context) {
        this(context, null);
    }

    public ProgressCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mProgress = false;
        setLayoutResource(2130968967);
    }

    public ProgressCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ProgressCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mProgress = false;
        setLayoutResource(2130968967);
    }

    public void setEmptyTextRes(int emptyTextRes) {
        this.mEmptyTextRes = emptyTextRes;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        View progressBar = view.findViewById(2131886936);
        if (progressBar != null) {
            boolean noDeviceFound = getPreferenceCount() != 0 ? getPreferenceCount() == 1 && getPreference(0) == this.mNoDeviceFoundPreference : true;
            progressBar.setVisibility(this.mProgress ? 0 : 8);
            if (noDeviceFound) {
                if (findPreference("no_device_found") == null) {
                    if (this.mNoDeviceFoundPreference == null) {
                        this.mNoDeviceFoundPreference = new Preference(getPreferenceManager().getContext());
                        this.mNoDeviceFoundPreference.setKey("no_device_found");
                        this.mNoDeviceFoundPreference.setLayoutResource(2130968935);
                        if (this.mEmptyTextRes > 0) {
                            this.mNoDeviceFoundPreference.setTitle(this.mEmptyTextRes);
                        }
                        this.mNoDeviceFoundPreference.setSelectable(false);
                    }
                    try {
                        addPreference(this.mNoDeviceFoundPreference);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    this.mNoDeviceFoundAdded = true;
                }
            } else if (this.mNoDeviceFoundAdded) {
                removePreference(this.mNoDeviceFoundPreference);
                this.mNoDeviceFoundAdded = false;
            }
        }
    }

    public void setProgress(boolean progressOn) {
        this.mProgress = progressOn;
        notifyChanged();
    }
}
