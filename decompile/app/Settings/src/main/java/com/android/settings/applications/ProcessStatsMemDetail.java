package com.android.settings.applications;

import android.os.Bundle;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.settings.InstrumentedFragment;
import com.android.settings.Utils;

public class ProcessStatsMemDetail extends InstrumentedFragment {
    double mMemCachedWeight;
    double mMemFreeWeight;
    double mMemKernelWeight;
    double mMemNativeWeight;
    private ViewGroup mMemStateParent;
    double[] mMemStateWeights;
    long[] mMemTimes;
    double mMemTotalWeight;
    private ViewGroup mMemUseParent;
    double mMemZRamWeight;
    private View mRootView;
    long mTotalTime;
    boolean mUseUss;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Bundle args = getArguments();
        this.mMemTimes = args.getLongArray("mem_times");
        this.mMemStateWeights = args.getDoubleArray("mem_state_weights");
        this.mMemCachedWeight = args.getDouble("mem_cached_weight");
        this.mMemFreeWeight = args.getDouble("mem_free_weight");
        this.mMemZRamWeight = args.getDouble("mem_zram_weight");
        this.mMemKernelWeight = args.getDouble("mem_kernel_weight");
        this.mMemNativeWeight = args.getDouble("mem_native_weight");
        this.mMemTotalWeight = args.getDouble("mem_total_weight");
        this.mUseUss = args.getBoolean("use_uss");
        this.mTotalTime = args.getLong("total_time");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(2130969038, container, false);
        Utils.prepareCustomPreferencesList(container, view, view, false);
        this.mRootView = view;
        createDetails();
        return view;
    }

    protected int getMetricsCategory() {
        return 22;
    }

    public void onPause() {
        super.onPause();
    }

    private void createDetails() {
        this.mMemStateParent = (ViewGroup) this.mRootView.findViewById(2131887017);
        this.mMemUseParent = (ViewGroup) this.mRootView.findViewById(2131887018);
        fillMemStateSection();
        fillMemUseSection();
    }

    private void addDetailsItem(ViewGroup parent, CharSequence title, float level, CharSequence value) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup item = (ViewGroup) inflater.inflate(2130968628, null);
        inflater.inflate(2130969257, (ViewGroup) item.findViewById(16908312));
        parent.addView(item);
        item.findViewById(16908294).setVisibility(8);
        TextView valueView = (TextView) item.findViewById(16908308);
        ((TextView) item.findViewById(16908310)).setText(title);
        valueView.setText(value);
        ((ProgressBar) item.findViewById(16908301)).setProgress(Math.round(100.0f * level));
    }

    private void fillMemStateSection() {
        CharSequence[] labels = getResources().getTextArray(2131361931);
        for (int i = 0; i < 4; i++) {
            if (this.mMemTimes[i] > 0) {
                addDetailsItem(this.mMemStateParent, labels[i], ((float) this.mMemTimes[i]) / ((float) this.mTotalTime), Formatter.formatShortElapsedTime(getActivity(), this.mMemTimes[i]));
            }
        }
    }

    private void addMemUseDetailsItem(ViewGroup parent, CharSequence title, double weight) {
        if (weight > 0.0d) {
            addDetailsItem(parent, title, (float) (weight / this.mMemTotalWeight), Formatter.formatShortFileSize(getActivity(), (long) ((1024.0d * weight) / ((double) this.mTotalTime))));
        }
    }

    private void fillMemUseSection() {
        CharSequence[] labels = getResources().getTextArray(2131361932);
        addMemUseDetailsItem(this.mMemUseParent, getResources().getText(2131626067), this.mMemKernelWeight);
        addMemUseDetailsItem(this.mMemUseParent, getResources().getText(2131626070), this.mMemZRamWeight);
        addMemUseDetailsItem(this.mMemUseParent, getResources().getText(2131626068), this.mMemNativeWeight);
        for (int i = 0; i < 14; i++) {
            addMemUseDetailsItem(this.mMemUseParent, labels[i], this.mMemStateWeights[i]);
        }
        addMemUseDetailsItem(this.mMemUseParent, getResources().getText(2131626069), this.mMemCachedWeight);
        addMemUseDetailsItem(this.mMemUseParent, getResources().getText(2131626071), this.mMemFreeWeight);
        addMemUseDetailsItem(this.mMemUseParent, getResources().getText(2131626072), this.mMemTotalWeight);
    }
}
