package com.huawei.systemmanager.mainscreen.detector.item;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.adblock.comm.AdBlock;
import com.huawei.systemmanager.adblock.ui.view.AdBlockAppListActivity;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import java.util.Iterator;
import java.util.List;

public class AdDetectItem extends DetectItem {
    private static final String TAG = "AdDetectItem";
    private int mTotalCount;

    private AdDetectItem() {
    }

    public String getTitle(Context ctx) {
        if (isOptimized()) {
            return ctx.getString(R.string.no_have_ad_application);
        }
        return ctx.getResources().getQuantityString(R.plurals.scan_result_advertises, this.mTotalCount, new Object[]{Integer.valueOf(this.mTotalCount)});
    }

    public int score() {
        return 0;
    }

    public Intent getOptimizeIntent(Context ctx) {
        Intent intent = new Intent();
        intent.setClass(ctx, AdBlockAppListActivity.class);
        return intent;
    }

    public int getOptimizeActionType() {
        return 3;
    }

    public String getOptimizeActionName() {
        return getContext().getString(R.string.main_screen_detect_item_ad_operation);
    }

    public int getItemType() {
        return 5;
    }

    public boolean isManulOptimize() {
        return true;
    }

    public String getName() {
        return getContext().getString(R.string.addetect_title);
    }

    public String getTag() {
        return TAG;
    }

    public DetectItem copy() {
        return this;
    }

    public static AdDetectItem create(List<ScanResultEntity> list, int totalCount, int checkedCount) {
        AdDetectItem item = new AdDetectItem();
        item.refreshState(totalCount, checkedCount);
        return item;
    }

    public void statOptimizeEvent() {
        HsmStat.statE(Events.E_OPTMIZE_ADVERTISE_VIEW);
    }

    private void refreshState(int totalCount, int checkedCount) {
        if (totalCount <= 0 || totalCount <= checkedCount) {
            this.mTotalCount = 0;
            setState(1);
            return;
        }
        this.mTotalCount = totalCount;
        setState(2);
    }

    public void refresh() {
        doScan();
    }

    public void doScan() {
        List<AdBlock> adBlocks = AdBlock.getAllAdBlocks(getContext());
        Iterator<AdBlock> iterator = adBlocks.iterator();
        int checkedCount = 0;
        while (iterator.hasNext()) {
            AdBlock adBlock = (AdBlock) iterator.next();
            if (!adBlock.hasAd()) {
                iterator.remove();
            } else if (adBlock.isEnable()) {
                checkedCount++;
            }
        }
        refreshState(adBlocks.size(), checkedCount);
    }
}
