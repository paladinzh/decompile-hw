package com.huawei.systemmanager.power.batteryoptimize;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.power.data.devstatus.DevStatusUtil;
import com.huawei.systemmanager.util.HwLog;

public class VibrateDetectItem extends PowerDetectItem {
    private static final String TAG = "VibrateDetectItem";

    public String getTitle() {
        if (isOptimized()) {
            return getContext().getResources().getString(R.string.power_vibrate_optimize_des);
        }
        return getContext().getResources().getString(R.string.power_vibrate_des);
    }

    public int getItemType() {
        return 10;
    }

    public void doScan() {
        boolean state = DevStatusUtil.isVibrateOptimzeState(getContext());
        if (state) {
            setState(1);
        } else {
            setState(2);
        }
        HwLog.i(TAG, "Power doscan called, isVibrateOptimzeState is " + state);
    }

    public void doOptimize() {
        HwLog.i(TAG, "Vibrate doOptimize called");
        DevStatusUtil.setVibrateState(getContext(), false);
        setState(3);
    }
}
