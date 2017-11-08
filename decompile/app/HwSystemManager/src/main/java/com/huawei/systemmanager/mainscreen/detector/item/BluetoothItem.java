package com.huawei.systemmanager.mainscreen.detector.item;

import android.content.Context;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.power.data.devstatus.DevStatusUtil;
import com.huawei.systemmanager.util.HwLog;

public class BluetoothItem extends DetectItem {
    public static final int BLUETOOTH_OFF = 0;
    public static final int BLUETOOTH_ON_NOT_PAIR = 1;
    public static final int BLUETOOTH_ON_PAIRED = 2;
    private static final String TAG = "BluetoothItem";
    private int mBluetoothState;

    public String getTitle(Context context) {
        if (isOptimized()) {
            return context.getString(R.string.power_bluetooth_optimize_des);
        }
        return context.getString(R.string.bluetooth_to_be_optimize);
    }

    protected int score() {
        return 2;
    }

    public int getItemType() {
        return 9;
    }

    public String getName() {
        return getContext().getString(R.string.power_bluetooth);
    }

    public void doScan() {
        if (!DevStatusUtil.getBluetoothState()) {
            this.mBluetoothState = 0;
            setState(1);
        } else if (DevStatusUtil.isBlueToothStateOptimize()) {
            this.mBluetoothState = 2;
            setState(1);
        } else {
            this.mBluetoothState = 1;
            setState(2);
        }
        HwLog.i(TAG, "doscan called, mBluetoothState is " + this.mBluetoothState);
    }

    public void refresh() {
        HwLog.i(TAG, "refresh called");
        doScan();
    }

    public String getTag() {
        return TAG;
    }

    public int getOptimizeActionType() {
        return 1;
    }

    public void doOptimize(Context context) {
        HwLog.i(TAG, "bluetooth doOptimize called");
        DevStatusUtil.setBluetoothState(false);
        this.mBluetoothState = 0;
        setState(3);
    }

    public boolean isVisiable() {
        if (this.mBluetoothState == 2) {
            return false;
        }
        return true;
    }

    public DetectItem copy() {
        return new BluetoothItem();
    }

    public String getOptimizeActionName() {
        return getContext().getString(R.string.main_screen_detect_operation_turn_off, new Object[]{Utility.getLocaleNumber(score())});
    }

    public boolean isManulOptimize() {
        return true;
    }

    public void statOptimizeEvent() {
        HsmStat.statE(Events.E_MAINSCREEN_DO_OPTIMZE_BLUETOOTH);
    }
}
