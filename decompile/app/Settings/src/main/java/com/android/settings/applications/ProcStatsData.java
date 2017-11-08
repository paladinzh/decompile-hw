package com.android.settings.applications;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.app.ProcessMap;
import com.android.internal.app.procstats.DumpUtils;
import com.android.internal.app.procstats.IProcessStats;
import com.android.internal.app.procstats.IProcessStats.Stub;
import com.android.internal.app.procstats.ProcessState;
import com.android.internal.app.procstats.ProcessStats;
import com.android.internal.app.procstats.ProcessStats.PackageState;
import com.android.internal.app.procstats.ProcessStats.ProcessDataCollection;
import com.android.internal.app.procstats.ProcessStats.TotalMemoryUseCollection;
import com.android.internal.app.procstats.ServiceState;
import com.android.internal.util.MemInfoReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProcStatsData {
    static final Comparator<ProcStatsEntry> sEntryCompare = new Comparator<ProcStatsEntry>() {
        public int compare(ProcStatsEntry lhs, ProcStatsEntry rhs) {
            if (lhs.mRunWeight < rhs.mRunWeight) {
                return 1;
            }
            if (lhs.mRunWeight > rhs.mRunWeight) {
                return -1;
            }
            if (lhs.mRunDuration < rhs.mRunDuration) {
                return 1;
            }
            if (lhs.mRunDuration > rhs.mRunDuration) {
                return -1;
            }
            return 0;
        }
    };
    private static ProcessStats sStatsXfer;
    private Context mContext;
    private long mDuration;
    private MemInfo mMemInfo;
    private int[] mMemStates = ProcessStats.ALL_MEM_ADJ;
    private PackageManager mPm;
    private IProcessStats mProcessStats = Stub.asInterface(ServiceManager.getService("procstats"));
    private int[] mStates = ProcessStats.BACKGROUND_PROC_STATES;
    private ProcessStats mStats;
    private boolean mUseUss;
    private long memTotalTime;
    private ArrayList<ProcStatsPackageEntry> pkgEntries;

    public static class MemInfo {
        long baseCacheRam;
        double freeWeight;
        double[] mMemStateWeights;
        long memTotalTime;
        public double realFreeRam;
        public double realTotalRam;
        public double realUsedRam;
        double totalRam;
        double totalScale;
        double usedWeight;
        double weightToRam;

        private MemInfo(Context context, TotalMemoryUseCollection totalMem, long memTotalTime) {
            this.mMemStateWeights = new double[14];
            this.memTotalTime = memTotalTime;
            calculateWeightInfo(context, totalMem, memTotalTime);
            double usedRam = (this.usedWeight * 1024.0d) / ((double) memTotalTime);
            double freeRam = (this.freeWeight * 1024.0d) / ((double) memTotalTime);
            this.totalRam = usedRam + freeRam;
            this.totalScale = this.realTotalRam / this.totalRam;
            this.weightToRam = (this.totalScale / ((double) memTotalTime)) * 1024.0d;
            this.realUsedRam = this.totalScale * usedRam;
            this.realFreeRam = this.totalScale * freeRam;
            MemoryInfo memInfo = new MemoryInfo();
            ((ActivityManager) context.getSystemService("activity")).getMemoryInfo(memInfo);
            if (((double) memInfo.hiddenAppThreshold) >= this.realFreeRam) {
                this.realUsedRam = freeRam;
                this.realFreeRam = 0.0d;
                this.baseCacheRam = (long) this.realFreeRam;
                return;
            }
            this.realUsedRam += (double) memInfo.hiddenAppThreshold;
            this.realFreeRam -= (double) memInfo.hiddenAppThreshold;
            this.baseCacheRam = memInfo.hiddenAppThreshold;
        }

        private void calculateWeightInfo(Context context, TotalMemoryUseCollection totalMem, long memTotalTime) {
            MemInfoReader memReader = new MemInfoReader();
            memReader.readMemInfo();
            this.realTotalRam = (double) memReader.getTotalSize();
            this.freeWeight = totalMem.sysMemFreeWeight + totalMem.sysMemCachedWeight;
            this.usedWeight = totalMem.sysMemKernelWeight + totalMem.sysMemNativeWeight;
            if (!totalMem.hasSwappedOutPss) {
                this.usedWeight += totalMem.sysMemZRamWeight;
            }
            for (int i = 0; i < 14; i++) {
                if (i == 7) {
                    this.mMemStateWeights[i] = 0.0d;
                } else {
                    this.mMemStateWeights[i] = totalMem.processStateWeight[i];
                    if (i >= 9) {
                        this.freeWeight += totalMem.processStateWeight[i];
                    } else {
                        this.usedWeight += totalMem.processStateWeight[i];
                    }
                }
            }
        }
    }

    public ProcStatsData(Context context, boolean useXfer) {
        this.mContext = context;
        this.mPm = context.getPackageManager();
        if (useXfer) {
            this.mStats = sStatsXfer;
        }
    }

    public void xferStats() {
        sStatsXfer = this.mStats;
    }

    public int getMemState() {
        int factor = this.mStats.mMemFactor;
        if (factor == -1) {
            return 0;
        }
        if (factor >= 4) {
            factor -= 4;
        }
        return factor;
    }

    public MemInfo getMemInfo() {
        if (this.mMemInfo == null) {
            refreshStats(true);
        }
        return this.mMemInfo;
    }

    public void setDuration(long duration) {
        if (duration != this.mDuration) {
            this.mDuration = duration;
            refreshStats(true);
        }
    }

    public long getDuration() {
        return this.mDuration;
    }

    public List<ProcStatsPackageEntry> getEntries() {
        return this.pkgEntries;
    }

    public void refreshStats(boolean forceLoad) {
        if (this.mStats == null || forceLoad) {
            load();
        }
        this.pkgEntries = new ArrayList();
        long now = SystemClock.uptimeMillis();
        this.memTotalTime = DumpUtils.dumpSingleTime(null, null, this.mStats.mMemFactorDurations, this.mStats.mMemFactor, this.mStats.mStartTime, now);
        TotalMemoryUseCollection totalMem = new TotalMemoryUseCollection(ProcessStats.ALL_SCREEN_ADJ, this.mMemStates);
        this.mStats.computeTotalMemoryUse(totalMem, now);
        this.mMemInfo = new MemInfo(this.mContext, totalMem, this.memTotalTime);
        ProcessDataCollection bgTotals = new ProcessDataCollection(ProcessStats.ALL_SCREEN_ADJ, this.mMemStates, this.mStates);
        ProcessDataCollection runTotals = new ProcessDataCollection(ProcessStats.ALL_SCREEN_ADJ, this.mMemStates, ProcessStats.NON_CACHED_PROC_STATES);
        createPkgMap(getProcs(bgTotals, runTotals), bgTotals, runTotals);
        if (totalMem.sysMemZRamWeight > 0.0d && !totalMem.hasSwappedOutPss) {
            distributeZRam(totalMem.sysMemZRamWeight);
        }
        this.pkgEntries.add(createOsEntry(bgTotals, runTotals, totalMem, this.mMemInfo.baseCacheRam));
    }

    private void createPkgMap(ArrayList<ProcStatsEntry> procEntries, ProcessDataCollection bgTotals, ProcessDataCollection runTotals) {
        ArrayMap<String, ProcStatsPackageEntry> pkgMap = new ArrayMap();
        for (int i = procEntries.size() - 1; i >= 0; i--) {
            ProcStatsEntry proc = (ProcStatsEntry) procEntries.get(i);
            proc.evaluateTargetPackage(this.mPm, this.mStats, bgTotals, runTotals, sEntryCompare, this.mUseUss);
            ProcStatsPackageEntry pkg = (ProcStatsPackageEntry) pkgMap.get(proc.mBestTargetPackage);
            if (pkg == null) {
                pkg = new ProcStatsPackageEntry(proc.mBestTargetPackage, this.memTotalTime);
                pkgMap.put(proc.mBestTargetPackage, pkg);
                this.pkgEntries.add(pkg);
            }
            pkg.addEntry(proc);
        }
    }

    private void distributeZRam(double zramWeight) {
        int i;
        long zramMem = (long) (zramWeight / ((double) this.memTotalTime));
        long totalTime = 0;
        for (i = this.pkgEntries.size() - 1; i >= 0; i--) {
            int j;
            ProcStatsPackageEntry entry = (ProcStatsPackageEntry) this.pkgEntries.get(i);
            for (j = entry.mEntries.size() - 1; j >= 0; j--) {
                totalTime += ((ProcStatsEntry) entry.mEntries.get(j)).mRunDuration;
            }
        }
        for (i = this.pkgEntries.size() - 1; i >= 0 && totalTime > 0; i--) {
            entry = (ProcStatsPackageEntry) this.pkgEntries.get(i);
            long pkgRunTime = 0;
            long maxRunTime = 0;
            for (j = entry.mEntries.size() - 1; j >= 0; j--) {
                ProcStatsEntry proc = (ProcStatsEntry) entry.mEntries.get(j);
                pkgRunTime += proc.mRunDuration;
                if (proc.mRunDuration > maxRunTime) {
                    maxRunTime = proc.mRunDuration;
                }
            }
            long pkgZRam = (zramMem * pkgRunTime) / totalTime;
            if (pkgZRam > 0) {
                zramMem -= pkgZRam;
                totalTime -= pkgRunTime;
                ProcStatsEntry procEntry = new ProcStatsEntry(entry.mPackage, 0, this.mContext.getString(2131626056), maxRunTime, pkgZRam, this.memTotalTime);
                procEntry.evaluateTargetPackage(this.mPm, this.mStats, null, null, sEntryCompare, this.mUseUss);
                entry.addEntry(procEntry);
            }
        }
    }

    private ProcStatsPackageEntry createOsEntry(ProcessDataCollection bgTotals, ProcessDataCollection runTotals, TotalMemoryUseCollection totalMem, long baseCacheRam) {
        ProcStatsPackageEntry osPkg = new ProcStatsPackageEntry("os", this.memTotalTime);
        if (totalMem.sysMemNativeWeight > 0.0d) {
            ProcStatsEntry osEntry = new ProcStatsEntry("os", 0, this.mContext.getString(2131626054), this.memTotalTime, (long) (totalMem.sysMemNativeWeight / ((double) this.memTotalTime)), this.memTotalTime);
            osEntry.evaluateTargetPackage(this.mPm, this.mStats, bgTotals, runTotals, sEntryCompare, this.mUseUss);
            osPkg.addEntry(osEntry);
        }
        if (totalMem.sysMemKernelWeight > 0.0d) {
            osEntry = new ProcStatsEntry("os", 0, this.mContext.getString(2131626055), this.memTotalTime, (long) (totalMem.sysMemKernelWeight / ((double) this.memTotalTime)), this.memTotalTime);
            osEntry.evaluateTargetPackage(this.mPm, this.mStats, bgTotals, runTotals, sEntryCompare, this.mUseUss);
            osPkg.addEntry(osEntry);
        }
        if (baseCacheRam > 0) {
            osEntry = new ProcStatsEntry("os", 0, this.mContext.getString(2131626057), this.memTotalTime, baseCacheRam / 1024, this.memTotalTime);
            osEntry.evaluateTargetPackage(this.mPm, this.mStats, bgTotals, runTotals, sEntryCompare, this.mUseUss);
            osPkg.addEntry(osEntry);
        }
        return osPkg;
    }

    private ArrayList<ProcStatsEntry> getProcs(ProcessDataCollection bgTotals, ProcessDataCollection runTotals) {
        int iu;
        int iv;
        ArrayList<ProcStatsEntry> procEntries = new ArrayList();
        ProcessMap<ProcStatsEntry> entriesMap = new ProcessMap();
        int N = this.mStats.mPackages.getMap().size();
        for (int ipkg = 0; ipkg < N; ipkg++) {
            SparseArray<SparseArray<PackageState>> pkgUids = (SparseArray) this.mStats.mPackages.getMap().valueAt(ipkg);
            for (iu = 0; iu < pkgUids.size(); iu++) {
                SparseArray<PackageState> vpkgs = (SparseArray) pkgUids.valueAt(iu);
                for (iv = 0; iv < vpkgs.size(); iv++) {
                    PackageState st = (PackageState) vpkgs.valueAt(iv);
                    for (int iproc = 0; iproc < st.mProcesses.size(); iproc++) {
                        ProcStatsEntry ent;
                        ProcessState pkgProc = (ProcessState) st.mProcesses.valueAt(iproc);
                        ProcessState proc = (ProcessState) this.mStats.mProcesses.get(pkgProc.getName(), pkgProc.getUid());
                        if (proc == null) {
                            Log.w("ProcStatsManager", "No process found for pkg " + st.mPackageName + "/" + st.mUid + " proc name " + pkgProc.getName());
                        } else {
                            ent = (ProcStatsEntry) entriesMap.get(proc.getName(), proc.getUid());
                            if (ent == null) {
                                ent = new ProcStatsEntry(proc, st.mPackageName, bgTotals, runTotals, this.mUseUss);
                                if (ent.mRunWeight > 0.0d) {
                                    entriesMap.put(proc.getName(), proc.getUid(), ent);
                                    procEntries.add(ent);
                                }
                            } else {
                                ent.addPackage(st.mPackageName);
                            }
                        }
                    }
                }
            }
        }
        N = this.mStats.mPackages.getMap().size();
        for (int ip = 0; ip < N; ip++) {
            SparseArray<SparseArray<PackageState>> uids = (SparseArray) this.mStats.mPackages.getMap().valueAt(ip);
            for (iu = 0; iu < uids.size(); iu++) {
                vpkgs = (SparseArray) uids.valueAt(iu);
                for (iv = 0; iv < vpkgs.size(); iv++) {
                    PackageState ps = (PackageState) vpkgs.valueAt(iv);
                    int NS = ps.mServices.size();
                    for (int is = 0; is < NS; is++) {
                        ServiceState ss = (ServiceState) ps.mServices.valueAt(is);
                        if (ss.getProcessName() != null) {
                            ent = (ProcStatsEntry) entriesMap.get(ss.getProcessName(), uids.keyAt(iu));
                            if (ent != null) {
                                ent.addService(ss);
                            } else {
                                Log.w("ProcStatsManager", "No process " + ss.getProcessName() + "/" + uids.keyAt(iu) + " for service " + ss.getName());
                            }
                        }
                    }
                }
            }
        }
        return procEntries;
    }

    private void load() {
        try {
            ParcelFileDescriptor pfd = this.mProcessStats.getStatsOverTime(this.mDuration);
            this.mStats = new ProcessStats(false);
            InputStream is = new AutoCloseInputStream(pfd);
            this.mStats.read(is);
            try {
                is.close();
            } catch (IOException e) {
            }
            if (this.mStats.mReadError != null) {
                Log.w("ProcStatsManager", "Failure reading process stats: " + this.mStats.mReadError);
            }
        } catch (RemoteException e2) {
            Log.e("ProcStatsManager", "RemoteException:", e2);
        }
    }
}
