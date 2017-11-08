package com.android.systemui.observer;

import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.System;
import com.android.systemui.utils.HwLog;

public class ObserverPowerSaveMode extends ObserverItem<Integer> {
    private int mPowerSaveMode;

    public ObserverPowerSaveMode(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return System.getUriFor("SmartModeStatus");
    }

    public void onChange() {
        this.mPowerSaveMode = System.getIntForUser(this.mContext.getContentResolver(), "SmartModeStatus", 0, 0);
        HwLog.i(this.TAG, "onChange: power save mode = " + this.mPowerSaveMode);
    }

    public Integer getValue() {
        return Integer.valueOf(this.mPowerSaveMode);
    }
}
