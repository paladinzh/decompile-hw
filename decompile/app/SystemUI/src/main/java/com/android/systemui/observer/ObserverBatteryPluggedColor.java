package com.android.systemui.observer;

import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.System;
import com.android.systemui.utils.UserSwitchUtils;

public class ObserverBatteryPluggedColor extends ObserverItem<Boolean> {
    private boolean mUsePluggedColor;

    public ObserverBatteryPluggedColor(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return System.getUriFor("plugged_battery_color");
    }

    public void onChange() {
        boolean z = false;
        if (System.getIntForUser(this.mContext.getContentResolver(), "plugged_battery_color", 0, UserSwitchUtils.getCurrentUser()) != 0) {
            z = true;
        }
        this.mUsePluggedColor = z;
    }

    public Boolean getValue() {
        return Boolean.valueOf(this.mUsePluggedColor);
    }
}
