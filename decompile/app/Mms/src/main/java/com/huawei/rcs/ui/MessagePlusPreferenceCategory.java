package com.huawei.rcs.ui;

import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import com.google.android.gms.R;

public class MessagePlusPreferenceCategory extends PreferenceCategory {
    public MessagePlusPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected View onCreateView(ViewGroup parent) {
        Resources res = getContext().getResources();
        View v = super.onCreateView(parent);
        v.setLayoutParams(new LayoutParams(-1, res.getDimensionPixelSize(R.dimen.preference_category_min_height)));
        return v;
    }
}
