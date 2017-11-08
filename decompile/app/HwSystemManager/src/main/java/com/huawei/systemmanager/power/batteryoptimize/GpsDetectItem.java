package com.huawei.systemmanager.power.batteryoptimize;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.power.data.devstatus.DevStatusUtil;
import com.huawei.systemmanager.util.HwLog;

public class GpsDetectItem extends PowerDetectItem {
    private static final String TAG = "GpsDetectItem";

    public String getTitle() {
        if (isOptimized()) {
            return getContext().getResources().getString(R.string.power_gps_optimize_des);
        }
        return getContext().getResources().getString(R.string.power_gps_des);
    }

    public int getItemType() {
        return 8;
    }

    public void doRefreshOptimize() {
        HwLog.i(TAG, "Gps doRefreshOptimize called");
        boolean isOptimized = false;
        int mode = DevStatusUtil.getGpsDetailState(getContext());
        if (mode == 0 || mode == 2) {
            isOptimized = true;
        }
        if (isOptimized) {
            setState(3);
            HwLog.i(TAG, "Gps doRefreshOptimize successful");
        }
    }

    public void doScan() {
        boolean state = DevStatusUtil.isGpsOptimzeState(getContext());
        if (state) {
            setState(1);
        } else {
            setState(2);
        }
        HwLog.i(TAG, "Power doscan called, isGpsOptimzeState state is " + state);
    }

    public void doOptimize() {
        HwLog.i(TAG, "Gps doOptimize called");
        boolean isOptimized = false;
        int mode = DevStatusUtil.getGpsDetailState(getContext());
        if (mode == 3) {
            isOptimized = DevStatusUtil.setGpsState(getContext(), 2);
        } else if (mode == 1) {
            isOptimized = DevStatusUtil.setGpsState(getContext(), 0);
        }
        if (isOptimized) {
            setState(3);
            HwLog.i(TAG, "Gps doOptimize successful");
        }
    }
}
