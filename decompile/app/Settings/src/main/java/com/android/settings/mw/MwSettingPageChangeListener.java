package com.android.settings.mw;

import android.support.v4.view.ViewPager.OnPageChangeListener;

public class MwSettingPageChangeListener implements OnPageChangeListener {
    private MwNavigationSpotsView mNavigationSpotsView;

    public MwSettingPageChangeListener(MwNavigationSpotsView aNavigationSpotsView) {
        this.mNavigationSpotsView = aNavigationSpotsView;
    }

    public void onPageScrollStateChanged(int aState) {
        switch (aState) {
        }
    }

    public void onPageScrolled(int aPosition, float aPositionOffset, int aPositionOnOffsetPixels) {
    }

    public void onPageSelected(int aPosition) {
        this.mNavigationSpotsView.setPageIndex(aPosition);
    }
}
