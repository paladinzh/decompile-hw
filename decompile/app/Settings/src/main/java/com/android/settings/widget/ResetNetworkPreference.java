package com.android.settings.widget;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class ResetNetworkPreference extends Preference {
    private CharSequence mSummary;

    public ResetNetworkPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ResetNetworkPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResetNetworkPreference(Context context) {
        super(context);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        TextView summaryTextView = (TextView) view.findViewById(2131886940);
        if (summaryTextView != null && !TextUtils.isEmpty(this.mSummary)) {
            summaryTextView.setText(this.mSummary);
        }
    }

    public void setSummaryTextView(int summaryResId) {
        CharSequence summary = getContext().getResources().getText(summaryResId);
        if (summary != null || this.mSummary == null) {
            if (summary == null) {
                return;
            }
            if (summary.equals(this.mSummary)) {
                return;
            }
        }
        this.mSummary = summary;
    }
}
