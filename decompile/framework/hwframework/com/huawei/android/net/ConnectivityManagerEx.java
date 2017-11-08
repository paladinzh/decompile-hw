package com.huawei.android.net;

import huawei.android.net.HwConnectivityExManager;

public class ConnectivityManagerEx {
    private static ConnectivityManagerEx mInstace = new ConnectivityManagerEx();

    public static ConnectivityManagerEx getDefault() {
        return mInstace;
    }

    public void setSmartKeyguardLevel(String level) {
        HwConnectivityExManager.getDefault().setSmartKeyguardLevel(level);
    }

    public void setUseCtrlSocket(boolean state) {
        HwConnectivityExManager.getDefault().setUseCtrlSocket(state);
    }
}
