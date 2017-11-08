package com.huawei.powergenie.modules.apppower.hibernation.states;

import android.os.SystemClock;
import com.huawei.powergenie.api.IAppManager;
import com.huawei.powergenie.api.IPolicy;
import com.huawei.powergenie.api.IPowerStats;
import com.huawei.powergenie.modules.apppower.hibernation.ASHLog;
import com.huawei.powergenie.modules.apppower.hibernation.actions.AppAction;

public abstract class AppState {
    protected AppStateRecord mAppRecord;
    private String mExtraInfo;
    protected final IAppManager mIAppManager;
    protected final IPolicy mIPolicy;
    private final String mName;
    private long mStart;
    private final AppAction mStateAction;

    public AppState(AppStateRecord record, String name, AppAction stateAction) {
        this.mAppRecord = record;
        this.mName = name;
        this.mStateAction = stateAction;
        this.mIAppManager = (IAppManager) record.getPGContext().getService("appmamager");
        this.mIPolicy = (IPolicy) record.getPGContext().getService("policy");
    }

    public void startState() {
        startTime();
        doAction();
        if (!this.mAppRecord.isVisible()) {
            processVisibleApp(false);
        }
    }

    public void endState() {
        clearAction();
        clearDelayMsg();
        this.mExtraInfo = null;
    }

    protected void goRunning(String reason) {
        this.mExtraInfo = reason;
        nextState(this.mAppRecord.STATE_RUNNING);
    }

    protected void goDoze() {
        nextState(this.mAppRecord.STATE_DOZE);
    }

    protected void goHibernation() {
        nextState(this.mAppRecord.STATE_HIBERNATION);
    }

    protected void goHibernation(String reason) {
        this.mExtraInfo = reason;
        nextState(this.mAppRecord.STATE_HIBERNATION);
    }

    private void nextState(AppState state) {
        this.mAppRecord.transitionTo(state);
    }

    private void doAction() {
        if (this.mStateAction != null) {
            this.mStateAction.performAction();
        }
    }

    private void clearAction() {
        if (this.mStateAction != null) {
            this.mStateAction.clearAction();
        }
    }

    protected void clearDelayMsg() {
    }

    protected void startTime() {
        this.mStart = SystemClock.elapsedRealtime();
    }

    protected long getDuration() {
        return SystemClock.elapsedRealtime() - this.mStart;
    }

    protected void handleProcessStart() {
    }

    protected void handleBinderCall(int calledpid) {
    }

    public void handleExit(boolean isCrash) {
        if (this.mStateAction != null) {
            this.mStateAction.handleExit(isCrash);
        }
    }

    protected void handleProcessExit() {
        goRunning("proc_exit");
    }

    public void handlePkgInstalled() {
        goRunning("pkg_installed");
    }

    public void handleTopView(boolean disappear) {
    }

    public void handleWallpaperChanged(boolean enable) {
    }

    public void processVisibleApp(boolean visible) {
    }

    public void handleNotification(boolean newNotice, boolean cancelAll, String opPkg) {
    }

    public void handleWidgetEnabled() {
    }

    public void handleNetPacket() {
    }

    public void handleAppsAlarm() {
    }

    public void checkMsgTimeout() {
        if (this.mStateAction != null) {
            this.mStateAction.checkMsgTimeout();
        }
    }

    public void handleConnectivityChange() {
    }

    public void handleAudioStart() {
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("{ ");
        b.append(this.mName);
        b.append(" duration=");
        b.append(getDuration());
        b.append(" }");
        return b.toString();
    }

    public String getName() {
        return this.mName;
    }

    public String getTransitionReason() {
        if (this.mExtraInfo != null) {
            return this.mExtraInfo;
        }
        return "";
    }

    public boolean requestHibernate(String reason) {
        return false;
    }

    public boolean requestRunning(String reason) {
        return false;
    }

    public boolean handleCalledByOtherApp(String exceptionType) {
        return false;
    }

    public void handleBCOverflow() {
    }

    public void handleBCNotify(String action) {
    }

    public void handleFastUnfreezeApp() {
    }

    public void handleScreenOn() {
    }

    public void handleSreenUnlock() {
        if (this.mStateAction != null) {
            this.mStateAction.handleSreenUnlock();
        }
    }

    public void handleScreenOff() {
        if (this.mAppRecord.isScrOffRejectMsgApp()) {
            ASHLog.i("screen off in extreme v2, go hibernation...");
            goHibernation();
        }
    }

    public void handleBroadcastANR() {
    }

    public void handleBastetProxyState(boolean ready) {
        if (this.mStateAction != null) {
            this.mStateAction.handleBastetProxyState(ready);
        }
    }

    public void handleUserWalking() {
    }

    public void handleNFCPayChg() {
    }

    public void handleCallBusy() {
    }

    public void handleActiveHighPowerGps() {
    }

    public void handleUnfreezeDependPids() {
    }

    public void iStats(int type, String reason) {
        IPowerStats ips = (IPowerStats) this.mAppRecord.getPGContext().getService("powerstats");
        if (ips != null) {
            ips.iStats(type, this.mAppRecord.getPkgName(), 1, reason);
        }
    }
}
