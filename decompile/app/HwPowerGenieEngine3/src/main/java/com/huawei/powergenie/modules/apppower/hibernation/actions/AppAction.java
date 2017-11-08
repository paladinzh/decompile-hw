package com.huawei.powergenie.modules.apppower.hibernation.actions;

import android.content.Context;
import com.huawei.powergenie.api.IAppManager;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import com.huawei.powergenie.modules.apppower.hibernation.ASHLog;
import com.huawei.powergenie.modules.apppower.hibernation.actions.adapter.ASHAdapter;
import com.huawei.powergenie.modules.apppower.hibernation.states.AppStateRecord;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AppAction {
    private final ASHAdapter mASHAdapter;
    protected final AppStateRecord mAppRecord;
    private long mBCProxyDelayTime = -1;
    protected final Context mContext;
    private final IAppManager mIAppManager;
    private boolean mInitialBCExclude = false;
    private boolean mIsNeedRestoreWL = false;
    private boolean mRestoreActiveWhenExit = false;

    public abstract void clearAction();

    public abstract void performAction();

    public AppAction(AppStateRecord record) {
        this.mAppRecord = record;
        this.mContext = record.getContext();
        this.mASHAdapter = ASHAdapter.getInstance(record.getPGContext());
        this.mIAppManager = (IAppManager) record.getPGContext().getService("appmamager");
    }

    public void checkMsgTimeout() {
        triggerUnifiedHeartbeat();
    }

    public void handleExit(boolean isCrash) {
        cancelUnifiedHeartbeat("exit");
    }

    public void handleSreenUnlock() {
    }

    protected boolean notifyBastetProxy() {
        if ("com.tencent.mm".equals(this.mAppRecord.getPkgName())) {
            return false;
        }
        ArrayList<Integer> bastetPids = this.mAppRecord.getBastetPids();
        boolean result = false;
        if (bastetPids != null && bastetPids.size() > 0) {
            result = this.mASHAdapter.notifyBastetProxy(bastetPids);
            ASHLog.i("bastet proxy " + this.mAppRecord.getBastetPids() + (result ? " OK !" : " Fail!"));
        }
        return result;
    }

    protected boolean notifyBastetUnProxy() {
        if ("com.tencent.mm".equals(this.mAppRecord.getPkgName())) {
            return false;
        }
        ArrayList<Integer> bastetPids = this.mAppRecord.getBastetPids();
        boolean result = false;
        if (bastetPids != null && bastetPids.size() > 0) {
            result = this.mASHAdapter.notifyBastetUnProxy(bastetPids);
            ASHLog.i("bastet unproxy " + this.mAppRecord.getBastetPids() + (result ? " OK !" : " Fail!"));
        }
        return result;
    }

    public boolean getBastetProxyState() {
        if ("com.tencent.mm".equals(this.mAppRecord.getPkgName())) {
            return false;
        }
        return this.mAppRecord.getBastetProxyState();
    }

    public void handleBastetProxyState(boolean ready) {
    }

    protected boolean freezeAppProcess() {
        ArrayList<Integer> pkgPidList = this.mAppRecord.getPids();
        boolean result = false;
        if (pkgPidList != null) {
            int appUid = this.mAppRecord.getUids().size() > 0 ? ((Integer) this.mAppRecord.getUids().get(0)).intValue() : -1;
            if (this.mAppRecord.getUids().size() > 1 && pkgPidList.size() > 0) {
                appUid = this.mAppRecord.getUid(((Integer) pkgPidList.get(0)).intValue());
            }
            result = this.mASHAdapter.freezeAppProcess(pkgPidList, this.mAppRecord.getPkgName(), appUid);
            ASHLog.i("Freeze " + this.mAppRecord.getPkgName() + (result ? " OK !" : " Fail !"));
        }
        return result;
    }

    protected boolean unFreezeAppProcess() {
        ArrayList<Integer> pkgPidList = this.mAppRecord.getPids();
        boolean result = false;
        if (pkgPidList != null) {
            int appUid = this.mAppRecord.getUids().size() > 0 ? ((Integer) this.mAppRecord.getUids().get(0)).intValue() : -1;
            if (this.mAppRecord.getUids().size() > 1 && pkgPidList.size() > 0) {
                appUid = this.mAppRecord.getUid(((Integer) pkgPidList.get(0)).intValue());
            }
            result = this.mASHAdapter.unFreezeAppProcess(pkgPidList, this.mAppRecord.getPkgName(), appUid);
            ASHLog.i("Unfreeze " + this.mAppRecord.getPkgName() + (result ? " OK !" : " Fail !"));
        }
        return result;
    }

    protected boolean pendingAppAlarms() {
        if (isPendingAppAlarms()) {
            return true;
        }
        boolean result = this.mASHAdapter.pendingAppAlarms(Arrays.asList(new String[]{this.mAppRecord.getPkgName()}));
        this.mAppRecord.updatePendingAppAlarms(true);
        ASHLog.i("Pending " + this.mAppRecord.getPkgName() + " alarm" + (result ? " OK !" : " Fail !"));
        return result;
    }

    protected boolean unPendingAppAlarms() {
        if (!isPendingAppAlarms()) {
            return true;
        }
        boolean result = this.mASHAdapter.unpendingAppAlarms(Arrays.asList(new String[]{this.mAppRecord.getPkgName()}));
        this.mAppRecord.updatePendingAppAlarms(false);
        ASHLog.i("Unpending " + this.mAppRecord.getPkgName() + " alarm" + (result ? " OK !" : " Fail !"));
        return result;
    }

    private void triggerUnifiedHeartbeat() {
        if (this.mAppRecord.getScrOffDuration() > 10000 && !isUnifiedHeartbeat() && this.mAppRecord.isPermitUnifiedHeartbeat()) {
            startUnifiedHeartbeat("scroff_unified");
        }
    }

    protected void startUnifiedHeartbeat(String reason) {
        if (!isUnifiedHeartbeat()) {
            List<String> pkgList = Arrays.asList(new String[]{this.mAppRecord.getPkgName()});
            long interval = getAlarmAdjustInterval();
            if (interval > 0) {
                this.mASHAdapter.periodAdjustAlarms(pkgList, interval);
                this.mAppRecord.updateUnifiedHeartbeat(true);
                ASHLog.i(this.mAppRecord.getPkgName() + " is unified heartbeat(ms): " + interval + " reason: " + reason);
                return;
            }
            ASHLog.i(this.mAppRecord.getPkgName() + " is not unified heartbeat");
        }
    }

    protected void cancelUnifiedHeartbeat(String reason) {
        if (isUnifiedHeartbeat()) {
            this.mASHAdapter.removePeriodAdjustAlarms(Arrays.asList(new String[]{this.mAppRecord.getPkgName()}));
            this.mAppRecord.updateUnifiedHeartbeat(false);
            ASHLog.i("cancel unified heartbeat: " + this.mAppRecord.getPkgName() + " reason:" + reason);
        }
    }

    protected boolean isUnifiedHeartbeat() {
        return this.mAppRecord.isUnifiedHeartbeat();
    }

    protected boolean isPendingAppAlarms() {
        return this.mAppRecord.isPendingAppAlarms();
    }

    protected void setAppNetworkRestrict() {
        ArrayList<Integer> uidsList = this.mAppRecord.getUids();
        for (Integer uid : uidsList) {
            this.mASHAdapter.setFirewallUidRule(uid.intValue(), true);
            this.mASHAdapter.closeSocketsForUid(uid.intValue());
        }
        ASHLog.i("setAppNetworkRestrict >> " + this.mAppRecord.getPkgName() + ", uids : " + uidsList);
        ASHLog.i("close sockets >> " + this.mAppRecord.getPkgName() + ", uids : " + uidsList);
    }

    protected void removeAppNetworkRestrict() {
        ArrayList<Integer> uidsList = this.mAppRecord.getUids();
        for (Integer uid : uidsList) {
            this.mASHAdapter.setFirewallUidRule(uid.intValue(), false);
        }
        ASHLog.i("removeAppNetworkRestrict >> " + this.mAppRecord.getPkgName() + ", uids : " + uidsList);
    }

    protected long proxyAppBroadcast() {
        List<String> pkgList = Arrays.asList(new String[]{this.mAppRecord.getPkgName()});
        if (this.mAppRecord.getAppType() == 9 && !this.mInitialBCExclude) {
            this.mASHAdapter.setActionExcludePkg("android.intent.action.SCREEN_OFF", this.mAppRecord.getPkgName());
            this.mInitialBCExclude = true;
        }
        long delay = this.mASHAdapter.proxyAppBroadcast(pkgList);
        ASHLog.i("proxy " + this.mAppRecord.getPkgName() + " broadcast" + (delay >= 0 ? " OK !" : " Fail !"));
        this.mBCProxyDelayTime = delay;
        return delay;
    }

    protected boolean dropProcessBC(int pid, List<String> actions) {
        return this.mASHAdapter.dropProcessBC(pid, actions);
    }

    public boolean dropPkgBC(String pkg, List<String> actions) {
        return this.mASHAdapter.dropPkgBC(pkg, actions);
    }

    protected long unproxyAppBroadcast() {
        long delay = this.mASHAdapter.unproxyAppBroadcast(Arrays.asList(new String[]{this.mAppRecord.getPkgName()}));
        ASHLog.i("unproxy " + this.mAppRecord.getPkgName() + " broadcast" + (delay >= 0 ? " OK !" : " Fail !"));
        return delay;
    }

    protected boolean proxyPackageAllBC() {
        return this.mASHAdapter.proxyPackageAllBC(this.mAppRecord.getPkgName());
    }

    public boolean clearProxyBCConfig() {
        return this.mASHAdapter.clearProxyBCConfig(this.mAppRecord.getPkgName());
    }

    protected void proxyApp() {
        for (Integer uid : this.mAppRecord.getUids()) {
            ASHLog.i("proxy app:" + this.mAppRecord.getPkgName() + ",uid:" + uid + ",result:" + this.mASHAdapter.proxyApp(this.mAppRecord.getPkgName(), uid.intValue(), true));
        }
    }

    protected void unproxyApp() {
        for (Integer uid : this.mAppRecord.getUids()) {
            ASHLog.i("unproxy app:" + this.mAppRecord.getPkgName() + ",uid:" + uid + ",result:" + this.mASHAdapter.proxyApp(this.mAppRecord.getPkgName(), uid.intValue(), false));
        }
    }

    public void proxyWakeLock() {
        for (Integer pid : this.mAppRecord.getPids()) {
            int uid = this.mAppRecord.getUid(pid.intValue());
            this.mASHAdapter.proxyWakeLock(pid.intValue(), uid);
            ASHLog.i("Proxy wakelock uid = " + uid + " pid = " + pid);
        }
    }

    public void unproxyWakeLock() {
        for (Integer pid : this.mAppRecord.getPids()) {
            int uid = this.mAppRecord.getUid(pid.intValue());
            this.mASHAdapter.unproxyWakeLock(pid.intValue(), uid);
            ASHLog.i("Unproxy wakelock uid = " + uid + " pid = " + pid);
        }
    }

    public void forceReleaseWakeLock() {
        if (this.mAppRecord.hasWakelock()) {
            for (Integer pid : this.mAppRecord.getPids()) {
                int uid = this.mAppRecord.getUid(pid.intValue());
                this.mASHAdapter.forceReleaseWakeLock(pid.intValue(), uid);
                ASHLog.i("release wakelock uid = " + uid + " pid = " + pid);
            }
            this.mIsNeedRestoreWL = true;
            return;
        }
        ASHLog.d("No wakelock need to release, app : " + this.mAppRecord.getPkgName());
    }

    public void forceRestoreWakeLock() {
        if (this.mIsNeedRestoreWL) {
            for (Integer pid : this.mAppRecord.getPids()) {
                int uid = this.mAppRecord.getUid(pid.intValue());
                this.mASHAdapter.forceRestoreWakeLock(pid.intValue(), uid);
                ASHLog.i("force restore wakelock uid = " + uid + " pid = " + pid);
            }
            this.mIsNeedRestoreWL = false;
            return;
        }
        ASHLog.d("No need to restore wakelock, app : " + this.mAppRecord.getPkgName());
    }

    public boolean addNetPacketListener() {
        boolean result = true;
        for (Integer uid : this.mAppRecord.getUids()) {
            result |= this.mASHAdapter.addNetPacketListener(uid);
            ASHLog.i("add listener " + this.mAppRecord.getPkgName() + " net packet" + (result ? " OK !" : " Fail !") + ",uid:" + uid);
        }
        return result;
    }

    public void removeNetPacketListener() {
        for (Integer uid : this.mAppRecord.getUids()) {
            this.mASHAdapter.removeNetPacketListener(uid);
        }
    }

    public boolean isImTypeApp() {
        return this.mAppRecord.isImTypeApp();
    }

    public boolean isConnected() {
        return this.mAppRecord.isConnected();
    }

    public boolean isProtectAppByUser() {
        return this.mAppRecord.isProtectAppByUser();
    }

    private long getAlarmAdjustInterval() {
        long interval = 300000;
        int type = this.mAppRecord.getAppType();
        boolean isIdleState = isDeepUserState();
        switch (type) {
            case NativeAdapter.PLATFORM_UNKNOWN /*-1*/:
                break;
            case NativeAdapter.PLATFORM_MTK /*1*/:
            case 4:
            case 9:
                if (!isIdleState) {
                    interval = 600000;
                    break;
                }
                interval = 900000;
                break;
            case NativeAdapter.PLATFORM_HI /*2*/:
            case NativeAdapter.PLATFORM_K3V3 /*3*/:
            case 11:
                interval = 300000;
                break;
            case 5:
            case 6:
            case 7:
            case 8:
                if (!isIdleState) {
                    interval = 600000;
                    break;
                }
                interval = 1800000;
                break;
            case 10:
                interval = 0;
                break;
            default:
                ASHLog.i("Unknow type = " + type);
                break;
        }
        if (interval <= 300000 || !isProtectAppByUser()) {
            return interval;
        }
        return 300000;
    }

    private boolean isDeepUserState() {
        return false;
    }

    protected void setUnprotectedAppInactive() {
        int type = this.mAppRecord.getAppType();
        if (!(type == 18 || type == 7)) {
            if (type != 6) {
                return;
            }
        }
        if (this.mIAppManager.isCleanDBExist() && this.mIAppManager.isCleanUnprotectApp(this.mAppRecord.getPkgName())) {
            if (this.mAppRecord.hasIgnoredActiveGps()) {
                this.mRestoreActiveWhenExit = true;
            }
            this.mASHAdapter.setAppInactive(this.mAppRecord.getPkgName(), true);
        }
    }

    protected void setAppActiveIfNeeded() {
        if (this.mRestoreActiveWhenExit) {
            this.mRestoreActiveWhenExit = false;
            this.mASHAdapter.setAppInactive(this.mAppRecord.getPkgName(), false);
        }
    }

    protected int getProcUTime() {
        return this.mASHAdapter.getProcUTime(this.mAppRecord.getMmPushPid());
    }
}
