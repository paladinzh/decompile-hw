package com.android.server.pfw.policy;

import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.BatteryStats.Uid;
import android.os.BatteryStats.Uid.Proc;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.util.SparseArray;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.os.BatteryStatsImpl;
import com.android.server.pfw.HwPFWService;
import com.android.server.pfw.log.HwPFWLogger;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwPFWAppRestartPolicy extends HwPFWPolicy {
    private static final long CHECK_ONE_HOUR_TIME = 3600000;
    private static final long CHECK_TOTAL_LAUNCH_TIME = 14400000;
    private static final int MAX_PROC_LAUNCH_TIMES = 10;
    private static final int MAX_PROC_TOTAL_LAUNCH_TIMES = 30;
    private static final String TAG = "PFW.HwPFWAppRestartPolicy";
    private static AtomicBoolean exists = new AtomicBoolean(false);
    private long mAppRestartInitTime0 = 0;
    private long mAppRestartInitTime1 = 0;
    private Context mContext;
    private boolean mEnabled = true;
    private boolean mIsScrOff = false;
    private Map<String, Integer> mLastHrProcLaunchMap = new HashMap();
    private Map<String, Integer> mLastProcLaunchMap = new HashMap();
    private Handler mProcCalcHandler;
    private Runnable mProcLaunch = new Runnable() {
        public void run() {
            HwPFWLogger.d(HwPFWAppRestartPolicy.TAG, "process launch counts runnable");
            new Thread() {
                public void run() {
                    HwPFWAppRestartPolicy.this.calcProcLaunchFlg(false);
                }
            }.start();
        }
    };
    private Map<String, Integer> mProcLaunchMap = new HashMap();
    private HwPFWService mService;

    public HwPFWAppRestartPolicy(Context context, HwPFWService service) {
        super(context);
        this.mContext = context;
        this.mService = service;
        this.mProcCalcHandler = new Handler();
    }

    public void handleBroadcastIntent(Intent intent) {
        if (this.mEnabled) {
            String action = intent.getAction();
            if (action != null) {
                HwPFWLogger.d(TAG, "receive action = " + action);
                if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                    calcProcLaunchFlg(true);
                    this.mAppRestartInitTime0 = SystemClock.elapsedRealtime();
                    this.mAppRestartInitTime1 = this.mAppRestartInitTime0;
                } else if (action.equals("android.intent.action.SCREEN_ON")) {
                    this.mIsScrOff = false;
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    this.mIsScrOff = true;
                } else if (action.equals(HwPFWService.ACTION_PFW_WAKEUP_TIMER)) {
                    this.mProcCalcHandler.postDelayed(this.mProcLaunch, 0);
                } else if (action.equals("android.intent.action.ACTION_POWER_DISCONNECTED") && this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED")).getIntExtra(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL, 0) >= 90) {
                    synchronized (this.mLastHrProcLaunchMap) {
                        this.mLastHrProcLaunchMap.clear();
                        HwPFWLogger.d(TAG, "clear mLastHrProcLaunchMap");
                    }
                    synchronized (this.mLastProcLaunchMap) {
                        this.mLastProcLaunchMap.clear();
                        HwPFWLogger.d(TAG, "clear mLastProcLaunchMap");
                    }
                }
            }
        }
    }

    private void cloneProcHashMap(Map<String, Integer> src, Map<String, Integer> dst) {
        dst.clear();
        for (Entry<String, Integer> ent : src.entrySet()) {
            dst.put((String) ent.getKey(), (Integer) ent.getValue());
        }
    }

    private String getCurDefaultIms() {
        String inputMethod = Secure.getString(this.mContext.getContentResolver(), "default_input_method");
        if (inputMethod == null) {
            return null;
        }
        String[] defaultIms = inputMethod.split("/");
        HwPFWLogger.d(TAG, "curDefault IMs = " + defaultIms[0]);
        return defaultIms[0];
    }

    private void calcProcLaunchFlg(boolean isFstTimes) {
        if (exists.compareAndSet(false, true)) {
            long now = SystemClock.elapsedRealtime();
            getProcLaunches(0);
            if (isFstTimes) {
                synchronized (this.mLastProcLaunchMap) {
                    cloneProcHashMap(this.mProcLaunchMap, this.mLastProcLaunchMap);
                }
                synchronized (this.mLastHrProcLaunchMap) {
                    cloneProcHashMap(this.mProcLaunchMap, this.mLastHrProcLaunchMap);
                }
                exists.set(false);
                return;
            }
            String ims = getCurDefaultIms();
            for (Entry<String, Integer> ent : this.mProcLaunchMap.entrySet()) {
                String proc = (String) ent.getKey();
                if (proc != null) {
                    int count = 0;
                    if (ent.getValue() != null) {
                        count = ((Integer) ent.getValue()).intValue();
                    }
                    int preCount = 0;
                    synchronized (this.mLastProcLaunchMap) {
                        if (this.mLastProcLaunchMap.get(proc) != null) {
                            preCount = ((Integer) this.mLastProcLaunchMap.get(proc)).intValue();
                        }
                    }
                    int preHrCount = 0;
                    synchronized (this.mLastHrProcLaunchMap) {
                        if (this.mLastHrProcLaunchMap.get(proc) != null) {
                            preHrCount = ((Integer) this.mLastHrProcLaunchMap.get(proc)).intValue();
                        }
                    }
                    String pacName = proc.split(":")[0];
                    ApplicationInfo ai = null;
                    try {
                        ai = this.mContext.getPackageManager().getApplicationInfo(pacName, 0);
                    } catch (Exception e) {
                        HwPFWLogger.d(TAG, "failed to get application info");
                    }
                    if (ai == null || (ai.flags & 1) == 0 || ai.hwFlags != 0) {
                        boolean isUnprotectedApp;
                        synchronized (this.mService.getStopPkgListLock()) {
                            isUnprotectedApp = this.mService.getStopPkgList().contains(pacName);
                        }
                        boolean isNeedProcess = false;
                        if (!isUnprotectedApp && count - preCount >= 30) {
                            isNeedProcess = true;
                        } else if (isUnprotectedApp && count - preCount >= 10) {
                            isNeedProcess = true;
                        } else if (isUnprotectedApp && count - preHrCount >= 30) {
                            isNeedProcess = true;
                            if (this.mIsScrOff && ims != null && ims.equals(pacName)) {
                                this.mService.getActivityManager().killBackgroundProcesses(ims);
                            }
                        }
                        HwPFWLogger.d(TAG, "proc: " + proc + " launch " + count + "times, lastTimes = " + preCount);
                        if (isNeedProcess) {
                            String pac = getActivityRunTopPac();
                            if ((pac == null || !pac.equals(pacName)) && (ims == null || !ims.equals(pacName))) {
                                HwPFWLogger.d(TAG, pacName + " is clean and added to forbid restart list");
                                this.mService.addForbidRestartApp(pacName);
                                this.mService.getActivityManager().forceStopPackage(pacName);
                            }
                        }
                    }
                }
            }
            if (now - this.mAppRestartInitTime1 >= 30) {
                synchronized (this.mLastHrProcLaunchMap) {
                    cloneProcHashMap(this.mProcLaunchMap, this.mLastHrProcLaunchMap);
                }
                this.mAppRestartInitTime1 = now;
            }
            if (now - this.mAppRestartInitTime0 >= CHECK_TOTAL_LAUNCH_TIME) {
                this.mService.clearAllForbidRestartApp();
                this.mAppRestartInitTime0 = now;
            }
            synchronized (this.mLastProcLaunchMap) {
                cloneProcHashMap(this.mProcLaunchMap, this.mLastProcLaunchMap);
            }
            exists.set(false);
        }
    }

    private String getActivityRunTopPac() {
        String pacName = null;
        try {
            ComponentName cn = ((RunningTaskInfo) this.mService.getActivityManager().getRunningTasks(1).get(0)).topActivity;
            if (cn != null) {
                pacName = cn.getPackageName();
            }
        } catch (Exception ex) {
            HwPFWLogger.e(TAG, "getActivityRunTopPac catch Exception :" + ex.getMessage());
        }
        return pacName;
    }

    private void getProcLaunches(int which) {
        synchronized (this.mService.getBatteryLockObject()) {
            BatteryStatsHelper bsh = this.mService.getBatteryStatsHelper();
            if (bsh == null) {
                return;
            }
            bsh.clearStats();
            BatteryStatsImpl stats = (BatteryStatsImpl) bsh.getStats();
            if (stats == null) {
                return;
            }
            SparseArray<? extends Uid> uidStats = stats.getUidStats();
            int NU = uidStats.size();
            this.mProcLaunchMap.clear();
            for (int iu = 0; iu < NU; iu++) {
                Uid u = (Uid) uidStats.valueAt(iu);
                if (u.getUid() >= 10000) {
                    Map<String, ? extends Proc> processStats = u.getProcessStats();
                    if (processStats.size() > 0) {
                        for (Entry<String, ? extends Proc> ent : processStats.entrySet()) {
                            String procName = (String) ent.getKey();
                            int starts = ((Proc) ent.getValue()).getStarts(which);
                            if (starts > 0) {
                                this.mProcLaunchMap.put(procName, Integer.valueOf(starts));
                            }
                        }
                    }
                }
            }
        }
    }
}
