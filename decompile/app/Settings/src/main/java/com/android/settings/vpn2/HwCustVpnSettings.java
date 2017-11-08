package com.android.settings.vpn2;

import android.content.Context;

public class HwCustVpnSettings {
    public boolean isShowVpnL2TP() {
        return false;
    }

    public String[] getTypes(Context context) {
        return new String[]{""};
    }
}
