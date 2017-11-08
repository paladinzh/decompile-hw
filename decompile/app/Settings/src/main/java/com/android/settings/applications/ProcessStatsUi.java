package com.android.settings.applications;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.util.Log;
import com.android.settings.applications.ProcStatsData.MemInfo;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProcessStatsUi extends ProcessStatsBase {
    public static final int[] BACKGROUND_AND_SYSTEM_PROC_STATES = new int[]{0, 2, 3, 4, 5, 6, 7, 8, 9};
    public static final int[] CACHED_PROC_STATES = new int[]{11, 12, 13};
    public static final int[] FOREGROUND_PROC_STATES = new int[]{1};
    static final Comparator<ProcStatsPackageEntry> sMaxPackageEntryCompare = new Comparator<ProcStatsPackageEntry>() {
        public int compare(ProcStatsPackageEntry lhs, ProcStatsPackageEntry rhs) {
            double rhsMax = (double) Math.max(rhs.mMaxBgMem, rhs.mMaxRunMem);
            double lhsMax = (double) Math.max(lhs.mMaxBgMem, lhs.mMaxRunMem);
            if (lhsMax == rhsMax) {
                return 0;
            }
            return lhsMax < rhsMax ? 1 : -1;
        }
    };
    static final Comparator<ProcStatsPackageEntry> sPackageEntryCompare = new Comparator<ProcStatsPackageEntry>() {
        public int compare(ProcStatsPackageEntry lhs, ProcStatsPackageEntry rhs) {
            double rhsWeight = Math.max(rhs.mRunWeight, rhs.mBgWeight);
            double lhsWeight = Math.max(lhs.mRunWeight, lhs.mBgWeight);
            if (lhsWeight == rhsWeight) {
                return 0;
            }
            return lhsWeight < rhsWeight ? 1 : -1;
        }
    };
    private PreferenceGroup mAppListGroup;
    private PackageManager mPm;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mPm = getActivity().getPackageManager();
        addPreferencesFromResource(2131230848);
    }

    protected int getMetricsCategory() {
        return 23;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (!(preference instanceof ProcessStatsPreference)) {
            return false;
        }
        startProcessStatsDetailFragment(this.mStatsManager.getMemInfo(), ((ProcessStatsPreference) preference).getEntry());
        return super.onPreferenceTreeClick(preference);
    }

    private void startProcessStatsDetailFragment(MemInfo memInfo, ProcStatsPackageEntry entry) {
        Bundle args = new Bundle();
        args.putParcelable("package_entry", entry);
        args.putDouble("weight_to_ram", memInfo.weightToRam);
        args.putLong("total_time", memInfo.memTotalTime);
        args.putDouble("max_memory_usage", memInfo.usedWeight * memInfo.weightToRam);
        args.putDouble("total_scale", memInfo.totalScale);
        Intent intent = new Intent("com.android.settings.PROCESS_STATS_DETAIL");
        intent.putExtras(args);
        startActivity(intent);
    }

    public void refreshUi() {
        this.mAppListGroup = getPreferenceScreen();
        this.mAppListGroup.removeAll();
        this.mAppListGroup.setOrderingAsAdded(false);
        Context context = getActivity();
        MemInfo memInfo = this.mStatsManager.getMemInfo();
        List<ProcStatsPackageEntry> pkgEntries = this.mStatsManager.getEntries();
        if (pkgEntries == null) {
            Log.e("ProcessStatsUi", "pkgEntries == null");
            return;
        }
        int i;
        double maxMemory;
        int N = pkgEntries.size();
        for (i = 0; i < N; i++) {
            ((ProcStatsPackageEntry) pkgEntries.get(i)).updateMetrics();
        }
        Collections.sort(pkgEntries, this.mShowMax ? sMaxPackageEntryCompare : sPackageEntryCompare);
        if (this.mShowMax) {
            maxMemory = memInfo.realTotalRam;
        } else {
            maxMemory = memInfo.usedWeight * memInfo.weightToRam;
        }
        for (i = 0; i < pkgEntries.size(); i++) {
            ProcStatsPackageEntry pkg = (ProcStatsPackageEntry) pkgEntries.get(i);
            if (pkg.mPackage != null) {
                ProcessStatsPreference pref = new ProcessStatsPreference(getPrefContext());
                pkg.retrieveUiData(context, this.mPm);
                pref.init(pkg, this.mPm, maxMemory, memInfo.weightToRam, memInfo.totalScale, !this.mShowMax);
                pref.setOrder(i);
                this.mAppListGroup.addPreference(pref);
            }
        }
    }
}
