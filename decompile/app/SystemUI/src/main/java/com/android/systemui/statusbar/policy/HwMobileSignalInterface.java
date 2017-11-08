package com.android.systemui.statusbar.policy;

import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.SparseArray;
import com.android.systemui.statusbar.policy.MobileSignalController.MobileIconGroup;
import com.android.systemui.statusbar.policy.NetworkController.SignalCallback;

public interface HwMobileSignalInterface {
    void addIconGroupsHuawei(SparseArray<MobileIconGroup> sparseArray);

    int getDataNetType(int i, int i2);

    int getDefaultDataSubId(int i);

    MobileIconGroup getIconsHuawei();

    int getTypeIconHuawei(TelephonyManager telephonyManager, int i, int i2, boolean z, boolean z2, boolean z3);

    int handleShowFiveSignalException(int i);

    boolean isCAStateEnable();

    boolean isRoamingHuawei();

    boolean isRoamingHuawei(ServiceState serviceState);

    boolean isShowActivity();

    boolean isShowMmsNetworkIcon();

    void notifyListenerHuawei(SignalCallback signalCallback, MobileIconGroup mobileIconGroup, int i, boolean z);

    void setCAState(boolean z);

    void updateCallState(int i, String str);

    void updateDataActivity(int i);

    void updateDataConnectionState(int i, int i2);

    void updateServiceState(ServiceState serviceState);

    void updateSignalStrength(SignalStrength signalStrength);
}
