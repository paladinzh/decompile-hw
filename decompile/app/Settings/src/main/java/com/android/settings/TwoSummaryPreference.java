package com.android.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class TwoSummaryPreference extends Preference {
    private CharSequence mNetherSummary;

    public TwoSummaryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.TwoSummaryPreference);
        this.mNetherSummary = a.getString(0);
        a.recycle();
    }

    public TwoSummaryPreference(Context context) {
        super(context, null);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        TextView netherSummaryView = (TextView) view.findViewById(2131886914);
        if (netherSummaryView != null) {
            CharSequence summary = getNetherSummary();
            if (TextUtils.isEmpty(summary)) {
                netherSummaryView.setVisibility(8);
            } else {
                netherSummaryView.setText(summary);
                netherSummaryView.setVisibility(0);
            }
        }
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
}
