package com.android.settings.wifi;

import android.content.Context;
import android.content.Intent;
import android.support.v7.preference.TwoStatePreference;

public class HwCustHotspotAuthentication {
    public static final int BLUETOOTH_TETHERING = 2;
    public static final int USB_TETHERING = 1;
    public static final int WIFI_TETHERING = 0;

    public void initHwCustHotspotAuthenticationImpl(Context mContext) {
    }

    public void custReceiveBroadcast(Intent intent) {
    }

    public void custTetherReceiver(Intent intent) {
    }

    public void custStop() {
    }

    public boolean isHotspotAuthorization(boolean enable, TwoStatePreference mCheckBox, int mTetherChoice) {
        return false;
    }

    public boolean custUpdateTethering(boolean enabled, TwoStatePreference mUsbTether) {
        return false;
    }

    public boolean isTetheringAllowed(TwoStatePreference aCheckBox) {
        return true;
    }

    public void handleCustErrorView(TwoStatePreference aCheckBox) {
    }
}
