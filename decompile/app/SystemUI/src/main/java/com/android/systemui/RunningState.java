package com.android.systemui;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManagerNative;
import android.content.Context;
import android.os.RemoteException;
import android.util.SparseArray;
import com.android.systemui.utils.HwLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RunningState {
    private int MAX_SERVICES = 100;
    private ArrayList<ProcessItem> mAllProcessItems = new ArrayList();
    public long mBackgroundProcessMemory;
    private ArrayList<ProcessItem> mInterestingProcesses = new ArrayList();
    final Object mLock = new Object();
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
            this.mSize = pss << 10;
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
        AppProcessInfo ainfo;
        this.mSequence++;
        boolean changed = false;
        List<RunningServiceInfo> services = am.getRunningServices(this.MAX_SERVICES);
        int NS = services != null ? services.size() : 0;
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
        List<RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        int NP = processes != null ? processes.size() : 0;
        this.mTmpAppProcesses.clear();
        for (i = 0; i < NP; i++) {
            RunningAppProcessInfo pi = (RunningAppProcessInfo) processes.get(i);
            this.mTmpAppProcesses.put(pi.pid, new AppProcessInfo(pi));
        }
        for (i = 0; i < NS; i++) {
            si = (RunningServiceInfo) services.get(i);
            if (si.restarting == 0 && si.pid > 0) {
                ainfo = (AppProcessInfo) this.mTmpAppProcesses.get(si.pid);
                if (ainfo != null) {
                    ainfo.hasServices = true;
                    if (si.foreground) {
                        ainfo.hasForegroundServices = true;
                    }
                }
            }
        }
        for (i = 0; i < NS; i++) {
            si = (RunningServiceInfo) services.get(i);
            if (si.restarting == 0 && si.pid > 0) {
                ainfo = (AppProcessInfo) this.mTmpAppProcesses.get(si.pid);
                if (!(ainfo == null || ainfo.hasForegroundServices || ainfo.info.importance >= 300)) {
                    boolean skip = false;
                    ainfo = (AppProcessInfo) this.mTmpAppProcesses.get(ainfo.info.importanceReasonPid);
                    while (ainfo != null) {
                        if (!ainfo.hasServices) {
                            if (!isInterestingProcess(ainfo.info)) {
                                ainfo = (AppProcessInfo) this.mTmpAppProcesses.get(ainfo.info.importanceReasonPid);
                            }
                        }
                        skip = true;
                        break;
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
                ProcessItem processItem = new ProcessItem(context);
                procs.put(si.process, processItem);
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
        for (i = 0; i < NP; i++) {
            pi = (RunningAppProcessInfo) processes.get(i);
            proc = (ProcessItem) this.mServiceProcessesByPid.get(pi.pid);
            if (proc == null) {
                proc = (ProcessItem) this.mRunningProcesses.get(pi.pid);
                if (proc == null) {
                    changed = true;
                    processItem = new ProcessItem(context);
                    processItem.mPid = pi.pid;
                    this.mRunningProcesses.put(pi.pid, processItem);
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
        long backgroundProcessMemory = 0;
        try {
            int numProc = this.mAllProcessItems.size();
            int[] pids = new int[numProc];
            for (i = 0; i < numProc; i++) {
                pids[i] = ((ProcessItem) this.mAllProcessItems.get(i)).mPid;
            }
            long[] pss = ActivityManagerNative.getDefault().getProcessPss(pids);
            for (i = 0; i < pids.length; i++) {
                proc = (ProcessItem) this.mAllProcessItems.get(i);
                changed |= proc.updateSize(pss[i]);
                if (proc.mCurSeq != this.mSequence && proc.mRunningProcessInfo.importance >= 400) {
                    backgroundProcessMemory += proc.mSize;
                }
            }
        } catch (RemoteException e) {
            HwLog.e("RunningState", "RemoteException");
        }
        synchronized (this.mLock) {
            this.mBackgroundProcessMemory = backgroundProcessMemory;
        }
        return changed;
    }
}
