package com.android.settings;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class NetherSummaryImageRadioPreference extends RadioListPreference {
    private CharSequence mNetherSummary;

    public NetherSummaryImageRadioPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        updateView(view);
        super.onBindViewHolder(view);
    }

    public CharSequence getNetherSummary() {
        return this.mNetherSummary;
    }

    public void setNetherSummary(CharSequence summary) {
        if (summary != null || this.mNetherSummary == null) {
            if (summary == null) {
                return;
            }
            if (summary.equals(this.mNetherSummary)) {
                return;
            }
        }
        this.mNetherSummary = summary;
        notifyChanged();
    }

    private void updateView(PreferenceViewHolder view) {
        TextView netherSummaryView = (TextView) view.findViewById(2131886914);
        if (netherSummaryView != null) {
            CharSequence summary = getNetherSummary();
            if (TextUtils.isEmpty(summary)) {
                netherSummaryView.setVisibility(8);
                return;
            }
            netherSummaryView.setText(summary);
            netherSummaryView.setVisibility(0);
        }
    }
}
