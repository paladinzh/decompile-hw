package com.android.settings.vpn2;

import android.security.KeyStore;

public class VpnUtils {
    public static String getLockdownVpn() {
        byte[] value = KeyStore.getInstance().get("LOCKDOWN_VPN");
        if (value == null) {
            return null;
        }
        return new String(value);
    }
}
