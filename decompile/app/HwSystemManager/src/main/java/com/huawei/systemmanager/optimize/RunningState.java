package com.huawei.systemmanager.optimize;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.util.SparseArray;
import com.huawei.systemmanager.spacecleanner.engine.base.ITrashEngine.IUpdateListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class RunningState {
    private int MAX_SERVICES = 100;
    private ArrayList<ProcessItem> mAllProcessItems = new ArrayList();
    private final AtomicLong mBackgroundProcessMemory = new AtomicLong();
    private ArrayList<ProcessItem> mInterestingProcesses = new ArrayList();
    private ArrayList<ProcessItem> mProcessItems = new ArrayList();
    private SparseArray<ProcessItem> mRunningProcesses = new SparseArray();
    int mSequence = 0;
    private SparseArray<HashMap<String, ProcessItem>> mServiceProcessesByName = new SparseArray();
    private SparseArray<ProcessItem> mServiceProcessesByPid = new SparseArray();
    private SparseArray<AppProcessInfo> mTmpAppProcesses = new SparseArray();

    private static class AppProcessInfo {
        boolean hasForegroundServices;
        boolean hasServices;
        RunningAppProcessInfo info;

        AppProcessInfo(RunningAppProcessInfo _info) {
            this.info = _info;
        }
    }

    private static class ProcessItem {
        int mCurSeq;
        SparseArray<ProcessItem> mDependentProcesses = new SparseArray();
        int mPid;
        RunningAppProcessInfo mRunningProcessInfo;
        long mSize;

        public ProcessItem(Context context) {
        }

        boolean updateSize(long pss) {
            this.mSize = 1024 * pss;
            return false;
        }
    }

    private boolean isInterestingProcess(RunningAppProcessInfo pi) {
        if ((pi.flags & 1) != 0) {
            return true;
        }
        return (pi.flags & 2) == 0 && pi.importance >= 100 && pi.importance < 170 && pi.importanceReasonCode == 0;
    }

    public boolean update(Context context, ActivityManager am) {
        this.mSequence++;
        boolean changed = false;
        List<RunningServiceInfo> services = getRunningService(am);
        List<RunningAppProcessInfo> processes = getRunningProcess(am, services);
        if (dealService(context, services)) {
            changed = true;
        }
        if (dealRunningProcess(context, processes)) {
            changed = true;
        }
        return updateBackgroundProcessMemory(changed);
    }

    private List<RunningServiceInfo> getRunningService(ActivityManager am) {
        List<RunningServiceInfo> services = am.getRunningServices(this.MAX_SERVICES);
        if (services == null) {
            return Collections.EMPTY_LIST;
        }
        int NS = services.size();
        int i = 0;
        while (i < NS) {
            RunningServiceInfo si = (RunningServiceInfo) services.get(i);
            if (!si.started && si.clientLabel == 0) {
                services.remove(i);
                i--;
                NS--;
            } else if ((si.flags & 8) != 0) {
                services.remove(i);
                i--;
                NS--;
            }
            i++;
        }
        return services;
    }

    private List<RunningAppProcessInfo> getRunningProcess(ActivityManager am, List<RunningServiceInfo> services) {
        List<RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        if (processes == null) {
            return Collections.EMPTY_LIST;
        }
        int i;
        int NP = processes.size();
        this.mTmpAppProcesses.clear();
        for (i = 0; i < NP; i++) {
            RunningAppProcessInfo pi = (RunningAppProcessInfo) processes.get(i);
            this.mTmpAppProcesses.put(pi.pid, new AppProcessInfo(pi));
        }
        if (services == null) {
            return processes;
        }
        int NS = services.size();
        for (i = 0; i < NS; i++) {
            RunningServiceInfo si = (RunningServiceInfo) services.get(i);
            if (si.restarting == 0 && si.pid > 0) {
                AppProcessInfo ainfo = (AppProcessInfo) this.mTmpAppProcesses.get(si.pid);
                if (ainfo != null) {
                    ainfo.hasServices = true;
                    if (si.foreground) {
                        ainfo.hasForegroundServices = true;
                    }
                }
            }
        }
        return processes;
    }

    private boolean dealService(Context context, List<RunningServiceInfo> services) {
        boolean changed = false;
        int NS = services.size();
        for (int i = 0; i < NS; i++) {
            RunningServiceInfo si = (RunningServiceInfo) services.get(i);
            if (si.restarting == 0 && si.pid > 0) {
                AppProcessInfo ainfo = (AppProcessInfo) this.mTmpAppProcesses.get(si.pid);
                if (!(ainfo == null || ainfo.hasForegroundServices || ainfo.info.importance >= IUpdateListener.ERROR_CODE_NO_NETWORK)) {
                    boolean skip = false;
                    ainfo = (AppProcessInfo) this.mTmpAppProcesses.get(ainfo.info.importanceReasonPid);
                    while (ainfo != null) {
                        if (ainfo.hasServices || isInterestingProcess(ainfo.info)) {
                            skip = true;
                            break;
                        }
                        ainfo = (AppProcessInfo) this.mTmpAppProcesses.get(ainfo.info.importanceReasonPid);
                    }
                    if (skip) {
                    }
                }
            }
            HashMap<String, ProcessItem> procs = (HashMap) this.mServiceProcessesByName.get(si.uid);
            if (procs == null) {
                procs = new HashMap();
                this.mServiceProcessesByName.put(si.uid, procs);
            }
            ProcessItem proc = (ProcessItem) procs.get(si.process);
            if (proc == null) {
                changed = true;
                proc = new ProcessItem(context);
                procs.put(si.process, proc);
            }
            if (proc.mCurSeq != this.mSequence) {
                int pid = si.restarting == 0 ? si.pid : 0;
                if (pid != proc.mPid) {
                    changed = true;
                    if (proc.mPid != pid) {
                        if (proc.mPid != 0) {
                            this.mServiceProcessesByPid.remove(proc.mPid);
                        }
                        if (pid != 0) {
                            this.mServiceProcessesByPid.put(pid, proc);
                        }
                        proc.mPid = pid;
                    }
                }
                proc.mDependentProcesses.clear();
                proc.mCurSeq = this.mSequence;
            }
        }
        return changed;
    }

    private boolean dealRunningProcess(Context context, List<RunningAppProcessInfo> processes) {
        if (processes == null) {
            return false;
        }
        int i;
        int NP = processes.size();
        boolean changed = false;
        for (i = 0; i < NP; i++) {
            RunningAppProcessInfo pi = (RunningAppProcessInfo) processes.get(i);
            ProcessItem proc = (ProcessItem) this.mServiceProcessesByPid.get(pi.pid);
            if (proc == null) {
                proc = (ProcessItem) this.mRunningProcesses.get(pi.pid);
                if (proc == null) {
                    changed = true;
                    proc = new ProcessItem(context);
                    proc.mPid = pi.pid;
                    this.mRunningProcesses.put(pi.pid, proc);
                }
                proc.mDependentProcesses.clear();
            }
            if (isInterestingProcess(pi)) {
                if (!this.mInterestingProcesses.contains(proc)) {
                    changed = true;
                    this.mInterestingProcesses.add(proc);
                }
                proc.mCurSeq = this.mSequence;
            }
            proc.mRunningProcessInfo = pi;
        }
        this.mAllProcessItems.clear();
        this.mAllProcessItems.addAll(this.mProcessItems);
        int NRP = this.mRunningProcesses.size();
        for (i = 0; i < NRP; i++) {
            proc = (ProcessItem) this.mRunningProcesses.valueAt(i);
            if (proc.mCurSeq != this.mSequence) {
                if (proc.mRunningProcessInfo.importance >= 400) {
                    this.mAllProcessItems.add(proc);
                } else if (proc.mRunningProcessInfo.importance <= 200) {
                    this.mAllProcessItems.add(proc);
                }
            }
        }
        return changed;
    }

    private boolean updateBackgroundProcessMemory(boolean changed) {
        long backgroundProcessMemory = 0;
        try {
            int i;
            int numProc = this.mAllProcessItems.size();
            int[] pids = new int[numProc];
            for (i = 0; i < numProc; i++) {
                pids[i] = ((ProcessItem) this.mAllProcessItems.get(i)).mPid;
            }
            long[] pss = ActivityManagerNative.getDefault().getProcessPss(pids);
            for (i = 0; i < pids.length; i++) {
                ProcessItem proc = (ProcessItem) this.mAllProcessItems.get(i);
                changed |= proc.updateSize(pss[i]);
                if (proc.mCurSeq != this.mSequence && proc.mRunningProcessInfo.importance >= 400) {
                    backgroundProcessMemory += proc.mSize;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mBackgroundProcessMemory.set(backgroundProcessMemory);
        return changed;
    }

    public long getBackgroundProcessMemory() {
        return this.mBackgroundProcessMemory.get();
    }

    public void reset() {
        this.mServiceProcessesByName.clear();
        this.mServiceProcessesByPid.clear();
        this.mInterestingProcesses.clear();
        this.mRunningProcesses.clear();
        this.mProcessItems.clear();
        this.mAllProcessItems.clear();
    }
}
