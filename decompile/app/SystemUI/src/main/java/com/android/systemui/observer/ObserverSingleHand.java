package com.android.systemui.observer;

import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.System;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;

public class ObserverSingleHand extends ObserverItem<Boolean> {
    private boolean mSingleHandEnabled;

    public ObserverSingleHand(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return System.getUriFor("single_hand_screen_zoom");
    }

    public void onChange() {
        boolean z = true;
        if (System.getIntForUser(this.mContext.getContentResolver(), "single_hand_screen_zoom", 1, UserSwitchUtils.getCurrentUser()) != 1) {
            z = false;
        }
        this.mSingleHandEnabled = z;
        HwLog.i("ObserverSingleHand", "onChange: SingleHandEnabled = " + this.mSingleHandEnabled);
    }

    public Boolean getValue() {
        return Boolean.valueOf(this.mSingleHandEnabled);
    }
}
