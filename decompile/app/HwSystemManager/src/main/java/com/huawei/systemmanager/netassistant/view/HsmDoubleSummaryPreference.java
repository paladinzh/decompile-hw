package com.huawei.systemmanager.netassistant.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.huawei.systemmanager.R;

public class HsmDoubleSummaryPreference extends Preference {
    private static final String TAG = "HsmDoubleSummaryPreference";
    String mSummary2;

    @TargetApi(21)
    public HsmDoubleSummaryPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public HsmDoubleSummaryPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HsmDoubleSummaryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HsmDoubleSummaryPreference(Context context) {
        super(context);
    }

    protected View onCreateView(ViewGroup parent) {
        return super.onCreateView(parent);
    }

    public void setSummary2(CharSequence cs) {
        this.mSummary2 = cs.toString();
    }

    public void setSummary2(int resId) {
        this.mSummary2 = getContext().getResources().getString(resId);
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        TextView summary2Txt = (TextView) view.findViewById(R.id.summary2);
        if (summary2Txt != null) {
            if (TextUtils.isEmpty(this.mSummary2)) {
                summary2Txt.setVisibility(8);
            } else {
                summary2Txt.setVisibility(0);
                summary2Txt.setText(this.mSummary2);
            }
        }
    }

    public void removeSummary2() {
        this.mSummary2 = null;
    }
}
