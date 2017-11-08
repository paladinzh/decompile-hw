package com.huawei.systemmanager.comm.widget.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.huawei.systemmanager.R;

public class ArrowPreferecen extends Preference {
    private String mSummary2;

    public ArrowPreferecen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public ArrowPreferecen(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setPersistent(false);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.arrow_perference);
        this.mSummary2 = a.getString(0);
        a.recycle();
        setLayoutResource(R.layout.preference_status_3);
        setWidgetLayoutResource(R.layout.preference_widget_arrow);
    }

    public void setSummary2(String summary2) {
        this.mSummary2 = summary2;
        notifyChanged();
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        TextView summaryView2 = (TextView) view.findViewById(R.id.summary2);
        if (summaryView2 == null) {
            return;
        }
        if (TextUtils.isEmpty(this.mSummary2)) {
            summaryView2.setVisibility(8);
            return;
        }
        summaryView2.setVisibility(0);
        summaryView2.setText(this.mSummary2);
    }
}
