package com.android.settings.navigation;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.navigation.FrontFingerDemoPagerAdapter.FrontFingerDemoPageInfo;
import com.android.settings.views.pagerHelper.PagerHelperSpotsView;
import java.lang.reflect.Field;

public class NaviSettingsBaseFragment extends SettingsPreferenceFragment {
    static final int[][] DEMO_ANIMATIONS = new int[][]{NaviUtils.FP_BACK_DRAWABLES, NaviUtils.FP_HOME_DRAWABLES, NaviUtils.FP_RECENT_DRAWABLES, NaviUtils.FP_VOICE_DRAWABLES};
    static final int[][] DEMO_ANIMATIONS_GLOBAL = new int[][]{NaviUtils.FP_BACK_DRAWABLES, NaviUtils.FP_HOME_DRAWABLES, NaviUtils.FP_RECENT_DRAWABLES, NaviUtils.FP_GAPP_DRAWABLES};
    static final int[] DEMO_ANIME_DURATIONS = new int[]{2200, 3000, 2344, 3576};
    static final int[] DEMO_ANIME_DURATIONS_GLOBAL = new int[]{2200, 3000, 2344, 4556};
    static final int[] DEMO_DESCRIPTIONS = new int[]{2131628844, 2131628845, 2131628582, 2131628584};
    static final int[] DEMO_DESCRIPTIONS_GLOBAL = new int[]{2131628844, 2131628845, 2131628582, 2131628927};
    static final int[] DEMO_SUMMARIES = new int[]{2131628577, 2131628579, 2131628581, 2131628583};
    static final int[] DEMO_SUMMARIES_GLOBAL = new int[]{2131628577, 2131628579, 2131628581, 2131628928};
    protected FrontFingerDemoPagerAdapter mAdapter;
    protected Handler mLoopAnimeHandler = new LoopAnimeHandler();
    protected PagerHelperSpotsView mSpotView;
    protected boolean mStopAnimeLoop = false;
    protected ViewPager mViewPager;

    public class DemoPageChangeListener implements OnPageChangeListener {
        public void onPageScrollStateChanged(int aState) {
            if (aState == 1 && NaviSettingsBaseFragment.this.mLoopAnimeHandler != null) {
                NaviSettingsBaseFragment.this.mStopAnimeLoop = true;
                NaviSettingsBaseFragment.this.mLoopAnimeHandler.removeMessages(1000);
            }
        }

        public void onPageScrolled(int aPosition, float aPositionOffset, int aPositionOnOffsetPixels) {
        }

        public void onPageSelected(int position) {
            Log.d("NaviSettingsBaseFragment", "onPageSelected, position = " + position);
            if (NaviSettingsBaseFragment.this.mSpotView != null) {
                NaviSettingsBaseFragment.this.mSpotView.setPageIndex(position);
            }
            LazyLoadingAnimationContainer previousAnimation = NaviSettingsBaseFragment.this.mAdapter.getLiveAnimation(position <= 0 ? 3 : position - 1);
            LazyLoadingAnimationContainer animation = NaviSettingsBaseFragment.this.mAdapter.getLiveAnimation(position);
            Log.d("NaviSettingsBaseFragment", "animation = " + animation);
            if (animation != null) {
                animation.stop();
                animation.start();
            }
            if (previousAnimation != null) {
                previousAnimation.stop();
            }
        }
    }

    private class LoopAnimeHandler extends Handler {
        private LoopAnimeHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1000 && !NaviSettingsBaseFragment.this.mStopAnimeLoop) {
                int nextItem = NaviSettingsBaseFragment.this.mViewPager.getCurrentItem();
                nextItem = nextItem >= 3 ? 0 : nextItem + 1;
                Log.d("NaviSettingsBaseFragment", "MSG_NEXT_PAGE handled, nextItem = " + nextItem);
                NaviSettingsBaseFragment.this.mViewPager.setCurrentItem(nextItem, true);
                NaviSettingsBaseFragment.this.mLoopAnimeHandler.removeMessages(1000);
                NaviSettingsBaseFragment.this.mLoopAnimeHandler.sendEmptyMessageDelayed(1000, (long) (Utils.isChinaArea() ? NaviSettingsBaseFragment.DEMO_ANIME_DURATIONS[nextItem] : NaviSettingsBaseFragment.DEMO_ANIME_DURATIONS_GLOBAL[nextItem]));
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(2130968825, container, false);
        ViewGroup prefs_container = (ViewGroup) view.findViewById(2131886191);
        Utils.prepareCustomPreferencesList(container, view, prefs_container, false);
        prefs_container.addView(super.onCreateView(inflater, prefs_container, savedInstanceState));
        this.mViewPager = (ViewPager) view.findViewById(2131886710);
        buildPagerAdapter();
        this.mViewPager.setAdapter(this.mAdapter);
        setupScroller();
        this.mSpotView = (PagerHelperSpotsView) view.findViewById(2131886711);
        this.mSpotView.setPageCount(this.mAdapter.getCount());
        this.mViewPager.setOnPageChangeListener(new DemoPageChangeListener());
        this.mViewPager.setCurrentItem(0);
        this.mViewPager.setOffscreenPageLimit(3);
        return view;
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    public boolean useNormalDividerOnly() {
        return true;
    }

    protected FrontFingerDemoPagerAdapter buildPagerAdapter() {
        FrontFingerDemoPageInfo adapterInfo = new FrontFingerDemoPageInfo();
        if (Utils.isChinaArea()) {
            adapterInfo.drawables = DEMO_ANIMATIONS;
            adapterInfo.summaries = DEMO_SUMMARIES;
            adapterInfo.descriptions = DEMO_DESCRIPTIONS;
            adapterInfo.pageCount = 4;
        } else {
            adapterInfo.drawables = DEMO_ANIMATIONS_GLOBAL;
            adapterInfo.summaries = DEMO_SUMMARIES_GLOBAL;
            adapterInfo.descriptions = DEMO_DESCRIPTIONS_GLOBAL;
            adapterInfo.pageCount = 4;
        }
        this.mAdapter = new FrontFingerDemoPagerAdapter(getActivity(), adapterInfo);
        return this.mAdapter;
    }

    protected void stopAnimLoop() {
        Log.i("NaviSettingsBaseFragment", "stop anime loop");
        this.mLoopAnimeHandler.removeMessages(1000);
        this.mStopAnimeLoop = true;
        for (int idx = 0; idx < 4; idx++) {
            LazyLoadingAnimationContainer anime = this.mAdapter.getLiveAnimation(idx);
            if (anime != null) {
                anime.stop();
            }
        }
    }

    protected void startAnimeLoop() {
        Log.i("NaviSettingsBaseFragment", "start anime loop");
        this.mStopAnimeLoop = false;
        this.mLoopAnimeHandler.removeMessages(1000);
        this.mLoopAnimeHandler.sendEmptyMessageDelayed(1000, 2200);
    }

    private void printSetupScrollerError(Exception e) {
        Log.e("NaviSettingsBaseFragment", "Failed to setup customized scroller");
        e.printStackTrace();
    }

    private void setupScroller() {
        try {
            Field scrollerField = ViewPager.class.getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            scrollerField.set(this.mViewPager, new CustomizedSpeedScroller(this.mViewPager.getContext(), 1500));
        } catch (NoSuchFieldException e) {
            printSetupScrollerError(e);
        } catch (IllegalAccessException e2) {
            printSetupScrollerError(e2);
        } catch (RuntimeException e3) {
            printSetupScrollerError(e3);
        }
    }
}
