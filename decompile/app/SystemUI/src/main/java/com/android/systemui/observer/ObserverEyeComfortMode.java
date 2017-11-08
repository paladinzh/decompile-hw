package com.android.systemui.observer;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.System;
import com.android.systemui.qs.tiles.EyeComfortModeTile;
import com.android.systemui.utils.UserSwitchUtils;

@SuppressLint({"NewApi"})
public class ObserverEyeComfortMode extends ObserverItem<Boolean> {
    Boolean mIsEyeComfortOn = Boolean.valueOf(false);

    public ObserverEyeComfortMode(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return System.getUriFor("eyes_protection_mode");
    }

    public Boolean getValue() {
        return this.mIsEyeComfortOn;
    }

    public void onChange() {
        boolean z = true;
        int state = System.getIntForUser(this.mContext.getContentResolver(), "eyes_protection_mode", 0, UserSwitchUtils.getCurrentUser());
        if (!EyeComfortModeTile.isComfortFeatureSupported()) {
            z = false;
        } else if (!(1 == state || 3 == state)) {
            z = false;
        }
        this.mIsEyeComfortOn = Boolean.valueOf(z);
    }
}
