package com.huawei.systemmanager.mainscreen.detector.item;

import android.content.Context;
import android.content.Intent;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.netassistant.ui.NetAssistantMainActivity;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;

public class TrafficDataItem extends DetectItem {
    private static final String TAG = "TrafficDataItem";
    private boolean mVisiable;

    public void doScan() {
        int state = SimCardManager.getInstance().getSimPackageState();
        if (state == 1) {
            this.mVisiable = false;
            setState(1);
            HwLog.i(TAG, "doScan called, current no simcard");
        } else if (state == 2) {
            this.mVisiable = true;
            setState(1);
            HwLog.i(TAG, "doScan called, at least one card set data");
        } else {
            this.mVisiable = true;
            setState(2);
            HwLog.i(TAG, "doScan called, all card not set traffic");
        }
    }

    public Intent getOptimizeIntent(Context ctx) {
        return new Intent(ctx, NetAssistantMainActivity.class);
    }

    public String getName() {
        return getContext().getString(R.string.net_assistant_package_setting);
    }

    public String getTitle(Context ctx) {
        if (isOptimized()) {
            return ctx.getString(R.string.main_screen_detect_item_traffic_safe);
        }
        return ctx.getString(R.string.main_screen_detect_item_traffic_unsafe);
    }

    public String getDescription(Context ctx) {
        return ctx.getString(R.string.main_screen_detect_item_traffic_description);
    }

    public int getItemType() {
        return 3;
    }

    public int getOptimizeActionType() {
        return 3;
    }

    public String getOptimizeActionName() {
        return getContext().getString(R.string.main_screen_detect_operation_turn_on, new Object[]{Utility.getLocaleNumber(getScore())});
    }

    public boolean isManulOptimize() {
        return true;
    }

    public boolean isVisiable() {
        return this.mVisiable;
    }

    protected int score() {
        return 4;
    }

    public void refresh() {
        HwLog.i(TAG, "refresh called");
        doScan();
    }

    public boolean isEnable() {
        return CustomizeManager.getInstance().isFeatureEnabled(30);
    }

    public String getTag() {
        return TAG;
    }

    public DetectItem copy() {
        TrafficDataItem item = new TrafficDataItem();
        item.mVisiable = this.mVisiable;
        item.setState(getState());
        return item;
    }

    public void statOptimizeEvent() {
        HsmStat.statE(Events.E_MAINSCREEN_DO_OPTIMZE_TRAFFIC);
    }
}
