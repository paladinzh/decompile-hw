package com.android.settings.smartcover;

import android.content.Context;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;

public class RadioButtonPreference extends CheckBoxPreference {
    private OnClickListener mListener;

    public interface OnClickListener {
        void onRadioButtonClicked(RadioButtonPreference radioButtonPreference);
    }

    public RadioButtonPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mListener = null;
        setWidgetLayoutResource(2130969001);
    }

    public RadioButtonPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mListener = null;
        setWidgetLayoutResource(2130969001);
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
        if (view != null) {
            super.onBindViewHolder(view);
            TextView title = (TextView) view.findViewById(16908310);
            if (title != null) {
                title.setSingleLine(false);
                title.setMaxLines(3);
            }
        }
    }
}
