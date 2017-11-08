package com.android.systemui.observer;

import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.System;
import com.android.systemui.utils.UserSwitchUtils;

public class ObserverVirtualKeyType extends ObserverItem<Integer> {
    private int mVirtualKeyType = 0;

    public ObserverVirtualKeyType(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return System.getUriFor("virtual_key_type");
    }

    public void onChange() {
        this.mVirtualKeyType = System.getIntForUser(this.mContext.getContentResolver(), "virtual_key_type", 0, UserSwitchUtils.getCurrentUser());
    }

    public Integer getValue() {
        return Integer.valueOf(this.mVirtualKeyType);
    }
}
