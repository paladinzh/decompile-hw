package com.huawei.powergenie.core.app;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import com.huawei.powergenie.api.IAppType;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.IPolicy;
import com.huawei.powergenie.api.IPowerStats;
import com.huawei.powergenie.api.IScenario;
import com.huawei.powergenie.core.XmlHelper;
import com.huawei.powergenie.debugtest.DbgUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class WakelockMonitor {
    private static final boolean DEBUG_USB = DbgUtils.DBG_USB;
    private ActivityManager mActivityManager;
    private final AppManager mAppManager;
    private final Context mContext;
    private Runnable mGpsActivieMonitor = new Runnable() {
        public void run() {
            WakelockMonitor.this.handleHighPowerGps();
            WakelockMonitor.this.mHandler.postDelayed(this, 300000);
        }
    };
    private final ArrayList<String> mGsfPkg = new ArrayList<String>() {
        {
            add("com.google.android.gsf");
            add("com.google.android.gsf.login");
            add("com.google.android.gms");
        }
    };
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
        }
    };
    private final IAppType mIAppType;
    private final ICoreContext mICoreContext;
    private final IDeviceState mIDeviceState;
    private final IPolicy mIPolicy;
    private final IScenario mIScenario;
    private final IPowerStats mIps;
    private final ArrayList<String> mScrOnTimeOutBlackList = new ArrayList();
    private long mScroffTime = 0;
    private Runnable mWakelockMonitor = new Runnable() {
        public void run() {
            if (WakelockMonitor.this.mICoreContext.isScreenOff()) {
                WakelockMonitor.this.checkAppWakelock();
                WakelockMonitor.this.mHandler.postDelayed(this, 300000);
            }
        }
    };

    protected WakelockMonitor(ICoreContext pgcontext, AppManager appManager) {
        this.mICoreContext = pgcontext;
        this.mAppManager = appManager;
        this.mContext = pgcontext.getContext();
        this.mIPolicy = (IPolicy) this.mICoreContext.getService("policy");
        this.mIDeviceState = (IDeviceState) this.mICoreContext.getService("device");
        this.mIAppType = appManager;
        this.mIScenario = (IScenario) this.mICoreContext.getService("scenario");
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mIps = (IPowerStats) this.mICoreContext.getService("powerstats");
        init();
    }

    private void init() {
        XmlHelper.loadCustAppList("timeout_app_blacklist.xml", null, this.mScrOnTimeOutBlackList);
        if (this.mScrOnTimeOutBlackList.size() > 0) {
            Log.i("WakelockMonitor", "PG7 not supports block screen off wakelock blacklist: " + this.mScrOnTimeOutBlackList);
            this.mScrOnTimeOutBlackList.clear();
        }
    }

    protected void handleScreenState(boolean sreenOn) {
        if (sreenOn) {
            this.mHandler.removeCallbacks(this.mWakelockMonitor);
            if (!this.mHandler.hasCallbacks(this.mGpsActivieMonitor)) {
                Log.i("WakelockMonitor", "start gps active monitor.");
                this.mHandler.postDelayed(this.mGpsActivieMonitor, 300000);
                return;
            }
            return;
        }
        this.mScroffTime = SystemClock.elapsedRealtime();
        startWakelockMonitor();
    }

    protected void handlePowerDiscontected() {
        if (this.mICoreContext.isScreenOff()) {
            startWakelockMonitor();
        }
    }

    private void startWakelockMonitor() {
        if (DEBUG_USB || !this.mIDeviceState.isCharging()) {
            Log.i("WakelockMonitor", "start wakelock monitor.");
            this.mHandler.removeCallbacks(this.mWakelockMonitor);
            this.mHandler.postDelayed(this.mWakelockMonitor, 300000);
            return;
        }
        Log.i("WakelockMonitor", "charging state and not start wakelock monitor.");
    }

    private void checkAppWakelock() {
        Log.d("WakelockMonitor", "check wakelock");
        List<RunningAppProcessInfo> processes = this.mActivityManager.getRunningAppProcesses();
        if (processes == null) {
            Log.w("WakelockMonitor", "Running app process is null !");
            return;
        }
        for (RunningAppProcessInfo pi : processes) {
            if (pi.pid > 0) {
                String[] pkgs = pi.pkgList;
                if (pkgs != null && pkgs.length > 0) {
                    String appPkg = pkgs[0];
                    long sensorTime = this.mIDeviceState.getScrOffActiveSensorTime(pi.uid, this.mScroffTime);
                    if (sensorTime >= 300000) {
                        Log.w("WakelockMonitor", "find high power sensor app: " + appPkg + " sensorTime:" + sensorTime + " uid:" + pi.uid + " pid:" + pi.pid);
                        this.mAppManager.processAbnormalPowerApp(appPkg, sensorTime, 0, 0, "sensor", false);
                        if (this.mIps != null) {
                            long sensorStartTime = this.mIDeviceState.getSensorStartTime(pi.uid);
                            this.mIps.iStats(7, appPkg, 1, sensorTime, this.mScroffTime < sensorStartTime ? sensorStartTime : this.mScroffTime);
                        }
                    }
                    long wakeTime = this.mIDeviceState.getWkTimeByUidPid(pi.uid, pi.pid);
                    if (wakeTime < 60000 || !this.mGsfPkg.contains(appPkg)) {
                        long gpsTime = 0;
                        if (this.mIDeviceState.hasActiveGps(pi.uid)) {
                            gpsTime = this.mIDeviceState.getScrOffGpsTime(pi.uid);
                        }
                        if (wakeTime >= 300000 || gpsTime >= 300000) {
                            Log.w("WakelockMonitor", "high power wakelock app: " + appPkg + " WakeTime:" + wakeTime + " GpsTime:" + gpsTime + " UID:" + pi.uid + " PID:" + pi.pid + " tag:" + this.mIDeviceState.getWkTagByUidPid(pi.uid, pi.pid));
                            if (this.mIDeviceState.isDlUploading(pi.uid)) {
                                Log.i("WakelockMonitor", "downloading or uploading app hold wakelock: " + appPkg);
                            } else if (gpsTime >= 300000) {
                                sendHighPowerGps(appPkg, gpsTime, "gps");
                            } else {
                                this.mAppManager.processAbnormalPowerApp(appPkg, wakeTime, 0, 0, "wakelock", false);
                            }
                        }
                        if (DEBUG_USB && (wakeTime > 10000 || gpsTime > 10000)) {
                            Log.w("WakelockMonitor", "high power wakelock app: " + appPkg + " WakeTime:" + wakeTime + " GpsTime:" + gpsTime + " UID:" + pi.uid + " PID:" + pi.pid + " tag:" + this.mIDeviceState.getWkTagByUidPid(pi.uid, pi.pid));
                        }
                    } else {
                        boolean canKillGoogleApp = this.mIDeviceState.isChinaOperator() && !this.mIPolicy.hasGmsApps();
                        if (canKillGoogleApp) {
                            Log.i("WakelockMonitor", "Force kill the google app : " + appPkg + " hold wakelock time : " + wakeTime + " wlTag:" + this.mIDeviceState.getWkTagByUidPid(pi.uid, pi.pid));
                            this.mAppManager.forceStopApp(appPkg, "gms wakelock");
                        }
                    }
                }
            }
        }
    }

    private void handleHighPowerGps() {
        Log.d("WakelockMonitor", "check gps high power");
        if (!(handleWearableHighPowerGps() || !this.mIDeviceState.hasActiveGps() || this.mIDeviceState.isScreenOff())) {
            List<RunningAppProcessInfo> processes = this.mActivityManager.getRunningAppProcesses();
            if (processes == null) {
                Log.w("WakelockMonitor", "Running app process is null !");
                return;
            }
            for (RunningAppProcessInfo pi : processes) {
                if (pi.pid > 0) {
                    String[] pkgs = pi.pkgList;
                    if (pkgs != null) {
                        long gpsTime = this.mIDeviceState.getCurrentGpsTime(pi.uid);
                        if (gpsTime >= 300000) {
                            String frontPkg = this.mIScenario.getFrontPkg();
                            if (frontPkg == null || !frontPkg.equals(pkgs[0])) {
                                Log.i("WakelockMonitor", "gps high power app: " + pkgs[0] + " time:" + gpsTime + " uid:" + pi.uid + " pid:" + pi.pid);
                                sendHighPowerGps(pkgs[0], gpsTime, "location");
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean handleWearableHighPowerGps() {
        if ("com.google.android.wearable.app.cn".equals(this.mIScenario.getFrontPkg())) {
            Log.i("WakelockMonitor", "wear app is front app ,don't handle it");
            return false;
        }
        if (this.mIDeviceState.hasActiveGps() && this.mIDeviceState.getBtDisConnectedTime() > 300000) {
            int wearableUid = this.mAppManager.getUidByPkg("com.google.android.wearable.app.cn");
            if (wearableUid > 0) {
                long gpsTime = 0;
                if (this.mIDeviceState.hasActiveGps(wearableUid)) {
                    gpsTime = this.mIDeviceState.getCurrentGpsTime(wearableUid);
                }
                if (gpsTime >= 300000) {
                    this.mAppManager.forceStopApp("com.google.android.wearable.app.cn", "gps high power");
                    Log.i("WakelockMonitor", "com.google.android.wearable.app.cn already disconnect bluetooth gpstime : " + gpsTime + " gps high power, kill and restart it");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isFrontNavigationApp(String appName) {
        String frontPkg = this.mIScenario.getFrontPkg();
        if (frontPkg != null && frontPkg.equals(appName) && this.mIAppType.getAppType(appName) == 13) {
            return true;
        }
        return false;
    }

    private void sendHighPowerGps(String appName, long gpsTime, String gpsTag) {
        if ("com.amap.android.ams".equals(appName)) {
            handleUsingLocationServicePkgs(gpsTime);
        } else if (isFrontNavigationApp(appName)) {
            Log.i("WakelockMonitor", "front navigation app high power." + appName);
        } else {
            if (this.mIAppType.getAppType(appName) == 13) {
                long deltaTime = this.mIDeviceState.getAudioStopDeltaTime(this.mAppManager.getUidByPkg(appName));
                if (deltaTime > 0 && deltaTime < 600000) {
                    Log.i("WakelockMonitor", "bg navigation app high power, audio pass time: " + deltaTime);
                    return;
                }
            }
            reportGpsToIPS(appName, gpsTime);
            this.mAppManager.processAbnormalPowerApp(appName, gpsTime, 0, 0, "gps", false);
        }
    }

    private void handleUsingLocationServicePkgs(long gpsTime) {
        for (String pkg : this.mAppManager.getUsingLocationServicePkgs()) {
            if (isFrontNavigationApp(pkg)) {
                Log.i("WakelockMonitor", "front navigation app high power." + pkg);
            } else {
                long deltaTime = this.mIDeviceState.getAudioStopDeltaTime(this.mAppManager.getUidByPkg(pkg));
                if (this.mIAppType.getAppType(pkg) != 13 || deltaTime <= 0 || deltaTime >= 600000) {
                    reportGpsToIPS(pkg, gpsTime);
                    this.mAppManager.processAbnormalPowerApp(pkg, gpsTime, 0, 0, "gps", false);
                } else {
                    Log.i("WakelockMonitor", pkg + " bg navigation app high power, audio pass time: " + deltaTime);
                }
            }
        }
    }

    protected void handleNonActiveTimeout() {
        if (!this.mICoreContext.isScreenOff()) {
            List<RunningAppProcessInfo> processes = this.mActivityManager.getRunningAppProcesses();
            int NP = processes != null ? processes.size() : 0;
            Log.w("WakelockMonitor", "a app hold a wakelock to block the screen timeout.");
            for (int i = 0; i < NP; i++) {
                RunningAppProcessInfo pi = (RunningAppProcessInfo) processes.get(i);
                if (pi.pid > 0 && UserHandle.getAppId(pi.uid) >= 10000) {
                    String[] pkgs = pi.pkgList;
                    List<String> arrayList = new ArrayList(Arrays.asList(pkgs));
                    String frontPkg = this.mIScenario.getFrontPkg();
                    if (!(frontPkg == null || arrayList.contains(frontPkg) || !this.mIDeviceState.isHoldWakeLockByUid(pi.uid, 10))) {
                        long date = System.currentTimeMillis();
                        for (String pkgName : pkgs) {
                            if (pkgName != null) {
                                if (this.mAppManager.isShowTopView(pi.pid, pi.uid, pkgName)) {
                                    Log.w("WakelockMonitor", "hold screen wakelock and has top view app: " + pkgName);
                                    this.mAppManager.processAbnormalPowerApp(pkgName, 0, 0, 0, "wakelock", true);
                                } else if (this.mScrOnTimeOutBlackList.contains(pkgName)) {
                                    Log.w("WakelockMonitor", "hold screen wakelock and in blacklist app: " + pkgName);
                                    this.mAppManager.forceStopApp(pkgName, "screenTimeout");
                                } else {
                                    Log.w("WakelockMonitor", "hold screen wakelock app: " + pkgName);
                                    this.mAppManager.processAbnormalPowerApp(pkgName, 0, 0, 0, "wakelock", true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void reportGpsToIPS(String pkg, long gpsTime) {
        if (this.mIps != null && this.mIDeviceState.isScreenOff()) {
            this.mIps.iStats(6, pkg, 1, gpsTime, this.mScroffTime);
        }
    }
}
