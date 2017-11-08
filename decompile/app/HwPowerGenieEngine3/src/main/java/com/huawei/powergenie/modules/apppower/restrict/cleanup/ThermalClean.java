package com.huawei.powergenie.modules.apppower.restrict.cleanup;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.util.Log;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IPowerStats;
import com.huawei.powergenie.modules.apppower.restrict.cleanup.ProcessCpuTracker.ProcLoad;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class ThermalClean extends BaseClean {
    private static int mCurrentPolicy = 0;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    ThermalClean.this.mTopCpuLoadAppList.clear();
                    if (ThermalClean.mCurrentPolicy != 1) {
                        if (ThermalClean.this.mProcessCpuTracker == null) {
                            ThermalClean.this.mProcessCpuTracker = new ProcessCpuTracker(false);
                        }
                        if (ThermalClean.this.mProcessCpuTracker.mFirst) {
                            ThermalClean.this.mProcessCpuTracker.init();
                        }
                        ThermalClean.this.mProcessCpuTracker.update();
                        ThermalClean.this.dumpCurCpuTracker();
                    }
                    ArrayList<String> canCleanApps = ThermalClean.this.getThermalCleanApps();
                    if (canCleanApps != null && canCleanApps.size() > 0) {
                        ThermalClean.this.wakeupApps(canCleanApps, "ThermalClean");
                        Message mesg = ThermalClean.this.mHandler.obtainMessage(102);
                        mesg.obj = canCleanApps;
                        ThermalClean.this.mHandler.removeMessages(102);
                        ThermalClean.this.mHandler.sendMessageDelayed(mesg, 1000);
                        return;
                    }
                    return;
                case 102:
                    ThermalClean.this.startThermalClean(msg.obj);
                    return;
                default:
                    return;
            }
        }
    };
    private long mLastCleanTime = SystemClock.elapsedRealtime();
    private ProcessCpuTracker mProcessCpuTracker;
    private long mScreenOnTime = SystemClock.elapsedRealtime();
    private ArrayList<String> mTopCpuLoadAppList = new ArrayList();

    public ThermalClean(ICoreContext coreContext) {
        super(coreContext);
    }

    protected void handleThermalClean(long value) {
        Log.i("ThermalClean", "Thermal trigger clean app policy=" + value);
        if (value == 0) {
            Log.i("ThermalClean", "Thermal clean do nothing");
        } else if (this.mIAppManager.isAbnormalPowerAppClsSwitchOn()) {
            mCurrentPolicy = (int) value;
            if (this.mIDeviceState.isCalling()) {
                Log.i("ThermalClean", "call busy and not start clean apps.");
            } else if (SystemClock.elapsedRealtime() - this.mLastCleanTime >= 120000) {
                Message msg = this.mHandler.obtainMessage(101);
                this.mHandler.removeMessages(101);
                this.mHandler.sendMessageDelayed(msg, 2000);
            } else {
                Log.d("ThermalClean", "clean interval time is too short...");
            }
        } else {
            Log.i("ThermalClean", "Abnormal clean switch is off, thermal clean do nothing.");
        }
    }

    private ArrayList<String> getThermalCleanApps() {
        if (this.mIAppManager.isCleanDBExist()) {
            long start = SystemClock.uptimeMillis();
            ArrayList<String> canCleanApps = getCleanApps();
            Log.i("ThermalClean", "Thermal get Clean Apps Num:" + canCleanApps.size() + " expend time(ms):" + (SystemClock.uptimeMillis() - start));
            return canCleanApps;
        }
        Log.e("ThermalClean", "no clean db in sm app!");
        return null;
    }

    private void startThermalClean(ArrayList<String> canCleanApps) {
        if (this.mIAppManager.isCleanDBExist()) {
            long start = SystemClock.uptimeMillis();
            canCleanApps.removeAll(this.mIScenario.getAboveLauncherPkgs());
            for (String appPkg : canCleanApps) {
                forceStopApp(appPkg, "Thermal Clean");
            }
            Log.i("ThermalClean", "Thermal Clean Apps Num:" + canCleanApps.size() + " expend time(ms):" + (SystemClock.uptimeMillis() - start));
            this.mLastCleanTime = SystemClock.elapsedRealtime();
            return;
        }
        Log.e("ThermalClean", "no clean db in sm app!");
    }

    private ArrayList<String> getCleanApps() {
        ArrayList<String> cleanApps = new ArrayList();
        ArrayList<String> unprotectAppsByUser = this.mIAppManager.getCleanUnprotectApps();
        HashSet forceProtectApps = null;
        List<RunningAppProcessInfo> processes = this.mActivityManager.getRunningAppProcesses();
        int NP = processes != null ? processes.size() : 0;
        for (int i = 0; i < NP; i++) {
            RunningAppProcessInfo pi = (RunningAppProcessInfo) processes.get(i);
            if (pi.importance != 170 && UserHandle.getAppId(pi.uid) >= 10000) {
                String[] pkgs = pi.pkgList;
                if (pkgs == null) {
                    Log.i("ThermalClean", "no any pkgs about: " + pi.processName);
                } else if (mCurrentPolicy != 1 || isUnprotectApps(pkgs, unprotectAppsByUser)) {
                    if ((mCurrentPolicy == 1 || !this.mTopCpuLoadAppList.contains(pi.processName)) && this.mIDeviceState.isDlUploading(pi.uid)) {
                        Log.i("ThermalClean", "Downloading app, don't kill proc:" + pi.processName);
                    } else {
                        boolean permitClean;
                        if (forceProtectApps == null) {
                            forceProtectApps = getForceProtectApps();
                        }
                        if (mCurrentPolicy == 1 || mCurrentPolicy == 2 || !this.mTopCpuLoadAppList.contains(pi.processName)) {
                            if (!this.mIAppManager.isIgnoreAudioApps(pkgs)) {
                                if (this.mIDeviceState.isPlayingSound(pi.pid)) {
                                    for (String name : pkgs) {
                                        forceProtectApps.add(name);
                                        cleanApps.remove(name);
                                    }
                                    Log.i("ThermalClean", "don't kill audio playing proc: " + pi.processName);
                                } else {
                                    long deltaTime = this.mIDeviceState.getAudioStopDeltaTime(pi.uid);
                                    if (deltaTime <= 0 || deltaTime > 3000) {
                                        ArrayList<Integer> myPid = new ArrayList();
                                        myPid.add(Integer.valueOf(pi.pid));
                                        if (this.mIAppManager.isDependsAudioActiveApp(myPid)) {
                                            for (String name2 : pkgs) {
                                                forceProtectApps.add(name2);
                                                cleanApps.remove(name2);
                                            }
                                            Log.i("ThermalClean", "don't kill depends other app to audio playing proc: " + pi.processName);
                                        }
                                    } else {
                                        Log.i("ThermalClean", "don't kill audio pause proc: " + pi.processName);
                                    }
                                }
                            }
                        }
                        if (this.mIDeviceState.hasActiveGps(pi.uid)) {
                            permitClean = true;
                            for (String activeGpsPkg : pkgs) {
                                if (!permitCleanGpsApp(activeGpsPkg)) {
                                    Log.i("ThermalClean", "don't kill active gps: " + pi.processName);
                                    permitClean = false;
                                    break;
                                }
                            }
                            if (permitClean) {
                                Log.i("ThermalClean", "permit clean active gps proc: " + pi.processName);
                            }
                        }
                        if (!(this.mTopCpuLoadAppList.contains(pi.processName) && mCurrentPolicy == 3)) {
                            permitClean = true;
                            for (String name22 : pkgs) {
                                if (this.mIAppType.getAppType(name22) == 11) {
                                    permitClean = false;
                                    break;
                                }
                            }
                            if (!permitClean) {
                            }
                        }
                        if (!isForceProtectApps(pkgs, forceProtectApps)) {
                            for (String name222 : pkgs) {
                                if (!cleanApps.contains(name222)) {
                                    cleanApps.add(name222);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (forceProtectApps != null) {
            forceProtectApps.clear();
        }
        if (cleanApps.size() > 0) {
            ArrayList<String> importenceApp = new ArrayList();
            for (String name2222 : cleanApps) {
                if (!(this.mIAppManager.isCleanProtectApp(name2222) || this.mIAppManager.isCleanUnprotectApp(name2222))) {
                    importenceApp.add(name2222);
                }
            }
            cleanApps.removeAll(importenceApp);
        }
        if (cleanApps.size() > 0) {
            IPowerStats ips = (IPowerStats) this.mICoreContext.getService("powerstats");
            if (ips != null) {
                ips.iStats(10000, cleanApps);
            }
        }
        return cleanApps;
    }

    private boolean isUnprotectApps(String[] pkgs, ArrayList<String> unprotectApps) {
        for (String appPkg : pkgs) {
            if (unprotectApps != null && !unprotectApps.contains(appPkg) && !this.mIAppManager.isForceCleanApp(appPkg)) {
                return false;
            }
        }
        return true;
    }

    private boolean isForceProtectApps(String[] pkgs, HashSet<String> forceProtectApps) {
        for (String appPkg : pkgs) {
            if (forceProtectApps.contains(appPkg)) {
                return true;
            }
            if (mCurrentPolicy == 1 && this.mIDeviceState.hasBluetoothConnected(appPkg, 0, 0)) {
                Log.i("ThermalClean", "force protect ble active app: " + appPkg);
                return true;
            }
        }
        return false;
    }

    private HashSet<String> getForceProtectApps() {
        HashSet<String> forceProtectList = new HashSet();
        String usingLauncher = getUsingLauncher();
        String defaultLauncher = getDefaultLauncher();
        String curLiveWallpaper = getCurLiveWallpaper();
        String defaultInputmethod = getDefaultInputMethod();
        ArrayList<String> importentService = getImportentServices();
        ArrayList<String> protectFrontApps = getProtectFrontApps();
        forceProtectList.addAll(mForceProtectList);
        forceProtectList.addAll(importentService);
        forceProtectList.addAll(protectFrontApps);
        if (!forceProtectList.contains(usingLauncher)) {
            forceProtectList.add(usingLauncher);
        }
        if (!(defaultLauncher == null || forceProtectList.contains(defaultLauncher))) {
            forceProtectList.add(defaultLauncher);
        }
        if (!(curLiveWallpaper == null || forceProtectList.contains(curLiveWallpaper))) {
            forceProtectList.add(curLiveWallpaper);
        }
        if (!(defaultInputmethod == null || forceProtectList.contains(defaultInputmethod))) {
            forceProtectList.add(defaultInputmethod);
        }
        ArrayList<String> clockList = this.mIAppType.getAppsByType(10);
        if (clockList != null) {
            for (String clock : clockList) {
                if (!forceProtectList.contains(clock)) {
                    forceProtectList.add(clock);
                }
            }
        }
        ArrayList<String> activeLocationApps = this.mIAppManager.getActiveHighPowerLocationApps(this.mContext);
        if (activeLocationApps != null) {
            if (activeLocationApps.contains("com.amap.android.ams")) {
                ArrayList<String> usingLocationServicePkgs = this.mIAppManager.getUsingLocationServicePkgs();
                activeLocationApps.addAll(usingLocationServicePkgs);
                Log.i("ThermalClean", "add apps that using gps by ams to active gps pkglist: " + usingLocationServicePkgs);
            }
            for (String activeGpsPkg : activeLocationApps) {
                if (permitCleanGpsApp(activeGpsPkg)) {
                    Log.i("ThermalClean", "permit clean active gps app: " + activeGpsPkg);
                } else {
                    forceProtectList.add(activeGpsPkg);
                }
            }
        }
        String defaultSmsPkg = this.mIAppType.getDefaultSmsApplication();
        if (defaultSmsPkg != null) {
            forceProtectList.add(defaultSmsPkg);
        }
        Object guestSms = null;
        int curUser = this.mIAppManager.getCurUserId();
        if (curUser != 0) {
            guestSms = Secure.getStringForUser(this.mContext.getContentResolver(), "sms_default_application", curUser);
        }
        if (guestSms != null) {
            forceProtectList.add(guestSms);
        }
        return forceProtectList;
    }

    private ArrayList<String> getProtectFrontApps() {
        ArrayList<String> protectApps = new ArrayList();
        String autoFrontPkgAfterScrOff = this.mIScenario.getAutoFrontPkgAfterScrOff();
        if (autoFrontPkgAfterScrOff != null) {
            if (this.mIAppType.getAppType(autoFrontPkgAfterScrOff) == 9) {
                protectApps.add(autoFrontPkgAfterScrOff);
                Log.i("ThermalClean", "protect front scrlock app: " + autoFrontPkgAfterScrOff);
            } else {
                Log.i("ThermalClean", "not protect auto front app: " + autoFrontPkgAfterScrOff);
            }
        }
        if (this.mIScenario.getAboveLauncherPkgs().size() > 0) {
            protectApps.addAll(this.mIScenario.getAboveLauncherPkgs());
            ArrayList<String> topTaskApps = getTopTasksApps(1);
            if (autoFrontPkgAfterScrOff == null || !topTaskApps.contains(autoFrontPkgAfterScrOff)) {
                protectApps.addAll(topTaskApps);
                Log.i("ThermalClean", "protect top task app: " + topTaskApps);
            }
        }
        return protectApps;
    }

    private boolean permitCleanGpsApp(String activeLocationApps) {
        if (this.mIPolicy.getPowerMode() == 1) {
            return true;
        }
        if ("com.codoon.gps".equals(activeLocationApps) || "cn.ledongli.ldl".equals(activeLocationApps) || "me.chunyu.Pedometer".equals(activeLocationApps) || "com.fitmix.sdk".equals(activeLocationApps) || "com.yuedong.sport".equals(activeLocationApps) || "com.mandian.android.dongdong".equals(activeLocationApps)) {
            return false;
        }
        return this.mIAppManager.isIgnoreGpsApp(activeLocationApps) || this.mIAppType.getAppType(activeLocationApps) != 13;
    }

    private void dumpCurCpuTracker() {
        if (this.mProcessCpuTracker != null) {
            ArrayList<ProcLoad> mRetProcList = new ArrayList();
            this.mProcessCpuTracker.printCurrentState(SystemClock.uptimeMillis(), mRetProcList);
            int i = 0;
            while (i < mRetProcList.size()) {
                try {
                    ProcLoad procLoadInfo = (ProcLoad) mRetProcList.get(i);
                    int load = procLoadInfo.getProcLoad();
                    if (load >= 5) {
                        String procName = procLoadInfo.getProcName();
                        this.mTopCpuLoadAppList.add(procName);
                        Log.i("ThermalClean", "Proc=" + procName + " Load=" + load);
                    }
                    i++;
                } catch (Exception e) {
                    Log.e("ThermalClean", "dumpCurCpuTracker Exception!");
                }
            }
            Log.i("ThermalClean", "CurCpuTracker Top CPU load app:" + this.mTopCpuLoadAppList);
        }
    }
}
