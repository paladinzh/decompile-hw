package com.android.settings.applications;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.settingslib.Utils;
import java.util.ArrayList;

public class ProcStatsPackageEntry implements Parcelable {
    public static final Creator<ProcStatsPackageEntry> CREATOR = new Creator<ProcStatsPackageEntry>() {
        public ProcStatsPackageEntry createFromParcel(Parcel in) {
            return new ProcStatsPackageEntry(in);
        }

        public ProcStatsPackageEntry[] newArray(int size) {
            return new ProcStatsPackageEntry[size];
        }
    };
    private static boolean DEBUG = false;
    long mAvgBgMem;
    long mAvgRunMem;
    long mBgDuration;
    double mBgWeight;
    final ArrayList<ProcStatsEntry> mEntries = new ArrayList();
    long mMaxBgMem;
    long mMaxRunMem;
    final String mPackage;
    long mRunDuration;
    double mRunWeight;
    public String mUiLabel;
    public ApplicationInfo mUiTargetApp;
    private long mWindowLength;

    public ProcStatsPackageEntry(String pkg, long windowLength) {
        this.mPackage = pkg;
        this.mWindowLength = windowLength;
    }

    public ProcStatsPackageEntry(Parcel in) {
        this.mPackage = in.readString();
        in.readTypedList(this.mEntries, ProcStatsEntry.CREATOR);
        this.mBgDuration = in.readLong();
        this.mAvgBgMem = in.readLong();
        this.mMaxBgMem = in.readLong();
        this.mBgWeight = in.readDouble();
        this.mRunDuration = in.readLong();
        this.mAvgRunMem = in.readLong();
        this.mMaxRunMem = in.readLong();
        this.mRunWeight = in.readDouble();
    }

    public void addEntry(ProcStatsEntry entry) {
        this.mEntries.add(entry);
    }

    public void updateMetrics() {
        this.mMaxBgMem = 0;
        this.mAvgBgMem = 0;
        this.mBgDuration = 0;
        this.mBgWeight = 0.0d;
        this.mMaxRunMem = 0;
        this.mAvgRunMem = 0;
        this.mRunDuration = 0;
        this.mRunWeight = 0.0d;
        int N = this.mEntries.size();
        for (int i = 0; i < N; i++) {
            ProcStatsEntry entry = (ProcStatsEntry) this.mEntries.get(i);
            this.mBgDuration = Math.max(entry.mBgDuration, this.mBgDuration);
            this.mAvgBgMem += entry.mAvgBgMem;
            this.mBgWeight += entry.mBgWeight;
            this.mRunDuration = Math.max(entry.mRunDuration, this.mRunDuration);
            this.mAvgRunMem += entry.mAvgRunMem;
            this.mRunWeight += entry.mRunWeight;
            this.mMaxBgMem += entry.mMaxBgMem;
            this.mMaxRunMem += entry.mMaxRunMem;
        }
        if (N != 0) {
            this.mAvgBgMem /= (long) N;
            this.mAvgRunMem /= (long) N;
        }
    }

    public void retrieveUiData(Context context, PackageManager pm) {
        this.mUiTargetApp = null;
        this.mUiLabel = this.mPackage;
        try {
            if ("os".equals(this.mPackage)) {
                this.mUiTargetApp = pm.getApplicationInfo("android", 41472);
                this.mUiLabel = context.getString(2131626053);
                return;
            }
            this.mUiTargetApp = pm.getApplicationInfo(this.mPackage, 41472);
            this.mUiLabel = this.mUiTargetApp.loadLabel(pm).toString();
        } catch (NameNotFoundException e) {
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPackage);
        dest.writeTypedList(this.mEntries);
        dest.writeLong(this.mBgDuration);
        dest.writeLong(this.mAvgBgMem);
        dest.writeLong(this.mMaxBgMem);
        dest.writeDouble(this.mBgWeight);
        dest.writeLong(this.mRunDuration);
        dest.writeLong(this.mAvgRunMem);
        dest.writeLong(this.mMaxRunMem);
        dest.writeDouble(this.mRunWeight);
    }

    public static CharSequence getFrequency(float amount, Context context) {
        if (amount > 0.95f) {
            return context.getString(2131626965, new Object[]{Utils.formatPercentage((int) (100.0f * amount))});
        } else if (amount > 0.25f) {
            return context.getString(2131626966, new Object[]{Utils.formatPercentage((int) (100.0f * amount))});
        } else {
            return context.getString(2131626967, new Object[]{Utils.formatPercentage((int) (100.0f * amount))});
        }
    }
}
