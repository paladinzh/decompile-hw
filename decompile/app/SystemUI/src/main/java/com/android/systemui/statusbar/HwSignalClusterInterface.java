package com.android.systemui.statusbar;

import android.telephony.SubscriptionInfo;
import com.android.systemui.statusbar.policy.MobileSignalController;
import java.util.List;

public interface HwSignalClusterInterface {
    void applyHuawei(boolean z, boolean z2, boolean z3);

    void setMobileDataIndicatorsHuawei(boolean z, int i, int i2, String str, String str2, boolean z2, int i3, boolean z3, boolean z4, MobileSignalController mobileSignalController);

    void setWifiActivityIconId(boolean z, int i, String str, boolean z2, boolean z3, boolean z4, boolean z5);

    void setWifiViewResource(boolean z);

    void updateExtData(int i, int i2, boolean z, boolean z2, int... iArr);

    void updateSubs(List<SubscriptionInfo> list);
}
