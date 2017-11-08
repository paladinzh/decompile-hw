package com.huawei.powergenie.modules.apppower.hibernation;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import com.huawei.powergenie.R;
import com.huawei.powergenie.api.IAppManager;
import com.huawei.powergenie.api.IAppType;
import com.huawei.powergenie.api.IContextAware;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.IPolicy;
import com.huawei.powergenie.api.IScenario;
import com.huawei.powergenie.core.KStateAction;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.core.StateAction;
import com.huawei.powergenie.core.XmlHelper;
import com.huawei.powergenie.debugtest.DbgUtils;
import com.huawei.powergenie.debugtest.LogUtils;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import com.huawei.powergenie.modules.apppower.hibernation.actions.adapter.ASHAdapter;
import com.huawei.powergenie.modules.apppower.hibernation.states.AppStateRecord;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class ASHManager implements ASHStateInterface {
    private static final boolean DEBUG_USB = DbgUtils.DBG_USB;
    private static final ArrayList<String> mBlackListApps = new ArrayList<String>() {
    };
    private static final ArrayList<Integer> mCanNotProxyApps = new ArrayList();
    private static final ArrayList<Integer> mCanProxyApps = new ArrayList();
    private static final ArrayList<String> mForeignProtectApps = new ArrayList();
    private static final ArrayList<String> mFrontPkgsAboveLauncher = new ArrayList();
    private static ASHStateInterface mStateInterface;
    private static final ArrayList<String> mSystemCoreApps = new ArrayList();
    private static final ArrayList<String> mSystemCoreProcesses = new ArrayList();
    private boolean doCheckUnMactchPid = false;
    private Context mContext;
    private String mCurLiveWallpaperPkg = null;
    private long mExitFreezeTime = 0;
    private String mFrontPkg = null;
    private AshHandler mHandler;
    private final IAppManager mIAppManager;
    private final IAppType mIAppType;
    private final IContextAware mIContextAware;
    private ICoreContext mICoreContext;
    private final IDeviceState mIDeviceState;
    private final IPolicy mIPolicy;
    private final IScenario mIScenario;
    private boolean mIsInitApps = false;
    private boolean mIsNeedUnfreezeImportantApp = true;
    private boolean mIsPowerSaveMode = true;
    private int mLastPid = 0;
    private String mLastSpeedupPkg = "";
    private final int mModId;
    private long mMsgTimeoutLastCheck = 0;
    private final HashMap<String, AppStateRecord> mSHDeadApps = new HashMap();
    private final HashMap<String, AppStateRecord> mSmartHibernationApps = new HashMap();
    private long mStartScreenOnTime = SystemClock.elapsedRealtime();

    private final class AshHandler extends Handler {
        public AshHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    ASHManager.this.initRunningApps();
                    return;
                case 101:
                    if (ASHManager.this.mIsPowerSaveMode) {
                        ASHManager.this.updateAppsFromOS();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public ASHManager(ICoreContext coreContext, int modId) {
        this.mModId = modId;
        this.mContext = coreContext.getContext();
        mStateInterface = this;
        this.mICoreContext = coreContext;
        this.mIAppManager = (IAppManager) coreContext.getService("appmamager");
        this.mIDeviceState = (IDeviceState) coreContext.getService("device");
        this.mIContextAware = (IContextAware) coreContext.getService("ca");
        this.mIPolicy = (IPolicy) coreContext.getService("policy");
        this.mIAppType = (IAppType) coreContext.getService("appmamager");
        this.mIScenario = (IScenario) coreContext.getService("scenario");
    }

    public void handleStart() {
        ASHLog.i("Start !");
        this.mHandler = new AshHandler(this.mICoreContext.getActionsHandlerLooper());
        if ((!DEBUG_USB && this.mIDeviceState.isCharging()) || isNeedCloseAsh()) {
            this.mIsPowerSaveMode = false;
            ASHLog.i("charging ash power disable!");
        }
        initProtectApps();
    }

    public boolean handleAction(PowerAction action) {
        int actionId = action.getActionId();
        KStateAction kAction;
        switch (actionId) {
            case NativeAdapter.PLATFORM_HI /*2*/:
            case 4:
                handleUserWalking();
                break;
            case 208:
                break;
            case 224:
                handleAppsAlarm(action.getPkgName());
                break;
            case 226:
                handleNotification(true, action.getPkgName(), false, action.getExtraString());
                break;
            case 227:
                handleNotification(false, action.getPkgName(), action.getExtraBoolean(), action.getExtraString());
                break;
            case 230:
                handleAppFront(action.getPkgName(), true);
                break;
            case 245:
                this.mLastSpeedupPkg = action.getPkgName();
                break;
            case 255:
                if (this.mIsInitApps && this.mIsPowerSaveMode) {
                    AppStateRecord appRecord = getAppRecord(action.getPkgName());
                    if (appRecord != null) {
                        appRecord.handleAudioStart();
                        break;
                    }
                }
                break;
            case 256:
                if (this.mIsInitApps && this.mIsPowerSaveMode) {
                    handleFreezerAction(action.getPkgName(), action.getExtraString(), action.getExtraInt(), action.getExtraValString("from"));
                    break;
                }
            case 260:
                if (!this.mIsInitApps || !this.mIsPowerSaveMode) {
                    this.mIAppManager.removeProcessDependency(((KStateAction) action).getPid());
                    break;
                }
                kAction = (KStateAction) action;
                handleProcessExit(kAction.getPid());
                ArrayList<Integer> value = kAction.getUid();
                if (!this.mIDeviceState.isScreenOff() && value != null && value.size() > 0 && ((Integer) value.get(0)).intValue() == 0) {
                    exitFreeze("low_memory");
                    break;
                }
                break;
            case 261:
                KStateAction ksAction = (KStateAction) action;
                if (!this.mIsInitApps || !this.mIsPowerSaveMode) {
                    recordCanProxyApps(ksAction.getUid());
                    break;
                }
                handleNetPacket(ksAction.getUid());
                break;
                break;
            case 263:
                break;
            case 264:
                handleTopViewChanged(false, action.getPkgName(), action.getExtraInt());
                break;
            case 270:
                if (this.mIsInitApps && this.mIsPowerSaveMode) {
                    if (action.getExtraInt() < this.mLastPid) {
                        this.doCheckUnMactchPid = true;
                    }
                    this.mLastPid = action.getExtraInt();
                    handleProcessStart(action.getExtraInt(), (int) action.getExtraLong(), action.getPkgName(), null, action.getExtraString());
                    break;
                }
            case 273:
                handleAppWidgetEnabled(action.getPkgName());
                break;
            case 274:
                if (this.mIsInitApps && this.mIsPowerSaveMode) {
                    kAction = (KStateAction) action;
                    ArrayList<Integer> callingPids = kAction.getUid();
                    int callingPid = -1;
                    if (callingPids != null) {
                        callingPid = ((Integer) callingPids.get(0)).intValue();
                    }
                    handleBinderCall(callingPid, kAction.getPid());
                    break;
                }
            case 275:
                handleCtsState(action.getExtraBoolean());
                break;
            case 277:
                if (this.mIsInitApps && this.mIsPowerSaveMode) {
                    delayUpdateApps(3000);
                    break;
                }
            case 278:
                updateAppType();
                break;
            case 279:
                handleActiveHighPowerGps(action.getPkgName());
                break;
            case 280:
                handleUnfreezeDependPids(action.getExtraListInteger());
                break;
            case 281:
                requestHibernateApps(action.getExtraListString(), action.getExtraString());
                break;
            case 283:
            case 337:
                String nfcApp = this.mIAppManager.getNFCPayApp();
                if (nfcApp != null) {
                    handleNFCPayChg(nfcApp);
                    break;
                }
                break;
            case 284:
                requestWakeupApps(action.getExtraListString(), action.getExtraString());
                break;
            case 300:
                handleScreenOn();
                if (this.mIsInitApps && this.mIsPowerSaveMode) {
                    delayUpdateApps(60000);
                    break;
                }
            case 301:
                this.mHandler.removeMessages(101);
                handleScreenOff();
                this.mIsNeedUnfreezeImportantApp = true;
                break;
            case 302:
                delayToInitApps();
                break;
            case 303:
                handleShutdownEvent();
                break;
            case 304:
                handleSreenUnlock();
                break;
            case 305:
                handlePackageState(false, action.getExtraString());
                break;
            case 307:
                handlePackageState(true, action.getExtraString());
                break;
            case 310:
                if (!DEBUG_USB) {
                    stopPowerSaveMode();
                    break;
                }
                break;
            case 311:
                if (!isNeedCloseAsh()) {
                    restartPowerSaveMode();
                    break;
                }
                break;
            case 312:
            case 314:
                handleConnectivityChange();
                break;
            case 318:
                handleWallpaperChanged();
                break;
            case 322:
                handleCallBusy();
                break;
            case 324:
                handleCrashRestart();
                delayToInitApps();
                break;
            case 350:
                handlePowerModeChanged(action.getExtraInt());
                break;
            case 359:
                ASHLog.i("handle user switched to:" + ((StateAction) action).getIntent().getExtra("android.intent.extra.user_handle"));
                break;
            case 508:
                unFreezeImportantApps();
                break;
            default:
                ASHLog.d("unknown action:" + actionId);
                break;
        }
        if (this.mFrontPkg == null || !this.mFrontPkg.equals(action.getPkgName())) {
            handleAppFront(action.getPkgName(), false);
        }
        return true;
    }

    public void handlePackageState(boolean removed, String pkgName) {
        AppStateRecord appRecord;
        if (!removed) {
            appRecord = (AppStateRecord) this.mSmartHibernationApps.get(pkgName);
            if (appRecord != null) {
                ASHLog.i("add app :" + pkgName);
                appRecord.handlePkgInstalled();
            }
        } else if (this.mIAppManager.getCurUserId() == 0 || this.mIAppManager.getUidByPkgFromOwner(pkgName) <= 0) {
            appRecord = (AppStateRecord) this.mSmartHibernationApps.remove(pkgName);
            if (appRecord != null) {
                ASHLog.i(pkgName + " pkg removed, remove record from SHA.");
                appRecord.handleExit(false);
            } else if (((AppStateRecord) this.mSHDeadApps.remove(pkgName)) != null) {
                ASHLog.i(pkgName + " pkg removed, remove record from SHDA.");
            }
        } else {
            ASHLog.i(pkgName + " remove from guest mode, reserve owner record.");
        }
    }

    public void handleShutdownEvent() {
        for (Entry entry : this.mSmartHibernationApps.entrySet()) {
            ((AppStateRecord) entry.getValue()).handleExit(false);
        }
        this.mSmartHibernationApps.clear();
    }

    private void delayToInitApps() {
        if (!this.mIsInitApps) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100), 30000);
        }
    }

    private void initRunningApps() {
        ASHLog.i("init running apps ");
        this.mIsInitApps = true;
        this.mCurLiveWallpaperPkg = this.mIAppType.getCurLiveWallpaper();
        if (this.mIsPowerSaveMode) {
            updateAppsFromOS();
        }
        this.mICoreContext.addAction(this.mModId, 261);
        this.mICoreContext.addAction(this.mModId, 260);
        this.mICoreContext.addAction(this.mModId, 274);
        this.mICoreContext.addAction(this.mModId, 270);
    }

    private void initProtectApps() {
        XmlHelper.loadResAppList(this.mContext, R.xml.system_core_processes, null, mSystemCoreProcesses);
        XmlHelper.loadResAppList(this.mContext, R.xml.system_core_apps, null, mSystemCoreApps);
        ArrayList<String> custProtectApps = new ArrayList();
        XmlHelper.loadCustAppList("ash_protect_apps.xml", null, custProtectApps);
        if (custProtectApps.size() > 0) {
            ASHLog.i("initProtectApps custProtectApps = " + custProtectApps);
            for (String name : custProtectApps) {
                if (!mSystemCoreApps.contains(name)) {
                    mSystemCoreApps.add(name);
                }
            }
        }
        ArrayList<String> locationProvider = this.mIAppType.getAppsByType(14);
        ASHLog.i("location Provider :" + locationProvider);
        mSystemCoreApps.addAll(locationProvider);
    }

    private void delayUpdateApps(long delay) {
        this.mHandler.removeMessages(101);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(101), delay);
    }

    private void recordCanProxyApps(ArrayList<Integer> uidList) {
        if (uidList.size() != 2) {
            ASHLog.i("not can proxy apps message, ignore.");
            return;
        }
        int state = ((Integer) uidList.get(0)).intValue();
        int pid = ((Integer) uidList.get(1)).intValue();
        ASHLog.i("recordCanProxyApps: state: " + state + ", pid: " + pid);
        if (state == -1) {
            if (!mCanProxyApps.contains(Integer.valueOf(pid))) {
                mCanProxyApps.add(Integer.valueOf(pid));
            }
        } else if (state == -2) {
            if (mCanProxyApps.contains(Integer.valueOf(pid))) {
                mCanProxyApps.remove(Integer.valueOf(pid));
            } else {
                mCanNotProxyApps.add(Integer.valueOf(pid));
            }
        }
    }

    private void distributeCanProxyPid() {
        ASHLog.i("distributeCanProxyPid: mCanProxyApps: " + mCanProxyApps + ", mCanNotProxyApps: " + mCanNotProxyApps);
        for (Entry entry : this.mSmartHibernationApps.entrySet()) {
            AppStateRecord appRecord = (AppStateRecord) entry.getValue();
            for (Integer pid : mCanProxyApps) {
                if (appRecord.hasPid(pid.intValue())) {
                    appRecord.addBastetPid(pid.intValue());
                }
            }
            for (Integer pid2 : mCanNotProxyApps) {
                if (appRecord.hasPid(pid2.intValue())) {
                    appRecord.removeBastetPid(pid2.intValue());
                }
            }
        }
        mCanProxyApps.clear();
        mCanNotProxyApps.clear();
    }

    private void updateAppsFromOS() {
        if (this.mICoreContext.isRestartAfterCrash()) {
            ASHLog.w("not handle process start when update apps,because pg was crashed.");
        }
        ASHLog.i("refresh apps");
        List<RunningAppProcessInfo> processes = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        int NP = processes != null ? processes.size() : 0;
        ArrayList<Integer> deadPids = getAllPids();
        ASHLog.i("all pids: " + deadPids);
        for (int i = 0; i < NP; i++) {
            RunningAppProcessInfo pi = (RunningAppProcessInfo) processes.get(i);
            if (deadPids.contains(Integer.valueOf(pi.pid))) {
                deadPids.remove(Integer.valueOf(pi.pid));
            } else if (!this.mICoreContext.isRestartAfterCrash() && pi.pid > 0) {
                handleProcessStart(pi.pid, pi.uid, pi.processName, Arrays.asList(pi.pkgList), null);
            }
        }
        if (deadPids.size() > 0) {
            ASHLog.w("dead pids:" + deadPids);
            for (Integer pid : deadPids) {
                handleProcessExit(pid.intValue());
            }
        }
    }

    private boolean isSystemCoreProc(String procName) {
        if (procName == null || !mSystemCoreProcesses.contains(procName)) {
            return false;
        }
        return true;
    }

    private boolean isSystemCoreApp(List<String> pkgs) {
        if (pkgs != null) {
            for (String name : pkgs) {
                if (!mSystemCoreApps.contains(name)) {
                    if (mForeignProtectApps.contains(name)) {
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean isMatchSystemCoreApp(String procName) {
        if (procName != null) {
            for (String name : mSystemCoreApps) {
                if (procName.startsWith(name)) {
                    return true;
                }
            }
        }
        for (String name2 : mForeignProtectApps) {
            if (procName.startsWith(name2)) {
                return true;
            }
        }
        return false;
    }

    private boolean exitFreeze(String reason) {
        long now = SystemClock.elapsedRealtime();
        if (now - this.mExitFreezeTime < 60000) {
            return false;
        }
        if (now - this.mStartScreenOnTime < 10000) {
            ASHLog.i("just screen on, do noting");
            return false;
        }
        ASHLog.i("exit freeze, reason:" + reason);
        int i = 5;
        Iterator iter = this.mSmartHibernationApps.entrySet().iterator();
        while (iter.hasNext() && i > 0) {
            if (((AppStateRecord) ((Entry) iter.next()).getValue()).requestRunning(reason)) {
                i--;
            }
        }
        this.mExitFreezeTime = now;
        return true;
    }

    private void handleWallpaperChanged() {
        ASHLog.i("handleWallpaperChanged ");
        String newLiveWallpaper = this.mIAppType.getCurLiveWallpaper();
        AppStateRecord appRecord = getAppRecord(newLiveWallpaper);
        if (appRecord != null) {
            appRecord.handleWallpaperChanged(true);
        }
        appRecord = getAppRecord(this.mCurLiveWallpaperPkg);
        if (appRecord != null) {
            appRecord.handleWallpaperChanged(false);
        }
        this.mCurLiveWallpaperPkg = newLiveWallpaper;
    }

    private void handleAppWidgetEnabled(String name) {
        AppStateRecord appRecord = getAppRecord(name);
        if (appRecord != null) {
            appRecord.handleAppWidgetEnabled();
        }
    }

    private void handleFreezerAction(String exceptionType, String pkgName, int processId, String fromPkg) {
        if ("broadcast".equals(exceptionType)) {
            handleBroadcastANR(pkgName, processId);
        } else if (exceptionType != null && (exceptionType.endsWith("startservice") || exceptionType.endsWith("serviceboot") || exceptionType.endsWith("startprovider") || exceptionType.endsWith("bindservice") || exceptionType.endsWith("cleanUpservice") || exceptionType.endsWith("mediakey") || exceptionType.endsWith("acquire_provider"))) {
            handleCalledByOtherApp(exceptionType, pkgName, processId, fromPkg);
        } else if ("overflow_bc".equals(exceptionType)) {
            handleBCOverflow(exceptionType, pkgName, processId);
        } else if ("input_timeout".equals(exceptionType)) {
            handleTouchInput(exceptionType, pkgName, processId);
        } else {
            handleBCNotify(exceptionType, pkgName, processId, fromPkg);
        }
    }

    private void handleBCNotify(String action, String pkgName, int processId, String fromPkg) {
        ASHLog.i(fromPkg + " send broadcast:" + action + " notify package: " + pkgName + ", pid: " + processId);
        AppStateRecord appRecord = getAppRecord(pkgName);
        if (appRecord != null) {
            appRecord.handleBCNotify(action);
        }
    }

    private void handleBCOverflow(String action, String pkgName, int processId) {
        ASHLog.i("package = " + pkgName + ", pid = " + processId + " action =" + action + ", bc unfreeze app");
        AppStateRecord appRecord = getAppRecord(pkgName);
        if (appRecord != null) {
            appRecord.handleBCOverflow();
        } else if (processId != -1) {
            for (Entry entry : this.mSmartHibernationApps.entrySet()) {
                appRecord = (AppStateRecord) entry.getValue();
                if (appRecord.hasPid(processId)) {
                    appRecord.handleBCOverflow();
                }
            }
        }
    }

    private void handleTouchInput(String reason, String pkgName, int processId) {
        ASHLog.i("package = " + pkgName + ", pid = " + processId + ", touch input");
        if (processId > 0) {
            for (Entry entry : this.mSmartHibernationApps.entrySet()) {
                AppStateRecord appRecord = (AppStateRecord) entry.getValue();
                if (appRecord.hasPid(processId)) {
                    appRecord.requestRunning(reason);
                } else if (appRecord.isCurrentInputMethod()) {
                    appRecord.requestRunning(reason);
                }
            }
        }
    }

    private void unFreezeImportantApps() {
        if (this.mIsNeedUnfreezeImportantApp) {
            String frontPkg = this.mIScenario.getFrontPkg();
            if (frontPkg != null) {
                AppStateRecord appRecord = getAppRecord(frontPkg);
                if (appRecord != null) {
                    appRecord.requestRunning("scron_front");
                }
            }
            for (Entry entry : this.mSmartHibernationApps.entrySet()) {
                ((AppStateRecord) entry.getValue()).handleFastUnfreezeApp();
            }
        }
    }

    private void handleScreenOn() {
        ASHLog.i("ash->screen on");
        unFreezeImportantApps();
        this.mStartScreenOnTime = SystemClock.elapsedRealtime();
        for (Entry entry : this.mSmartHibernationApps.entrySet()) {
            ((AppStateRecord) entry.getValue()).handleScreenOn();
        }
        this.mIsNeedUnfreezeImportantApp = false;
        if (!this.mIDeviceState.isKeyguardPresent()) {
            handleSreenUnlock();
        }
    }

    private void handleSreenUnlock() {
        ASHLog.i("ash->screen unlock");
        unFreezeImportantApps();
        for (Entry entry : this.mSmartHibernationApps.entrySet()) {
            ((AppStateRecord) entry.getValue()).handleSreenUnlock();
        }
        this.mIContextAware.stopMotionDetection();
    }

    private void handleScreenOff() {
        ASHLog.i("ash->screen off");
        boolean needMotionDetection = false;
        for (Entry entry : this.mSmartHibernationApps.entrySet()) {
            AppStateRecord appRecord = (AppStateRecord) entry.getValue();
            appRecord.handleScreenOff();
            if (appRecord.hasActiveSensor()) {
                needMotionDetection = true;
            }
        }
        if (needMotionDetection || this.mIDeviceState.hasActiveGps()) {
            this.mIContextAware.startMotionDetection(30);
        }
    }

    private void handleCalledByOtherApp(String exceptionType, String pkgName, int processId, String fromPkg) {
        AppStateRecord appRecord = getAppRecord(pkgName);
        if (appRecord != null) {
            ASHLog.i("pkg " + fromPkg + " calling: " + pkgName + ", pid: " + processId + " ,type: " + exceptionType);
            appRecord.handleCalledByOtherApp(exceptionType);
        } else if (processId != 0) {
            for (Entry entry : this.mSmartHibernationApps.entrySet()) {
                AppStateRecord record = (AppStateRecord) entry.getValue();
                if (record.getPids().contains(Integer.valueOf(processId))) {
                    ASHLog.i("pid " + fromPkg + " calling: " + pkgName + ", pid: " + processId + " ,type: " + exceptionType);
                    record.handleCalledByOtherApp(exceptionType);
                }
            }
        }
    }

    private void handleBroadcastANR(String pkgName, int processId) {
        AppStateRecord appRecord = getAppRecord(pkgName);
        if (appRecord != null) {
            ASHLog.i("pkg:" + pkgName + " pid:" + processId + " broadcast will anr");
            appRecord.handleBroadcastANR();
        }
        for (Entry entry : this.mSmartHibernationApps.entrySet()) {
            AppStateRecord record = (AppStateRecord) entry.getValue();
            if (record.getPids().contains(Integer.valueOf(processId))) {
                ASHLog.i("find by pid pkg:" + pkgName + " pid:" + processId + " broadcast will anr");
                record.handleBroadcastANR();
            }
        }
    }

    private void handleProcessStart(int pid, int uid, String procName, List<String> pkgList, String reason) {
        if (this.mIDeviceState.isScreenOff() && reason != null) {
            ASHLog.i("start process:" + procName + " reason:" + reason + " pid:" + pid + " uid:" + uid + " pkgs:" + pkgList);
        }
        if (this.doCheckUnMactchPid && getAllPids().contains(Integer.valueOf(pid))) {
            ASHLog.i("find pid has exist in ash ,exit it : pid:" + pid);
            handleProcessExit(pid);
        }
        if (UserHandle.getAppId(uid) >= 10000 && !isSystemCoreProc(procName)) {
            AppStateRecord appRecord;
            if (pkgList == null) {
                pkgList = this.mIAppManager.getPkgNameByUid(this.mContext, uid);
                if (pkgList == null) {
                    ASHLog.e("start process:" + procName + " not find pkgs");
                    return;
                } else if (!isProcAndPkgMatch(pkgList, procName)) {
                    pkgList = this.mIAppManager.getPkgFromSystem(uid);
                    ASHLog.i("refresh proc and package match :" + pkgList);
                }
            }
            if (this.mIAppManager.isStandbyDBExist()) {
                if (!isAllowedFrzStandbyApps(pkgList)) {
                    ASHLog.d("sm not allow frz app:" + pkgList);
                    return;
                } else if (isSystemCoreApp(pkgList)) {
                    ASHLog.i("not ctrl importance proc:" + procName);
                    return;
                } else if (isMatchSystemCoreApp(procName)) {
                    ASHLog.i("not ctrl system core app proc:" + procName);
                    return;
                } else if (pkgList.size() > 0 && isNoIconSystemApp((String) pkgList.get(0))) {
                    ASHLog.i("sm allow frz hide app:" + procName);
                }
            } else if (!isSystemCoreApp(pkgList) && !isMatchSystemCoreApp(procName)) {
                for (String pkg : pkgList) {
                    if (isNoIconSystemApp(pkg)) {
                        return;
                    }
                }
            } else {
                return;
            }
            ASHLog.d("init process:" + procName);
            if (pkgList.size() > 1 && reason != null) {
                ArrayList<String> pkgsInPid = new ArrayList();
                for (String appPkg : pkgList) {
                    appRecord = getAppRecord(appPkg);
                    if (appRecord == null) {
                        appRecord = removeAppDeadRecord(appPkg);
                        if (appRecord != null) {
                            this.mSHDeadApps.put(appPkg, appRecord);
                        }
                    }
                    if (appRecord == null) {
                        appRecord = new AppStateRecord(this.mICoreContext, this.mContext, appPkg);
                        this.mSHDeadApps.put(appPkg, appRecord);
                    }
                    if (appRecord.hasProcName(procName)) {
                        pkgsInPid.add(appPkg);
                        ASHLog.i(appPkg + " runs in the process:" + procName);
                    } else {
                        ASHLog.i(appPkg + " don't runs in the process:" + procName);
                    }
                }
                pkgList = pkgsInPid;
            }
            boolean doUpdate = false;
            for (String appPkg2 : pkgList) {
                appRecord = getAppRecord(appPkg2);
                if (appRecord == null) {
                    appRecord = removeAppDeadRecord(appPkg2);
                    if (appRecord == null) {
                        appRecord = addAppRecord(appPkg2);
                    } else if (32 > this.mSmartHibernationApps.size()) {
                        this.mSmartHibernationApps.put(appPkg2, appRecord);
                        appRecord.handleRestart();
                    } else {
                        ASHLog.w("warnning: app record upto the max:32");
                        LogUtils.c("ASH_OUT_CTRL_APP", appPkg2);
                        return;
                    }
                }
                if (appRecord != null) {
                    if (pkgList.size() > 1) {
                        appRecord.addSharePid(pid, pkgList);
                    }
                    appRecord.handleProcessStart(pid, uid, isVisibleApp(appPkg2, reason));
                    if ("com.tencent.mm:push".equals(procName)) {
                        appRecord.setMmPushPid(pid);
                    }
                    if (appRecord.getPids().size() > 15) {
                        ASHLog.i("pkg:" + appRecord.getPkgName() + ", has too many pid,will do updateApps");
                        doUpdate = true;
                    }
                }
            }
            if (doUpdate) {
                updateAppsFromOS();
            }
        }
    }

    private void handleNFCPayChg(String nfcApp) {
        for (Entry entry : this.mSmartHibernationApps.entrySet()) {
            AppStateRecord appRecord = (AppStateRecord) entry.getValue();
            if (nfcApp.equals(appRecord.getPkgName())) {
                appRecord.handleNFCPayChg();
            }
        }
    }

    private void handleUserWalking() {
        ASHLog.i("user start walking");
        for (Entry entry : this.mSmartHibernationApps.entrySet()) {
            AppStateRecord appRecord = (AppStateRecord) entry.getValue();
            if (appRecord.hasIgnoredActiveGps()) {
                appRecord.handleUserWalking();
            }
        }
    }

    private void handleCallBusy() {
        for (Entry entry : this.mSmartHibernationApps.entrySet()) {
            AppStateRecord appRecord = (AppStateRecord) entry.getValue();
            if ("com.huawei.camera".equals(appRecord.getPkgName())) {
                appRecord.handleCallBusy();
            }
        }
    }

    public boolean isAllowedFrzStandbyApps(List<String> pkgList) {
        for (String pkg : pkgList) {
            if (!this.mIAppManager.isStandbyProtectApp(pkg) && !this.mIAppManager.isStandbyUnprotectApp(pkg)) {
                return false;
            }
        }
        return true;
    }

    private boolean isProcAndPkgMatch(List<String> pkgList, String procName) {
        if (pkgList == null || procName == null) {
            ASHLog.w("pkgList or procName is null !");
            return false;
        }
        for (String name : pkgList) {
            if (procName.startsWith(name)) {
                return true;
            }
        }
        ASHLog.i("no one pkg can match the procName :" + procName + " pkgList :" + pkgList);
        return false;
    }

    private boolean isNoIconSystemApp(String pkg) {
        if (!this.mIAppManager.isSystemApp(this.mContext, pkg) || this.mIAppManager.hasLauncherIcon(this.mContext, pkg)) {
            return false;
        }
        ASHLog.i("no launcher icon system app: " + pkg);
        return true;
    }

    private boolean isVisibleApp(String appPkg, String reason) {
        boolean z = true;
        if (appPkg != null && mFrontPkgsAboveLauncher.contains(appPkg)) {
            return true;
        }
        if (reason == null) {
            return false;
        }
        if (!"activity".equals(reason)) {
            z = false;
        }
        return z;
    }

    private boolean isNeedCloseAsh() {
        if (SystemProperties.getBoolean("persist.sys.perfmode_ash_off", false)) {
            return this.mIPolicy.isOffPowerMode();
        }
        return false;
    }

    private void handleBinderCall(int callingPid, int calledpid) {
        ASHLog.i(callingPid + " binder call " + calledpid);
        for (Entry entry : this.mSmartHibernationApps.entrySet()) {
            AppStateRecord appRecord = (AppStateRecord) entry.getValue();
            if (appRecord.hasPid(calledpid)) {
                appRecord.handleBinderCall(calledpid);
            }
        }
    }

    private void handleProcessExit(int pid) {
        ASHLog.d("process exit:" + pid);
        ArrayList<String> deadApp = new ArrayList();
        for (Entry entry : this.mSmartHibernationApps.entrySet()) {
            AppStateRecord appRecord = (AppStateRecord) entry.getValue();
            if (appRecord.hasPid(pid)) {
                if (appRecord.getPids().size() <= 1) {
                    String pkgName = (String) entry.getKey();
                    deadApp.add(pkgName);
                    this.mSHDeadApps.put(pkgName, appRecord);
                    appRecord.handleExit(true, pid);
                } else {
                    appRecord.handleProcessExit(pid);
                }
            }
        }
        for (String pkg : deadApp) {
            ASHLog.i("remove app record pkg: " + pkg);
            this.mSmartHibernationApps.remove(pkg);
            mFrontPkgsAboveLauncher.remove(pkg);
        }
        this.mIAppManager.removeProcessDependency(pid);
    }

    private void stopPowerSaveMode() {
        if (this.mIsPowerSaveMode) {
            ASHLog.i("stop saving power");
            this.mIsPowerSaveMode = false;
            this.doCheckUnMactchPid = false;
            this.mLastPid = 0;
            if (this.mIsInitApps) {
                for (Entry entry : this.mSmartHibernationApps.entrySet()) {
                    AppStateRecord appRecord = (AppStateRecord) entry.getValue();
                    appRecord.handleExit(false);
                    this.mSHDeadApps.put((String) entry.getKey(), appRecord);
                }
                this.mSmartHibernationApps.clear();
            }
        }
    }

    private void restartPowerSaveMode() {
        if (!this.mIsPowerSaveMode) {
            ASHLog.i("restart saving power");
            this.mIsPowerSaveMode = true;
            if (this.mIsInitApps) {
                updateAppsFromOS();
                distributeCanProxyPid();
            }
        }
    }

    private void handlePowerModeChanged(int newMode) {
        if (this.mIPolicy.getOldPowerMode() == 4) {
            ASHLog.i(" Exit extreme mode to unfreeze all apps...");
            for (Entry entry : this.mSmartHibernationApps.entrySet()) {
                ((AppStateRecord) entry.getValue()).requestRunning("exit_extreme");
            }
        }
        if (SystemProperties.getBoolean("persist.sys.perfmode_ash_off", false)) {
            if (newMode == 3) {
                stopPowerSaveMode();
            } else if (DEBUG_USB || !this.mIDeviceState.isCharging()) {
                restartPowerSaveMode();
            }
        }
    }

    private void handleTopViewChanged(boolean add, String spid, int uid) {
        ASHLog.i("view " + (add ? "add" : "remove") + " pid:" + spid + " uid:" + uid);
        if (UserHandle.getAppId(uid) > 10000) {
            int pid = Integer.parseInt(spid);
            if (pid > 0) {
                for (Entry entry : this.mSmartHibernationApps.entrySet()) {
                    AppStateRecord appRecord = (AppStateRecord) entry.getValue();
                    if (appRecord.getPids().contains(Integer.valueOf(pid))) {
                        appRecord.handleTopViewChanged(add);
                    }
                }
            }
        }
    }

    private void handleNotification(boolean newNotice, String appPkg, boolean cancelAll, String opPkg) {
        AppStateRecord appRecord = getAppRecord(appPkg);
        if (appRecord != null) {
            appRecord.handleNotification(newNotice, cancelAll, opPkg);
        }
    }

    private void handleCtsState(boolean ctsStart) {
        if (ctsStart) {
            ASHLog.i("stop motion detection for stc");
            this.mIContextAware.stopMotionDetection();
        }
    }

    private void handleAppFront(String newFrontPkg, boolean isLauncher) {
        ASHLog.d("front pkg : " + newFrontPkg + " launcher: " + isLauncher);
        handleVisibleAppChanged(newFrontPkg, true);
        if (isLauncher) {
            for (String pkg : mFrontPkgsAboveLauncher) {
                handleVisibleAppChanged(pkg, false);
            }
            this.mFrontPkg = newFrontPkg;
            mFrontPkgsAboveLauncher.clear();
            return;
        }
        if (mFrontPkgsAboveLauncher.size() <= 1 || !mFrontPkgsAboveLauncher.contains(newFrontPkg) || this.mLastSpeedupPkg == null || this.mLastSpeedupPkg.equals(newFrontPkg)) {
            mFrontPkgsAboveLauncher.add(newFrontPkg);
        } else {
            int index = mFrontPkgsAboveLauncher.lastIndexOf(newFrontPkg);
            String rmPkg;
            if (index == mFrontPkgsAboveLauncher.size() - 2) {
                rmPkg = (String) mFrontPkgsAboveLauncher.remove(mFrontPkgsAboveLauncher.size() - 1);
                if (!mFrontPkgsAboveLauncher.contains(rmPkg)) {
                    handleVisibleAppChanged(rmPkg, false);
                }
            } else if (index == mFrontPkgsAboveLauncher.size() - 3) {
                rmPkg = (String) mFrontPkgsAboveLauncher.remove(mFrontPkgsAboveLauncher.size() - 1);
                if (!mFrontPkgsAboveLauncher.contains(rmPkg)) {
                    handleVisibleAppChanged(rmPkg, false);
                }
                rmPkg = (String) mFrontPkgsAboveLauncher.remove(mFrontPkgsAboveLauncher.size() - 1);
                if (!mFrontPkgsAboveLauncher.contains(rmPkg)) {
                    handleVisibleAppChanged(rmPkg, false);
                }
            } else {
                mFrontPkgsAboveLauncher.add(newFrontPkg);
            }
        }
        this.mFrontPkg = newFrontPkg;
        if (mFrontPkgsAboveLauncher.size() > 10) {
            mFrontPkgsAboveLauncher.remove(0);
            ASHLog.w("keep front pkg less than 10...");
        }
    }

    private void handleVisibleAppChanged(String appPkg, boolean visible) {
        if (this.mIsInitApps) {
            AppStateRecord appRecord = getAppRecord(appPkg);
            if (appRecord != null) {
                appRecord.processVisibleApp(visible);
            }
        }
    }

    private boolean isBastetProxyState(ArrayList<Integer> uidList, AppStateRecord appRecord) {
        ASHLog.d("isBastetProxyState size: " + uidList.size() + ", 0: " + uidList.get(0) + ", pids: " + appRecord.getPids());
        if (uidList.size() == 2 && ((((Integer) uidList.get(0)).intValue() == -1 || ((Integer) uidList.get(0)).intValue() == -2) && appRecord.hasPid(((Integer) uidList.get(1)).intValue()))) {
            ASHLog.d("isBastetProxyState true");
            return true;
        }
        ASHLog.d("isBastetProxyState false");
        return false;
    }

    private boolean isThawBastetPackage(ArrayList<Integer> uidList, AppStateRecord appRecord) {
        if (uidList.size() == 2 && ((Integer) uidList.get(0)).intValue() == 0 && appRecord.hasPid(((Integer) uidList.get(1)).intValue())) {
            ASHLog.d("isThawBastetPackage true");
            return true;
        }
        ASHLog.d("isThawBastetPackage false");
        return false;
    }

    private boolean isContainsAnyOne(ArrayList<Integer> A, ArrayList<Integer> B) {
        if (A == null || B == null) {
            return false;
        }
        for (Integer i : B) {
            if (A.contains(i)) {
                return true;
            }
        }
        return false;
    }

    private void handleNetPacket(ArrayList<Integer> uidList) {
        if (uidList == null) {
            ASHLog.e("handleNetPacket, null, return");
            return;
        }
        for (Entry entry : this.mSmartHibernationApps.entrySet()) {
            AppStateRecord appRecord = (AppStateRecord) entry.getValue();
            if (appRecord == null) {
                ASHLog.e("handleNetPacket, appRecord is null, return.");
                return;
            } else if (isBastetProxyState(uidList, appRecord)) {
                ASHLog.i("handleNetPacket, action: " + uidList.get(0) + ", pid: " + uidList.get(1));
                appRecord.handleBastetProxyState(uidList);
            } else if (isThawBastetPackage(uidList, appRecord) || (!appRecord.getBastetProxyState() && isContainsAnyOne(uidList, appRecord.getUids()))) {
                appRecord.handleNetPacket();
            }
        }
    }

    private void handleAppsAlarm(String pkgName) {
        if (pkgName != null) {
            AppStateRecord appRecord = (AppStateRecord) this.mSmartHibernationApps.get(pkgName);
            if (appRecord != null) {
                appRecord.handleAppsAlarm();
            }
            checkMsgTimeout();
        }
    }

    public void checkMsgTimeout() {
        if (this.mIDeviceState.isScreenOff()) {
            long now = SystemClock.elapsedRealtime();
            if (now - this.mMsgTimeoutLastCheck >= 5000) {
                for (Entry entry : this.mSmartHibernationApps.entrySet()) {
                    AppStateRecord appRecord = (AppStateRecord) entry.getValue();
                    if (appRecord != null) {
                        appRecord.checkMsgTimeout();
                    }
                }
                this.mMsgTimeoutLastCheck = now;
            }
        }
    }

    private void handleCrashRestart() {
        ASHLog.e(" Crash restart to cancel all action...");
        ASHAdapter actionAdapter = ASHAdapter.getInstance(this.mICoreContext);
        if (actionAdapter != null) {
            WakeLock wakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "hibernation");
            wakeLock.acquire();
            actionAdapter.unFreezeAllAppProcess();
            actionAdapter.unpendingAllAlarms();
            actionAdapter.removeAllPeriodAdjustAlarms();
            actionAdapter.unproxyAllAppBroadcast();
            actionAdapter.unproxyAllAppWakeLock();
            actionAdapter.forceRestoreAllAppWakeLock();
            actionAdapter.unproxyAllApps();
            actionAdapter.notifyBastetUnProxyAll();
            actionAdapter.recoveryFirewallUidRule();
            wakeLock.release();
        }
    }

    private void handleConnectivityChange() {
        for (Entry entry : this.mSmartHibernationApps.entrySet()) {
            AppStateRecord appRecord = (AppStateRecord) entry.getValue();
            if (appRecord != null) {
                appRecord.handleConnectivityChange();
            }
        }
    }

    private ArrayList<Integer> getAllPids() {
        ArrayList<Integer> allPids = new ArrayList();
        for (Entry entry : this.mSmartHibernationApps.entrySet()) {
            for (Integer pid : ((AppStateRecord) entry.getValue()).getPids()) {
                if (!allPids.contains(pid)) {
                    allPids.add(pid);
                }
            }
        }
        return allPids;
    }

    private AppStateRecord addAppRecord(String pkg) {
        if (pkg != null) {
            ASHLog.d("new app record pkg: " + pkg);
            if (32 > this.mSmartHibernationApps.size()) {
                AppStateRecord appRecord = new AppStateRecord(this.mICoreContext, this.mContext, pkg);
                this.mSmartHibernationApps.put(pkg, appRecord);
                return appRecord;
            }
            ASHLog.w("warnning: app record upto the max:32");
            return null;
        }
        ASHLog.e("ERROR: new app record pkg is null");
        return null;
    }

    private AppStateRecord getAppRecord(String pkg) {
        if (pkg != null) {
            return (AppStateRecord) this.mSmartHibernationApps.get(pkg);
        }
        return null;
    }

    private AppStateRecord removeAppDeadRecord(String pkg) {
        if (pkg != null) {
            return (AppStateRecord) this.mSHDeadApps.remove(pkg);
        }
        return null;
    }

    private void updateAppType() {
        for (Entry entry : this.mSmartHibernationApps.entrySet()) {
            updateAppRecordType((AppStateRecord) entry.getValue());
        }
        for (Entry entry2 : this.mSHDeadApps.entrySet()) {
            updateAppRecordType((AppStateRecord) entry2.getValue());
        }
    }

    private void updateAppRecordType(AppStateRecord appRecord) {
        if (appRecord != null) {
            int appType = appRecord.getAppType();
            if (appType == -1 || !(appType == 6 || appType == 7 || appType == 8)) {
                appType = this.mIAppType.getAppType(appRecord.getPkgName());
                appRecord.setAppType(appType);
                ASHLog.d("update " + appRecord.getPkgName() + " type = " + appType);
            }
        }
    }

    private void handleActiveHighPowerGps(String pkgName) {
        AppStateRecord appStateRecord = getAppRecord(pkgName);
        if (appStateRecord != null) {
            appStateRecord.handleActiveHighPowerGps();
        }
    }

    private void handleUnfreezeDependPids(ArrayList<Integer> requestUnfreezePids) {
        if (requestUnfreezePids != null) {
            ASHLog.i("request unfreeze depend pid: " + requestUnfreezePids);
            for (Entry entry : this.mSmartHibernationApps.entrySet()) {
                AppStateRecord appRecord = (AppStateRecord) entry.getValue();
                if (appRecord.hasPid((ArrayList) requestUnfreezePids)) {
                    appRecord.handleUnfreezeDependPids();
                }
            }
        }
    }

    private void requestHibernateApps(ArrayList<String> pkgList, String reason) {
        Map<String, AppStateRecord> smartHibernationApps = this.mSmartHibernationApps;
        ASHLog.i("request hibernate pkgs: " + pkgList + " reason: " + reason);
        Iterator<String> it = pkgList.iterator();
        while (it.hasNext()) {
            String pkgName = (String) it.next();
            AppStateRecord app = (AppStateRecord) smartHibernationApps.get(pkgName);
            if (app != null) {
                app.requestHibernate(reason);
            } else {
                ASHLog.w("can not hibernate pkg: " + pkgName);
            }
        }
    }

    private void requestWakeupApps(ArrayList<String> pkgList, String reason) {
        Map<String, AppStateRecord> smartHibernationApps = this.mSmartHibernationApps;
        ASHLog.i("request wakeup pkgs: " + pkgList + " reason: " + reason);
        Iterator<String> it = pkgList.iterator();
        while (it.hasNext()) {
            AppStateRecord app = (AppStateRecord) smartHibernationApps.get((String) it.next());
            if (app != null) {
                app.requestRunning(reason);
            }
        }
    }

    public static ASHStateInterface getPGDebugUI() {
        return mStateInterface;
    }

    public Map getApplicationMap() {
        return this.mSmartHibernationApps;
    }

    public ArrayList<String> getAboveLauncherPkgs() {
        return mFrontPkgsAboveLauncher;
    }

    public boolean dump(PrintWriter pw) {
        pw.println("ASHManager: ");
        pw.println("  App State Info :");
        for (Entry entry : ((HashMap) this.mSmartHibernationApps.clone()).entrySet()) {
            AppStateRecord appRecord = (AppStateRecord) entry.getValue();
            pw.println("    PackageName:" + appRecord.getPkgName());
            pw.println("        State:" + appRecord.getStateName());
            pw.println("        Type:" + appRecord.getAppType());
            pw.println("        UID:" + appRecord.getUids());
            pw.println("        PID:" + appRecord.getPids());
            pw.println("        DURATION:" + (appRecord.getDuration() / 1000) + "s");
            ArrayList<Integer> pids = appRecord.getBastetPids();
            if (pids != null && pids.size() > 0) {
                pw.println("        BastetPids:" + pids);
            }
        }
        return true;
    }
}
