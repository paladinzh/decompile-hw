package com.android.systemui.statusbar.phone;

import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;

public class HwCustPhoneStatusBarPolicy {
    public HwCustPhoneStatusBarPolicy(PhoneStatusBarPolicy phoneStatusBarPolicy, Context context) {
    }

    public void handleMoreActionCust(Intent intent) {
    }

    public boolean isVolteShow() {
        return true;
    }

    public void setEyeProtectIcon(StatusBarManager mService) {
    }

    public boolean isEyeShowSupport() {
        return false;
    }

    public void updateEye(StatusBarManager mService) {
    }
}
