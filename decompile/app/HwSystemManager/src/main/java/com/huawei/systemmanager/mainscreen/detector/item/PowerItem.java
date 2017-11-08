package com.huawei.systemmanager.mainscreen.detector.item;

import android.content.Context;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.power.model.PowerManagementModel;
import com.huawei.systemmanager.util.HwLog;

public class PowerItem extends DetectItem {
    private static final String TAG = "PowerItem";
    private int mModel = 0;

    protected int score() {
        return 3;
    }

    public void doScan() {
        this.mModel = PowerManagementModel.getInstance(getContext()).getPowerModeState();
        if (this.mModel == 1 || this.mModel == 2) {
            setState(1);
        } else {
            setState(2);
        }
    }

    public void doOptimize(Context ctx) {
        PowerManagementModel powerModel = PowerManagementModel.getInstance(ctx);
        powerModel.setPowerModeState(1);
        this.mModel = powerModel.getPowerModeState();
        setState(3);
    }

    public int getOptimizeActionType() {
        return 1;
    }

    public String getTitle(Context context) {
        switch (this.mModel) {
            case 1:
                return context.getString(R.string.power_mode_optimize_des, new Object[]{getContext().getString(R.string.smart_mode_title)});
            case 2:
                return context.getString(R.string.power_mode_optimize_des, new Object[]{getContext().getString(R.string.endurance_mode_title)});
            default:
                return context.getString(R.string.power_mode_des, new Object[]{getContext().getString(R.string.performance_mode_title)});
        }
    }

    public String getOptimizeActionName() {
        return getContext().getString(R.string.turn_on);
    }

    public int getItemType() {
        return 13;
    }

    public String getName() {
        return getContext().getString(R.string.save_mode);
    }

    public boolean isManulOptimize() {
        return false;
    }

    public void refresh() {
        HwLog.i(TAG, "refresh called");
        doScan();
    }

    public String getTag() {
        return TAG;
    }

    public DetectItem copy() {
        PowerItem item = new PowerItem();
        item.mModel = this.mModel;
        item.setState(getState());
        return item;
    }
}
