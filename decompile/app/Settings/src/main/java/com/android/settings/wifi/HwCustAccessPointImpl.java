package com.android.settings.wifi;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import com.android.settingslib.R$string;
import com.android.settingslib.wifi.AccessPoint;

public class HwCustAccessPointImpl extends HwCustAccessPoint {
    static final int SECURITY_EAP = 3;
    static final int SECURITY_NONE = 0;
    static final int SECURITY_PSK = 2;
    static final int SECURITY_WAPI_CERT = 5;
    static final int SECURITY_WAPI_PSK = 4;
    static final int SECURITY_WEP = 1;
    private static final String TAG = "HwCustAccessPointImpl";

    public HwCustAccessPointImpl(AccessPoint accessPoint) {
        super(accessPoint);
    }

    public int getSecurity(WifiConfiguration config) {
        int i = 0;
        if (config.allowedKeyManagement.get(6)) {
            return 4;
        }
        if (config.allowedKeyManagement.get(7)) {
            return 5;
        }
        if (config.wepKeys[0] != null) {
            i = 1;
        }
        return i;
    }

    public int getSecurity(ScanResult result) {
        if ("".equals("WAPI-PSK") || "".equals("WAPI-CERT")) {
            return 0;
        }
        if (result.capabilities.contains("WAPI-PSK")) {
            return 4;
        }
        if (result.capabilities.contains("WAPI-CERT")) {
            return 5;
        }
        return 0;
    }

    public String getSecurityString(int security, boolean concise) {
        switch (security) {
            case 4:
                return this.mAccessPoint.getContext().getString(R$string.wifi_security_wapi_psk);
            case 5:
                return this.mAccessPoint.getContext().getString(R$string.wifi_security_wapi_cert);
            default:
                String str;
                if (concise) {
                    str = "";
                } else {
                    str = this.mAccessPoint.getContext().getString(R$string.wifi_security_none);
                }
                return str;
        }
    }

    public StringBuilder custRefresh(StringBuilder summary) {
        return summary.append(this.mAccessPoint.getContext().getString(2131629175));
    }
}
