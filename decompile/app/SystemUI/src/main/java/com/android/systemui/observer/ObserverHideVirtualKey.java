package com.android.systemui.observer;

import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.System;
import com.android.systemui.utils.UserSwitchUtils;

public class ObserverHideVirtualKey extends ObserverItem<Boolean> {
    private boolean mHideVirtualKey = false;

    public ObserverHideVirtualKey(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return System.getUriFor("hide_virtual_key");
    }

    public void onChange() {
        boolean z = false;
        if (System.getIntForUser(this.mContext.getContentResolver(), "hide_virtual_key", 0, UserSwitchUtils.getCurrentUser()) != 0) {
            z = true;
        }
        this.mHideVirtualKey = z;
    }

    public Boolean getValue() {
        return Boolean.valueOf(this.mHideVirtualKey);
    }
}
