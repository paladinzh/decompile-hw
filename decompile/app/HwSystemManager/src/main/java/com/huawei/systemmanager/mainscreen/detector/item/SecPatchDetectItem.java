package com.huawei.systemmanager.mainscreen.detector.item;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.module.ModuleMgr;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.secpatch.adapter.SecPatchChecker;
import com.huawei.systemmanager.secpatch.common.ConstValues;
import com.huawei.systemmanager.util.HwLog;
import java.util.concurrent.atomic.AtomicInteger;

public class SecPatchDetectItem extends DetectItem {
    private static final String TAG = "SecPatchDetectItem";
    private AtomicInteger mPathNumber = new AtomicInteger();

    public String getName() {
        return getContext().getString(R.string.systemmanager_module_title_patches);
    }

    public String getTitle(Context ctx) {
        int pathNum = this.mPathNumber.get();
        if (pathNum <= 0) {
            return ctx.getString(R.string.main_screen_detect_item_securitypath_safe);
        }
        return ctx.getResources().getQuantityString(R.plurals.main_screen_detect_item_securitypath_unsafe, pathNum, new Object[]{Integer.valueOf(pathNum)});
    }

    public String getDescription(Context ctx) {
        return ctx.getString(R.string.main_screen_detect_item_securitypath_description);
    }

    public int getItemType() {
        return 4;
    }

    public void doScan() {
        int pathNum = SecPatchChecker.getPathItemNum(getContext());
        this.mPathNumber.set(pathNum);
        if (pathNum <= 0) {
            setState(1);
        } else {
            setState(2);
        }
        HwLog.i(TAG, "doScan, getSecpath num:" + pathNum);
    }

    public String getOptimizeActionName() {
        int score = score();
        return getContext().getString(R.string.main_screen_detect_operation_fix, new Object[]{Utility.getLocaleNumber(score)});
    }

    public Intent getOptimizeIntent(Context ctx) {
        return new Intent(ConstValues.INTENT_ACTION_UPDATE_SYSTEM);
    }

    public int getOptimizeActionType() {
        return 3;
    }

    protected int score() {
        return 25;
    }

    public boolean isManulOptimize() {
        return true;
    }

    public void refresh() {
        HwLog.i(TAG, "refresh called");
        doScan();
    }

    public boolean isEnable() {
        return ModuleMgr.MODULE_SECURITYPATCH.entryEnabled(GlobalContext.getContext());
    }

    public String getTag() {
        return TAG;
    }

    public DetectItem copy() {
        return this;
    }

    public void statOptimizeEvent() {
        HsmStat.statE(Events.E_MAINSCREEN_DO_OPTIMZE_SECPATCH);
    }
}
