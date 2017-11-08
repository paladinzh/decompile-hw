package com.huawei.systemmanager.power.batteryoptimize;

import android.content.Context;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PowerDetectItem {
    public static final int POWER_APP_ITEM = 1;
    public static final int POWER_AUTOROTATION_ITEM = 12;
    public static final int POWER_AUTOSYNC_ITEM = 9;
    public static final int POWER_BLUETOOTH_ITEM = 3;
    public static final int POWER_BRIGHTNESS_ITEM = 5;
    public static final int POWER_CLOSEAPP_ITEM = 4;
    public static final int POWER_FEEDBACK_ITEM = 11;
    public static final int POWER_GPS_ITEM = 8;
    public static final int POWER_MOBILEDATA_ITEM = 7;
    public static final int POWER_SCREENTIMEOUT_ITEM = 6;
    public static final int POWER_VIBRATE_ITEM = 10;
    public static final int POWER_WLAN_ITEM = 2;
    public static final int STATE_NEED_OPTIMIZED = 2;
    public static final int STATE_OPTIMIZED = 3;
    public static final int STATE_SECURITY = 1;
    private static final String TAG = "PowerDetectItem";
    private AtomicInteger mState = new AtomicInteger(1);

    public abstract void doOptimize();

    public abstract void doScan();

    public abstract int getItemType();

    public abstract String getTitle();

    public void setExData(int num) {
    }

    public boolean isOptimized() {
        int state = this.mState.get();
        if (state == 1 || state == 3) {
            return true;
        }
        return false;
    }

    public boolean isEnable() {
        return true;
    }

    public void doRefreshOptimize() {
        doOptimize();
    }

    protected int setState(int state) {
        return this.mState.getAndSet(state);
    }

    protected int getState() {
        return this.mState.get();
    }

    protected final Context getContext() {
        return GlobalContext.getContext();
    }
}
