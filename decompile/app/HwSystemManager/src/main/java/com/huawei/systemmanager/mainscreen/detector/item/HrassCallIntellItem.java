package com.huawei.systemmanager.mainscreen.detector.item;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.ui.BlockRulesActivity;
import com.huawei.harassmentinterception.ui.CallIntellBlockFragment.CallIntellBlockActivity;
import com.huawei.harassmentinterception.util.CallIntelligentGuide;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.util.HwLog;
import java.util.concurrent.atomic.AtomicBoolean;

public class HrassCallIntellItem extends DetectItem {
    private static final String TAG = "HrassCallIntellItem";
    private AtomicBoolean mVisiable = new AtomicBoolean();
    private Bundle scanRes;

    public void doScan() {
        Bundle res = CallIntelligentGuide.getCallIntellGuideState(GlobalContext.getContext());
        int state = res.getInt("state", 3);
        HwLog.i(TAG, "doscan state is:" + state);
        switch (state) {
            case 1:
                setState(3);
                this.mVisiable.set(true);
                break;
            case 2:
                setState(2);
                this.mVisiable.set(true);
                break;
            case 3:
                setState(3);
                this.mVisiable.set(false);
                break;
            default:
                HwLog.e(TAG, "unknow state:" + state);
                break;
        }
        this.scanRes = res;
    }

    public String getName() {
        return getContext().getString(R.string.harassment_intelligent_call_blocking);
    }

    public String getTitle(Context ctx) {
        if (isOptimized()) {
            return ctx.getString(R.string.harassment_callIntell_block_on_scan_title);
        }
        return ctx.getString(R.string.harassment_callIntell_block_off_scan_title);
    }

    public String getDescription(Context ctx) {
        return ctx.getString(R.string.harassment_callIntell_block_scan_des);
    }

    public int getItemType() {
        return 15;
    }

    public int getOptimizeActionType() {
        return 3;
    }

    public boolean isEnable() {
        if (Utility.isWifiOnlyMode()) {
            return false;
        }
        return CustomizeWrapper.shouldEnableIntelligentEngine();
    }

    public Intent getOptimizeIntent(Context ctx) {
        if (this.scanRes == null) {
            HwLog.e(TAG, "getOptimizeIntent called, but scanRes is null!");
            return null;
        }
        Intent intent = new Intent();
        if (this.scanRes.getBoolean(CallIntelligentGuide.KEY_DUALCARD, false)) {
            intent.setClass(ctx, BlockRulesActivity.class);
        } else {
            intent.putExtra(ConstValues.KEY_OP_CARD, 1);
            intent.setClass(ctx, CallIntellBlockActivity.class);
        }
        return intent;
    }

    public String getOptimizeActionName() {
        return getContext().getString(R.string.main_screen_detect_operation_turn_on, new Object[]{Utility.getLocaleNumber(score())});
    }

    public boolean isManulOptimize() {
        return true;
    }

    public boolean isVisiable() {
        return this.mVisiable.get();
    }

    protected int score() {
        return 3;
    }

    public void refresh() {
        doScan();
    }

    public DetectItem copy() {
        return new HrassCallIntellItem();
    }

    public String getTag() {
        return TAG;
    }
}
