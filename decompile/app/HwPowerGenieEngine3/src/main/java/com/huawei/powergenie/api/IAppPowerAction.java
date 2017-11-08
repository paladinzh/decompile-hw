package com.huawei.powergenie.api;

import java.util.ArrayList;
import java.util.List;

public interface IAppPowerAction {
    void closeSocketsForUid(int i);

    boolean dropPkgBC(String str, List<String> list);

    boolean dropProcessBC(int i, List<String> list);

    void exitDeviceIdle();

    void forceDeviceToIdle();

    void forceReleaseWakeLock(int i, int i2);

    void forceRestoreWakeLock(int i, int i2);

    void forceStopApp(String str, String str2);

    boolean freezeAppProcess(ArrayList<Integer> arrayList, String str, int i);

    int getProcUTime(int i);

    boolean isDeviceIdleMode();

    boolean netPacketListener(ArrayList<Integer> arrayList);

    boolean notifyBastetProxy(ArrayList<Integer> arrayList);

    boolean notifyBastetUnProxy(ArrayList<Integer> arrayList);

    boolean notifyBastetUnProxyAll();

    boolean pendingAppAlarms(List<String> list, boolean z);

    void periodAdjustAlarms(List<String> list, int i, long j, int i2);

    boolean proxyApp(String str, int i, boolean z, boolean z2);

    long proxyAppBroadcast(List<String> list);

    boolean proxyBCConfig(int i, String str, List<String> list);

    boolean proxyBCConfigEx(int i, String str, String str2);

    void proxyWakeLock(int i, int i2);

    void recoveryFirewallUidRule();

    void removeAllPeriodAdjustAlarms();

    void removePeriodAdjustAlarms(List<String> list, int i);

    void setActionExcludePkg(String str, String str2);

    void setAppInactive(String str, boolean z);

    void setFirewallUidRule(int i, boolean z);

    void setProxyBCActions(List<String> list);

    boolean unFreezeAllAppProcess();

    boolean unFreezeAppProcess(ArrayList<Integer> arrayList, String str, int i);

    boolean unpendingAllAlarms();

    boolean unpendingAppAlarms(List<String> list, boolean z);

    long unproxyAllAppBroadcast();

    long unproxyAllAppBroadcastByPid();

    long unproxyAppBroadcast(List<String> list);

    void unproxyWakeLock(int i, int i2);
}
