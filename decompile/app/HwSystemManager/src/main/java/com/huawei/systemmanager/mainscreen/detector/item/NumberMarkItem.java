package com.huawei.systemmanager.mainscreen.detector.item;

import android.content.Context;
import android.content.Intent;
import com.huawei.harassmentinterception.numbermark.HsmNumberMarkerManager;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;

public class NumberMarkItem extends DetectItem {
    private static final String TAG = "NumberMarkItem";

    public String getName() {
        return "";
    }

    public String getDescription(Context ctx) {
        return ctx.getString(R.string.main_screen_detect_online_number_mark_description);
    }

    public String getTitle(Context ctx) {
        if (isOptimized()) {
            return ctx.getString(R.string.main_screen_detect_online_number_mark_on);
        }
        return ctx.getString(R.string.main_screen_detect_online_number_mark_off);
    }

    public int getItemType() {
        return 16;
    }

    public int getOptimizeActionType() {
        return 3;
    }

    public boolean isManulOptimize() {
        return true;
    }

    protected int score() {
        return 3;
    }

    public void refresh() {
        doScan();
    }

    public void doScan() {
        if (HsmNumberMarkerManager.isContactUseNetwokMark(getContext())) {
            setState(3);
        } else {
            setState(2);
        }
    }

    public Intent getOptimizeIntent(Context ctx) {
        Intent intent = new Intent("com.android.contacts.NumberMarkSettings");
        intent.setPackage(HsmStatConst.CONTACTS_PACKAGE_NAME);
        return intent;
    }

    public boolean isEnable() {
        if (Utility.isWifiOnlyMode() || Utility.isDataOnlyMode()) {
            return false;
        }
        return HsmNumberMarkerManager.isContactSupprotNumberMark(getContext());
    }

    public DetectItem copy() {
        NumberMarkItem item = new NumberMarkItem();
        item.setState(getState());
        return item;
    }

    public String getTag() {
        return TAG;
    }

    public String getOptimizeActionName() {
        return getContext().getString(R.string.main_screen_detect_operation_turn_on, new Object[]{Utility.getLocaleNumber(score())});
    }

    public void statOptimizeEvent() {
        HsmStat.statE(Events.E_MAINSCREEN_DO_OPTIMIZE_NUMBER_MARK);
    }
}
