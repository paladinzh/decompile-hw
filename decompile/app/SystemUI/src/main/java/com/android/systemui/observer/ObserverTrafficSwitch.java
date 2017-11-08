package com.android.systemui.observer;

import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.System;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;

public class ObserverTrafficSwitch extends ObserverItem<Boolean> {
    private boolean mShowTrafficSwitch = false;

    public ObserverTrafficSwitch(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return System.getUriFor("traffic_switch");
    }

    public void onChange() {
        boolean z = true;
        if (System.getIntForUser(this.mContext.getContentResolver(), "traffic_switch", 0, UserSwitchUtils.getCurrentUser()) != 1) {
            z = false;
        }
        this.mShowTrafficSwitch = z;
        HwLog.i("ObserverTrafficSwitch", "onChange: show traffic = " + this.mShowTrafficSwitch);
    }

    public Boolean getValue() {
        return Boolean.valueOf(this.mShowTrafficSwitch);
    }
}
