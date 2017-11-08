package com.huawei.powergenie.modules.apppower.restrict.cleanup;

import android.os.FileUtils;
import android.os.Process;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.SystemClock;
import android.system.OsConstants;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import libcore.io.IoUtils;
import libcore.io.Libcore;

public class ProcessCpuTracker {
    private static final int[] LOAD_AVERAGE_FORMAT = new int[]{16416, 16416, 16416};
    private static final int[] PROCESS_FULL_STATS_FORMAT = new int[]{32, 4640, 32, 32, 32, 32, 32, 32, 32, 8224, 32, 8224, 32, 8224, 8224, 32, 32, 32, 32, 32, 32, 32, 8224};
    private static final int[] PROCESS_STATS_FORMAT = new int[]{32, 544, 32, 32, 32, 32, 32, 32, 32, 8224, 32, 8224, 32, 8224, 8224};
    private static final int[] SYSTEM_CPU_FORMAT = new int[]{288, 8224, 8224, 8224, 8224, 8224, 8224, 8224};
    private static final Comparator<Stats> sLoadComparator = new Comparator<Stats>() {
        public final int compare(Stats sta, Stats stb) {
            int i = -1;
            int ta = sta.rel_utime + sta.rel_stime;
            int tb = stb.rel_utime + stb.rel_stime;
            if (ta != tb) {
                if (ta <= tb) {
                    i = 1;
                }
                return i;
            } else if (sta.added != stb.added) {
                if (!sta.added) {
                    i = 1;
                }
                return i;
            } else if (sta.removed == stb.removed) {
                return 0;
            } else {
                if (!sta.added) {
                    i = 1;
                }
                return i;
            }
        }
    };
    private long mBaseIdleTime;
    private long mBaseIoWaitTime;
    private long mBaseIrqTime;
    private long mBaseSoftIrqTime;
    private long mBaseSystemTime;
    private long mBaseUserTime;
    private byte[] mBuffer = new byte[4096];
    private int[] mCurPids;
    private int[] mCurThreadPids;
    private long mCurrentSampleRealTime;
    private long mCurrentSampleTime;
    private long mCurrentSampleWallTime;
    protected boolean mFirst = true;
    private final boolean mIncludeThreads;
    private final long mJiffyMillis;
    private long mLastSampleRealTime;
    private long mLastSampleTime;
    private long mLastSampleWallTime;
    private float mLoad1 = 0.0f;
    private float mLoad15 = 0.0f;
    private float mLoad5 = 0.0f;
    private final float[] mLoadAverageData = new float[3];
    private final ArrayList<Stats> mProcStats = new ArrayList();
    private final long[] mProcessFullStatsData = new long[6];
    private final String[] mProcessFullStatsStringData = new String[6];
    private final long[] mProcessStatsData = new long[4];
    private int mRelIdleTime;
    private int mRelIoWaitTime;
    private int mRelIrqTime;
    private int mRelSoftIrqTime;
    private boolean mRelStatsAreGood;
    private int mRelSystemTime;
    private int mRelUserTime;
    private final long[] mSinglePidStatsData = new long[4];
    private final long[] mSystemCpuData = new long[7];
    private final ArrayList<Stats> mWorkingProcs = new ArrayList();
    private boolean mWorkingProcsSorted;

    public static class ProcLoad {
        private int mPid;
        private int mProcLoad;
        private String mProcName;

        public ProcLoad(int pid, String procName, int procLoad) {
            this.mPid = pid;
            this.mProcName = procName;
            this.mProcLoad = procLoad;
        }

        public String getProcName() {
            return this.mProcName;
        }

        public int getProcLoad() {
            return this.mProcLoad;
        }

        public String toString() {
            return "pid = " + this.mPid + ", mProcName = " + this.mProcName + ", mProcLoad = " + this.mProcLoad;
        }
    }

    public static class Stats {
        public boolean active;
        public boolean added;
        public String baseName;
        public long base_majfaults;
        public long base_minfaults;
        public long base_stime;
        public long base_uptime;
        public long base_utime;
        final String cmdlineFile;
        public boolean interesting;
        public String name;
        public int nameWidth;
        public final int pid;
        public int rel_majfaults;
        public int rel_minfaults;
        public int rel_stime;
        public long rel_uptime;
        public int rel_utime;
        public boolean removed;
        final String statFile;
        final ArrayList<Stats> threadStats;
        final String threadsDir;
        public final int uid;
        public long vsize;
        public boolean working;
        final ArrayList<Stats> workingThreads;

        Stats(int _pid, int parentPid, boolean includeThreads) {
            this.pid = _pid;
            if (parentPid < 0) {
                File procDir = new File("/proc", Integer.toString(this.pid));
                this.statFile = new File(procDir, "stat").toString();
                this.cmdlineFile = new File(procDir, "cmdline").toString();
                this.threadsDir = new File(procDir, "task").toString();
                if (includeThreads) {
                    this.threadStats = new ArrayList();
                    this.workingThreads = new ArrayList();
                } else {
                    this.threadStats = null;
                    this.workingThreads = null;
                }
            } else {
                this.statFile = new File(new File(new File(new File("/proc", Integer.toString(parentPid)), "task"), Integer.toString(this.pid)), "stat").toString();
                this.cmdlineFile = null;
                this.threadsDir = null;
                this.threadStats = null;
                this.workingThreads = null;
            }
            this.uid = FileUtils.getUid(this.statFile.toString());
        }
    }

    public ProcessCpuTracker(boolean includeThreads) {
        this.mIncludeThreads = includeThreads;
        this.mJiffyMillis = 1000 / Libcore.os.sysconf(OsConstants._SC_CLK_TCK);
    }

    public void onLoadChanged(float load1, float load5, float load15) {
    }

    public int onMeasureProcessName(String name) {
        return 0;
    }

    public void init() {
        this.mFirst = true;
        update();
    }

    public void update() {
        long nowUptime = SystemClock.uptimeMillis();
        long nowRealtime = SystemClock.elapsedRealtime();
        long nowWallTime = System.currentTimeMillis();
        long[] sysCpu = this.mSystemCpuData;
        if (Process.readProcFile("/proc/stat", SYSTEM_CPU_FORMAT, null, sysCpu, null)) {
            long usertime = (sysCpu[0] + sysCpu[1]) * this.mJiffyMillis;
            long systemtime = sysCpu[2] * this.mJiffyMillis;
            long idletime = sysCpu[3] * this.mJiffyMillis;
            long iowaittime = sysCpu[4] * this.mJiffyMillis;
            long irqtime = sysCpu[5] * this.mJiffyMillis;
            long softirqtime = sysCpu[6] * this.mJiffyMillis;
            this.mRelUserTime = (int) (usertime - this.mBaseUserTime);
            this.mRelSystemTime = (int) (systemtime - this.mBaseSystemTime);
            this.mRelIoWaitTime = (int) (iowaittime - this.mBaseIoWaitTime);
            this.mRelIrqTime = (int) (irqtime - this.mBaseIrqTime);
            this.mRelSoftIrqTime = (int) (softirqtime - this.mBaseSoftIrqTime);
            this.mRelIdleTime = (int) (idletime - this.mBaseIdleTime);
            this.mRelStatsAreGood = true;
            this.mBaseUserTime = usertime;
            this.mBaseSystemTime = systemtime;
            this.mBaseIoWaitTime = iowaittime;
            this.mBaseIrqTime = irqtime;
            this.mBaseSoftIrqTime = softirqtime;
            this.mBaseIdleTime = idletime;
        }
        this.mLastSampleTime = this.mCurrentSampleTime;
        this.mCurrentSampleTime = nowUptime;
        this.mLastSampleRealTime = this.mCurrentSampleRealTime;
        this.mCurrentSampleRealTime = nowRealtime;
        this.mLastSampleWallTime = this.mCurrentSampleWallTime;
        this.mCurrentSampleWallTime = nowWallTime;
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        try {
            this.mCurPids = collectStats("/proc", -1, this.mFirst, this.mCurPids, this.mProcStats);
            float[] loadAverages = this.mLoadAverageData;
            if (Process.readProcFile("/proc/loadavg", LOAD_AVERAGE_FORMAT, null, null, loadAverages)) {
                float load1 = loadAverages[0];
                float load5 = loadAverages[1];
                float load15 = loadAverages[2];
                if (load1 == this.mLoad1 && load5 == this.mLoad5) {
                    if (load15 != this.mLoad15) {
                    }
                }
                this.mLoad1 = load1;
                this.mLoad5 = load5;
                this.mLoad15 = load15;
                onLoadChanged(load1, load5, load15);
            }
            this.mWorkingProcsSorted = false;
            this.mFirst = false;
        } finally {
            StrictMode.setThreadPolicy(savedPolicy);
        }
    }

    private int[] collectStats(String statsFile, int parentPid, boolean first, int[] curPids, ArrayList<Stats> allProcs) {
        int[] pids = Process.getPids(statsFile, curPids);
        int NP = pids == null ? 0 : pids.length;
        int NS = allProcs.size();
        int curStatsIndex = 0;
        int i = 0;
        while (i < NP) {
            int pid = pids[i];
            if (pid < 0) {
                NP = pid;
                break;
            }
            Stats st = curStatsIndex < NS ? (Stats) allProcs.get(curStatsIndex) : null;
            long[] procStats;
            if (st != null && st.pid == pid) {
                st.added = false;
                st.working = false;
                curStatsIndex++;
                if (st.interesting) {
                    long uptime = SystemClock.uptimeMillis();
                    procStats = this.mProcessStatsData;
                    if (Process.readProcFile(st.statFile.toString(), PROCESS_STATS_FORMAT, null, procStats, null)) {
                        long minfaults = procStats[0];
                        long majfaults = procStats[1];
                        long utime = procStats[2] * this.mJiffyMillis;
                        long stime = procStats[3] * this.mJiffyMillis;
                        if (utime == st.base_utime && stime == st.base_stime) {
                            st.rel_utime = 0;
                            st.rel_stime = 0;
                            st.rel_minfaults = 0;
                            st.rel_majfaults = 0;
                            if (st.active) {
                                st.active = false;
                            }
                        } else {
                            if (!st.active) {
                                st.active = true;
                            }
                            if (parentPid < 0) {
                                getName(st, st.cmdlineFile);
                                if (st.threadStats != null) {
                                    this.mCurThreadPids = collectStats(st.threadsDir, pid, false, this.mCurThreadPids, st.threadStats);
                                }
                            }
                            st.rel_uptime = uptime - st.base_uptime;
                            st.base_uptime = uptime;
                            st.rel_utime = (int) (utime - st.base_utime);
                            st.rel_stime = (int) (stime - st.base_stime);
                            st.base_utime = utime;
                            st.base_stime = stime;
                            st.rel_minfaults = (int) (minfaults - st.base_minfaults);
                            st.rel_majfaults = (int) (majfaults - st.base_majfaults);
                            st.base_minfaults = minfaults;
                            st.base_majfaults = majfaults;
                            st.working = true;
                        }
                    }
                }
            } else if (st == null || st.pid > pid) {
                Stats stats = new Stats(pid, parentPid, this.mIncludeThreads);
                allProcs.add(curStatsIndex, stats);
                curStatsIndex++;
                NS++;
                String[] procStatsString = this.mProcessFullStatsStringData;
                procStats = this.mProcessFullStatsData;
                stats.base_uptime = SystemClock.uptimeMillis();
                if (Process.readProcFile(stats.statFile.toString(), PROCESS_FULL_STATS_FORMAT, procStatsString, procStats, null)) {
                    stats.vsize = procStats[5];
                    stats.interesting = true;
                    stats.baseName = procStatsString[0];
                    stats.base_minfaults = procStats[1];
                    stats.base_majfaults = procStats[2];
                    stats.base_utime = procStats[3] * this.mJiffyMillis;
                    stats.base_stime = procStats[4] * this.mJiffyMillis;
                } else {
                    Log.w("ProcessCpuTracker", "Skipping unknown process pid " + pid);
                    stats.baseName = "<unknown>";
                    stats.base_stime = 0;
                    stats.base_utime = 0;
                    stats.base_majfaults = 0;
                    stats.base_minfaults = 0;
                }
                if (parentPid < 0) {
                    getName(stats, stats.cmdlineFile);
                    if (stats.threadStats != null) {
                        this.mCurThreadPids = collectStats(stats.threadsDir, pid, true, this.mCurThreadPids, stats.threadStats);
                    }
                } else if (stats.interesting) {
                    stats.name = stats.baseName;
                    stats.nameWidth = onMeasureProcessName(stats.name);
                }
                stats.rel_utime = 0;
                stats.rel_stime = 0;
                stats.rel_minfaults = 0;
                stats.rel_majfaults = 0;
                stats.added = true;
                if (!first && stats.interesting) {
                    stats.working = true;
                }
            } else {
                st.rel_utime = 0;
                st.rel_stime = 0;
                st.rel_minfaults = 0;
                st.rel_majfaults = 0;
                st.removed = true;
                st.working = true;
                allProcs.remove(curStatsIndex);
                NS--;
                i--;
            }
            i++;
        }
        while (curStatsIndex < NS) {
            st = (Stats) allProcs.get(curStatsIndex);
            st.rel_utime = 0;
            st.rel_stime = 0;
            st.rel_minfaults = 0;
            st.rel_majfaults = 0;
            st.removed = true;
            st.working = true;
            allProcs.remove(curStatsIndex);
            NS--;
        }
        return pids;
    }

    public final void printCurrentState(long now, ArrayList<ProcLoad> mRetProcList) {
        if (mRetProcList != null) {
            int N = this.mWorkingProcs.size();
            int i = 0;
            while (i < N) {
                Stats st = (Stats) this.mWorkingProcs.get(i);
                String str = st.added ? " +" : st.removed ? " -" : "  ";
                mRetProcList.add(printProcessCPU(str, st.pid, st.name, (int) st.rel_uptime, st.rel_utime, st.rel_stime, 0, 0, 0, st.rel_minfaults, st.rel_majfaults));
                if (mRetProcList.size() < 5) {
                    if (!(st.removed || st.workingThreads == null)) {
                        int M = st.workingThreads.size();
                        int j = 0;
                        while (j < M) {
                            Stats tst = (Stats) st.workingThreads.get(j);
                            str = tst.added ? "   +" : tst.removed ? "   -" : "    ";
                            mRetProcList.add(printProcessCPU(str, tst.pid, tst.name, (int) st.rel_uptime, tst.rel_utime, tst.rel_stime, 0, 0, 0, 0, 0));
                            if (mRetProcList.size() < 5) {
                                j++;
                            } else {
                                return;
                            }
                        }
                        continue;
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    private int printRatio(long numerator, long denominator) {
        return (int) (((1000 * numerator) / denominator) / 10);
    }

    private ProcLoad printProcessCPU(String prefix, int pid, String label, int totalTime, int user, int system, int iowait, int irq, int softIrq, int minFaults, int majFaults) {
        if (totalTime == 0) {
            totalTime = 1;
        }
        return new ProcLoad(pid, label, printRatio((long) ((((user + system) + iowait) + irq) + softIrq), (long) totalTime));
    }

    private String readFile(String file, char endChar) {
        Throwable th;
        ThreadPolicy savedPolicy = StrictMode.allowThreadDiskReads();
        FileInputStream is = null;
        try {
            FileInputStream is2 = new FileInputStream(file);
            try {
                int len = is2.read(this.mBuffer);
                is2.close();
                if (len > 0) {
                    int i = 0;
                    while (i < len && this.mBuffer[i] != endChar) {
                        i++;
                    }
                    String str = new String(this.mBuffer, 0, i);
                    IoUtils.closeQuietly(is2);
                    StrictMode.setThreadPolicy(savedPolicy);
                    return str;
                }
                IoUtils.closeQuietly(is2);
                StrictMode.setThreadPolicy(savedPolicy);
                is = is2;
                return null;
            } catch (FileNotFoundException e) {
                is = is2;
                IoUtils.closeQuietly(is);
                StrictMode.setThreadPolicy(savedPolicy);
                return null;
            } catch (IOException e2) {
                is = is2;
                IoUtils.closeQuietly(is);
                StrictMode.setThreadPolicy(savedPolicy);
                return null;
            } catch (Throwable th2) {
                th = th2;
                is = is2;
                IoUtils.closeQuietly(is);
                StrictMode.setThreadPolicy(savedPolicy);
                throw th;
            }
        } catch (FileNotFoundException e3) {
            IoUtils.closeQuietly(is);
            StrictMode.setThreadPolicy(savedPolicy);
            return null;
        } catch (IOException e4) {
            IoUtils.closeQuietly(is);
            StrictMode.setThreadPolicy(savedPolicy);
            return null;
        } catch (Throwable th3) {
            th = th3;
            IoUtils.closeQuietly(is);
            StrictMode.setThreadPolicy(savedPolicy);
            throw th;
        }
    }

    private void getName(Stats st, String cmdlineFile) {
        String newName = st.name;
        if (st.name == null || st.name.equals("app_process") || st.name.equals("<pre-initialized>")) {
            String cmdName = readFile(cmdlineFile, '\u0000');
            if (cmdName != null && cmdName.length() > 1) {
                newName = cmdName;
                int i = cmdName.lastIndexOf("/");
                if (i > 0 && i < cmdName.length() - 1) {
                    newName = cmdName.substring(i + 1);
                }
            }
            if (newName == null) {
                newName = st.baseName;
            }
        }
        if (st.name == null || !newName.equals(st.name)) {
            st.name = newName;
            st.nameWidth = onMeasureProcessName(st.name);
        }
    }
}
