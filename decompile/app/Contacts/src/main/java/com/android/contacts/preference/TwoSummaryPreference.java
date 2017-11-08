package com.android.contacts.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.contacts.R$styleable;
import com.google.android.gms.R;

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

    protected void onBindView(View view) {
        TextView netherSummaryView = (TextView) view.findViewById(R.id.nether_summary);
        if (netherSummaryView != null) {
            CharSequence summary = getNetherSummary();
            if (TextUtils.isEmpty(summary)) {
                netherSummaryView.setVisibility(8);
            } else {
                netherSummaryView.setText(summary);
                netherSummaryView.setVisibility(0);
            }
        }
        super.onBindView(view);
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
