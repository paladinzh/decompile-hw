package com.huawei.systemmanager.mainscreen.detector.item;

import android.content.Context;
import android.content.Intent;
import com.huawei.netassistant.wifisecure.HsmWifiDetectManager;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;

public class WifiSecDetectItem extends DetectItem {
    private static final String TAG = "WifiSecDetectItem";

    public String getName() {
        return "";
    }

    public String getDescription(Context ctx) {
        return ctx.getString(R.string.main_screen_detect_item_wifi_secure_description);
    }

    public String getTitle(Context ctx) {
        if (isOptimized()) {
            return ctx.getString(R.string.main_screen_detect_item_wifi_secure_enabled);
        }
        return ctx.getString(R.string.main_screen_detect_item_wifi_secure_disabled);
    }

    public int getItemType() {
        return 17;
    }

    public int getOptimizeActionType() {
        return 3;
    }

    public boolean isManulOptimize() {
        return true;
    }

    protected int score() {
        return 1;
    }

    public void refresh() {
        doScan();
    }

    public void doScan() {
        if (HsmWifiDetectManager.isWifiSecDetectOn(getContext())) {
            setState(3);
        } else {
            setState(2);
        }
    }

    public Intent getOptimizeIntent(Context ctx) {
        Intent intent = new Intent("android.settings.WIFI_IP_SETTINGS");
        intent.setPackage(HsmStatConst.SETTING_PACKAGE_NAME);
        return intent;
    }

    public boolean isEnable() {
        return HsmWifiDetectManager.isSupportWifiSecDetct();
    }

    public DetectItem copy() {
        WifiSecDetectItem item = new WifiSecDetectItem();
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
        HsmStat.statE(Events.E_MAINSCREEN_DO_OPTIMZE_WIFISEC);
    }
}
