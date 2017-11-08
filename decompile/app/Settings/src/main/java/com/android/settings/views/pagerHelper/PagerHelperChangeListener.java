package com.android.settings.views.pagerHelper;

import android.support.v4.view.ViewPager.OnPageChangeListener;

public class PagerHelperChangeListener implements OnPageChangeListener {
    private PagerHelperSpotsView mNavigationSpotsView;

    public PagerHelperChangeListener(PagerHelperSpotsView aNavigationSpotsView) {
        this.mNavigationSpotsView = aNavigationSpotsView;
    }

    public void onPageScrollStateChanged(int aState) {
    }

    public void onPageScrolled(int aPosition, float aPositionOffset, int aPositionOnOffsetPixels) {
    }

    public void onPageSelected(int aPosition) {
        if (this.mNavigationSpotsView != null) {
            this.mNavigationSpotsView.setPageIndex(aPosition);
        }
    }
}
