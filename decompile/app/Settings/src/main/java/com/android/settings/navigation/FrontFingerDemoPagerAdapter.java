package com.android.settings.navigation;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;

public class FrontFingerDemoPagerAdapter extends PagerAdapter {
    private Context mContext;
    private ImageView mDemoBackgroundView;
    private ImageView mDemoImageView;
    private FrontFingerDemoPageInfo mDemoInfo;
    private TextView mDesc;
    private boolean mFirstRun;
    private Map<Integer, LazyLoadingAnimationContainer> mLiveAnimations;
    private int mPageLayoutRes = 2130968896;
    private TextView mSummary;

    public static class FrontFingerDemoPageInfo {
        public int[] descriptions;
        public int[][] drawables;
        public int pageCount;
        public int[] summaries;
    }

    public FrontFingerDemoPagerAdapter(Context context, FrontFingerDemoPageInfo demoInfo) {
        this.mDemoInfo = demoInfo;
        this.mContext = context;
        this.mLiveAnimations = new HashMap();
        this.mFirstRun = true;
    }

    public int getCount() {
        return this.mDemoInfo.pageCount;
    }

    public boolean isViewFromObject(View pageView, Object object) {
        return pageView == object;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        View demoView = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(this.mPageLayoutRes, null);
        this.mDemoImageView = (ImageView) demoView.findViewById(2131886848);
        this.mDemoBackgroundView = (ImageView) demoView.findViewById(2131886847);
        this.mDemoBackgroundView.setImageResource(2130837622);
        this.mSummary = (TextView) demoView.findViewById(2131886387);
        if (this.mDemoInfo.summaries[position] != 0) {
            this.mSummary.setText(this.mDemoInfo.summaries[position]);
        } else {
            this.mSummary.setVisibility(8);
        }
        this.mDesc = (TextView) demoView.findViewById(2131886500);
        if (this.mDemoInfo.descriptions[position] != 0) {
            this.mDesc.setText(this.mDemoInfo.descriptions[position]);
        } else {
            this.mDesc.setVisibility(8);
        }
        LazyLoadingAnimationContainer animation = new LazyLoadingAnimationContainer(this.mDemoImageView);
        animation.addAllFrames(this.mDemoInfo.drawables[position], 268);
        animation.setTag("anim_front_fp_" + position);
        this.mLiveAnimations.put(Integer.valueOf(position), animation);
        if (this.mFirstRun && position == 0) {
            animation.start();
            this.mFirstRun = false;
        }
        container.addView(demoView);
        return demoView;
    }

    public void destroyItem(View container, int position, Object object) {
        this.mDemoImageView = (ImageView) ((View) object).findViewById(2131886848);
        LazyLoadingAnimationContainer animation = (LazyLoadingAnimationContainer) this.mLiveAnimations.get(Integer.valueOf(position));
        if (animation.isRunning()) {
            animation.stop();
        }
        this.mLiveAnimations.remove(Integer.valueOf(position));
        ((ViewPager) container).removeView((View) object);
    }

    public LazyLoadingAnimationContainer getLiveAnimation(int position) {
        return (LazyLoadingAnimationContainer) this.mLiveAnimations.get(Integer.valueOf(position));
    }

    public void setPageLayoutRes(int resId) {
        this.mPageLayoutRes = resId;
    }
}
