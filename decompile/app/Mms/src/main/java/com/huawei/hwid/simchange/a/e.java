package com.huawei.hwid.simchange.a;

import android.content.Context;
import android.telephony.TelephonyManager;

/* compiled from: SingleSim */
public class e implements a {
    public boolean a() {
        return false;
    }

    public String a(Context context, int i) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (telephonyManager == null) {
            return null;
        }
        if (telephonyManager.getSimState() != 5) {
            return null;
        }
        return telephonyManager.getSimSerialNumber();
    }

    public int b(Context context, int i) {
        return ((TelephonyManager) context.getSystemService("phone")).getSimState();
    }
}
