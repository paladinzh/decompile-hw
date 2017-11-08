package com.android.settings.views.pagerHelper;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.TextView;

public class PagerHelperAdapter extends PagerAdapter {
    private Context mContext;
    private ImageView mDescImageView;
    private final int[] mDrawables;
    private int mHeight = 0;
    private HelperImageHeightCallback mHeightCallback;
    private final int[] mSummaries;
    private TextView mSummary;

    public interface HelperImageHeightCallback {
        void notify(int i);
    }

    public PagerHelperAdapter(Context aContext, int[] drawables, int[] summaries, HelperImageHeightCallback callback) {
        this.mContext = aContext;
        this.mDrawables = (int[]) drawables.clone();
        this.mSummaries = (int[]) summaries.clone();
        this.mHeightCallback = callback;
    }

    public int getCount() {
        return this.mDrawables.length;
    }

    public boolean isViewFromObject(View aPageView, Object aObject) {
        return aPageView == aObject;
    }

    public Object instantiateItem(ViewGroup aContainer, int aPosition) {
        View aView = ((Activity) this.mContext).getLayoutInflater().inflate(2130968899, null);
        this.mDescImageView = (ImageView) aView.findViewById(2131886849);
        this.mDescImageView.setImageResource(this.mDrawables[aPosition]);
        if (this.mHeight == 0) {
            this.mDescImageView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    PagerHelperAdapter.this.mHeight = PagerHelperAdapter.this.mDescImageView.getHeight();
                    if (PagerHelperAdapter.this.mHeightCallback != null) {
                        PagerHelperAdapter.this.mHeightCallback.notify(PagerHelperAdapter.this.mHeight);
                    }
                    PagerHelperAdapter.this.mDescImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
        this.mSummary = (TextView) aView.findViewById(2131886387);
        if (this.mSummaries[aPosition] != 0) {
            this.mSummary.setText(this.mSummaries[aPosition]);
        } else {
            this.mSummary.setVisibility(8);
        }
        if (this.mDescImageView.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) this.mDescImageView.getDrawable()).start();
        }
        aContainer.addView(aView);
        return aView;
    }

    public void destroyItem(View container, int position, Object object) {
        this.mDescImageView = (ImageView) ((View) object).findViewById(2131886849);
        if (this.mDescImageView.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) this.mDescImageView.getBackground()).stop();
        }
        ((ViewPager) container).removeView((View) object);
    }
}
