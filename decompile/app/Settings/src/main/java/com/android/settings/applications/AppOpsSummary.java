package com.android.settings.applications;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceFrameLayout;
import android.preference.PreferenceFrameLayout.LayoutParams;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.InstrumentedFragment;
import com.android.settings.applications.AppOpsState.OpsTemplate;

public class AppOpsSummary extends InstrumentedFragment {
    static OpsTemplate[] sPageTemplates = new OpsTemplate[]{AppOpsState.LOCATION_TEMPLATE, AppOpsState.PERSONAL_TEMPLATE, AppOpsState.MESSAGING_TEMPLATE, AppOpsState.MEDIA_TEMPLATE, AppOpsState.DEVICE_TEMPLATE};
    private ViewGroup mContentContainer;
    int mCurPos;
    private LayoutInflater mInflater;
    CharSequence[] mPageNames;
    private View mRootView;
    private ViewPager mViewPager;

    class MyPagerAdapter extends FragmentPagerAdapter implements OnPageChangeListener {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            return new AppOpsCategory(AppOpsSummary.sPageTemplates[position]);
        }

        public int getCount() {
            return AppOpsSummary.sPageTemplates.length;
        }

        public CharSequence getPageTitle(int position) {
            return AppOpsSummary.this.mPageNames[position];
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        public void onPageSelected(int position) {
            AppOpsSummary.this.mCurPos = position;
        }

        public void onPageScrollStateChanged(int state) {
            if (state != 0) {
            }
        }
    }

    protected int getMetricsCategory() {
        return 15;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mInflater = inflater;
        View rootView = this.mInflater.inflate(2130968632, container, false);
        this.mContentContainer = container;
        this.mRootView = rootView;
        this.mPageNames = getResources().getTextArray(2131361886);
        this.mViewPager = (ViewPager) rootView.findViewById(2131886256);
        MyPagerAdapter adapter = new MyPagerAdapter(getChildFragmentManager());
        this.mViewPager.setAdapter(adapter);
        this.mViewPager.setOnPageChangeListener(adapter);
        PagerTabStrip tabs = (PagerTabStrip) rootView.findViewById(2131886257);
        TypedArray ta = tabs.getContext().obtainStyledAttributes(new int[]{16843829});
        int colorAccent = ta.getColor(0, 0);
        ta.recycle();
        tabs.setTabIndicatorColorResource(colorAccent);
        if (container instanceof PreferenceFrameLayout) {
            ((LayoutParams) rootView.getLayoutParams()).removeBorders = true;
        }
        return rootView;
    }
}
