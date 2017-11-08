package com.huawei.powergenie.modules.apppower.hibernation.states;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.os.SystemClock;
import com.huawei.powergenie.api.IAppManager;
import com.huawei.powergenie.api.IAppType;
import com.huawei.powergenie.api.IContextAware;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.IPolicy;
import com.huawei.powergenie.api.IPowerStats;
import com.huawei.powergenie.api.IScenario;
import com.huawei.powergenie.core.app.AppInfoRecord;
import com.huawei.powergenie.debugtest.LogUtils;
import com.huawei.powergenie.modules.apppower.hibernation.ASHLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public final class AppStateRecord {
    protected final AppState STATE_DOZE;
    protected final AppState STATE_HIBERNATION;
    protected final AppState STATE_RUNNING;
    private final AppInfoRecord mAppInfo;
    private int mAppType;
    private final ArrayList<Integer> mBastetProxyPids = new ArrayList();
    private final Context mContext;
    private int mDeadPid = -1;
    private final IAppManager mIAppManager;
    private final IAppType mIAppType;
    private final IContextAware mIContextAware;
    private final ICoreContext mICoreContext;
    private final IDeviceState mIDeviceState;
    private final IPolicy mIPolicy;
    private final IScenario mIScenario;
    private long mInvisibilityStart = -1;
    private boolean mIsNeedFastUnfreeze = false;
    private boolean mIsPendingHeartbeat = false;
    private boolean mIsUnifiedHeartbeat = false;
    private int mMmPushPid = -1;
    private HashMap<Integer, Integer> mPidUidMap = new HashMap();
    private final ArrayList<Integer> mPids = new ArrayList();
    private final String mPkgName;
    private final HashMap<Integer, List<String>> mPkgsSharePid = new HashMap();
    private boolean mRestoreActiveGps = false;
    private AppState mState;
    private int mUid = -1;
    private final ArrayList<Integer> mUids = new ArrayList();

    public AppStateRecord(ICoreContext coreContext, Context context, String pkgName) {
        this.mICoreContext = coreContext;
        this.mContext = context;
        this.mPkgName = pkgName;
        this.mIAppManager = (IAppManager) coreContext.getService("appmamager");
        this.mIPolicy = (IPolicy) coreContext.getService("policy");
        this.mIDeviceState = (IDeviceState) coreContext.getService("device");
        this.mIContextAware = (IContextAware) coreContext.getService("ca");
        this.mIScenario = (IScenario) coreContext.getService("scenario");
        this.mIAppType = (IAppType) coreContext.getService("appmamager");
        this.STATE_RUNNING = new AppStateRunning(this);
        this.STATE_DOZE = new AppStateDoze(this);
        this.STATE_HIBERNATION = new AppStateHibernation(this);
        this.mState = this.STATE_RUNNING;
        this.mState.startTime();
        this.mAppInfo = this.mIAppManager.getAppInfoRecord(this.mPkgName);
        this.mUid = this.mAppInfo.getHostUid();
        if (this.mUid < 0) {
            this.mUid = this.mAppInfo.getUserUid(this.mIAppManager.getCurUserId());
        }
        this.mAppType = this.mIAppType.getAppType(pkgName);
    }

    public void handleRestart() {
        this.mState = this.STATE_RUNNING;
        this.mState.startTime();
    }

    public void handleExit(boolean isCrash) {
        handleExit(isCrash, -1);
    }

    public void handleExit(boolean isCrash, int pid) {
        this.mDeadPid = pid;
        this.mState.endState();
        this.mState.handleExit(isCrash);
        StringBuilder msg = new StringBuilder();
        msg.append(this.mPkgName).append("\t").append(this.mAppType).append("\t").append(getDuration()).append("\t").append(this.mState.getName()).append("-> exit").append("\t").append(isCrash ? "not_running" : "stop_ash");
        LogUtils.c("ASH_STATE", msg.toString());
        if (isCrash) {
            this.mBastetProxyPids.clear();
            ASHLog.i(this.mPkgName + " " + this.mState + " transition to: end, reason:not_running");
        } else {
            ASHLog.i(this.mPkgName + " " + this.mState + " transition to: end, reason:stop_ash");
        }
        this.mPids.clear();
        this.mDeadPid = -1;
        this.mInvisibilityStart = -1;
        this.mState = this.STATE_RUNNING;
    }

    public void handleBinderCall(int calledpid) {
        this.mState.handleBinderCall(calledpid);
    }

    public void handleProcessStart(int pid, int uid, boolean visible) {
        addPid(pid, uid);
        if (visible) {
            processVisibleApp(true);
        } else {
            processVisibleApp(false);
        }
        this.mState.handleProcessStart();
    }

    public void handleProcessExit(int pid) {
        this.mDeadPid = pid;
        this.mState.handleProcessExit();
        removePid(pid);
        this.mDeadPid = -1;
        removeBastetPid(pid);
    }

    public boolean isVisible() {
        return this.mInvisibilityStart <= 0;
    }

    private boolean isFrontApp() {
        return this.mPkgName.equals(this.mIScenario.getFrontPkg());
    }

    public void handleTopViewChanged(boolean add) {
        if (!add && !hasTopView()) {
            this.mState.handleTopView(true);
        }
    }

    public boolean hasTopView() {
        for (Integer pid : this.mPids) {
            if (this.mIAppManager.isShowTopView(pid.intValue(), this.mUid, this.mPkgName)) {
                return true;
            }
        }
        return false;
    }

    public void handlePkgInstalled() {
        this.mState.handlePkgInstalled();
    }

    public void handleWallpaperChanged(boolean enable) {
        ASHLog.i("app:" + this.mPkgName + "  Wallpaper Enable:" + enable);
        this.mState.handleWallpaperChanged(enable);
    }

    public void handleNotification(boolean newNotice, boolean cancelAll, String opPkg) {
        this.mState.handleNotification(newNotice, cancelAll, opPkg);
    }

    public void handleAppWidgetEnabled() {
        this.mState.handleWidgetEnabled();
    }

    public void handleNetPacket() {
        ASHLog.i("handle net packet, app: " + this.mPkgName);
        this.mState.handleNetPacket();
    }

    public void handleAppsAlarm() {
        ASHLog.d("alarm start = " + this.mPkgName);
        this.mState.handleAppsAlarm();
    }

    public void checkMsgTimeout() {
        this.mState.checkMsgTimeout();
    }

    public void handleConnectivityChange() {
        if (isImTypeApp() || this.mAppType == 17) {
            ASHLog.i("Connectivity Change to notify im app: " + this.mPkgName);
            this.mState.handleConnectivityChange();
        } else if (isProtectAppByUser()) {
            ASHLog.i("Connectivity Change to notify app in cleanProtectList: " + this.mPkgName);
            this.mState.handleConnectivityChange();
        }
    }

    public void handleAudioStart() {
        this.mState.handleAudioStart();
    }

    public void handleCallBusy() {
        this.mState.handleCallBusy();
    }

    public void handleUnfreezeDependPids() {
        this.mState.handleUnfreezeDependPids();
    }

    public boolean hasNotification() {
        return this.mIAppManager.hasNotification(this.mPkgName);
    }

    public boolean hasActiveAudio() {
        if (isFrontApp() || !this.mIAppManager.isIgnoreAudioApp(this.mPkgName)) {
            for (Integer pid : this.mPids) {
                if (this.mIDeviceState.isPlayingSound(pid.intValue())) {
                    return true;
                }
            }
            long deltaTime = this.mIDeviceState.getAudioStopDeltaTime(this.mUid);
            if (deltaTime <= 0 || (deltaTime > 30000 && (deltaTime > 1800000 || this.mIAppType.getAppType(this.mPkgName) != 13 || ignoreActiveGps()))) {
                if (deltaTime > 0) {
                    ASHLog.i(" ignore audio app: " + this.mPkgName + " stop delta time:" + deltaTime);
                }
                return false;
            }
            ASHLog.i(this.mPkgName + " audio stop deltaTime = " + deltaTime);
            return true;
        }
        ASHLog.i(" ignore audio app: " + this.mPkgName);
        return false;
    }

    public boolean isIgnoreFrontApp() {
        if (!this.mIDeviceState.isScreenOff() || !isFrontApp() || !this.mIAppManager.isIgnoreFrontApp(this.mPkgName)) {
            return false;
        }
        ASHLog.i(" ignore front app: " + this.mPkgName);
        return true;
    }

    public boolean isPermitRestrictNetApp() {
        if (!this.mIAppManager.isPermitRestrictNetApp(this.mPkgName)) {
            return false;
        }
        ASHLog.i("permit restrict net app: " + this.mPkgName);
        return true;
    }

    public boolean hasBluetoothConnected() {
        if (this.mIPolicy.getPowerMode() == 1) {
            return false;
        }
        for (Integer intValue : getUids()) {
            if (this.mIDeviceState.hasBluetoothConnected(this.mPkgName, intValue.intValue(), 0)) {
                return true;
            }
        }
        return false;
    }

    public void startMotionDetection() {
        if (this.mIDeviceState.isScreenOff()) {
            this.mIContextAware.startMotionDetection(30);
        }
    }

    public boolean hasActiveGps() {
        for (Integer intValue : getUids()) {
            if (this.mIDeviceState.hasActiveGps(intValue.intValue())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasActiveSensor() {
        for (Integer intValue : getUids()) {
            if (this.mIDeviceState.hasActiveSensor(intValue.intValue())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasVaildSensor() {
        for (Integer intValue : getUids()) {
            if (this.mIDeviceState.hasVaildSensor(intValue.intValue())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasDataTransmitting() {
        for (Integer uid : getUids()) {
            if (this.mIDeviceState.isDlUploading(uid.intValue())) {
                return true;
            }
        }
        return false;
    }

    public long getDuration() {
        return this.mState.getDuration();
    }

    public int getDeadPid() {
        return this.mDeadPid;
    }

    public boolean isDependedByOtherApp() {
        return this.mIAppManager.isDependedByOtherApp(getPids());
    }

    public boolean isDependedByFrontApp() {
        if (!isFrontApp()) {
            return this.mIAppManager.isDependedByFrontApp(this.mPkgName);
        }
        ASHLog.i(this.mPkgName + "  is front app");
        return true;
    }

    public boolean isDependsAudioActiveApp() {
        return this.mIAppManager.isDependsAudioActiveApp(getPids());
    }

    public boolean isScrOffRejectMsgApp() {
        if (!this.mIPolicy.isExtremeModeV2()) {
            return false;
        }
        if (this.mIAppManager.isExtrModeV2ReserveApp(this.mPkgName) && (isImTypeApp() || hasActiveAudio())) {
            return false;
        }
        return true;
    }

    public boolean isIAwareProtectNotCleanApp() {
        if (this.mIAppManager.isIAwareProtectNotCleanApp(this.mPkgName)) {
            return true;
        }
        return false;
    }

    public void processVisibleApp(boolean visible) {
        ASHLog.d("app:" + this.mPkgName + "  visible:" + visible);
        if (this.mInvisibilityStart == -1 || isVisible() != visible) {
            if (visible) {
                this.mInvisibilityStart = 0;
            } else {
                this.mInvisibilityStart = SystemClock.elapsedRealtime();
            }
            this.mState.processVisibleApp(visible);
        }
    }

    public void transitionTo(AppState state) {
        if (state != null && this.mState != state) {
            ASHLog.i(this.mPkgName + " " + this.mState + " transition to: " + state.getName() + " reason:" + this.mState.getTransitionReason());
            if (this.mState == this.STATE_HIBERNATION && state == this.STATE_RUNNING) {
                IPowerStats ips = (IPowerStats) this.mICoreContext.getService("powerstats");
                String reason = this.mState.getTransitionReason();
                if (!(ips == null || "".equals(reason))) {
                    ips.iStats(5, this.mPkgName, 1, reason);
                }
            }
            this.mState.endState();
            this.mState = state;
            this.mState.startState();
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public ICoreContext getPGContext() {
        return this.mICoreContext;
    }

    public String getPkgName() {
        return this.mPkgName;
    }

    private void addPid(int pid, int uid) {
        if (!this.mPids.contains(Integer.valueOf(pid))) {
            this.mPids.add(Integer.valueOf(pid));
        }
        if (uid > 0) {
            this.mPidUidMap.put(Integer.valueOf(pid), Integer.valueOf(uid));
            refreshUids();
        }
    }

    private void removePid(int pid) {
        this.mPids.remove(Integer.valueOf(pid));
        this.mPidUidMap.remove(Integer.valueOf(pid));
        this.mPkgsSharePid.remove(Integer.valueOf(pid));
        refreshUids();
    }

    public ArrayList<Integer> getPids() {
        return this.mPids;
    }

    public boolean hasProcName(String procName) {
        return this.mAppInfo.hasProcName(procName);
    }

    public void addSharePid(int pid, List<String> pkgs) {
        if (pkgs != null) {
            this.mPkgsSharePid.put(Integer.valueOf(pid), pkgs);
        }
    }

    public boolean isPkgsSharePid() {
        for (Entry entry : this.mPkgsSharePid.entrySet()) {
            ASHLog.i(((List) entry.getValue()) + " is running in the pid:" + ((Integer) entry.getKey()));
        }
        if (this.mPkgsSharePid.size() > 0) {
            return true;
        }
        return false;
    }

    public int getUid(int pid) {
        Integer integer = (Integer) this.mPidUidMap.get(Integer.valueOf(pid));
        if (integer != null) {
            return integer.intValue();
        }
        ASHLog.e("not find uid by pid:" + pid);
        return -1;
    }

    public ArrayList<Integer> getUids() {
        return this.mUids;
    }

    private void refreshUids() {
        this.mUids.clear();
        if (this.mUid > 0) {
            this.mUids.add(Integer.valueOf(this.mUid));
        }
        for (Entry entry : this.mPidUidMap.entrySet()) {
            Integer uid = (Integer) entry.getValue();
            if (!this.mUids.contains(uid)) {
                this.mUids.add(uid);
            }
        }
        ASHLog.d("refreshUids,pkg:" + this.mPkgName + ",uids:" + this.mUids);
    }

    public boolean hasPid(int pid) {
        return this.mPids.contains(Integer.valueOf(pid));
    }

    public boolean hasPid(ArrayList<Integer> pids) {
        for (Integer pid : pids) {
            if (this.mPids.contains(pid)) {
                return true;
            }
        }
        return false;
    }

    public int setAppType(int type) {
        this.mAppType = type;
        return type;
    }

    public int getAppType() {
        return this.mAppType;
    }

    public void setMmPushPid(int pid) {
        this.mMmPushPid = pid;
    }

    public int getMmPushPid() {
        return this.mMmPushPid;
    }

    public String getStateName() {
        return this.mState.getName();
    }

    public boolean handleCalledByOtherApp(String exceptionType) {
        return this.mState.handleCalledByOtherApp(exceptionType);
    }

    public void updateFastUnfreezeState(boolean isNeedFastUnfreeze) {
        this.mIsNeedFastUnfreeze = isNeedFastUnfreeze;
    }

    public boolean isNeedFastUnfreeze() {
        return this.mIsNeedFastUnfreeze;
    }

    public void handleFastUnfreezeApp() {
        this.mState.handleFastUnfreezeApp();
    }

    public void handleScreenOn() {
        this.mState.handleScreenOn();
    }

    public void handleSreenUnlock() {
        this.mState.handleSreenUnlock();
    }

    public void handleScreenOff() {
        this.mState.handleScreenOff();
    }

    public void handleBCOverflow() {
        this.mState.handleBCOverflow();
    }

    public void handleBCNotify(String action) {
        this.mState.handleBCNotify(action);
    }

    public boolean requestHibernate(String reason) {
        if (!isVisible()) {
            return this.mState.requestHibernate(reason);
        }
        ASHLog.w("can not request hibernate visible app:" + this.mPkgName);
        return false;
    }

    public boolean requestRunning(String reason) {
        return this.mState.requestRunning(reason);
    }

    public void handleBroadcastANR() {
        this.mState.handleBroadcastANR();
    }

    public boolean hasIconOnLauncher() {
        if (this.mIAppManager.hasLauncherIcon(this.mContext, this.mPkgName)) {
            return true;
        }
        ASHLog.i("no launcher icon app: " + this.mPkgName);
        return false;
    }

    public boolean isCurrentInputMethod() {
        String defaultInputMethodName = this.mIAppType.getDefaultInputMethod();
        if (defaultInputMethodName == null || !defaultInputMethodName.equals(this.mPkgName)) {
            return false;
        }
        ASHLog.i("Default InputMethod : " + defaultInputMethodName);
        return true;
    }

    public boolean isNFCOn() {
        return this.mIDeviceState.isNFCOn();
    }

    public boolean isNFCPayApp() {
        String payApp = this.mIAppManager.getNFCPayApp();
        if (payApp == null || !payApp.equals(this.mPkgName)) {
            return false;
        }
        return true;
    }

    public boolean isCurrentLiveWallpaper() {
        String curLiveWallpaper = this.mIAppType.getCurLiveWallpaper();
        return curLiveWallpaper == null ? false : curLiveWallpaper.equals(this.mPkgName);
    }

    public boolean isCurrentLauncher() {
        boolean z = false;
        ArrayList<String> launcherList = this.mIAppType.getAppsByType(1);
        String str = null;
        if (1 == launcherList.size()) {
            str = (String) launcherList.get(0);
            return str == null ? false : str.equals(this.mPkgName);
        }
        List<RunningTaskInfo> runningTasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1000);
        for (int i = 0; i < runningTasks.size(); i++) {
            String taskName = ((RunningTaskInfo) runningTasks.get(i)).topActivity.getPackageName();
            if (launcherList.contains(taskName)) {
                str = taskName;
                break;
            }
        }
        if (str != null) {
            z = str.equals(this.mPkgName);
        }
        return z;
    }

    public boolean isDefaultLauncher() {
        if ("com.huawei.hwmwlauncher".equals(this.mPkgName)) {
            return true;
        }
        String defaultLauncher = this.mIAppType.getDefaultLauncher();
        return defaultLauncher == null ? false : defaultLauncher.equals(this.mPkgName);
    }

    public boolean isAlarmClockApp() {
        ArrayList<String> clockList = this.mIAppType.getAppsByType(10);
        return clockList == null ? false : clockList.contains(this.mPkgName);
    }

    public boolean hasActiveAppWidget() {
        ArrayList<String> appWidgetPkgs = new ArrayList();
        this.mIAppManager.loadAppWidget(appWidgetPkgs);
        if (!appWidgetPkgs.contains(this.mPkgName)) {
            return false;
        }
        ASHLog.i("has widget app: " + this.mPkgName);
        return true;
    }

    public boolean isScreenOff() {
        return this.mIDeviceState.isScreenOff();
    }

    public long getScrOffDuration() {
        return this.mIDeviceState.getScrOffDuration();
    }

    public boolean isMonkeyRunning() {
        return this.mIDeviceState.isMonkeyRunning();
    }

    public boolean isKeyguardPresent() {
        return this.mIDeviceState.isKeyguardPresent();
    }

    public boolean isKeyguardSecure() {
        return this.mIDeviceState.isKeyguardSecure();
    }

    public boolean hasWakelock() {
        for (Integer intValue : getUids()) {
            int uid = intValue.intValue();
            if (this.mIDeviceState.isHoldWakeLockByUid(uid, -1)) {
                ASHLog.i("has wakelock app: " + this.mPkgName + ", uid: " + uid);
                return true;
            }
        }
        return false;
    }

    public boolean isImTypeApp() {
        if (this.mIAppManager.isCleanUnprotectApp(this.mPkgName)) {
            return false;
        }
        if (this.mAppType == 11 || this.mAppType == 3 || this.mAppType == 2) {
            return true;
        }
        return false;
    }

    public boolean isProtectAppByUser() {
        if (this.mIAppManager.isSimplifiedChinese()) {
            ASHLog.d("sys is simple chinese using china policy ctrl app: " + this.mPkgName);
            return false;
        }
        boolean isIgnoreApp;
        if (this.mAppType == 19 || this.mAppType == 18) {
            isIgnoreApp = true;
        } else {
            isIgnoreApp = this.mIAppManager.isIgnoreGpsApp(this.mPkgName);
        }
        if (isIgnoreApp) {
            ASHLog.d("isProtectAppByUser, can ignore app: " + this.mPkgName);
            return false;
        }
        boolean isOverSea = !this.mIPolicy.isChinaMarketProduct() ? (this.mIDeviceState.isChinaOperator() && this.mIDeviceState.hasOperator()) ? false : true : false;
        if (isOverSea || this.mIPolicy.isOffPowerMode()) {
            if (this.mIAppManager.isForeignSuperAppPolicy()) {
                ASHLog.d("foreign super app policy is open for : " + this.mPkgName);
                return this.mIAppManager.isForeignSuperApp(this.mPkgName);
            } else if (!this.mIAppManager.isCleanUnprotectApp(this.mPkgName)) {
                ASHLog.d("isProtectAppByUser, not found in unprotect clean list: " + this.mPkgName);
                return true;
            } else if (!this.mAppInfo.isHostOwerContain() && this.mIAppManager.getCurUserId() == 0) {
                ASHLog.d("isProtectAppByUser, now is owner and not found packageinfo: " + this.mPkgName);
                return true;
            }
        }
        return false;
    }

    public boolean isConnected() {
        return this.mIDeviceState.isNetworkConnected();
    }

    public boolean isIgnoreGpsApp() {
        return this.mIAppManager.isIgnoreGpsApp(this.mPkgName);
    }

    public boolean ignoreActiveGps() {
        if (this.mIAppManager.isIgnoreGpsApp(this.mPkgName)) {
            ASHLog.i("blacklist gps app: " + this.mPkgName + " ignore active gps.");
            return true;
        } else if (isIgnoreAppType()) {
            return true;
        } else {
            if (isSleepState()) {
                ASHLog.i("sleep state: " + this.mPkgName + " ignore active gps.");
                this.mRestoreActiveGps = true;
                return true;
            } else if (!this.mIDeviceState.isScreenOff() || this.mIContextAware.getUserStationaryDuration() < 300000) {
                return false;
            } else {
                ASHLog.i("ignore active gps:" + this.mPkgName + ", stationary: " + this.mIContextAware.getUserStationaryDuration() + "ms, screen off:" + this.mIDeviceState.getScrOffDuration() + "ms");
                this.mRestoreActiveGps = true;
                return true;
            }
        }
    }

    public boolean ignoreFrontActiveGps() {
        if (!this.mIDeviceState.isScreenOff() || !hasActiveGps() || !ignoreActiveGps() || this.mIDeviceState.getGpsTime(this.mUid) <= 300000 || this.mIAppType.getAppType(this.mPkgName) == 13) {
            return false;
        }
        ASHLog.i("ignore front active gps: " + this.mPkgName);
        this.mRestoreActiveGps = true;
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean ignoreNavigationApp() {
        if (!isFrontApp() && hasActiveGps() && this.mIAppType.getAppType(this.mPkgName) == 13) {
            long deltaTime = this.mIDeviceState.getAudioStopDeltaTime(this.mUid);
            if (deltaTime > 0 && deltaTime > 1800000) {
                ASHLog.i(" ignore no audio navi app = " + this.mPkgName);
                return true;
            }
        }
        return false;
    }

    public boolean isIgnoreAppType() {
        int type = this.mIAppType.getAppType(this.mPkgName);
        if (type != 5 && type != 8 && type != 12 && ((type != 6 || !this.mIPolicy.isChinaMarketProduct()) && type != 18 && type != 19 && type != 20)) {
            return false;
        }
        ASHLog.i(this.mPkgName + " ignore app type = " + type);
        return true;
    }

    public void handleActiveHighPowerGps() {
        this.mState.handleActiveHighPowerGps();
    }

    public boolean restoreActiveGps() {
        if (!this.mRestoreActiveGps) {
            return false;
        }
        this.mRestoreActiveGps = false;
        return true;
    }

    public boolean hasIgnoredActiveGps() {
        return this.mRestoreActiveGps;
    }

    public void handleUserWalking() {
        this.mState.handleUserWalking();
    }

    public void handleNFCPayChg() {
        this.mState.handleNFCPayChg();
    }

    public boolean isListenerNetPackets() {
        return this.mIDeviceState.isListenerNetPackets(this.mPkgName);
    }

    public boolean isSleepState() {
        if (1 == this.mIContextAware.getUserState()) {
            return true;
        }
        return false;
    }

    public boolean isCalling() {
        return this.mIDeviceState.isCalling();
    }

    public void addBastetPid(int pid) {
        if (!this.mBastetProxyPids.contains(Integer.valueOf(pid))) {
            this.mBastetProxyPids.add(Integer.valueOf(pid));
        }
    }

    public void removeBastetPid(int pid) {
        this.mBastetProxyPids.remove(Integer.valueOf(pid));
    }

    public ArrayList<Integer> getBastetPids() {
        return this.mBastetProxyPids;
    }

    public boolean getBastetProxyState() {
        ASHLog.d("getBastetProxyState: " + this.mBastetProxyPids.size());
        if (this.mBastetProxyPids.size() > 0) {
            return true;
        }
        return false;
    }

    public void handleBastetProxyState(ArrayList<Integer> uidList) {
        if (!"com.tencent.mm".equals(this.mPkgName)) {
            boolean ready = ((Integer) uidList.get(0)).intValue() == -1;
            int pid = ((Integer) uidList.get(1)).intValue();
            if (ready) {
                addBastetPid(pid);
            } else {
                removeBastetPid(pid);
            }
            this.mState.handleBastetProxyState(ready);
        }
    }

    public void updateUnifiedHeartbeat(boolean isUnified) {
        this.mIsUnifiedHeartbeat = isUnified;
    }

    public void updatePendingAppAlarms(boolean isPending) {
        this.mIsPendingHeartbeat = isPending;
    }

    public boolean isUnifiedHeartbeat() {
        return this.mIsUnifiedHeartbeat;
    }

    public boolean isPendingAppAlarms() {
        return this.mIsPendingHeartbeat;
    }

    public boolean isPermitUnifiedHeartbeat() {
        String pkgName = this.mPkgName;
        if (pkgName.contains("whatsapp") || pkgName.contains("google") || pkgName.contains("cn.ledongli") || pkgName.startsWith("com.xdja") || "com.huawei.health".equals(pkgName)) {
            return false;
        }
        if (this.mIPolicy.isExtremeModeV2()) {
            ASHLog.i("unified heartbeat in extreme mode2 pkg: " + this.mPkgName);
            return true;
        } else if (isAlarmClockApp() || isVisible() || hasActiveAppWidget()) {
            return false;
        } else {
            if (this.mIAppManager.isCleanUnprotectApp(this.mPkgName)) {
                ASHLog.i("unified heartbeat unprotect app : " + this.mPkgName);
                return true;
            }
            int totalCount = this.mIAppManager.getTotalScrOffAlarmCount(this.mPkgName);
            int totalFreq = this.mIAppManager.getTotalScrOffAlarmFreq(this.mPkgName);
            int curScrOffCount = this.mIAppManager.getCurScrOffAlarmCount(this.mPkgName);
            int curScrOffFreq = this.mIAppManager.getCurScrOffAlarmFreq(this.mPkgName);
            if ((totalCount >= 5 && totalFreq < 600 && totalFreq > 0) || (curScrOffCount > 3 && curScrOffFreq < 600 && curScrOffFreq > 0)) {
                ASHLog.i("alarm total count: " + totalCount + " freq: " + totalFreq + "current count: " + curScrOffCount + " freq: " + curScrOffFreq);
                if (!(isSleepState() || this.mAppType == 11 || this.mAppType == 3 || this.mAppType == 4 || this.mAppType == 5 || this.mAppType == 6 || this.mAppType == 8 || pkgName.contains("weibo"))) {
                    if (pkgName.contains("news")) {
                    }
                }
                return true;
            }
            return false;
        }
    }
}
