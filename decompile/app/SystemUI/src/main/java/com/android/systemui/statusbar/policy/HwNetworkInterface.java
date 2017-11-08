package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import java.util.List;

public interface HwNetworkInterface {
    int getAirplaneIcon(int i);

    MobileSignalController getControllerBySubId(int i);

    int getDefaultDataSubId(int i);

    void handleBroadcastHuawei(Intent intent);

    void initLTEPlusState(Intent intent);

    void initMobileState();

    boolean isSuspend(int i, boolean z, ServiceState serviceState);

    void registerHuawei(IntentFilter intentFilter, BroadcastReceiver broadcastReceiver, Handler handler);

    List<SubscriptionInfo> sortSubsriptions(List<SubscriptionInfo> list);

    void unregisterHuawei();

    void updateOtherSubState(int i, int i2);

    List<SubscriptionInfo> updateSubcriptions(List<SubscriptionInfo> list);
}
