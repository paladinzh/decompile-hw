package com.huawei.systemmanager.power.batteryoptimize;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.power.data.devstatus.DevStatusUtil;
import com.huawei.systemmanager.util.HwLog;

public class MobileDataDetectItem extends PowerDetectItem {
    private static final String TAG = "MobileDataDetectItem";

    public String getTitle() {
        if (isOptimized()) {
            return getContext().getResources().getString(R.string.power_mobiledata_optimize_des);
        }
        return getContext().getResources().getString(R.string.power_mobiledata_des);
    }

    public boolean isEnable() {
        if (Utility.isWifiOnlyMode() || DevStatusUtil.isAirModeOn(getContext())) {
            return false;
        }
        return true;
    }

    public int getItemType() {
        return 7;
    }

    public void doScan() {
        boolean state = DevStatusUtil.isMobileDateOptimzeState(getContext());
        if (state) {
            setState(1);
        } else {
            setState(2);
        }
        HwLog.i(TAG, "Power doscan called, isMobileDateOptimzeState is " + state);
    }

    public void doOptimize() {
        HwLog.i(TAG, "MobileDataState doOptimize called");
        DevStatusUtil.setMobileDataState(getContext(), false);
        setState(3);
    }
}
