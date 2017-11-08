package com.huawei.powergenie.modules.apppower.hibernation.actions.adapter;

import android.os.SystemProperties;
import com.huawei.powergenie.api.IAppPowerAction;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.modules.apppower.hibernation.ASHLog;
import java.util.ArrayList;
import java.util.List;

public class ASHAdapter {
    private static final ArrayList<Integer> mNetPacketUids = new ArrayList();
    private static final List<String> mProxyActionList = new ArrayList<String>() {
        {
            add("android.intent.action.ANY_DATA_STATE");
            add("android.intent.action.TIME_TICK");
            add("android.intent.action.BATTERY_CHANGED");
            add("android.net.wifi.SCAN_RESULTS");
            add("android.net.wifi.STATE_CHANGE");
            add("android.intent.action.CONFIGURATION_CHANGED");
            add("android.intent.action.SERVICE_STATE");
            add("android.net.conn.CONNECTIVITY_CHANGE");
            add("android.net.wifi.supplicant.STATE_CHANGE");
            add("android.intent.action.USER_PRESENT");
            add("android.intent.action.PACKAGE_RESTARTED");
            add("android.intent.action.CLOSE_SYSTEM_DIALOGS");
            add("android.net.wifi.RSSI_CHANGED");
            add("android.location.GPS_ENABLED_CHANGE");
            add("org.agoo.android.intent.action.ELECTION_RESULT_V4");
        }
    };
    private static ASHAdapter sInstance;
    private String mCloseActionSwitcher = SystemProperties.get("persist.sys.pg_close_action", null);
    private final IAppPowerAction mIAppPowerAction;

    public static synchronized ASHAdapter getInstance(ICoreContext coreContext) {
        ASHAdapter aSHAdapter;
        synchronized (ASHAdapter.class) {
            if (sInstance == null) {
                sInstance = new ASHAdapter(coreContext);
            }
            aSHAdapter = sInstance;
        }
        return aSHAdapter;
    }

    private ASHAdapter(ICoreContext coreContext) {
        this.mIAppPowerAction = (IAppPowerAction) coreContext.getService("appmamager");
        initBroadcastProxy();
    }

    private void initBroadcastProxy() {
        proxyBCConfig(4, "20", null);
        if (!proxyBCConfig(1, null, mProxyActionList)) {
            this.mIAppPowerAction.setProxyBCActions(mProxyActionList);
        }
        if (!this.mIAppPowerAction.proxyBCConfigEx(2, "com.eg.android.AlipayGphone", "android.intent.action.CLOSE_SYSTEM_DIALOGS")) {
            setActionExcludePkg("android.intent.action.CLOSE_SYSTEM_DIALOGS", "com.eg.android.AlipayGphone");
        }
        this.mIAppPowerAction.proxyBCConfigEx(5, "android.intent.action.SCREEN_ON", "android.intent.action.SCREEN_OFF");
    }

    private boolean isActionClosed(String closeAction) {
        if (this.mCloseActionSwitcher == null || !this.mCloseActionSwitcher.contains(closeAction)) {
            return false;
        }
        ASHLog.i("Action " + closeAction + " is closed !");
        return true;
    }

    public boolean notifyBastetProxy(ArrayList<Integer> bastetPids) {
        return this.mIAppPowerAction.notifyBastetProxy(bastetPids);
    }

    public boolean notifyBastetUnProxy(ArrayList<Integer> bastetPids) {
        return this.mIAppPowerAction.notifyBastetUnProxy(bastetPids);
    }

    public boolean notifyBastetUnProxyAll() {
        ASHLog.i("unProxy all bastet app process!");
        return this.mIAppPowerAction.notifyBastetUnProxyAll();
    }

    public boolean freezeAppProcess(ArrayList<Integer> freezePidsList, String appPkg, int appUid) {
        if (isActionClosed("freeze")) {
            return false;
        }
        return this.mIAppPowerAction.freezeAppProcess(freezePidsList, appPkg, appUid);
    }

    public boolean unFreezeAppProcess(ArrayList<Integer> unFreezePidsList, String appPkg, int appUid) {
        if (isActionClosed("freeze")) {
            return false;
        }
        return this.mIAppPowerAction.unFreezeAppProcess(unFreezePidsList, appPkg, appUid);
    }

    public boolean unFreezeAllAppProcess() {
        if (isActionClosed("freeze")) {
            return false;
        }
        ASHLog.i("unFreeze all app process !");
        return this.mIAppPowerAction.unFreezeAllAppProcess();
    }

    public boolean pendingAppAlarms(List<String> pkgList) {
        if (isActionClosed("pending_alarm")) {
            return false;
        }
        return this.mIAppPowerAction.pendingAppAlarms(pkgList, true);
    }

    public boolean unpendingAppAlarms(List<String> pkgList) {
        if (isActionClosed("pending_alarm")) {
            return false;
        }
        return this.mIAppPowerAction.unpendingAppAlarms(pkgList, true);
    }

    public boolean unpendingAllAlarms() {
        if (isActionClosed("pending_alarm")) {
            return false;
        }
        ASHLog.i("unpending all app alarms !");
        return this.mIAppPowerAction.unpendingAllAlarms();
    }

    public void periodAdjustAlarms(List<String> pkgList, long interval) {
        if (!isActionClosed("adjust_alarm")) {
            this.mIAppPowerAction.periodAdjustAlarms(pkgList, 0, interval, 1);
        }
    }

    public void removePeriodAdjustAlarms(List<String> pkgList) {
        if (!isActionClosed("adjust_alarm")) {
            this.mIAppPowerAction.removePeriodAdjustAlarms(pkgList, 0);
        }
    }

    public void removeAllPeriodAdjustAlarms() {
        if (!isActionClosed("adjust_alarm")) {
            ASHLog.i("cancel all app alarm period adjust !");
            this.mIAppPowerAction.removeAllPeriodAdjustAlarms();
        }
    }

    public void setFirewallUidRule(int uid, boolean restrict) {
        if (!isActionClosed("restrict_firewall_rule")) {
            this.mIAppPowerAction.setFirewallUidRule(uid, restrict);
        }
    }

    public void recoveryFirewallUidRule() {
        if (!isActionClosed("restrict_firewall_rule")) {
            ASHLog.i("ASHAdapter", "recovery all firewall uid rule !");
            this.mIAppPowerAction.recoveryFirewallUidRule();
        }
    }

    public long proxyAppBroadcast(List<String> pkgs) {
        if (isActionClosed("proxy_bc")) {
            return -1;
        }
        return this.mIAppPowerAction.proxyAppBroadcast(pkgs);
    }

    public long unproxyAppBroadcast(List<String> pkgs) {
        if (isActionClosed("proxy_bc")) {
            return -1;
        }
        return this.mIAppPowerAction.unproxyAppBroadcast(pkgs);
    }

    public long unproxyAllAppBroadcast() {
        if (isActionClosed("proxy_bc")) {
            return -1;
        }
        ASHLog.i("cancel all app broadcast proxy !");
        return Math.max(this.mIAppPowerAction.unproxyAllAppBroadcast(), this.mIAppPowerAction.unproxyAllAppBroadcastByPid());
    }

    public void setActionExcludePkg(String action, String pkg) {
        if (!isActionClosed("proxy_bc")) {
            this.mIAppPowerAction.setActionExcludePkg(action, pkg);
        }
    }

    public boolean proxyBCConfig(int type, String key, List<String> value) {
        if (isActionClosed("proxy_bc")) {
            return false;
        }
        return this.mIAppPowerAction.proxyBCConfig(type, key, value);
    }

    public boolean dropProcessBC(int pid, List<String> actions) {
        if (isActionClosed("proxy_bc")) {
            return false;
        }
        return this.mIAppPowerAction.dropProcessBC(pid, actions);
    }

    public boolean dropPkgBC(String pkg, List<String> actions) {
        if (isActionClosed("proxy_bc")) {
            return false;
        }
        return this.mIAppPowerAction.dropPkgBC(pkg, actions);
    }

    public boolean proxyPackageAllBC(String pkgName) {
        return proxyBCConfig(1, pkgName, null);
    }

    public boolean clearProxyBCConfig(String pkgName) {
        return proxyBCConfig(1, pkgName, mProxyActionList);
    }

    public boolean addNetPacketListener(Integer uid) {
        if (!mNetPacketUids.contains(uid)) {
            mNetPacketUids.add(uid);
        }
        return this.mIAppPowerAction.netPacketListener(mNetPacketUids);
    }

    public void removeNetPacketListener(Integer uid) {
        if (mNetPacketUids.contains(uid)) {
            mNetPacketUids.remove(uid);
            if (mNetPacketUids.size() == 0) {
                this.mIAppPowerAction.netPacketListener(mNetPacketUids);
            }
        }
    }

    public int getProcUTime(int pid) {
        return this.mIAppPowerAction.getProcUTime(pid);
    }

    public void proxyWakeLock(int pid, int uid) {
        if (!isActionClosed("proxy_wl")) {
            this.mIAppPowerAction.proxyWakeLock(pid, uid);
        }
    }

    public void unproxyWakeLock(int pid, int uid) {
        if (!isActionClosed("proxy_wl")) {
            this.mIAppPowerAction.unproxyWakeLock(pid, uid);
        }
    }

    public void unproxyAllAppWakeLock() {
        if (!isActionClosed("proxy_wl")) {
            ASHLog.i("cancel all app wakelock proxy !");
            this.mIAppPowerAction.unproxyWakeLock(-1, -1);
        }
    }

    public void forceReleaseWakeLock(int pid, int uid) {
        if (!isActionClosed("release_wl")) {
            this.mIAppPowerAction.forceReleaseWakeLock(pid, uid);
        }
    }

    public void forceRestoreWakeLock(int pid, int uid) {
        if (!isActionClosed("restore_wl")) {
            this.mIAppPowerAction.forceRestoreWakeLock(pid, uid);
        }
    }

    public void forceRestoreAllAppWakeLock() {
        if (!isActionClosed("restore_wl")) {
            ASHLog.i("restore all app wakelock !");
            this.mIAppPowerAction.forceRestoreWakeLock(-1, -1);
        }
    }

    public boolean proxyApp(String pkg, int uid, boolean proxy) {
        return this.mIAppPowerAction.proxyApp(pkg, uid, proxy, false);
    }

    public boolean unproxyAllApps() {
        ASHLog.i("unproxy all apps!");
        return proxyApp("", 0, false);
    }

    public void setAppInactive(String packageName, boolean inactive) {
        this.mIAppPowerAction.setAppInactive(packageName, inactive);
    }

    public void closeSocketsForUid(int uid) {
        if (!isActionClosed("close_sockets")) {
            this.mIAppPowerAction.closeSocketsForUid(uid);
        }
    }
}
