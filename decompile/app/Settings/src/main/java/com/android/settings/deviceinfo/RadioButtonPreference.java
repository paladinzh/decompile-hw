package com.android.settings.deviceinfo;

import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.widget.RadioButton;

public class RadioButtonPreference extends CheckBoxPreference {
    private RadioButton mButton;
    protected boolean mIsChecked;
    private OnClickListener mListener;

    public interface OnClickListener {
        void onRadioButtonClicked(RadioButtonPreference radioButtonPreference);
    }

    public void onClick() {
        if (this.mListener != null) {
            this.mListener.onRadioButtonClicked(this);
        }
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mButton = (RadioButton) view.findViewById(16908289);
        this.mButton.setChecked(this.mIsChecked);
    }

    public boolean isChecked() {
        if (this.mButton != null) {
            return this.mButton.isChecked();
        }
        return this.mIsChecked;
    }

    public void setChecked(boolean checked) {
        if (this.mIsChecked != checked) {
            this.mIsChecked = checked;
            if (this.mButton != null) {
                this.mButton.setChecked(checked);
            }
        }
    }
}
