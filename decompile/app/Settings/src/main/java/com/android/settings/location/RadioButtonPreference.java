package com.android.settings.location;

import android.content.Context;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.RadioButton;
import android.widget.TextView;

public class RadioButtonPreference extends CheckBoxPreference {
    private RadioButton mButton;
    protected boolean mChecked;
    private OnClickListener mListener;

    public interface OnClickListener {
        void onRadioButtonClicked(RadioButtonPreference radioButtonPreference);
    }

    public RadioButtonPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mChecked = false;
        this.mListener = null;
        setWidgetLayoutResource(2130969001);
    }

    public RadioButtonPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842895);
    }

    public RadioButtonPreference(Context context) {
        this(context, null);
    }

    public void setOnClickListener(OnClickListener listener) {
        this.mListener = listener;
    }

    public void onClick() {
        if (this.mListener != null) {
            this.mListener.onRadioButtonClicked(this);
        }
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        TextView title = (TextView) view.findViewById(16908310);
        if (title != null) {
            title.setSingleLine(false);
            title.setMaxLines(3);
        }
        this.mButton = (RadioButton) view.findViewById(16908289);
        this.mButton.setChecked(this.mChecked);
    }

    public boolean isChecked() {
        if (this.mButton != null) {
            return this.mButton.isChecked();
        }
        return this.mChecked;
    }

    public void setChecked(boolean checked) {
        if (this.mChecked != checked) {
            this.mChecked = checked;
            if (this.mButton != null) {
                this.mButton.setChecked(checked);
            }
        }
    }
}
