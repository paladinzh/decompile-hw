package com.android.settings.applications;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceFrameLayout;
import android.preference.PreferenceFrameLayout.LayoutParams;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.InstrumentedFragment;

public class BackgroundCheckSummary extends InstrumentedFragment {
    private LayoutInflater mInflater;

    protected int getMetricsCategory() {
        return 258;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mInflater = inflater;
        View rootView = this.mInflater.inflate(2130968641, container, false);
        if (container instanceof PreferenceFrameLayout) {
            ((LayoutParams) rootView.getLayoutParams()).removeBorders = true;
        }
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.add(2131886272, new AppOpsCategory(AppOpsState.RUN_IN_BACKGROUND_TEMPLATE, true), "appops");
        ft.commitAllowingStateLoss();
        return rootView;
    }
}
