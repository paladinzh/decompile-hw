package com.huawei.systemmanager.power.batteryoptimize;

import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HwLog;

public class ProtectAppDetectItem extends PowerDetectItem {
    private static final String TAG = "ProtectAppDetectItem";
    private static final int ZERO = 0;
    private int mProtectAppNum = 0;

    public ProtectAppDetectItem(int mProtectAppNum) {
        this.mProtectAppNum = mProtectAppNum;
    }

    public String getTitle() {
        if (isOptimized()) {
            return getContext().getResources().getString(R.string.power_closeapp_optimize_des);
        }
        return String.format(getContext().getResources().getQuantityString(R.plurals.power_optimize_protectApp_optimized, this.mProtectAppNum, new Object[]{Integer.valueOf(this.mProtectAppNum)}), new Object[0]);
    }

    public void setExData(int num) {
        this.mProtectAppNum = num;
        doScan();
    }

    public int getItemType() {
        return 4;
    }

    public void doScan() {
        boolean state = false;
        if (this.mProtectAppNum == 0) {
            state = true;
        }
        if (state) {
            setState(1);
        } else {
            setState(2);
        }
        HwLog.i(TAG, "Power doscan called, ProtectApp State is " + state);
    }

    public void doOptimize() {
        HwLog.i(TAG, "ProtectApp doOptimize called, do nothing");
        setState(3);
    }
}
