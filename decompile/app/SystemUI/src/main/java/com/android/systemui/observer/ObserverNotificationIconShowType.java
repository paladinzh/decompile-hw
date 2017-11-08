package com.android.systemui.observer;

import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.System;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;

public class ObserverNotificationIconShowType extends ObserverItem<Integer> {
    private int mShowType = 0;

    public ObserverNotificationIconShowType(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return System.getUriFor("notification_way_switch");
    }

    public void onChange() {
        this.mShowType = System.getIntForUser(this.mContext.getContentResolver(), "notification_way_switch", 0, UserSwitchUtils.getCurrentUser());
        HwLog.i(this.TAG, "onChange: mShowType=" + this.mShowType);
    }

    public Integer getValue() {
        return Integer.valueOf(this.mShowType);
    }
}
