package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.view.View;
import com.android.systemui.operatorname.HwOperatorNameParentView;
import com.android.systemui.qs.QSPanel;

public class HwCustPhoneStatusBar {
    Context mContext;

    public HwCustPhoneStatusBar(Context context) {
        this.mContext = context;
    }

    public String getCustomOperatorName(int sub, String str) {
        return str;
    }

    public void updateSimStatusCardView(HwOperatorNameParentView operatorNameParent) {
    }

    public void setPlmnToSettings(String networkName, int subscription) {
    }

    public boolean isNotShowJapaneseNoService(String networkName) {
        return false;
    }

    public int getSimStateVSimFixup(int state, int sub) {
        return state;
    }

    public boolean hasIccCardVSimFixup(boolean present, int sub) {
        return present;
    }

    public String getNetworkNameVSimFixup(String name, int sub) {
        return name;
    }

    public void updateNotificationFontSize(View parentView) {
    }

    @Deprecated
    public boolean isSetOperatorViewVisibleAsEri() {
        return true;
    }

    public boolean isSprintHomeNetwork() {
        return false;
    }

    public boolean isNotShowEmergencyForOrange(String networkName) {
        return false;
    }

    public boolean disableNavigationKey() {
        return false;
    }

    public void registerReceivers(QSPanel mQSPanel) {
    }

    public void unregisterReceivers() {
    }

    public boolean isRemoveEnable4G(Context context) {
        return false;
    }
}
