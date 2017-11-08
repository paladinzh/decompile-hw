package com.huawei.systemmanager.power.batteryoptimize;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.power.data.devstatus.DevStatusUtil;
import com.huawei.systemmanager.util.HwLog;

public class AutoRotationDetectItem extends PowerDetectItem {
    private static final String TAG = "AutoRotationDetectItem";

    public String getTitle() {
        if (isOptimized()) {
            return getContext().getResources().getString(R.string.power_autoratation_optimize_des);
        }
        return getContext().getResources().getString(R.string.power_autoratation_des);
    }

    public int getItemType() {
        return 12;
    }

    public void doScan() {
        boolean state = DevStatusUtil.isAutoRotateOptimzeState(getContext());
        if (state) {
            setState(1);
        } else {
            setState(2);
        }
        HwLog.i(TAG, "Power doscan called, isAutoRotateOptimzeState is " + state);
    }

    public void doOptimize() {
        HwLog.i(TAG, "AutoRotate doOptimize called");
        DevStatusUtil.setAutoRotateState(getContext(), false);
        setState(3);
    }
}
