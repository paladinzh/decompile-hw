package com.android.settings.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import com.android.settingslib.R$string;
import com.android.settingslib.wifi.AccessPoint;

public class HwCustAccessPoint {
    static final int SECURITY_NONE = 0;
    static final int SECURITY_WEP = 1;
    public AccessPoint mAccessPoint;

    public HwCustAccessPoint(AccessPoint accessPoint) {
        this.mAccessPoint = accessPoint;
    }

    public int getSecurity(WifiConfiguration config) {
        return config.wepKeys[0] != null ? 1 : 0;
    }

    public int getSecurity(ScanResult result) {
        return 0;
    }

    public String getSecurityString(int security, boolean concise) {
        return concise ? "" : this.mAccessPoint.getContext().getString(R$string.wifi_security_none);
    }
}
