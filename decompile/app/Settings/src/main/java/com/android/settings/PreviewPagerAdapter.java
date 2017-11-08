package com.android.settings;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class PreviewPagerAdapter extends PagerAdapter {
    private static final Interpolator FADE_IN_INTERPOLATOR = new DecelerateInterpolator();
    private static final Interpolator FADE_OUT_INTERPOLATOR = new AccelerateInterpolator();
    private int mAnimationCounter;
    private Runnable mAnimationEndAction;
    private boolean mIsLayoutRtl;
    private FrameLayout[] mPreviewFrames;

    private class PreviewFrameAnimatorListener implements AnimatorListener {
        private PreviewFrameAnimatorListener() {
        }

        public void onAnimationStart(Animator animation) {
            PreviewPagerAdapter previewPagerAdapter = PreviewPagerAdapter.this;
            previewPagerAdapter.mAnimationCounter = previewPagerAdapter.mAnimationCounter + 1;
        }

        public void onAnimationEnd(Animator animation) {
            PreviewPagerAdapter previewPagerAdapter = PreviewPagerAdapter.this;
            previewPagerAdapter.mAnimationCounter = previewPagerAdapter.mAnimationCounter - 1;
            PreviewPagerAdapter.this.runAnimationEndAction();
        }

        public void onAnimationCancel(Animator animation) {
        }

        public void onAnimationRepeat(Animator animation) {
        }
    }

    public PreviewPagerAdapter(Context context, boolean isLayoutRtl, int[] previewSampleResIds, Configuration[] configurations) {
        this.mIsLayoutRtl = isLayoutRtl;
        this.mPreviewFrames = new FrameLayout[previewSampleResIds.length];
        int i = 0;
        while (i < previewSampleResIds.length) {
            int p = this.mIsLayoutRtl ? (previewSampleResIds.length - 1) - i : i;
            this.mPreviewFrames[p] = new FrameLayout(context);
            this.mPreviewFrames[p].setLayoutParams(new LayoutParams(-1, -1));
            for (Configuration configuration : configurations) {
                Context configContext = context.createConfigurationContext(configuration);
                configContext.setTheme(context.getThemeResId());
                View sampleView = LayoutInflater.from(configContext).inflate(previewSampleResIds[i], this.mPreviewFrames[p], false);
                displaySampleText(context, sampleView);
                sampleView.setAlpha(0.0f);
                sampleView.setVisibility(4);
                this.mPreviewFrames[p].addView(sampleView);
            }
            i++;
        }
    }

    private void displaySampleText(Context context, View sampleView) {
        TextView tv2 = (TextView) sampleView.findViewById(2131886703);
        TextView tv3 = (TextView) sampleView.findViewById(2131886346);
        TextView tv4 = (TextView) sampleView.findViewById(2131886704);
        ((TextView) sampleView.findViewById(2131886702)).setText(context.getResources().getString(2131624413));
        tv2.setText(context.getResources().getString(2131624414));
        tv3.setText(String.format(context.getResources().getString(2131628886, new Object[]{Integer.valueOf(11)}), new Object[0]));
        tv4.setText(context.getResources().getString(2131624416));
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public int getCount() {
        return this.mPreviewFrames.length;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(this.mPreviewFrames[position]);
        return this.mPreviewFrames[position];
    }

    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    boolean isAnimating() {
        return this.mAnimationCounter > 0;
    }

    void setAnimationEndAction(Runnable action) {
        this.mAnimationEndAction = action;
    }

    void setPreviewLayer(int newIndex, int currentIndex, int currentItem, boolean animate) {
        for (FrameLayout previewFrame : this.mPreviewFrames) {
            if (currentIndex >= 0) {
                final View lastLayer = previewFrame.getChildAt(currentIndex);
                if (animate && previewFrame == this.mPreviewFrames[currentItem]) {
                    lastLayer.animate().alpha(0.0f).setInterpolator(FADE_OUT_INTERPOLATOR).setDuration(400).setListener(new PreviewFrameAnimatorListener()).withEndAction(new Runnable() {
                        public void run() {
                            lastLayer.setVisibility(4);
                        }
                    });
                } else {
                    lastLayer.setAlpha(0.0f);
                    lastLayer.setVisibility(4);
                }
            }
            final View nextLayer = previewFrame.getChildAt(newIndex);
            if (animate && previewFrame == this.mPreviewFrames[currentItem]) {
                nextLayer.animate().alpha(1.0f).setInterpolator(FADE_IN_INTERPOLATOR).setDuration(400).setListener(new PreviewFrameAnimatorListener()).withStartAction(new Runnable() {
                    public void run() {
                        nextLayer.setVisibility(0);
                    }
                });
            } else {
                nextLayer.setVisibility(0);
                nextLayer.setAlpha(1.0f);
            }
        }
    }

    private void runAnimationEndAction() {
        if (this.mAnimationEndAction != null && !isAnimating()) {
            this.mAnimationEndAction.run();
            this.mAnimationEndAction = null;
        }
    }
}
