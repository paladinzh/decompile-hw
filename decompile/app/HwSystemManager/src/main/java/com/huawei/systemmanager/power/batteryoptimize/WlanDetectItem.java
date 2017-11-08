package com.huawei.systemmanager.power.batteryoptimize;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.power.data.devstatus.DevStatusUtil;
import com.huawei.systemmanager.util.HwLog;

public class WlanDetectItem extends PowerDetectItem {
    private static final String TAG = "WlanDetectItem";

    public String getTitle() {
        if (isOptimized()) {
            return getContext().getString(R.string.power_wlan_optimize_des_new);
        }
        return getContext().getString(R.string.power_wlan_des);
    }

    public int getItemType() {
        return 2;
    }

    public boolean isEnable() {
        return !DevStatusUtil.isWlanStateOptimize(getContext());
    }

    public void doScan() {
        boolean on = DevStatusUtil.isWlanStateOptimize(getContext());
        if (on) {
            setState(1);
        } else {
            setState(2);
        }
        HwLog.i(TAG, "doscan called, mWlan is " + on);
    }

    public void doOptimize() {
        HwLog.i(TAG, "wifi doOptimize called");
        DevStatusUtil.setWifiState(GlobalContext.getContext(), false);
        setState(3);
    }
}
