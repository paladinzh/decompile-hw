package com.huawei.systemmanager.mainscreen.detector.item;

import android.content.Context;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;

public class VirusUpdateItem extends DetectItem {
    private static final String TAG = "VirusUpdateItem";

    public void doScan() {
        boolean autoUpdate = AntiVirusTools.isAutoUpdate(GlobalContext.getContext());
        if (autoUpdate) {
            setState(1);
        } else {
            setState(2);
        }
        HwLog.i(TAG, "doscan called, autoUpdata:" + autoUpdate);
    }

    protected int score() {
        return 3;
    }

    public void doOptimize(Context ctx) {
        AntiVirusTools.setAutoUpdate(ctx, true);
        setState(3);
    }

    public int getOptimizeActionType() {
        return 1;
    }

    public String getTitle(Context context) {
        if (isOptimized()) {
            return context.getString(R.string.virus_depot_open);
        }
        return context.getString(R.string.virus_depot_close);
    }

    public int getItemType() {
        return 12;
    }

    public String getOptimizeActionName() {
        return getContext().getString(R.string.turn_on);
    }

    public String getName() {
        return getContext().getString(R.string.title_auto_update_virus_lib);
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
        VirusUpdateItem item = new VirusUpdateItem();
        item.setState(getState());
        return item;
    }
}
