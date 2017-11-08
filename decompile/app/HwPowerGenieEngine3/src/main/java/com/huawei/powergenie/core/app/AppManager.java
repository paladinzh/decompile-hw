package com.huawei.powergenie.core.app;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.NetworkPolicyManager;
import android.net.Uri;
import android.os.Environment;
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.util.Log;
import android.util.Xml;
import com.huawei.powergenie.api.IAppManager;
import com.huawei.powergenie.api.IAppPowerAction;
import com.huawei.powergenie.api.IAppType;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.IPolicy;
import com.huawei.powergenie.api.IPowerStats;
import com.huawei.powergenie.api.IScenario;
import com.huawei.powergenie.api.ISdkService;
import com.huawei.powergenie.core.BaseService;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.core.StateAction;
import com.huawei.powergenie.debugtest.DbgUtils;
import com.huawei.powergenie.integration.adapter.AlarmAdapter;
import com.huawei.powergenie.integration.adapter.AppStandbyDozeAdapter;
import com.huawei.powergenie.integration.adapter.BroadcastAdapter;
import com.huawei.powergenie.integration.adapter.CommonAdapter;
import com.huawei.powergenie.integration.adapter.PGManagerAdapter;
import com.huawei.powergenie.integration.adapter.pged.FreezeInterface;
import com.huawei.powergenie.integration.adapter.pged.PgedAdapterFactory;
import com.huawei.powergenie.integration.eventhub.HookEvent;
import com.huawei.powergenie.integration.eventhub.MsgEvent;
import com.huawei.powergenie.modules.apppower.hibernation.ASHManager;
import com.huawei.powergenie.modules.apppower.hibernation.states.AppStateRecord;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AppManager extends BaseService implements IAppManager, IAppPowerAction, IAppType {
    private static final boolean CARE_IAWARE_PROTECT_APP = SystemProperties.getBoolean("ro.config.hw_protect_iwa_apps", true);
    private static ArrayList<String> mAppWidgetPkgs = null;
    private static HashMap<Integer, Integer> mAppsSigType = null;
    private static ArrayList<String> mBleAppsList = new ArrayList();
    private static HashMap<String, Integer> mCacheAppsSig = new HashMap();
    private static ArrayList<String> mCacheSystemApps = new ArrayList();
    private static final ArrayList<String> mHighPowerPkgList = new ArrayList();
    private static final ArrayList<String> mHighPowerReasonList = new ArrayList();
    private static final ArrayList<String> mIgnoreAudioList = new ArrayList<String>() {
        {
            add("com.meet.pianolearn");
            add("com.fgol.sharkfree3");
            add("com.gameloft.android.ANMP.Gloft");
        }
    };
    private static final ArrayList<String> mIgnoreFrontPkgList = new ArrayList<String>() {
        {
            add("com.myzaker.ZAKER_Phone");
            add("com.mymoney");
            add("com.tencent.news");
            add("com.tencent.mtt");
            add("com.tencent.reading");
            add("com.ss.android.article.news");
        }
    };
    private static final HashMap<String, Integer> mNotificationPkgs = new HashMap();
    private static final ArrayList<String> mPermitRestrictNetPkgList = new ArrayList<String>() {
        {
            add("com.ss.android.article.news");
        }
    };
    private static final HashMap<Integer, ArrayList<Integer>> mProcessDependencyMap = new HashMap();
    private static Set<String> mProxyGpsBlacklist = new HashSet();
    private static final HashMap<Integer, Integer> mTopViewPidCount = new HashMap();
    private static long mWidgetLastModifyTime = 0;
    private ActivityManager mActivityManager;
    private AlarmStats mAlarmStats;
    private AppBlackWhitelist mAppBlackWhitelist;
    private AppInfoManager mAppInfo;
    private AppPowerMonitor mAppPowerMonitor;
    private AppStandbyDozeAdapter mAppStandbyDozeAdapter = null;
    private AppTypeRecognise mAppTypeRecognise;
    private AudioManager mAudioManager;
    private ContentResolver mContentResolver;
    private Context mContext;
    private int mCurUserId = -1;
    private FreezeInterface mFreezeAdapter = null;
    private final HashSet<Integer> mHibernateAppPids = new HashSet();
    private final ArrayList<String> mHibernateAppPkgs = new ArrayList();
    private final HashSet<String> mHighPowerHistoryPkgs = new HashSet();
    private HashSet<String> mHighPowerPkgSinceScrOff = new HashSet();
    private ICoreContext mICoreContext;
    private IDeviceState mIDeviceState;
    private IPolicy mIPolicy;
    private IScenario mIScenario;
    private ISdkService mISdkService;
    private INetworkManagementService mNMS;
    private final ArrayList<String> mNotCleanAppsByIAware = new ArrayList();
    private PackageManager mPM;
    private AlarmAdapter mPendingAdapter = null;
    private String mPlayingAudioPkg;
    private NetworkPolicyManager mPolicyManager;
    private final HashMap<String, Integer> mProcessCrashCount = new HashMap();
    private final HashMap<String, Long> mProcessFirstCrashTime = new HashMap();
    private WakelockMonitor mWakelockMonitor;

    public AppManager(ICoreContext coreContext) {
        this.mICoreContext = coreContext;
        this.mContext = coreContext.getContext();
        this.mContentResolver = this.mContext.getContentResolver();
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mPM = this.mContext.getPackageManager();
        this.mPolicyManager = (NetworkPolicyManager) this.mContext.getSystemService("netpolicy");
        this.mAppBlackWhitelist = AppBlackWhitelist.getInstance(coreContext, this);
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mNMS = Stub.asInterface(ServiceManager.getService("network_management"));
    }

    public void start() {
        this.mAppBlackWhitelist.handleStart();
        this.mISdkService = (ISdkService) this.mICoreContext.getService("sdk");
        this.mIDeviceState = (IDeviceState) this.mICoreContext.getService("device");
        this.mIPolicy = (IPolicy) this.mICoreContext.getService("policy");
        this.mIScenario = (IScenario) this.mICoreContext.getService("scenario");
        this.mAppTypeRecognise = new AppTypeRecognise(this.mICoreContext, this);
        this.mAlarmStats = new AlarmStats(this.mICoreContext, this);
        this.mWakelockMonitor = new WakelockMonitor(this.mICoreContext, this);
        this.mAppPowerMonitor = new AppPowerMonitor(this.mICoreContext);
        this.mAppInfo = new AppInfoManager(this.mICoreContext, this);
        this.mPendingAdapter = AlarmAdapter.getInstance(this.mContext);
        this.mAppStandbyDozeAdapter = AppStandbyDozeAdapter.getInstance(this.mContext);
        this.mFreezeAdapter = PgedAdapterFactory.getFreezeAdapter();
        if (!this.mFreezeAdapter.checkPgedRunning()) {
            Log.e("AppManager", "hwpged is not running!");
            this.mFreezeAdapter = null;
        }
        addAction(this.mICoreContext, 233);
        addAction(this.mICoreContext, 204);
        addAction(this.mICoreContext, 228);
        addAction(this.mICoreContext, 246);
        addAction(this.mICoreContext, 358);
    }

    public void onInputHookEvent(HookEvent event) {
        String dependedProcessName;
        switch (event.getEventId()) {
            case 111:
                handleProcessStart(Integer.parseInt(event.getValue2()), Integer.parseInt(event.getValue3()), event.getPkgName(), event.getValue1());
                return;
            case 113:
                this.mAppTypeRecognise.handleAppFrontEvent(event.getPkgName());
                return;
            case 117:
                this.mAppTypeRecognise.handleInputStart();
                return;
            case 118:
                this.mAppTypeRecognise.handleInputEnd();
                return;
            case 121:
                if (this.mIDeviceState.isScreenOff()) {
                    this.mAlarmStats.handleAlarmStart(event.getPkgName(), Integer.parseInt(event.getValue1()), event.getValue2(), event.getValue4());
                    return;
                }
                return;
            case 122:
                handleNotification(event.getPkgName(), true);
                return;
            case 123:
                handleNotification(event.getPkgName(), false);
                return;
            case 147:
                this.mPlayingAudioPkg = event.getPkgName();
                Log.i("AppManager", "audio start play: " + this.mPlayingAudioPkg);
                return;
            case 151:
                handleTopViewChanged(true, event.getPkgName(), event.getValue1(), event.getValue2());
                return;
            case 152:
                handleTopViewChanged(false, event.getPkgName(), event.getValue1(), event.getValue2());
                return;
            case 166:
                dependedProcessName = event.getPkgName();
                if (event.getValue1() == null || event.getValue2() == null) {
                    Log.e("AppManager", "Add process dependency err, event: " + event);
                }
                addProcessDependency(Integer.parseInt(event.getValue1()), Integer.parseInt(event.getValue2()));
                return;
            case 167:
                dependedProcessName = event.getPkgName();
                removeProcessDependency(Integer.parseInt(event.getValue1()), Integer.parseInt(event.getValue2()));
                return;
            case 185:
                if ("cpu".equals(event.getValue1())) {
                    handleHighCpuLoad(event.getValue2());
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void onInputMsgEvent(MsgEvent event) {
        String pkgName;
        switch (event.getEventId()) {
            case 300:
                this.mAppPowerMonitor.handleScreenOn();
                this.mWakelockMonitor.handleScreenState(true);
                this.mAlarmStats.handleScreenState(true);
                this.mAppTypeRecognise.handleScreenState(true);
                this.mHighPowerPkgSinceScrOff.clear();
                synchronized (this.mNotCleanAppsByIAware) {
                    this.mNotCleanAppsByIAware.clear();
                }
                sendHighPowerAppListWhenScrOn();
                return;
            case 301:
                this.mAppPowerMonitor.handleScreenOff();
                this.mWakelockMonitor.handleScreenState(false);
                this.mAlarmStats.handleScreenState(false);
                this.mAppTypeRecognise.handleScreenState(false);
                return;
            case 302:
                this.mAppPowerMonitor.handleBootComplete();
                this.mAppInfo.handleBootComplete();
                return;
            case 303:
                this.mAppPowerMonitor.handleShutdown();
                return;
            case 304:
                this.mAppTypeRecognise.handleScreenUnlock();
                return;
            case 305:
                Uri data = event.getIntent().getData();
                if (data != null) {
                    pkgName = data.getSchemeSpecificPart();
                    if (pkgName != null) {
                        this.mAppTypeRecognise.handlePackageChange(true, pkgName);
                        this.mAppPowerMonitor.handlePackageState(true, pkgName);
                        this.mAppInfo.handlePackageState(true, pkgName);
                        this.mAppBlackWhitelist.handlePackageState(true, pkgName);
                        return;
                    }
                    return;
                }
                return;
            case 307:
                Uri pkgData = event.getIntent().getData();
                if (pkgData != null) {
                    pkgName = pkgData.getSchemeSpecificPart();
                    if (pkgName != null) {
                        this.mAppTypeRecognise.handlePackageChange(false, pkgName);
                        this.mAppPowerMonitor.handlePackageState(false, pkgName);
                        this.mAppInfo.handlePackageState(false, pkgName);
                        this.mAppBlackWhitelist.handlePackageState(false, pkgName);
                        return;
                    }
                    return;
                }
                return;
            case 311:
                this.mWakelockMonitor.handlePowerDiscontected();
                return;
            case 359:
                this.mCurUserId = event.getIntent().getIntExtra("android.intent.extra.user_handle", -1);
                this.mAppBlackWhitelist.updateBlackWhitelist();
                return;
            default:
                return;
        }
    }

    public boolean handleAction(PowerAction action) {
        if (!super.handleAction(action)) {
            return true;
        }
        this.mAppBlackWhitelist.handleAction(action);
        return this.mAppTypeRecognise.handleAppFront(action);
    }

    private void handleProcessStart(int pid, int uid, String procName, String reason) {
        if (procName != null && !"activity".equals(reason)) {
            Integer count = (Integer) this.mProcessCrashCount.get(procName);
            if (count != null) {
                HashMap hashMap = this.mProcessCrashCount;
                int intValue = count.intValue() + 1;
                count = Integer.valueOf(intValue);
                hashMap.put(procName, Integer.valueOf(intValue));
                if (count.intValue() > 15) {
                    Long start = (Long) this.mProcessFirstCrashTime.get(procName);
                    if (start != null) {
                        long duration = SystemClock.elapsedRealtime() - start.longValue();
                        if (duration / ((long) count.intValue()) <= 240000) {
                            Log.w("AppManager", "frequently auto start proc:" + procName + " totalCnt:" + count + " duration:" + (duration / 1000) + "s");
                            List<String> pkgList = getPkgNameByUid(this.mContext, uid);
                            if (pkgList != null && pkgList.size() > 0) {
                                processAbnormalPowerApp((String) pkgList.get(0), duration, count.intValue(), 0, "crash", false);
                                IPowerStats ips = (IPowerStats) this.mICoreContext.getService("powerstats");
                                if (ips != null) {
                                    ips.iStats(3, procName, count.intValue(), duration, -1);
                                }
                                this.mProcessCrashCount.remove(procName);
                                this.mProcessFirstCrashTime.remove(procName);
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            }
            this.mProcessCrashCount.put(procName, Integer.valueOf(1));
            this.mProcessFirstCrashTime.put(procName, Long.valueOf(SystemClock.elapsedRealtime()));
        }
    }

    public boolean unpendingAllAlarms() {
        return this.mPendingAdapter.unpendingAllAlarms();
    }

    public boolean pendingAppAlarms(List<String> pkgList, boolean reasonFrz) {
        return this.mPendingAdapter.pendingAppAlarms(pkgList, reasonFrz);
    }

    public boolean unpendingAppAlarms(List<String> pkgList, boolean reasonFrz) {
        return this.mPendingAdapter.unpendingAppAlarms(pkgList, reasonFrz);
    }

    public void periodAdjustAlarms(List<String> pkgList, int type, long interval, int mode) {
        this.mPendingAdapter.periodAdjustAlarms(pkgList, type, interval, mode);
    }

    public void removePeriodAdjustAlarms(List<String> pkgList, int type) {
        this.mPendingAdapter.removePeriodAdjustAlarms(pkgList, type);
    }

    public void removeAllPeriodAdjustAlarms() {
        this.mPendingAdapter.removeAllPeriodAdjustAlarms();
    }

    public int getSignature(Context context, String packageName) {
        if (mCacheAppsSig.containsKey(packageName)) {
            return ((Integer) mCacheAppsSig.get(packageName)).intValue();
        }
        int signature = 4;
        try {
            String appSig = this.mPM.getPackageInfo(packageName, 64).signatures[0].toCharsString();
            if (mAppsSigType == null) {
                mAppsSigType = new HashMap();
                initSignatureType(this.mPM, mAppsSigType);
            }
            Integer sigType = (Integer) mAppsSigType.get(Integer.valueOf(appSig.hashCode()));
            if (sigType != null) {
                signature = sigType.intValue();
            } else if (packageName.contains("huawei") || packageName.startsWith("com.android")) {
                signature = 4;
            } else {
                signature = 0;
            }
        } catch (Exception e) {
            Log.e("AppManager", "getSignature Exception = ", e);
        }
        if (mCacheAppsSig.size() < 30) {
            mCacheAppsSig.put(packageName, Integer.valueOf(signature));
        }
        return signature;
    }

    private void initSignatureType(PackageManager pm, HashMap<Integer, Integer> appsSigType) {
        try {
            appsSigType.put(Integer.valueOf(pm.getPackageInfo("com.android.phone", 64).signatures[0].toCharsString().hashCode()), Integer.valueOf(1));
        } catch (Exception e) {
            Log.e("AppManager", "paltform signature Exception = ", e);
        }
        try {
            appsSigType.put(Integer.valueOf(pm.getPackageInfo("com.android.providers.media", 64).signatures[0].toCharsString().hashCode()), Integer.valueOf(2));
        } catch (Exception e2) {
            Log.e("AppManager", "media signature Exception = ", e2);
        }
        try {
            appsSigType.put(Integer.valueOf(pm.getPackageInfo("com.android.contacts", 64).signatures[0].toCharsString().hashCode()), Integer.valueOf(3));
        } catch (Exception e22) {
            Log.e("AppManager", "shared signature Exception = ", e22);
        }
        PackageInfo packageInfo4 = null;
        try {
            packageInfo4 = pm.getPackageInfo("com.android.gallery3d", 64);
        } catch (Exception e3) {
            try {
                packageInfo4 = pm.getPackageInfo("com.android.exchange", 64);
            } catch (Exception e4) {
                try {
                    packageInfo4 = pm.getPackageInfo("com.android.htmlviewer", 64);
                } catch (Exception exc) {
                    Log.e("AppManager", "testkey signature Exception = ", exc);
                }
            }
        }
        if (packageInfo4 != null) {
            appsSigType.put(Integer.valueOf(packageInfo4.signatures[0].toCharsString().hashCode()), Integer.valueOf(4));
        }
    }

    public boolean isSystemApp(Context context, String packageName) {
        if (mCacheSystemApps.contains(packageName)) {
            return true;
        }
        ApplicationInfo info = null;
        try {
            info = this.mPM.getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            Log.w("AppManager", "NameNotFoundException " + e);
        }
        if (!isSystemApp(info)) {
            return false;
        }
        if (mCacheSystemApps.size() < 30) {
            mCacheSystemApps.add(packageName);
        }
        return true;
    }

    public boolean isSystemApp(ApplicationInfo info) {
        if (info == null) {
            return false;
        }
        if ((info.flags & 1) == 0 && (info.flags & 128) == 0) {
            return false;
        }
        return true;
    }

    public boolean isAllowedUninstallPkg(String pkg) {
        try {
            ApplicationInfo applicationInfo = this.mPM.getApplicationInfo(pkg, 0);
            if (isSystemApp(applicationInfo)) {
                return (applicationInfo == null || applicationInfo.sourceDir == null || !applicationInfo.sourceDir.contains("/system/delapp")) ? false : true;
            } else {
                return true;
            }
        } catch (NameNotFoundException e) {
            Log.w("AppManager", "NameNotFoundException " + e);
            return false;
        }
    }

    public ArrayList<String> getAllApps(Context context) {
        ArrayList<String> appsList = new ArrayList();
        List<PackageInfo> installedPackages = this.mPM.getInstalledPackages(0);
        int packageNum = installedPackages != null ? installedPackages.size() : 0;
        for (int i = 0; i < packageNum; i++) {
            String pkgName = ((PackageInfo) installedPackages.get(i)).packageName;
            if (!appsList.contains(pkgName)) {
                appsList.add(pkgName);
            }
        }
        return appsList;
    }

    public ArrayList<String> getRuningApp(Context context) {
        ArrayList<String> runningAppList = new ArrayList();
        List<RunningAppProcessInfo> processes = this.mActivityManager.getRunningAppProcesses();
        int NP = processes != null ? processes.size() : 0;
        for (int i = 0; i < NP; i++) {
            for (String name : ((RunningAppProcessInfo) processes.get(i)).pkgList) {
                if (!runningAppList.contains(name)) {
                    runningAppList.add(name);
                }
            }
        }
        return runningAppList;
    }

    public boolean hasLauncherIcon(Context context, String appPkg) {
        Intent verification = new Intent("android.intent.action.MAIN");
        verification.addCategory("android.intent.category.LAUNCHER");
        verification.setPackage(appPkg);
        List<ResolveInfo> tempActivities = this.mPM.queryIntentActivitiesAsUser(verification, 786432, getCurUserId());
        if (tempActivities == null || tempActivities.size() <= 0) {
            return false;
        }
        return true;
    }

    public ArrayList<String> getTopTasksApps(int tops) {
        ArrayList<String> topTasksApps = new ArrayList();
        int n = 0;
        for (RunningTaskInfo info : this.mActivityManager.getRunningTasks(tops)) {
            if (info.topActivity != null) {
                if (n >= tops) {
                    break;
                }
                String pkgName = info.topActivity.getPackageName();
                if (pkgName != null) {
                    topTasksApps.add(pkgName);
                }
                if (info.baseActivity != null) {
                    String basePkgName = info.baseActivity.getPackageName();
                    if (!(basePkgName == null || topTasksApps.contains(basePkgName))) {
                        topTasksApps.add(basePkgName);
                    }
                }
                n++;
            }
        }
        return topTasksApps;
    }

    public int getUidByPid(int pid) {
        List<RunningAppProcessInfo> processes = this.mActivityManager.getRunningAppProcesses();
        int NP = processes != null ? processes.size() : 0;
        for (int i = 0; i < NP; i++) {
            RunningAppProcessInfo pi = (RunningAppProcessInfo) processes.get(i);
            if (pi != null && pi.pid == pid) {
                return pi.uid;
            }
        }
        return 0;
    }

    public int getUidByPkg(String pkg) {
        int uid = -1;
        try {
            uid = this.mPM.getPackageUidAsUser(pkg, getCurUserId());
        } catch (Exception e) {
            Log.w("AppManager", "not found uid pkg:" + pkg);
        }
        return uid;
    }

    public int getUidByPkgFromOwner(String pkg) {
        try {
            return this.mPM.getPackageUidAsUser(pkg, 0);
        } catch (Exception e) {
            Log.w("AppManager", "not found owner uid for pkg:" + pkg);
            return -1;
        }
    }

    public ArrayList<Integer> getPidsByPkg(String pkg) {
        ArrayList<Integer> pidList = new ArrayList();
        if (pkg == null) {
            return pidList;
        }
        List<RunningAppProcessInfo> processes = this.mActivityManager.getRunningAppProcesses();
        int NP = processes != null ? processes.size() : 0;
        for (int i = 0; i < NP; i++) {
            RunningAppProcessInfo pi = (RunningAppProcessInfo) processes.get(i);
            if (pi != null && pi.pkgList != null) {
                for (String name : pi.pkgList) {
                    if (pkg.equals(name)) {
                        pidList.add(Integer.valueOf(pi.pid));
                        break;
                    }
                }
            }
        }
        return pidList;
    }

    public int getPidByUid(int uid) {
        List<RunningAppProcessInfo> processes = this.mActivityManager.getRunningAppProcesses();
        int NP = processes != null ? processes.size() : 0;
        for (int i = 0; i < NP; i++) {
            RunningAppProcessInfo pi = (RunningAppProcessInfo) processes.get(i);
            if (pi != null && pi.uid == uid) {
                return pi.pid;
            }
        }
        return -1;
    }

    public int getPidByProcName(String procName) {
        if (procName == null) {
            return -1;
        }
        List<RunningAppProcessInfo> processes = this.mActivityManager.getRunningAppProcesses();
        int NP = processes != null ? processes.size() : 0;
        for (int i = 0; i < NP; i++) {
            RunningAppProcessInfo pi = (RunningAppProcessInfo) processes.get(i);
            if (pi != null && procName.equals(pi.processName)) {
                return pi.pid;
            }
        }
        return -1;
    }

    public ArrayList<String> getPkgFromSystem(int uid) {
        ArrayList<String> pkgList = new ArrayList();
        String[] pkgNameList = this.mPM.getPackagesForUid(uid);
        if (pkgNameList != null) {
            for (Object add : pkgNameList) {
                pkgList.add(add);
            }
        }
        return pkgList;
    }

    public ArrayList<String> getPkgNameByUid(Context context, int uid) {
        ArrayList<String> pkgList = new ArrayList();
        String[] pkgNameList = this.mPM.getPackagesForUid(uid);
        if (pkgNameList != null) {
            for (Object add : pkgNameList) {
                pkgList.add(add);
            }
        }
        return pkgList;
    }

    public AppInfoRecord getAppInfoRecord(String pkg) {
        return this.mAppInfo.getAppInfoRecord(pkg);
    }

    public boolean isBleApp(Context context, String pkg) {
        synchronized (mBleAppsList) {
            if (!(pkg == null || context == null)) {
                if (mBleAppsList.contains(pkg)) {
                    return true;
                } else if (checkUseFeature(context, pkg, "android.hardware.bluetooth") || checkUseFeature(context, pkg, "android.hardware.bluetooth_le")) {
                    mBleAppsList.add(pkg);
                    Log.d("AppManager", "ble app:" + pkg);
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isSimplifiedChinese() {
        return "zh-Hans-CN".equals(SystemProperties.get("persist.sys.locale", ""));
    }

    private boolean checkUseFeature(Context context, String pkg, String useFeature) {
        try {
            PackageInfo packageInfo = this.mPM.getPackageInfo(pkg, 16384);
            if (!(packageInfo == null || packageInfo.reqFeatures == null)) {
                for (FeatureInfo item : packageInfo.reqFeatures) {
                    if (item.name != null && useFeature.equals(item.name)) {
                        return true;
                    }
                }
            }
        } catch (NameNotFoundException e) {
            Log.e("AppManager", "check use feature : " + e);
        }
        return false;
    }

    public void closeSocketsForUid(int uid) {
        long now = SystemClock.elapsedRealtime();
        long spend = SystemClock.elapsedRealtime() - now;
        Log.i("AppManager", "close sockets for uid : " + uid + ", succ: " + PGManagerAdapter.closeSocketsForUid(uid) + " spend(ms):" + spend);
    }

    public ArrayList<String> getActiveHighPowerLocationApps(Context context) {
        ArrayList<String> activeLocationApps = new ArrayList();
        List<PackageOps> appOps = ((AppOpsManager) context.getSystemService("appops")).getPackagesForOps(new int[]{42});
        if (appOps != null) {
            int numPackages = appOps.size();
            for (int packageInd = 0; packageInd < numPackages; packageInd++) {
                PackageOps packageOp = (PackageOps) appOps.get(packageInd);
                List<OpEntry> opEntries = packageOp.getOps();
                if (opEntries != null) {
                    int numOps = opEntries.size();
                    for (int opInd = 0; opInd < numOps; opInd++) {
                        OpEntry opEntry = (OpEntry) opEntries.get(opInd);
                        if (opEntry.getOp() == 42 && opEntry.isRunning()) {
                            String packageName = packageOp.getPackageName();
                            if (packageName != null) {
                                activeLocationApps.add(packageName);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return activeLocationApps;
    }

    private File getAppWidgetFile() {
        File settingsFile = new File(Environment.getUserSystemDirectory(0), "appwidgets.xml");
        if (!settingsFile.exists()) {
            settingsFile = new File("/data/system/", "appwidgets.xml");
            if (!settingsFile.exists()) {
                Log.e("AppManager", "not found appwidgets.xml");
                return null;
            }
        }
        return settingsFile;
    }

    public void loadAppWidget(ArrayList<String> outAppWidgetPkgs) {
        File file = getAppWidgetFile();
        if (file != null) {
            if (file.exists()) {
                long curModifyTime = file.lastModified();
                if (mWidgetLastModifyTime == 0 || mWidgetLastModifyTime != curModifyTime || mAppWidgetPkgs == null) {
                    mWidgetLastModifyTime = curModifyTime;
                } else {
                    outAppWidgetPkgs.addAll(mAppWidgetPkgs);
                    return;
                }
            }
            try {
                FileInputStream stream = new FileInputStream(file);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream, null);
                    int type;
                    do {
                        type = parser.next();
                        if (type == 2) {
                            if ("p".equals(parser.getName())) {
                                String pkg = parser.getAttributeValue(null, "pkg");
                                if (!(pkg == null || outAppWidgetPkgs.contains(pkg))) {
                                    outAppWidgetPkgs.add(pkg);
                                }
                            }
                        }
                    } while (type != 1);
                } catch (NullPointerException e) {
                    Log.w("AppManager", "NullPointer " + e);
                } catch (XmlPullParserException e2) {
                    Log.w("AppManager", "failed parsing " + e2);
                } catch (IOException e3) {
                    Log.w("AppManager", "IOException " + e3);
                }
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e32) {
                        Log.w("AppManager", "Failed to close state FileInputStream " + e32);
                    }
                }
                mAppWidgetPkgs = (ArrayList) outAppWidgetPkgs.clone();
            } catch (FileNotFoundException e4) {
                Log.w("AppManager", "Failed to read state: " + e4);
            }
        }
    }

    public int getCurUserId() {
        if (this.mCurUserId == -1) {
            ActivityManager activityManager = this.mActivityManager;
            this.mCurUserId = ActivityManager.getCurrentUser();
        }
        return this.mCurUserId;
    }

    public void proxyWakeLock(int pid, int uid) {
        PGManagerAdapter.proxyWakeLockByPidUid(pid, uid, true);
    }

    public void unproxyWakeLock(int pid, int uid) {
        PGManagerAdapter.proxyWakeLockByPidUid(pid, uid, false);
    }

    public void forceReleaseWakeLock(int pid, int uid) {
        PGManagerAdapter.forceReleaseWakeLockByPidUid(pid, uid);
    }

    public void forceRestoreWakeLock(int pid, int uid) {
        PGManagerAdapter.forceRestoreWakeLockByPidUid(pid, uid);
    }

    public boolean notifyBastetProxy(ArrayList<Integer> bastetPids) {
        if (this.mFreezeAdapter == null) {
            return false;
        }
        return this.mFreezeAdapter.notifyBastetProxy(bastetPids);
    }

    public boolean notifyBastetUnProxy(ArrayList<Integer> bastetPids) {
        if (this.mFreezeAdapter == null) {
            return false;
        }
        return this.mFreezeAdapter.notifyBastetUnProxy(bastetPids);
    }

    public boolean notifyBastetUnProxyAll() {
        if (this.mFreezeAdapter == null) {
            return false;
        }
        return this.mFreezeAdapter.notifyBastetUnProxyAll();
    }

    private boolean isFreezePid(int pid) {
        return this.mHibernateAppPids.contains(Integer.valueOf(pid));
    }

    public boolean freezeAppProcess(ArrayList<Integer> freezePidsList, String appPkg, int appUid) {
        if (this.mFreezeAdapter == null) {
            return false;
        }
        boolean result = this.mFreezeAdapter.freezeProcess(freezePidsList);
        this.mHibernateAppPkgs.add(appPkg);
        this.mHibernateAppPids.addAll(freezePidsList);
        this.mISdkService.handleStateChanged(6, 1, 0, appPkg, appUid);
        return result;
    }

    public boolean unFreezeAppProcess(ArrayList<Integer> unFreezePidsList, String appPkg, int appUid) {
        if (this.mFreezeAdapter == null) {
            return false;
        }
        boolean result = this.mFreezeAdapter.unfreezeProcess(unFreezePidsList);
        this.mHibernateAppPkgs.remove(appPkg);
        this.mHibernateAppPids.removeAll(unFreezePidsList);
        this.mISdkService.handleStateChanged(6, 2, 0, appPkg, appUid);
        ArrayList<Integer> dependedPids = getDependedPids(unFreezePidsList);
        if (dependedPids != null) {
            ArrayList<Integer> requestUnfreezePids = new ArrayList();
            for (Integer pid : dependedPids) {
                if (isFreezePid(pid.intValue()) && !requestUnfreezePids.contains(pid)) {
                    requestUnfreezePids.add(pid);
                }
            }
            if (requestUnfreezePids.size() > 0) {
                Log.i("AppManager", "request unfreeze depended pids: " + requestUnfreezePids);
                StateAction stAction = StateAction.obtain();
                stAction.resetAs(280, 1, "unfreeze depended pids");
                stAction.updatePkgName("unfreeze");
                stAction.putExtraListInteger(requestUnfreezePids);
                notifyPowerActionChanged(this.mICoreContext, stAction);
            }
        }
        return result;
    }

    public boolean unFreezeAllAppProcess() {
        if (this.mFreezeAdapter == null) {
            return false;
        }
        return this.mFreezeAdapter.unfreezeAllProcess();
    }

    public boolean netPacketListener(ArrayList<Integer> uidsList) {
        if (this.mFreezeAdapter == null) {
            Log.e("AppManager", "mFreezeAdapter is null for netPacketListener!");
            return false;
        } else if (uidsList != null) {
            return this.mFreezeAdapter.netPacketListener(uidsList.size(), uidsList);
        } else {
            Log.e("AppManager", "uidsList is null for netPacketListener!");
            return false;
        }
    }

    public int getProcUTime(int pid) {
        if (this.mFreezeAdapter == null) {
            return -1;
        }
        return this.mFreezeAdapter.getProcUTime(pid);
    }

    public long proxyAppBroadcast(List<String> pkgs) {
        return PGManagerAdapter.proxyBroadcast(pkgs, true);
    }

    public long unproxyAppBroadcast(List<String> pkgs) {
        if (pkgs == null) {
            return -1;
        }
        return PGManagerAdapter.proxyBroadcast(pkgs, false);
    }

    public long unproxyAllAppBroadcast() {
        return PGManagerAdapter.proxyBroadcast(null, false);
    }

    public long unproxyAllAppBroadcastByPid() {
        return PGManagerAdapter.proxyBroadcastByPid(null, false);
    }

    public void setProxyBCActions(List<String> actions) {
        PGManagerAdapter.setProxyBCActions(actions);
    }

    public void setActionExcludePkg(String action, String pkg) {
        PGManagerAdapter.setActionExcludePkg(action, pkg);
    }

    public boolean proxyBCConfig(int type, String key, List<String> value) {
        return PGManagerAdapter.proxyBCConfig(type, key, value);
    }

    public boolean proxyBCConfigEx(int type, String key, String value) {
        List<String> values = new ArrayList();
        values.add(value);
        return PGManagerAdapter.proxyBCConfig(type, key, values);
    }

    public boolean dropProcessBC(int pid, List<String> actions) {
        return PGManagerAdapter.proxyBCConfig(3, String.valueOf(pid), actions);
    }

    public boolean dropPkgBC(String pkg, List<String> actions) {
        return PGManagerAdapter.proxyBCConfig(2, pkg, actions);
    }

    public void setFirewallUidRule(int uid, boolean restrict) {
        if (this.mNMS != null) {
            try {
                this.mNMS.setFirewallUidRule(2, uid, restrict ? 2 : 1);
            } catch (Exception e) {
                Log.w("AppManager", "setFirewallUidRule Exception = ", e);
            }
        }
    }

    public void recoveryFirewallUidRule() {
        removeWhiteList("com.huawei.powergenie");
        addWhiteList("com.huawei.powergenie");
    }

    public boolean proxyApp(String pkg, int uid, boolean proxy, boolean isBacklistProxy) {
        if (isBacklistProxy) {
            if (proxy) {
                mProxyGpsBlacklist.add(pkg);
            } else {
                mProxyGpsBlacklist.remove(pkg);
            }
        } else if (!proxy && mProxyGpsBlacklist.contains(pkg)) {
            Log.i("AppManager", "In gps restrict blacklist cannot unproxy : " + pkg);
            return false;
        }
        return PGManagerAdapter.proxyApp(pkg, uid, proxy);
    }

    public boolean hibernateApps(List<String> pkgNames, String reason) {
        if (pkgNames == null || pkgNames.size() == 0) {
            Log.e("AppManager", "no any pkgs for hibernate apps.");
            return false;
        }
        Log.i("AppManager", "hibernateApps pkgs: " + pkgNames + " reason: " + reason);
        StateAction stAction = StateAction.obtain();
        stAction.resetAs(281, 1, "hibernate apps");
        stAction.putExtra((ArrayList) pkgNames);
        stAction.putExtra(reason);
        notifyPowerActionChanged(this.mICoreContext, stAction);
        return true;
    }

    public boolean wakeupApps(List<String> pkgNames, String reason) {
        if (pkgNames == null || pkgNames.size() == 0) {
            Log.e("AppManager", "no any pkgs for wakeup apps.");
            return false;
        }
        StateAction stAction = StateAction.obtain();
        stAction.resetAs(284, 1, "wakeup apps");
        stAction.putExtra((ArrayList) pkgNames);
        stAction.putExtra(reason);
        notifyPowerActionChanged(this.mICoreContext, stAction);
        return true;
    }

    public List<String> getHibernateApps() {
        return this.mHibernateAppPkgs;
    }

    public void forceStopApp(String pkg, String reason) {
        Log.i("AppManager", "Force stop:" + pkg + " Reason:" + reason);
        this.mActivityManager.forceStopPackageAsUser(pkg, -2);
    }

    public void killProc(int pid, String reason) {
        Log.i("AppManager", "kill proc PID:" + pid + " Reason:" + reason);
        PGManagerAdapter.killProc(pid);
    }

    public boolean isIgnoreAudioApps(String[] pkgs) {
        for (String name : pkgs) {
            if (isIgnoreAudioApp(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean isIgnoreAudioApp(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        for (String item : mIgnoreAudioList) {
            if (pkgName.contains(item)) {
                return true;
            }
        }
        if (!this.mAppBlackWhitelist.isIgnoreAudioApp(pkgName)) {
            return false;
        }
        Log.i("AppManager", "ignore audio by cloud push : " + pkgName);
        return true;
    }

    public boolean isIgnoreAudioType(String pkgName) {
        int type = getAppType(pkgName);
        if (type != 5 && type != 19 && type != 18) {
            return false;
        }
        Log.i("AppManager", "ignore audio by type : " + pkgName + "  type : " + type);
        return true;
    }

    public boolean isIgnoreFrontApp(String pkgName) {
        return mIgnoreFrontPkgList.contains(pkgName);
    }

    public boolean isForceCleanApp(String pkgName) {
        return this.mAppBlackWhitelist.isForceCleanApp(pkgName);
    }

    public boolean isPermitRestrictNetApp(String pkgName) {
        return true;
    }

    public boolean isAbnormalPowerAppClsSwitchOn() {
        return System.getIntForUser(this.mContentResolver, "super_high_power_switch", 1, -2) == 1;
    }

    private void handleHighCpuLoad(String highCpuInfo) {
        if (highCpuInfo == null) {
            Log.i("AppManager", "no high cpu load");
            return;
        }
        String[] cpuHightPowerItems = highCpuInfo.split("#");
        ArrayList<String> highCpuLoadPkg = new ArrayList();
        if (cpuHightPowerItems != null) {
            for (String item : cpuHightPowerItems) {
                String[] cpuLoadInfo = item.split(":");
                if (cpuLoadInfo.length > 3) {
                    highCpuLoadPkg.add(cpuLoadInfo[1]);
                }
            }
            String frontPkg = this.mIScenario.getFrontPkg();
            if (frontPkg != null) {
                highCpuLoadPkg.remove(frontPkg);
            }
            Log.i("AppManager", "high cpu load exception app = " + highCpuLoadPkg);
            for (String pkg : highCpuLoadPkg) {
                if (this.mAppBlackWhitelist.isFroceKillProc(pkg)) {
                    Log.i("AppManager", "cloud configration proc:" + pkg);
                    int pid = getPidByProcName(pkg);
                    if (pid != -1) {
                        killProc(pid, "cpuHighLoad");
                    }
                } else {
                    processAbnormalPowerApp(pkg, 1200000, -1, 2, "wakelock", false);
                }
            }
            return;
        }
        Log.i("AppManager", "high cpu load to normal for all. ");
    }

    private boolean isAbnormalPowerNeedProtectApp(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        String frontPkg = this.mIScenario.getFrontPkg();
        if (frontPkg == null || !frontPkg.equals(pkgName)) {
            int uid = getUidByPkg(pkgName);
            if (!(isIgnoreAudioApp(pkgName) || isIgnoreAudioType(pkgName))) {
                if (this.mIDeviceState.isPlayingSoundByUid(uid)) {
                    Log.i("AppManager", "playing audio power abnormal uid=" + uid);
                    return true;
                }
                long deltaTime = this.mIDeviceState.getAudioStopDeltaTime(uid);
                if (deltaTime > 0 && deltaTime < 30000) {
                    Log.i("AppManager", "audio stop app power abnormal, uid=" + uid + " deltaTime=" + deltaTime);
                    return true;
                } else if (getAppType(pkgName) == 13 && deltaTime > 0 && deltaTime < 1800000) {
                    Log.i("AppManager", "navi app power abnormal audio stop time : " + deltaTime);
                    return true;
                }
            }
            if (this.mIDeviceState.isDlUploading(uid)) {
                Log.i("AppManager", "downloading or uploading app power abnormal:" + pkgName);
                return true;
            } else if (pkgName.equals(getDefaultInputMethod()) && !this.mAppBlackWhitelist.isForceCleanApp(pkgName)) {
                Log.i("AppManager", "IME app power abnormal:" + pkgName);
                return true;
            } else if (!pkgName.equals(getCurLiveWallpaper()) && !pkgName.equals(getDefaultLauncher()) && !pkgName.equals(getUsingLauncher()) && !pkgName.equals(getDefaultSmsApplication())) {
                return false;
            } else {
                Log.i("AppManager", "improtent app power abnormal:" + pkgName);
                return true;
            }
        }
        Log.i("AppManager", "front app power abnormal:" + pkgName);
        return true;
    }

    private boolean canCleanAbnormalPowerApp(String pkgName, String reason) {
        if (!this.mIDeviceState.isScreenOff() || "sensor".equals(reason)) {
            return false;
        }
        boolean isCleanUnprotect = isCleanUnprotectApp(pkgName);
        if (!isAbnormalPowerAppClsSwitchOn()) {
            synchronized (this.mNotCleanAppsByIAware) {
                if (isCleanUnprotect) {
                    if (!this.mNotCleanAppsByIAware.contains(pkgName)) {
                        return true;
                    }
                }
            }
        } else if (isCleanUnprotect || this.mAppBlackWhitelist.isForceCleanApp(pkgName)) {
            Log.i("AppManager", "abnormal power unprotect or force clean app:" + pkgName);
            return true;
        }
        return false;
    }

    public void processAbnormalPowerApp(String pkgName, long wakelockTime, int wakeupNum, int wakeupInterval, String reason, boolean onlyNotify) {
        if (pkgName != null) {
            if (this.mIDeviceState.isCharging() && !DbgUtils.DBG_USB) {
                Log.d("AppManager", "chargin not notify high power app: " + pkgName + ", wakelockTime : " + wakelockTime + "ms, count: " + wakeupNum + ", interval: " + wakeupInterval + "ms, reason: " + reason);
            } else if (!isAbnormalPowerNeedProtectApp(pkgName)) {
                if (this.mHighPowerHistoryPkgs.size() < 20) {
                    this.mHighPowerHistoryPkgs.add(pkgName);
                    Log.d("AppManager", "high power history pkgs: " + this.mHighPowerHistoryPkgs);
                }
                if (onlyNotify || !canCleanAbnormalPowerApp(pkgName, reason)) {
                    if ("gps".equals(reason) && this.mIDeviceState.isScreenOff()) {
                        StateAction stAction = StateAction.obtain();
                        stAction.resetAs(279, 1, "gps high power");
                        stAction.updatePkgName(pkgName);
                        notifyPowerActionChanged(this.mICoreContext, stAction);
                    }
                    sendHighPowerApp(pkgName, wakelockTime, wakeupNum, wakeupInterval, reason);
                } else {
                    forceStopApp(pkgName, "Screen Off Clean High Power");
                    BroadcastAdapter.sendHighPowerCleanApp(this.mContext, pkgName);
                    IPowerStats ips = (IPowerStats) this.mICoreContext.getService("powerstats");
                    if (ips != null) {
                        ArrayList<String> pkgs = new ArrayList();
                        pkgs.add(pkgName);
                        ips.iStats(2, pkgs);
                    }
                }
            }
        }
    }

    private void sendHighPowerApp(String pkgName, long wakelockTime, int wakeupNum, int wakeupInterval, String reason) {
        if (!this.mHighPowerPkgSinceScrOff.contains(pkgName)) {
            if (!"sensor".equals(reason)) {
                this.mHighPowerPkgSinceScrOff.add(pkgName);
            }
            if (isAllowedUninstallPkg(pkgName)) {
                if ((isCleanProtectApp(pkgName) || isCleanUnprotectApp(pkgName)) && !"sensor".equals(reason)) {
                    if (this.mIDeviceState.isScreenOff()) {
                        synchronized (this) {
                            mHighPowerPkgList.add(pkgName);
                            mHighPowerReasonList.add(reason);
                        }
                    } else {
                        ArrayList<String> pkgList = new ArrayList();
                        ArrayList<String> reasonList = new ArrayList();
                        pkgList.add(pkgName);
                        reasonList.add(reason);
                        BroadcastAdapter.sendWasteBatteryApp(this.mContext, pkgName, wakelockTime, wakeupNum, wakeupInterval, System.currentTimeMillis(), reason, pkgList, reasonList);
                    }
                    Log.i("AppManager", "Find a waste battery app: " + pkgName + ", wakelockTime: " + wakelockTime + "ms, wakeupNum: " + wakeupNum + ", wakeupInterval: " + wakeupInterval + ", reason: " + reason);
                }
                DbgUtils.sendNotification("a waste battery app.", pkgName + " reason: " + reason);
                return;
            }
            Log.i("AppManager", "System app cannot tips high power: " + pkgName);
        }
    }

    private void sendHighPowerAppListWhenScrOn() {
        synchronized (this) {
            if (mHighPowerPkgList.size() > 0 && mHighPowerReasonList.size() > 0) {
                BroadcastAdapter.sendWasteBatteryApp(this.mContext, (String) mHighPowerPkgList.get(0), 0, 0, 0, 0, (String) mHighPowerReasonList.get(0), mHighPowerPkgList, mHighPowerReasonList);
                mHighPowerPkgList.clear();
                mHighPowerReasonList.clear();
            }
        }
    }

    private void handleNotification(String appPkg, boolean newNotification) {
        if (appPkg != null && !"android".equals(appPkg)) {
            Integer count;
            HashMap hashMap;
            int intValue;
            if (newNotification) {
                count = (Integer) mNotificationPkgs.get(appPkg);
                if (count != null) {
                    hashMap = mNotificationPkgs;
                    intValue = count.intValue() + 1;
                    count = Integer.valueOf(intValue);
                    hashMap.put(appPkg, Integer.valueOf(intValue));
                } else {
                    mNotificationPkgs.put(appPkg, Integer.valueOf(1));
                }
            } else {
                count = (Integer) mNotificationPkgs.get(appPkg);
                if (count == null || count.intValue() <= 1) {
                    mNotificationPkgs.remove(appPkg);
                } else {
                    hashMap = mNotificationPkgs;
                    intValue = count.intValue() - 1;
                    count = Integer.valueOf(intValue);
                    hashMap.put(appPkg, Integer.valueOf(intValue));
                }
            }
        }
    }

    public boolean hasNotification(String appPkg) {
        Integer count = (Integer) mNotificationPkgs.get(appPkg);
        if (count == null || count.intValue() <= 0) {
            return false;
        }
        return true;
    }

    private void handleTopViewChanged(boolean add, String strPid, String strUid, String viewType) {
        if (viewType != null && isTopView(Integer.parseInt(viewType))) {
            int pid = Integer.parseInt(strPid);
            int uid = 0;
            if (strUid != null) {
                uid = Integer.parseInt(strUid);
            }
            if (UserHandle.getAppId(uid) > 10000 && pid > 0) {
                Integer count = (Integer) mTopViewPidCount.get(Integer.valueOf(pid));
                HashMap hashMap;
                Integer valueOf;
                int intValue;
                if (add) {
                    if (count != null) {
                        hashMap = mTopViewPidCount;
                        valueOf = Integer.valueOf(pid);
                        intValue = count.intValue() + 1;
                        count = Integer.valueOf(intValue);
                        hashMap.put(valueOf, Integer.valueOf(intValue));
                    } else {
                        mTopViewPidCount.put(Integer.valueOf(pid), Integer.valueOf(1));
                    }
                } else if (count == null || count.intValue() <= 1) {
                    mTopViewPidCount.remove(Integer.valueOf(pid));
                } else {
                    hashMap = mTopViewPidCount;
                    valueOf = Integer.valueOf(pid);
                    intValue = count.intValue() - 1;
                    count = Integer.valueOf(intValue);
                    hashMap.put(valueOf, Integer.valueOf(intValue));
                }
            }
        }
    }

    private boolean isTopView(int viewType) {
        if (viewType == 2002 || viewType == 2003 || viewType == 2010 || viewType == 2006 || viewType == 2015) {
            return true;
        }
        return false;
    }

    public boolean isShowTopView(int pid, int uid, String pkgName) {
        return mTopViewPidCount.containsKey(Integer.valueOf(pid)) && !isSysAlertOpsLimit(uid, pkgName);
    }

    private boolean isSysAlertOpsLimit(int uid, String packageName) {
        List<PackageOps> pkgOpsList = ((AppOpsManager) this.mContext.getSystemService("appops")).getOpsForPackage(uid, packageName, new int[]{24});
        if (pkgOpsList != null) {
            for (int i = 0; i < pkgOpsList.size(); i++) {
                PackageOps pkgOps = (PackageOps) pkgOpsList.get(i);
                for (int j = 0; j < pkgOps.getOps().size(); j++) {
                    OpEntry opEntry = (OpEntry) pkgOps.getOps().get(j);
                    if (opEntry != null && opEntry.getOp() == 24 && opEntry.getMode() == 1) {
                        Log.i("AppManager", "The float window of application " + packageName + " is restricted by HW System Manager.");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void addProcessDependency(int pid, int dependedPID) {
        if (pid > 0 && dependedPID > 0) {
            synchronized (mProcessDependencyMap) {
                ArrayList<Integer> dependency = (ArrayList) mProcessDependencyMap.get(Integer.valueOf(pid));
                if (dependency == null) {
                    dependency = new ArrayList();
                }
                dependency.add(Integer.valueOf(dependedPID));
                mProcessDependencyMap.put(Integer.valueOf(pid), dependency);
            }
        }
    }

    private void removeProcessDependency(int pid, int dependedPID) {
        if (pid > 0 && dependedPID > 0) {
            synchronized (mProcessDependencyMap) {
                ArrayList<Integer> dependency = (ArrayList) mProcessDependencyMap.get(Integer.valueOf(pid));
                if (dependency != null) {
                    dependency.remove(Integer.valueOf(dependedPID));
                    if (dependency.size() == 0) {
                        mProcessDependencyMap.remove(Integer.valueOf(pid));
                    }
                }
            }
        }
    }

    public void removeProcessDependency(int pid) {
        if (pid > 0) {
            synchronized (mProcessDependencyMap) {
                if (((ArrayList) mProcessDependencyMap.get(Integer.valueOf(pid))) != null) {
                    Log.i("AppManager", "removeProcessDependency pid:" + pid);
                    mProcessDependencyMap.remove(Integer.valueOf(pid));
                }
            }
        }
    }

    public boolean isDependedByOtherApp(ArrayList<Integer> pids) {
        synchronized (mProcessDependencyMap) {
            for (Entry entry : mProcessDependencyMap.entrySet()) {
                Integer dependPid = (Integer) entry.getKey();
                if (!pids.contains(dependPid)) {
                    ArrayList<Integer> dependedPids = (ArrayList) entry.getValue();
                    for (Integer pid : dependedPids) {
                        if (dependPid != pid && pids.contains(pid)) {
                            if (isFreezePid(dependPid.intValue())) {
                                Log.i("AppManager", "skip freeze pid: " + dependPid + " depends " + dependedPids);
                            } else {
                                Log.i("AppManager", "check dependency pid: " + pid + " is depended by pid: " + dependPid);
                                return true;
                            }
                        }
                    }
                    continue;
                }
            }
            return false;
        }
    }

    public ArrayList<Integer> getDependedPids(ArrayList<Integer> pids) {
        if (pids == null) {
            Log.w("AppManager", "getDependencyRelation pid is null!");
            return null;
        }
        ArrayList<Integer> dependedPids = new ArrayList();
        synchronized (mProcessDependencyMap) {
            for (Integer pid : pids) {
                ArrayList<Integer> depends = (ArrayList) mProcessDependencyMap.get(pid);
                if (depends != null) {
                    dependedPids.addAll(depends);
                }
            }
        }
        return dependedPids;
    }

    public ArrayList<String> getUsingLocationServicePkgs() {
        int i;
        ArrayList<String> pkgList = new ArrayList();
        List<RunningAppProcessInfo> processes = this.mActivityManager.getRunningAppProcesses();
        int NP = processes != null ? processes.size() : 0;
        ArrayList<Integer> locationServicePidList = new ArrayList();
        for (i = 0; i < NP; i++) {
            RunningAppProcessInfo pi = (RunningAppProcessInfo) processes.get(i);
            if (pi != null && pi.pkgList != null) {
                for (String name : pi.pkgList) {
                    if ("com.amap.android.ams".equals(name)) {
                        locationServicePidList.add(Integer.valueOf(pi.pid));
                        break;
                    }
                }
            }
        }
        if (locationServicePidList.size() == 0) {
            return pkgList;
        }
        ArrayList<Integer> dependPidList = getDependPids((Integer) locationServicePidList.get(0));
        if (dependPidList.size() == 0) {
            return pkgList;
        }
        for (Integer dependPid : dependPidList) {
            int dependUid = -1;
            List<String> dependPkgs = new ArrayList();
            for (i = 0; i < NP; i++) {
                pi = (RunningAppProcessInfo) processes.get(i);
                if (pi != null && pi.pkgList != null && pi.pid == dependPid.intValue()) {
                    dependUid = pi.uid;
                    dependPkgs = Arrays.asList(pi.pkgList);
                    break;
                }
            }
            if (dependUid != -1 && UserHandle.getAppId(dependUid) >= 10000) {
                pkgList.addAll(dependPkgs);
            }
        }
        return pkgList;
    }

    private ArrayList<Integer> getDependPids(Integer pid) {
        ArrayList<Integer> dependPidList = new ArrayList();
        if (pid == null) {
            return dependPidList;
        }
        synchronized (mProcessDependencyMap) {
            for (Entry entry : mProcessDependencyMap.entrySet()) {
                Integer dependPid = (Integer) entry.getKey();
                if (!pid.equals(dependPid) && ((ArrayList) entry.getValue()).contains(pid)) {
                    dependPidList.add(dependPid);
                }
            }
        }
        return dependPidList;
    }

    public boolean isDependsAudioActiveApp(ArrayList<Integer> pids) {
        if (pids == null) {
            return false;
        }
        ArrayList<Integer> dependedPids = getDependedPids(pids);
        if (dependedPids != null) {
            for (Integer pid : dependedPids) {
                if (this.mIDeviceState.isPlayingSound(pid.intValue())) {
                    Log.i("AppManager", pids + " depends " + pid + " playing audio.");
                    return true;
                }
            }
        }
        return false;
    }

    private HashSet<Integer> getFrontProcessIds() {
        Map<String, AppStateRecord> smartHibernationApps = ASHManager.getPGDebugUI().getApplicationMap();
        HashSet<Integer> processIDSets = new HashSet();
        for (String pkgName : ASHManager.getPGDebugUI().getAboveLauncherPkgs()) {
            AppStateRecord rec = (AppStateRecord) smartHibernationApps.get(pkgName);
            if (rec != null) {
                processIDSets.addAll(rec.getPids());
            }
        }
        return processIDSets;
    }

    private HashSet<Integer> getFrontAppDependencyPIDs() {
        HashSet<Integer> dependendedPIDSet = new HashSet();
        HashSet<Integer> frontPIDSet = getFrontProcessIds();
        synchronized (mProcessDependencyMap) {
            for (Integer pid : frontPIDSet) {
                ArrayList<Integer> dependedPIDs = (ArrayList) mProcessDependencyMap.get(pid);
                if (dependedPIDs != null) {
                    dependendedPIDSet.addAll(dependedPIDs);
                }
            }
        }
        return dependendedPIDSet;
    }

    public boolean isDependedByFrontApp(String pkgName) {
        HashSet<Integer> allDependencySet = getFrontAppDependencyPIDs();
        AppStateRecord rec = (AppStateRecord) ASHManager.getPGDebugUI().getApplicationMap().get(pkgName);
        if (rec != null) {
            for (Integer pid : rec.getPids()) {
                if (allDependencySet.contains(pid)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void handleNonActiveTimeout() {
        this.mWakelockMonitor.handleNonActiveTimeout();
    }

    public long getTotalWakeupsSinceScrOff() {
        return this.mAlarmStats.getTotalWakeupsSinceScrOff();
    }

    public boolean isAlarmFreqEmpty() {
        return this.mAlarmStats.isAlarmFreqEmpty();
    }

    public ArrayList<String> getCurScrOffAlarmApps(int topNum, int maxFreq) {
        return this.mAlarmStats.getCurScrOffAlarmApps(topNum, maxFreq);
    }

    public int getCurScrOffAlarmCount(String pkg) {
        return this.mAlarmStats.getCurScrOffAlarmCount(pkg);
    }

    public int getCurScrOffAlarmFreq(String pkg) {
        return this.mAlarmStats.getCurScrOffAlarmFreq(pkg);
    }

    public int getTotalScrOffAlarmCount(String pkg) {
        return this.mAlarmStats.getTotalScrOffAlarmCount(pkg);
    }

    public int getTotalScrOffAlarmFreq(String pkg) {
        return this.mAlarmStats.getTotalScrOffAlarmFreq(pkg);
    }

    public ArrayList<String> getTotalScrOffAlarmApps(int topNum, int maxInterval, int minCnt) {
        return this.mAlarmStats.getTotalScrOffAlarmApps(topNum, maxInterval, minCnt);
    }

    protected void dispatchStateAction(int actionId) {
        StateAction stAction = StateAction.obtain();
        stAction.resetAs(actionId, 0, "");
        notifyPowerActionChanged(this.mICoreContext, stAction);
    }

    public boolean isCleanDBExist() {
        return this.mAppBlackWhitelist.isCleanDBExist();
    }

    public boolean isStandbyDBExist() {
        return this.mAppBlackWhitelist.isStandbyDBExist();
    }

    public boolean isStandbyProtectApp(String pkg) {
        return this.mAppBlackWhitelist.isStandbyProtectApp(pkg);
    }

    public boolean isStandbyUnprotectApp(String pkg) {
        return this.mAppBlackWhitelist.isStandbyUnprotectApp(pkg);
    }

    public boolean isCleanProtectApp(String pkg) {
        return this.mAppBlackWhitelist.isCleanProtectApp(pkg);
    }

    public boolean isCleanUnprotectApp(String pkg) {
        return this.mAppBlackWhitelist.isCleanUnprotectApp(pkg);
    }

    public String getNFCPayApp() {
        return this.mAppBlackWhitelist.getNFCPayApp();
    }

    public ArrayList<String> getCleanProtectApps() {
        return this.mAppBlackWhitelist.getCleanProtectApps();
    }

    public ArrayList<String> getCleanUnprotectApps() {
        return this.mAppBlackWhitelist.getCleanUnprotectApps();
    }

    public boolean isIgnoreGpsApp(String pkg) {
        return this.mAppBlackWhitelist.isIgnoreGpsApp(pkg);
    }

    public boolean isExtrModeV2ReserveApp(String pkg) {
        return this.mAppBlackWhitelist.isExtrModeV2ReserveApp(pkg);
    }

    public ArrayList<String> getExtrModeV2ReserveApps() {
        return this.mAppBlackWhitelist.getExtrModeV2ReserveApps();
    }

    public int getAppType(String pkgName) {
        return this.mAppTypeRecognise.getAppType(pkgName);
    }

    public ArrayList<String> getAppsByType(int type) {
        return this.mAppTypeRecognise.getAppsByType(type);
    }

    public void updateAppType(int newType, String appPkg) {
        this.mAppTypeRecognise.updateAppType(newType, appPkg);
    }

    public String getUsingLauncher() {
        return this.mAppTypeRecognise.getUsingLauncher();
    }

    public String getDefaultLauncher() {
        return this.mAppTypeRecognise.getDefaultLauncher();
    }

    public String getCurLiveWallpaper() {
        return this.mAppTypeRecognise.getCurLiveWallpaper();
    }

    public String getDefaultInputMethod() {
        return this.mAppTypeRecognise.getDefaultInputMethod();
    }

    public String getDefaultSmsApplication() {
        return CommonAdapter.getDefaultSmsApplication(this.mContext);
    }

    public String getPlayingPkg() {
        return this.mPlayingAudioPkg;
    }

    public ArrayList<String> getAudioPlayingPkg() {
        ArrayList<String> audioPlayingPkg = new ArrayList();
        if (this.mAudioManager.isMusicActive() || this.mIDeviceState.isPlayingSound() || this.mIDeviceState.isHeadsetOn()) {
            audioPlayingPkg.add("com.duomi.android");
            if (!(this.mPlayingAudioPkg == null || audioPlayingPkg.contains(this.mPlayingAudioPkg))) {
                audioPlayingPkg.add(this.mPlayingAudioPkg);
            }
            if (this.mAppTypeRecognise != null) {
                ArrayList<String> allMusic = this.mAppTypeRecognise.getAppsByType(12);
                if (allMusic != null) {
                    audioPlayingPkg.addAll(allMusic);
                }
            }
        }
        return audioPlayingPkg;
    }

    public void setAppInactive(String packageName, boolean inactive) {
        this.mAppStandbyDozeAdapter.setAppInactive(packageName, inactive);
    }

    public void forceDeviceToIdle() {
        this.mAppStandbyDozeAdapter.forceDeviceToIdle();
    }

    public void exitDeviceIdle() {
        this.mAppStandbyDozeAdapter.exitDeviceIdle();
    }

    public boolean isDeviceIdleMode() {
        return this.mAppStandbyDozeAdapter.isDeviceIdleMode();
    }

    public void addWhiteList(String pkg) {
        this.mAppStandbyDozeAdapter.addWhiteList(pkg);
    }

    public void removeWhiteList(String pkg) {
        this.mAppStandbyDozeAdapter.removeWhiteList(pkg);
    }

    public void removeProtectAppsFromIAware(ArrayList<String> cleanApps) {
        if (CARE_IAWARE_PROTECT_APP) {
            if (cleanApps.size() > 0 && (this.mIPolicy.getPowerMode() == 2 || this.mIPolicy.getPowerMode() == 3)) {
                long st = SystemClock.uptimeMillis();
                List<String> iAwareList = CommonAdapter.getIAwareProtectList();
                if (iAwareList != null) {
                    Log.i("AppManager", "clean apps: " + cleanApps);
                    Log.i("AppManager", "iAware protect apps: " + iAwareList + " spend(ms):" + (SystemClock.uptimeMillis() - st));
                    String autoFrontPkgAfterScrOff = this.mIScenario.getAutoFrontPkgAfterScrOff();
                    if (autoFrontPkgAfterScrOff != null && iAwareList.contains(autoFrontPkgAfterScrOff)) {
                        iAwareList.remove(autoFrontPkgAfterScrOff);
                        Log.i("AppManager", "from iaware protect apps remove auto front pkg: " + autoFrontPkgAfterScrOff);
                    }
                    for (String pkg : cleanApps) {
                        if (iAwareList.contains(pkg) && ("com.huawei.espacev2".equals(pkg) || isIgnoreGpsApp(pkg) || isIgnoreAudioApp(pkg) || isForceCleanApp(pkg))) {
                            iAwareList.remove(pkg);
                            Log.i("AppManager", "remove history high power pkg: " + pkg);
                        }
                    }
                    if (this.mHighPowerHistoryPkgs.size() > 0) {
                        iAwareList.removeAll(this.mHighPowerHistoryPkgs);
                        Log.i("AppManager", "from iaware protect apps remove history high power apps: " + this.mHighPowerHistoryPkgs);
                        Log.i("AppManager", "remian iaware protect apps: " + iAwareList);
                    }
                    synchronized (this.mNotCleanAppsByIAware) {
                        this.mNotCleanAppsByIAware.clear();
                        this.mNotCleanAppsByIAware.addAll(iAwareList);
                        iAwareList.removeAll(cleanApps);
                        cleanApps.removeAll(this.mNotCleanAppsByIAware);
                        this.mNotCleanAppsByIAware.removeAll(iAwareList);
                    }
                    if (DbgUtils.DBG_USB) {
                        Log.i("AppManager", "remian clean: " + cleanApps + " not clean: " + this.mNotCleanAppsByIAware);
                    }
                    if (this.mNotCleanAppsByIAware.size() > 0) {
                        IPowerStats ips = (IPowerStats) this.mICoreContext.getService("powerstats");
                        if (ips != null) {
                            ips.iStats(1, this.mNotCleanAppsByIAware);
                        }
                    }
                } else {
                    Log.i("AppManager", "getIAwareProtectList return null ");
                }
            }
            return;
        }
        Log.i("AppManager", "Don't care iaware protect apps");
    }

    public boolean isIAwareProtectNotCleanApp(String pkg) {
        if (this.mNotCleanAppsByIAware.size() <= 0 || !this.mNotCleanAppsByIAware.contains(pkg)) {
            return false;
        }
        return true;
    }

    public boolean isForeignSuperApp(String pkgName) {
        return this.mAppBlackWhitelist.isForeignSuperApp(pkgName);
    }

    public boolean isForeignSuperAppPolicy() {
        return this.mAppBlackWhitelist.isForeignSuperAppPolicy();
    }

    public void dump(PrintWriter pw, String[] args) {
        pw.println("\nAPP MANAGER");
        pw.println("  App Dependency:");
        for (Entry entry : mProcessDependencyMap.entrySet()) {
            ArrayList<Integer> dependedPids = (ArrayList) entry.getValue();
            pw.println("        Pid:" + ((Integer) entry.getKey()) + " depends-> " + dependedPids);
        }
    }
}
