package com.android.systemui.observer;

import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.System;
import com.android.systemui.utils.UserSwitchUtils;

public class ObserverBatteryPercent extends ObserverItem<Boolean> {
    private boolean mShowBatteryPercent;

    public ObserverBatteryPercent(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return System.getUriFor("battery_percent_switch");
    }

    public void onChange() {
        boolean z = false;
        if (System.getIntForUser(this.mContext.getContentResolver(), "battery_percent_switch", 0, UserSwitchUtils.getCurrentUser()) != 0) {
            z = true;
        }
        this.mShowBatteryPercent = z;
    }

    public Boolean getValue() {
        return Boolean.valueOf(this.mShowBatteryPercent);
    }
}
