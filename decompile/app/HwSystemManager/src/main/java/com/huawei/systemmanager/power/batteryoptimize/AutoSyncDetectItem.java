package com.huawei.systemmanager.power.batteryoptimize;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.power.data.devstatus.DevStatusUtil;
import com.huawei.systemmanager.util.HwLog;

public class AutoSyncDetectItem extends PowerDetectItem {
    private static final String TAG = "AutoSyncDetectItem";

    public String getTitle() {
        if (isOptimized()) {
            return getContext().getResources().getString(R.string.power_autosync_optimize_des);
        }
        return getContext().getResources().getString(R.string.power_autosync_des);
    }

    public int getItemType() {
        return 9;
    }

    public void doScan() {
        boolean state = DevStatusUtil.isAutoSyncOptimizeState();
        if (state) {
            setState(1);
        } else {
            setState(2);
        }
        HwLog.i(TAG, "Power doscan called, isAutoSyncOptimizeState is " + state);
    }

    public void doOptimize() {
        HwLog.i(TAG, "AutoSync doOptimize called");
        DevStatusUtil.setAutoSyncState(false);
        setState(3);
    }
}
