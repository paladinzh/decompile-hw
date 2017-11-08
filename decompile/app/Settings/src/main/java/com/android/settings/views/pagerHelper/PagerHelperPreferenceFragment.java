package com.android.settings.views.pagerHelper;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.views.pagerHelper.PagerHelperAdapter.HelperImageHeightCallback;

public abstract class PagerHelperPreferenceFragment extends SettingsPreferenceFragment {
    private PagerHelperAdapter mAdapter;
    private HelperImageHeightCallback mHelperImageHeightCallback = new HelperImageHeightCallback() {
        public void notify(int height) {
            if (PagerHelperPreferenceFragment.this.mViewPager != null) {
                LayoutParams params = PagerHelperPreferenceFragment.this.mViewPager.getLayoutParams();
                params.height = height;
                PagerHelperPreferenceFragment.this.mViewPager.setLayoutParams(params);
            }
        }
    };
    private View mPagerLayout;
    private ViewPager mViewPager;

    public abstract int[] getDrawables();

    public abstract int[] getSummaries();

    protected int getMetricsCategory() {
        return 100000;
    }

    public int getPageIndex() {
        return 0;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initPagerLayout(inflater, container);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void initPagerLayout(LayoutInflater inflater, ViewGroup container) {
        this.mPagerLayout = inflater.inflate(2130968900, container, false);
        this.mViewPager = (ViewPager) this.mPagerLayout.findViewById(2131886710);
        this.mAdapter = new PagerHelperAdapter(getActivity(), getDrawables(), getSummaries(), this.mHelperImageHeightCallback);
        this.mViewPager.setAdapter(this.mAdapter);
        PagerHelperSpotsView spotView = (PagerHelperSpotsView) this.mPagerLayout.findViewById(2131886711);
        if (getDrawables().length == 1) {
            spotView.setPageCount(1);
            spotView.setVisibility(8);
        } else {
            spotView.setPageCount(this.mAdapter.getCount());
        }
        this.mViewPager.setOnPageChangeListener(new PagerHelperChangeListener(spotView));
        this.mViewPager.setCurrentItem(getPageIndex());
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHeaderView(this.mPagerLayout);
    }
}
