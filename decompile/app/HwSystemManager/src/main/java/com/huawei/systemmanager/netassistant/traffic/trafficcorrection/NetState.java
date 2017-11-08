package com.huawei.systemmanager.netassistant.traffic.trafficcorrection;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.huawei.systemmanager.comm.misc.GlobalContext;

public class NetState {
    private static NetworkInfo getActiveNetInfo() {
        return ((ConnectivityManager) GlobalContext.getContext().getSystemService("connectivity")).getActiveNetworkInfo();
    }

    public static boolean isCurrentMobileType() {
        boolean z = false;
        NetworkInfo info = getActiveNetInfo();
        if (info == null) {
            return false;
        }
        if (info.getType() == 0) {
            z = true;
        }
        return z;
    }

    public static boolean isCurrentNetActive() {
        NetworkInfo info = getActiveNetInfo();
        if (info == null || !info.isConnected()) {
            return false;
        }
        return true;
    }
}
