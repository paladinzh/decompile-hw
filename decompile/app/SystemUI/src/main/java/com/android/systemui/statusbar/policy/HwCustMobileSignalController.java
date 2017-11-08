package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.util.SparseArray;
import com.android.systemui.statusbar.policy.MobileSignalController.MobileIconGroup;

public class HwCustMobileSignalController {
    protected Context mContext;
    protected SubscriptionInfo mInfo;
    protected HwMobileSignalController mParent;

    public HwCustMobileSignalController(HwMobileSignalController parent, Context context, SubscriptionInfo info) {
        this.mParent = parent;
        this.mContext = context;
        this.mInfo = info;
    }

    public int getSubscription() {
        return this.mInfo.getSubscriptionId();
    }

    public void updateDataNetType(int networkType) {
    }

    public void updateCarrierSwitchSettings(Context aContext, TelephonyManager aPhone, ServiceState aServiceState) {
    }

    public void updateDataConnectedIcon(int dataActivity) {
    }

    public void mapIconSets(SparseArray<MobileIconGroup> sparseArray) {
    }

    public MobileIconGroup getIcons(MobileIconGroup icons) {
        return icons;
    }

    public int updateDataType(int typeIcon, int dataNetType, int subId, boolean isCAstate) {
        return typeIcon;
    }

    public int updateDataTypeNoDataConnected(int typeIcon, int dataNetType, int subId, boolean isCAstate) {
        return typeIcon;
    }

    public String getDataSettingMode(String dataSettingMode) {
        return dataSettingMode;
    }

    public boolean isShowMmsNetworkIcon() {
        return false;
    }

    public void updateExtNetworkData(SignalStrength signalStrength, ServiceState serviceState, int callState) {
    }

    public boolean isUsingVoWifi() {
        return false;
    }
}
