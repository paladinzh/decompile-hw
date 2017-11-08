package com.android.settings;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.android.settings.widget.DotsPageIndicator;
import com.android.settings.widget.LabeledSeekBar;

public abstract class PreviewSeekBarPreferenceFragment extends SettingsPreferenceFragment {
    protected int mActivityLayoutResId;
    protected int mCurrentIndex;
    protected String[] mEntries;
    protected int mInitialIndex;
    private TextView mLabel;
    private View mLarger;
    private DotsPageIndicator mPageIndicator;
    private OnPageChangeListener mPageIndicatorPageChangeListener = new OnPageChangeListener() {
        public void onPageScrollStateChanged(int state) {
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        public void onPageSelected(int position) {
            PreviewSeekBarPreferenceFragment.this.setPagerIndicatorContentDescription(position);
        }
    };
    private OnPageChangeListener mPreviewPageChangeListener = new OnPageChangeListener() {
        public void onPageScrollStateChanged(int state) {
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        public void onPageSelected(int position) {
            PreviewSeekBarPreferenceFragment.this.mPreviewPager.sendAccessibilityEvent(16384);
        }
    };
    private ViewPager mPreviewPager;
    private PreviewPagerAdapter mPreviewPagerAdapter;
    protected int[] mPreviewSampleResIds;
    private View mSmaller;

    private class onPreviewSeekBarChangeListener implements OnSeekBarChangeListener {
        private boolean mSeekByTouch;

        private onPreviewSeekBarChangeListener() {
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            PreviewSeekBarPreferenceFragment.this.setPreviewLayer(progress, true);
            if (!this.mSeekByTouch) {
                PreviewSeekBarPreferenceFragment.this.commit();
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            this.mSeekByTouch = true;
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            if (PreviewSeekBarPreferenceFragment.this.mPreviewPagerAdapter.isAnimating()) {
                PreviewSeekBarPreferenceFragment.this.mPreviewPagerAdapter.setAnimationEndAction(new Runnable() {
                    public void run() {
                        PreviewSeekBarPreferenceFragment.this.commit();
                    }
                });
            } else {
                PreviewSeekBarPreferenceFragment.this.commit();
            }
            this.mSeekByTouch = false;
        }
    }

    protected abstract void commit();

    protected abstract Configuration createConfig(Configuration configuration, int i);

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup listContainer = (ViewGroup) root.findViewById(16908351);
        listContainer.removeAllViews();
        View content = inflater.inflate(this.mActivityLayoutResId, listContainer, false);
        listContainer.addView(content);
        this.mLabel = (TextView) content.findViewById(2131886697);
        int max = Math.max(1, this.mEntries.length - 1);
        final LabeledSeekBar seekBar = (LabeledSeekBar) content.findViewById(2131886699);
        seekBar.setLabels(this.mEntries);
        seekBar.setMax(max);
        seekBar.setProgress(this.mInitialIndex);
        seekBar.setOnSeekBarChangeListener(new onPreviewSeekBarChangeListener());
        this.mSmaller = content.findViewById(2131886698);
        this.mSmaller.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                int progress = seekBar.getProgress();
                if (progress > 0) {
                    seekBar.setProgress(progress - 1, true);
                }
            }
        });
        this.mLarger = content.findViewById(2131886700);
        this.mLarger.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                int progress = seekBar.getProgress();
                if (progress < seekBar.getMax()) {
                    seekBar.setProgress(progress + 1, true);
                }
            }
        });
        if (this.mEntries.length == 1) {
            seekBar.setEnabled(false);
        }
        Context context = getPrefContext();
        Configuration origConfig = context.getResources().getConfiguration();
        boolean isLayoutRtl = origConfig.getLayoutDirection() == 1;
        Configuration[] configurations = new Configuration[this.mEntries.length];
        for (int i = 0; i < this.mEntries.length; i++) {
            configurations[i] = createConfig(origConfig, i);
        }
        this.mPreviewPager = (ViewPager) content.findViewById(2131887005);
        this.mPreviewPagerAdapter = new PreviewPagerAdapter(context, isLayoutRtl, this.mPreviewSampleResIds, configurations);
        this.mPreviewPager.setAdapter(this.mPreviewPagerAdapter);
        this.mPreviewPager.setCurrentItem(isLayoutRtl ? this.mPreviewSampleResIds.length - 1 : 0);
        this.mPreviewPager.addOnPageChangeListener(this.mPreviewPageChangeListener);
        this.mPageIndicator = (DotsPageIndicator) content.findViewById(2131886696);
        if (this.mPreviewSampleResIds.length > 1) {
            this.mPageIndicator.setViewPager(this.mPreviewPager);
            this.mPageIndicator.setVisibility(0);
            this.mPageIndicator.setOnPageChangeListener(this.mPageIndicatorPageChangeListener);
        } else {
            this.mPageIndicator.setVisibility(8);
        }
        setPreviewLayer(this.mInitialIndex, false);
        return root;
    }

    private void setPreviewLayer(int index, boolean animate) {
        boolean z;
        boolean z2 = true;
        this.mLabel.setText(this.mEntries[index]);
        View view = this.mSmaller;
        if (index > 0) {
            z = true;
        } else {
            z = false;
        }
        view.setEnabled(z);
        View view2 = this.mLarger;
        if (index >= this.mEntries.length - 1) {
            z2 = false;
        }
        view2.setEnabled(z2);
        setPagerIndicatorContentDescription(this.mPreviewPager.getCurrentItem());
        this.mPreviewPagerAdapter.setPreviewLayer(index, this.mCurrentIndex, this.mPreviewPager.getCurrentItem(), animate);
        this.mCurrentIndex = index;
    }

    private void setPagerIndicatorContentDescription(int position) {
        this.mPageIndicator.setContentDescription(getPrefContext().getString(2131624405, new Object[]{Integer.valueOf(position + 1), Integer.valueOf(this.mPreviewSampleResIds.length)}));
    }
}
