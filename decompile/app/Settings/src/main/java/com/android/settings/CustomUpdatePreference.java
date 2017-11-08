package com.android.settings;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class CustomUpdatePreference extends Preference {
    private Context mContext;
    private TextView mTitle;

    public CustomUpdatePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }

    public CustomUpdatePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public CustomUpdatePreference(Context context) {
        super(context);
        this.mContext = context;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        this.mTitle = (TextView) view.findViewById(16908310);
        this.mTitle.setTextColor(this.mContext.getResources().getColor(2131427515));
        LayoutParams layoutParams = new LayoutParams(-2, -2);
        layoutParams.addRule(13);
        this.mTitle.setLayoutParams(layoutParams);
        super.onBindViewHolder(view);
    }
}
