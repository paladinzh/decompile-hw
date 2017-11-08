package com.huawei.systemmanager.mainscreen.detector.item;

import android.content.Context;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.power.data.devstatus.DevStatusUtil;
import com.huawei.systemmanager.util.HwLog;

public class WifiItem extends DetectItem {
    private static final String TAG = "WifiItem";
    public static final int WIFI_OFF = 0;
    public static final int WIFI_ON_CONNECT = 2;
    public static final int WIFI_ON_NOT_CONNECT = 1;
    private int mWifiState;

    public String getTitle(Context context) {
        if (isOptimized()) {
            return context.getString(R.string.power_wlan_optimize_des);
        }
        return context.getString(R.string.wlan_to_be_optimize);
    }

    protected int score() {
        return 2;
    }

    public void doScan() {
        if (!DevStatusUtil.getWifiState(getContext())) {
            this.mWifiState = 0;
            setState(1);
        } else if (DevStatusUtil.isWlanStateOptimize(getContext())) {
            this.mWifiState = 2;
            setState(1);
        } else {
            this.mWifiState = 1;
            setState(2);
        }
        HwLog.i(TAG, "doScan called, mWifiState is:" + this.mWifiState);
    }

    public boolean isVisiable() {
        if (this.mWifiState == 2) {
            return false;
        }
        return true;
    }

    public void doOptimize(Context context) {
        HwLog.i(TAG, "wifi doOptimize called");
        DevStatusUtil.setWifiState(getContext(), false);
        this.mWifiState = 0;
        setState(3);
    }

    public int getOptimizeActionType() {
        return 1;
    }

    public int getItemType() {
        return 8;
    }

    public String getOptimizeActionName() {
        return getContext().getString(R.string.main_screen_detect_operation_turn_off, new Object[]{Utility.getLocaleNumber(score())});
    }

    public String getName() {
        return getContext().getString(R.string.WIFI);
    }

    public boolean isManulOptimize() {
        return true;
    }

    public void refresh() {
        HwLog.i(TAG, "refresh called");
        doScan();
    }

    public String getTag() {
        return TAG;
    }

    public DetectItem copy() {
        return new WifiItem();
    }

    public void statOptimizeEvent() {
        HsmStat.statE(Events.E_MAINSCREEN_DO_OPTIMZE_WIFI);
    }
}
