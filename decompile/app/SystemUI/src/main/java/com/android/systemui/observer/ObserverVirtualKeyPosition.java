package com.android.systemui.observer;

import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.System;
import com.android.systemui.utils.UserSwitchUtils;

public class ObserverVirtualKeyPosition extends ObserverItem<Integer> {
    private int mVirtualKeyPosition = 1;

    public ObserverVirtualKeyPosition(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return System.getUriFor("virtual_key_position");
    }

    public void onChange() {
        this.mVirtualKeyPosition = System.getIntForUser(this.mContext.getContentResolver(), "virtual_key_position", 1, UserSwitchUtils.getCurrentUser());
    }

    public Integer getValue() {
        return Integer.valueOf(this.mVirtualKeyPosition);
    }
}
