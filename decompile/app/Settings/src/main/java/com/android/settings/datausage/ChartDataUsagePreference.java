package com.android.settings.datausage;

import android.content.Context;
import android.net.NetworkPolicy;
import android.net.NetworkStatsHistory;
import android.net.NetworkStatsHistory.Entry;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.format.Formatter.BytesResult;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import com.android.settings.Utils;
import com.android.settingslib.graph.UsageView;

public class ChartDataUsagePreference extends Preference {
    private long mEnd;
    private final int mLimitColor = -765666;
    private NetworkStatsHistory mNetwork;
    private NetworkPolicy mPolicy;
    private int mSecondaryColor;
    private int mSeriesColor;
    private long mStart;
    private final int mWarningColor;

    public ChartDataUsagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSelectable(false);
        this.mWarningColor = context.getTheme().obtainStyledAttributes(new int[]{16842808}).getColor(0, 0);
        setLayoutResource(2130968726);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        UsageView chart = (UsageView) holder.findViewById(2131886464);
        if (this.mNetwork != null) {
            int top = getTop();
            chart.clearPaths();
            chart.configureGraph(toInt(this.mEnd - this.mStart), top, false, false);
            calcPoints(chart);
            chart.setBottomLabels(new CharSequence[]{Utils.formatDateRange(getContext(), this.mStart, this.mStart), Utils.formatDateRange(getContext(), this.mEnd, this.mEnd)});
            bindNetworkPolicy(chart, this.mPolicy, top);
        }
    }

    public int getTop() {
        Entry entry = null;
        long totalData = 0;
        int start = this.mNetwork.getIndexBefore(this.mStart);
        int end = this.mNetwork.getIndexAfter(this.mEnd);
        for (int i = start; i <= end; i++) {
            entry = this.mNetwork.getValues(i, entry);
            totalData += entry.rxBytes + entry.txBytes;
        }
        return (int) (Math.max(totalData, this.mPolicy != null ? Math.max(this.mPolicy.limitBytes, this.mPolicy.warningBytes) : 0) / 524288);
    }

    private void calcPoints(UsageView chart) {
        SparseIntArray points = new SparseIntArray();
        Entry entry = null;
        long totalData = 0;
        int start = this.mNetwork.getIndexAfter(this.mStart);
        int end = this.mNetwork.getIndexAfter(this.mEnd);
        if (start >= 0) {
            points.put(0, 0);
            for (int i = start; i <= end; i++) {
                entry = this.mNetwork.getValues(i, entry);
                long startTime = entry.bucketStart;
                long endTime = startTime + entry.bucketDuration;
                totalData += entry.rxBytes + entry.txBytes;
                points.put(toInt((startTime - this.mStart) + 1), (int) (totalData / 524288));
                points.put(toInt(endTime - this.mStart), (int) (totalData / 524288));
            }
            if (points.size() > 1) {
                chart.addPath(points);
            }
        }
    }

    private int toInt(long l) {
        return (int) (l / 60000);
    }

    private void bindNetworkPolicy(UsageView chart, NetworkPolicy policy, int top) {
        CharSequence[] labels = new CharSequence[3];
        int middleVisibility = 0;
        int topVisibility = 0;
        if (policy != null) {
            if (policy.limitBytes != -1) {
                topVisibility = this.mLimitColor;
                labels[2] = getLabel(policy.limitBytes, 2131626346, this.mLimitColor);
            }
            if (policy.warningBytes != -1) {
                chart.setDividerLoc((int) (policy.warningBytes / 524288));
                float weight = ((float) (policy.warningBytes / 524288)) / ((float) top);
                chart.setSideLabelWeights(1.0f - weight, weight);
                middleVisibility = this.mWarningColor;
                labels[1] = getLabel(policy.warningBytes, 2131626345, this.mWarningColor);
            }
            chart.setSideLabels(labels);
            chart.setDividerColors(middleVisibility, topVisibility);
        }
    }

    private CharSequence getLabel(long bytes, int str, int mLimitColor) {
        BytesResult result = Formatter.formatBytes(getContext().getResources(), bytes, 1);
        return new SpannableStringBuilder().append(TextUtils.expandTemplate(getContext().getText(str), new CharSequence[]{result.value, result.units}), new ForegroundColorSpan(mLimitColor), 0);
    }

    public void setNetworkPolicy(NetworkPolicy policy) {
        this.mPolicy = policy;
        notifyChanged();
    }

    public void setVisibleRange(long start, long end) {
        this.mStart = start;
        this.mEnd = end;
        notifyChanged();
    }

    public long getInspectStart() {
        return this.mStart;
    }

    public long getInspectEnd() {
        return this.mEnd;
    }

    public void setNetworkStats(NetworkStatsHistory network) {
        this.mNetwork = network;
        notifyChanged();
    }

    public void setColors(int seriesColor, int secondaryColor) {
        this.mSeriesColor = seriesColor;
        this.mSecondaryColor = secondaryColor;
        notifyChanged();
    }
}
