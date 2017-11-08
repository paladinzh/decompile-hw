package com.huawei.systemmanager.mainscreen.detector.item;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.module.ModuleMgr;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.startupmgr.db.StartupDataMgrHelper;
import com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity;
import com.huawei.systemmanager.util.HwLog;
import java.util.concurrent.atomic.AtomicInteger;

public class BootupItem extends DetectItem {
    private static final int BOOTUP_APP_MAX_SCORE = 5;
    private static final int BOOTUP_APP_SUGGEST_NUM = 3;
    private static final String TAG = "BootupItem";
    private AtomicInteger mBootupAppNum = new AtomicInteger();

    public String getName() {
        return getContext().getString(R.string.systemmanager_module_title_autolaunch);
    }

    public String getTitle(Context ctx) {
        int num = this.mBootupAppNum.get();
        if (num <= 0) {
            return ctx.getString(R.string.main_screen_detect_item_bootup_safe);
        }
        return ctx.getResources().getQuantityString(R.plurals.main_screen_detect_item_bootup_unsafe_1, num, new Object[]{Integer.valueOf(num)});
    }

    public void doScan() {
        int startupResult = StartupDataMgrHelper.queryNormalStartupAllowCount(getContext());
        this.mBootupAppNum.set(startupResult);
        if (startupResult <= 3) {
            setState(1);
        } else {
            setState(2);
        }
        HwLog.i(TAG, "doscall called, bootupItem app num:" + startupResult);
    }

    public Intent getOptimizeIntent(Context ctx) {
        return new Intent(ctx, StartupNormalAppListActivity.class);
    }

    public String getOptimizeActionName() {
        int score = getScore();
        return getContext().getString(R.string.main_screen_detect_operation_forbidden, new Object[]{Utility.getLocaleNumber(score)});
    }

    public int getItemType() {
        return 6;
    }

    public int getOptimizeActionType() {
        return 3;
    }

    public boolean isManulOptimize() {
        return true;
    }

    protected int score() {
        return Math.min(Math.max(0, this.mBootupAppNum.get() - 3), 5);
    }

    public void refresh() {
        HwLog.i(TAG, "refresh called");
        doScan();
    }

    public String getTag() {
        return TAG;
    }

    public boolean isEnable() {
        return ModuleMgr.MODULE_BOOTUP.entryEnabled(getContext());
    }

    public DetectItem copy() {
        BootupItem item = new BootupItem();
        item.mBootupAppNum.set(this.mBootupAppNum.get());
        item.setState(getState());
        return item;
    }

    public void statOptimizeEvent() {
        HsmStat.statE(1007);
    }
}
