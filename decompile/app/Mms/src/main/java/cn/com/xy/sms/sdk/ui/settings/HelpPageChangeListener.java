package cn.com.xy.sms.sdk.ui.settings;

import android.support.v4.view.ViewPager.OnPageChangeListener;

public class HelpPageChangeListener implements OnPageChangeListener {
    private NavigationSpotsView mNavigationSpotsView;

    public HelpPageChangeListener(NavigationSpotsView aNavigationSpotsView) {
        this.mNavigationSpotsView = aNavigationSpotsView;
    }

    public void onPageScrollStateChanged(int aState) {
    }

    public void onPageScrolled(int aPosition, float aPositionOffset, int aPositionOnOffsetPixels) {
    }

    public void onPageSelected(int aPosition) {
        this.mNavigationSpotsView.setPageIndex(aPosition);
    }
}
