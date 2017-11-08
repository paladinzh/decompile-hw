package com.android.systemui.observer;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import com.android.systemui.utils.HwLog;

@SuppressLint({"NewApi"})
public class ObserverInstantShare extends ObserverItem<Boolean> {
    Boolean mIsInstantShareOn = Boolean.valueOf(false);

    public ObserverInstantShare(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return Global.getUriFor("instantshare_state");
    }

    public Boolean getValue() {
        return this.mIsInstantShareOn;
    }

    public void onChange() {
        boolean z = true;
        try {
            if (Global.getInt(getContentResolve(), "instantshare_state") != 1) {
                z = false;
            }
            this.mIsInstantShareOn = Boolean.valueOf(z);
        } catch (SettingNotFoundException e) {
            HwLog.e(this.TAG, "SettingNotFoundException::e=" + e);
        }
    }
}
