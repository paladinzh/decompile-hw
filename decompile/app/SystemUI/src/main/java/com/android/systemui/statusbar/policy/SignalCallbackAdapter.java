package com.android.systemui.statusbar.policy;

import android.telephony.SubscriptionInfo;
import com.android.systemui.statusbar.policy.NetworkController.IconState;
import com.android.systemui.statusbar.policy.NetworkController.SignalCallback;
import java.util.List;

public class SignalCallbackAdapter implements SignalCallback {
    public void setWifiIndicators(boolean enabled, IconState statusIcon, IconState qsIcon, boolean activityIn, boolean activityOut, String description) {
    }

    public void setMobileDataIndicators(IconState statusIcon, IconState qsIcon, int statusType, int qsType, boolean activityIn, boolean activityOut, String typeContentDescription, String description, boolean isWide, int subId) {
    }

    public void setSubs(List<SubscriptionInfo> list) {
    }

    public void setNoSims(boolean show) {
    }

    public void setEthernetIndicators(IconState icon) {
    }

    public void setIsAirplaneMode(IconState icon) {
    }

    public void setMobileDataEnabled(boolean enabled) {
    }

    public void setExtData(int sub, int inetCon, boolean isRoam, boolean isSuspend, int... extArgs) {
    }

    public void updateSubs(int sub1, int sub2) {
    }
}
