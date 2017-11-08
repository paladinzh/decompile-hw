package com.android.settings.vpn2;

import android.content.Context;

public class HwCustVpnSettingsImpl extends HwCustVpnSettings {
    public boolean isShowVpnL2TP() {
        return true;
    }

    public String[] getTypes(Context context) {
        return context.getResources().getStringArray(2131362017);
    }
}
