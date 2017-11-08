package com.android.settings.applications;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.text.format.Formatter;
import com.android.settings.AppProgressPreference;

public class ProcessStatsPreference extends AppProgressPreference {
    private ProcStatsPackageEntry mEntry;

    public ProcessStatsPreference(Context context) {
        super(context, null);
    }

    public void init(ProcStatsPackageEntry entry, PackageManager pm, double maxMemory, double weightToRam, double totalScale, boolean avg) {
        double amount;
        this.mEntry = entry;
        setTitle(TextUtils.isEmpty(entry.mUiLabel) ? entry.mPackage : entry.mUiLabel);
        if (entry.mUiTargetApp != null) {
            setIcon(entry.mUiTargetApp.loadIcon(pm));
        } else {
            setIcon(new ColorDrawable(0));
        }
        boolean statsForeground = entry.mRunWeight > entry.mBgWeight;
        if (avg) {
            amount = (statsForeground ? entry.mRunWeight : entry.mBgWeight) * weightToRam;
        } else {
            amount = (((double) (statsForeground ? entry.mMaxRunMem : entry.mMaxBgMem)) * totalScale) * 1024.0d;
        }
        setSummary(Formatter.formatShortFileSize(getContext(), (long) amount));
        setProgress((int) ((100.0d * amount) / maxMemory));
    }

    public ProcStatsPackageEntry getEntry() {
        return this.mEntry;
    }
}
