package com.huawei.systemmanager.mainscreen.detector.item;

import android.content.Context;
import android.content.Intent;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.optimize.process.ProtectAppControl;
import com.huawei.systemmanager.optimize.process.ProtectAppItem;
import com.huawei.systemmanager.spacecleanner.ui.ProcessWhiteListFragment.ProcessWhiteListActivity;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WhiteListItem extends DetectItem {
    private static final String TAG = "WhiteListItem";
    private int MAX_PROTECT_APP_NUM = 10;
    private int mReferenceNum = 10;
    private AtomicInteger protectListNum = new AtomicInteger();

    public void doScan() {
        Context ctx = GlobalContext.getContext();
        List<ProtectAppItem> applist = ProtectAppControl.getInstance(ctx).getProtectAppItems();
        this.mReferenceNum = Math.max(ProtectAppControl.getInstance(ctx).getDefaultProtectNum(applist), this.MAX_PROTECT_APP_NUM);
        List<String> result = Lists.newArrayList();
        for (ProtectAppItem item : applist) {
            if (item.isProtect()) {
                result.add(item.getPackageName());
            }
        }
        int number = result.size();
        this.protectListNum.set(number);
        if (number <= this.mReferenceNum) {
            setState(1);
        } else {
            setState(2);
        }
    }

    private int getUnOptimizeCount() {
        return this.protectListNum.get();
    }

    protected int score() {
        return Math.min(10, Math.max(0, getUnOptimizeCount() - this.mReferenceNum));
    }

    public String getTitle(Context ctx) {
        if (isOptimized()) {
            return ctx.getString(R.string.main_screen_detect_item_whitelist_safe_1_change);
        }
        return ctx.getString(R.string.main_screen_detect_item_whitelist_unsafe_1_change);
    }

    public String getDescription(Context ctx) {
        return ctx.getString(R.string.main_screen_detect_item_whitelist_description);
    }

    public Intent getOptimizeIntent(Context packageContext) {
        return new Intent(packageContext, ProcessWhiteListActivity.class);
    }

    public int getOptimizeActionType() {
        return 3;
    }

    public boolean isManulOptimize() {
        return true;
    }

    public int getItemType() {
        return 7;
    }

    public String getOptimizeActionName() {
        int score = getScore();
        return getContext().getString(R.string.main_screen_detect_operation_forbidden, new Object[]{Utility.getLocaleNumber(score)});
    }

    public String getName() {
        return getContext().getString(R.string.back_app);
    }

    public void refresh() {
        HwLog.i(TAG, "refresh called");
        doScan();
    }

    public String getTag() {
        return TAG;
    }

    public DetectItem copy() {
        WhiteListItem item = new WhiteListItem();
        item.protectListNum.set(this.protectListNum.get());
        item.setState(getState());
        return item;
    }

    public void statOptimizeEvent() {
        HsmStat.statE(Events.E_OPTMIZE_WHITLIST_VIEW);
    }
}
