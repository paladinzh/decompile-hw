package com.android.settings.applications;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.format.Formatter.BytesResult;
import com.android.settings.ItemUseStat;
import com.android.settings.ProgressBarPreference;
import com.android.settings.applications.ProcStatsData.MemInfo;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settingslib.Utils;

public class ProcessStatsSummary extends ProcessStatsBase implements OnPreferenceClickListener {
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private Preference mAppListPreference;
    private Preference mAverageUsed;
    private Preference mFree;
    private ProgressBarPreference mHeader;
    private Preference mPerformance;
    private Preference mTotalMemory;

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                ProcStatsData statsManager = new ProcStatsData(this.mContext, false);
                statsManager.setDuration(ProcessStatsSummary.sDurations[0]);
                MemInfo memInfo = statsManager.getMemInfo();
                String usedResult = ProcessStatsSummary.formatFileSize(this.mContext, memInfo.realUsedRam);
                String totalResult = ProcessStatsSummary.formatFileSize(this.mContext, memInfo.realTotalRam);
                this.mSummaryLoader.setSummary(this, this.mContext.getString(2131627099, new Object[]{usedResult, totalResult}));
            }
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230847);
        this.mHeader = new ProgressBarPreference(getActivity());
        getPreferenceScreen().addPreference(this.mHeader);
        this.mHeader.setOrder(-1);
        this.mPerformance = findPreference("performance");
        this.mTotalMemory = findPreference("total_memory");
        this.mAverageUsed = findPreference("average_used");
        this.mFree = findPreference("free");
        this.mAppListPreference = findPreference("apps_list");
        this.mAppListPreference.setOnPreferenceClickListener(this);
    }

    public void refreshUi() {
        Context context = getContext();
        if (context != null) {
            CharSequence memString;
            MemInfo memInfo = this.mStatsManager.getMemInfo();
            double usedRam = memInfo.realUsedRam;
            double totalRam = memInfo.realTotalRam;
            double freeRam = memInfo.realFreeRam;
            BytesResult usedResult = Formatter.formatBytes(context.getResources(), (long) usedRam, 0);
            CharSequence totalString = formatFileSize(context, totalRam);
            CharSequence freeString = formatFileSize(context, freeRam);
            CharSequence[] memStatesStr = getResources().getTextArray(2131361926);
            int memState = this.mStatsManager.getMemState();
            if (memState < 0 || memState >= memStatesStr.length - 1) {
                memString = memStatesStr[memStatesStr.length - 1];
            } else {
                memString = memStatesStr[memState];
            }
            int usedRatio = (int) ((usedRam / (freeRam + usedRam)) * 100.0d);
            CharSequence str1 = TextUtils.expandTemplate(getText(2131625307), new CharSequence[]{usedResult.value, usedResult.units});
            this.mHeader.setTitle(getString(2131627012, new Object[]{str1.toString()}));
            this.mHeader.setPercent(usedRatio);
            this.mPerformance.setSummary(memString);
            this.mTotalMemory.setSummary(totalString);
            this.mAverageUsed.setSummary(Utils.formatPercentage((long) usedRam, (long) totalRam));
            this.mFree.setSummary(freeString);
            String durationString = getString(this.sDurationLabels_ex[this.mDurationIndex], new Object[]{Integer.valueOf(this.DURATION_COUNT[this.mDurationIndex])});
            int numApps = this.mStatsManager.getEntries().size();
            this.mAppListPreference.setSummary(getResources().getQuantityString(2131689502, numApps, new Object[]{Integer.valueOf(numApps), durationString}));
        }
    }

    public static String formatFileSize(Context context, double ram) {
        if (context == null) {
            return "";
        }
        return Formatter.formatFileSize(context, (long) ram);
    }

    protected int getMetricsCategory() {
        return 202;
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference != this.mAppListPreference) {
            return false;
        }
        Intent intent = new Intent("com.android.settings.PROCESS_STATS_SUMMARY");
        intent.putExtra("fragment_stats", true);
        intent.putExtra("fragment_index", this.mDurationIndex);
        startActivity(intent);
        ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        return true;
    }
}
