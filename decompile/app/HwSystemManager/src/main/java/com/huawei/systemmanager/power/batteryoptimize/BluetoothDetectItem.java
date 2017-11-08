package com.huawei.systemmanager.power.batteryoptimize;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.power.data.devstatus.DevStatusUtil;
import com.huawei.systemmanager.util.HwLog;

public class BluetoothDetectItem extends PowerDetectItem {
    private static final String TAG = "BluetoothDetectItem";

    public String getTitle() {
        if (isOptimized()) {
            return getContext().getString(R.string.power_bluetooth_optimize_des);
        }
        return getContext().getString(R.string.power_bluetooth_des);
    }

    public int getItemType() {
        return 3;
    }

    public boolean isEnable() {
        return !DevStatusUtil.isBlueToothStateOptimize();
    }

    public void doScan() {
        boolean on = DevStatusUtil.isBlueToothStateOptimize();
        if (on) {
            setState(1);
        } else {
            setState(2);
        }
        HwLog.i(TAG, "doscan called, mBluetoothState is " + on);
    }

    public void doOptimize() {
        HwLog.i(TAG, "bluetooth doOptimize called");
        DevStatusUtil.setBluetoothState(false);
        setState(3);
    }
}
