package com.huawei.powergenie.modules.apppower.restrict.cleanup;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.util.Log;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IPowerStats;
import com.huawei.powergenie.integration.adapter.CommonAdapter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class ScreenLockClean extends BaseClean {
    private AppsComponentRestrict mAppsComponentRestrict;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    ArrayList<String> canCleanApps = ScreenLockClean.this.getScrLockCleanApps();
                    if (canCleanApps != null && canCleanApps.size() > 0) {
                        ScreenLockClean.this.wakeupApps(canCleanApps, "ScnOffClean");
                        Message mesg = ScreenLockClean.this.mHandler.obtainMessage(200);
                        mesg.obj = canCleanApps;
                        ScreenLockClean.this.mHandler.removeMessages(200);
                        ScreenLockClean.this.mHandler.sendMessageDelayed(mesg, 1000);
                        return;
                    }
                    return;
                case 200:
                    ScreenLockClean.this.startScrLockClean(msg.obj);
                    return;
                case 300:
                    ScreenLockClean.this.restrictAppAutoStart(((Boolean) msg.obj).booleanValue());
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mIsMultiWinActive = false;
    private long mScreenOnTime = SystemClock.elapsedRealtime();

    public ScreenLockClean(ICoreContext coreContext) {
        super(coreContext);
        this.mAppsComponentRestrict = AppsComponentRestrict.getInstance(coreContext);
    }

    protected void handleStart() {
        restrictAppAutoStart();
    }

    protected void handleCleanDBChange() {
        restrictAppAutoStart();
    }

    protected void handleScreenState(boolean sreenOn) {
        Message raasMsg = this.mHandler.obtainMessage(300, Boolean.valueOf(sreenOn));
        this.mHandler.removeMessages(300);
        this.mHandler.sendMessageDelayed(raasMsg, 2000);
        if (this.mIPolicy.getPowerMode() != 4) {
            if (sreenOn) {
                this.mScreenOnTime = SystemClock.elapsedRealtime();
                this.mHandler.removeMessages(100);
                this.mHandler.removeMessages(200);
            } else if (this.mIDeviceState.isCalling()) {
                Log.i("ScreenLockClean", "call busy and not start clean apps.");
            } else if (SystemClock.elapsedRealtime() - this.mScreenOnTime >= 10000) {
                Message msg = this.mHandler.obtainMessage(100);
                this.mHandler.removeMessages(100);
                this.mHandler.sendMessageDelayed(msg, 2000);
            }
        }
    }

    protected void handleAppStart(String startApp) {
        this.mAppsComponentRestrict.restoreAppComponent(startApp);
    }

    protected void handleAppFront(String frontApp) {
        this.mAppsComponentRestrict.restoreAppComponent(frontApp);
        if (this.mIsMultiWinActive || "com.huawei.hwmwlauncher".equals(frontApp)) {
            this.mIsMultiWinActive = this.mICoreContext.isMultiWinDisplay();
            Log.i("ScreenLockClean", "multi windows active : " + this.mIsMultiWinActive);
        }
    }

    private ArrayList<String> getScrLockCleanApps() {
        if (this.mIAppManager.isCleanDBExist()) {
            long start = SystemClock.uptimeMillis();
            ArrayList<String> canCleanApps = getCleanApps();
            Log.i("ScreenLockClean", "Screen Off get Clean Apps Num:" + canCleanApps.size() + " expend time(ms):" + (SystemClock.uptimeMillis() - start));
            return canCleanApps;
        }
        Log.e("ScreenLockClean", "no clean db in sm app!");
        return null;
    }

    private void startScrLockClean(ArrayList<String> canCleanApps) {
        if (this.mIAppManager.isCleanDBExist()) {
            this.mAppsComponentRestrict.restrictAppComponent(canCleanApps);
            long start = SystemClock.uptimeMillis();
            for (String appPkg : canCleanApps) {
                forceStopApp(appPkg, "Screen Off Clean");
            }
            Log.i("ScreenLockClean", "Screen Off Clean Apps Num:" + canCleanApps.size() + " expend time(ms):" + (SystemClock.uptimeMillis() - start));
            return;
        }
        Log.e("ScreenLockClean", "no clean db in sm app!");
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
                    Log.i("ScreenLockClean", "no any pkgs about: " + pi.processName);
                } else if (isUnprotectApps(pkgs, unprotectAppsByUser)) {
                    if (this.mIPolicy.getPowerMode() == 1 || !this.mIDeviceState.isDlUploading(pi.uid)) {
                        if (forceProtectApps == null) {
                            forceProtectApps = getForceProtectApps();
                        }
                        if (!this.mIAppManager.isIgnoreAudioApps(pkgs)) {
                            if (this.mIDeviceState.isPlayingSound(pi.pid)) {
                                for (String name : pkgs) {
                                    forceProtectApps.add(name);
                                    cleanApps.remove(name);
                                }
                                Log.i("ScreenLockClean", "don't kill audio playing proc: " + pi.processName);
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
                                        Log.i("ScreenLockClean", "don't kill depends other app to audio playing proc: " + pi.processName);
                                    }
                                } else {
                                    Log.i("ScreenLockClean", "don't kill audio pause proc: " + pi.processName);
                                }
                            }
                        }
                        if (this.mIDeviceState.hasActiveGps(pi.uid)) {
                            boolean permitClean = true;
                            for (String activeGpsPkg : pkgs) {
                                if (!permitCleanGpsApp(activeGpsPkg)) {
                                    Log.i("ScreenLockClean", "don't kill active gps: " + pi.processName);
                                    permitClean = false;
                                    break;
                                }
                            }
                            if (permitClean) {
                                Log.i("ScreenLockClean", "permit clean active gps proc: " + pi.processName);
                            }
                        }
                        if (!isForceProtectApps(pkgs, forceProtectApps)) {
                            for (String name22 : pkgs) {
                                if (!cleanApps.contains(name22)) {
                                    cleanApps.add(name22);
                                }
                            }
                        }
                    } else {
                        Log.i("ScreenLockClean", "Downloading app, don't kill proc:" + pi.processName);
                    }
                }
            }
        }
        if (forceProtectApps != null) {
            forceProtectApps.clear();
        }
        this.mIAppManager.removeProtectAppsFromIAware(cleanApps);
        if (cleanApps.size() > 0) {
            IPowerStats ips = (IPowerStats) this.mICoreContext.getService("powerstats");
            if (ips != null) {
                ips.iStats(0, cleanApps);
            }
        }
        return cleanApps;
    }

    private boolean isUnprotectApps(String[] pkgs, ArrayList<String> unprotectApps) {
        for (String appPkg : pkgs) {
            if (unprotectApps != null && !unprotectApps.contains(appPkg)) {
                return false;
            }
        }
        return true;
    }

    private boolean isForceProtectApps(String[] pkgs, HashSet<String> forceProtectApps) {
        int length = pkgs.length;
        int i = 0;
        while (i < length) {
            String appPkg = pkgs[i];
            if (forceProtectApps.contains(appPkg)) {
                return true;
            }
            if (this.mIPolicy.getPowerMode() == 1 || !this.mIDeviceState.hasBluetoothConnected(appPkg, 0, 0) || appPkg.contains("com.google.android.wearable.app.cn")) {
                i++;
            } else {
                Log.i("ScreenLockClean", "force protect ble active app: " + appPkg);
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
        ArrayList<String> audioPlayingPkg = getAudioPlayingPkg();
        if (audioPlayingPkg != null) {
            for (String pkg : audioPlayingPkg) {
                if (!forceProtectList.contains(pkg)) {
                    forceProtectList.add(pkg);
                }
            }
        }
        ArrayList<String> activeLocationApps = this.mIAppManager.getActiveHighPowerLocationApps(this.mContext);
        if (activeLocationApps != null) {
            if (activeLocationApps.contains("com.amap.android.ams")) {
                ArrayList<String> usingLocationServicePkgs = this.mIAppManager.getUsingLocationServicePkgs();
                activeLocationApps.addAll(usingLocationServicePkgs);
                Log.i("ScreenLockClean", "add apps that using gps by ams to active gps pkglist: " + usingLocationServicePkgs);
            }
            for (String activeGpsPkg : activeLocationApps) {
                if (permitCleanGpsApp(activeGpsPkg)) {
                    Log.i("ScreenLockClean", "permit clean active gps app: " + activeGpsPkg);
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
        if (this.mIsMultiWinActive) {
            ArrayList<String> topTaskApps = getTopTasksApps(3);
            if (topTaskApps != null) {
                for (String topPkg : topTaskApps) {
                    if (!forceProtectList.contains(topPkg)) {
                        forceProtectList.add(topPkg);
                        Log.i("ScreenLockClean", "multi windows don't kill last front app : " + topPkg);
                    }
                }
            }
        }
        ArrayList<String> activeWidgetApps = getActiveWidgetApp();
        if (activeWidgetApps != null && activeWidgetApps.size() > 0) {
            for (String name : activeWidgetApps) {
                if (!forceProtectList.contains(name)) {
                    forceProtectList.add(name);
                    Log.i("ScreenLockClean", "has active widget app : " + name);
                }
            }
        }
        return forceProtectList;
    }

    private ArrayList<String> getProtectFrontApps() {
        ArrayList<String> protectApps = new ArrayList();
        String autoFrontPkgAfterScrOff = this.mIScenario.getAutoFrontPkgAfterScrOff();
        if (autoFrontPkgAfterScrOff != null) {
            if (this.mIAppType.getAppType(autoFrontPkgAfterScrOff) == 9) {
                protectApps.add(autoFrontPkgAfterScrOff);
                Log.i("ScreenLockClean", "protect front scrlock app: " + autoFrontPkgAfterScrOff);
            } else {
                Log.i("ScreenLockClean", "not protect auto front app: " + autoFrontPkgAfterScrOff);
            }
        }
        if (this.mIScenario.getAboveLauncherPkgs().size() > 0) {
            protectApps.addAll(this.mIScenario.getAboveLauncherPkgs());
            ArrayList<String> topTaskApps = getTopTasksApps(1);
            if (autoFrontPkgAfterScrOff == null || !topTaskApps.contains(autoFrontPkgAfterScrOff)) {
                protectApps.addAll(topTaskApps);
                Log.i("ScreenLockClean", "protect top task app: " + topTaskApps);
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

    private void restrictAppAutoStart() {
        if (!SystemProperties.getBoolean("ro.config.hw_as_ctrl_enable", true)) {
            Log.i("ScreenLockClean", "RAAS switch is disabled.");
        } else if (this.mIPolicy.isChinaMarketProduct()) {
            Log.i("ScreenLockClean", "RAAS do nothing in china market.");
        } else {
            ArrayList<String> appList = this.mIAppManager.getCleanUnprotectApps();
            CommonAdapter.restrictAppAutoStart(false, null);
            if (appList != null && appList.size() > 0) {
                Log.i("ScreenLockClean", "ras list=" + appList);
                CommonAdapter.restrictAppAutoStart(true, appList);
            }
        }
    }

    private void restrictAppAutoStart(boolean isScrOn) {
        if (!SystemProperties.getBoolean("ro.config.hw_as_ctrl_enable", true)) {
            Log.i("ScreenLockClean", "RAAS switch is disabled.");
        } else if (this.mIPolicy.isChinaMarketProduct()) {
            CommonAdapter.restrictAppAutoStart(false, null);
            ArrayList<String> appList = this.mIAppManager.getCleanUnprotectApps();
            if (appList != null && appList.size() > 0) {
                if (!isScrOn) {
                    Log.i("ScreenLockClean", "ras list=" + appList);
                    CommonAdapter.restrictAppAutoStart(true, appList);
                } else if (appList.contains("com.google.android.wearable.app.cn")) {
                    List<String> list = new ArrayList();
                    list.add("com.google.android.wearable.app.cn");
                    Log.i("ScreenLockClean", "ras list=" + list);
                    CommonAdapter.restrictAppAutoStart(true, list);
                }
            }
        } else {
            Log.i("ScreenLockClean", "RAAS china policy do nothing in oversea.");
        }
    }
}
