package com.android.systemui.observer;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;

@SuppressLint({"NewApi"})
public class ObserverAirplaneModeOn extends ObserverItem<Boolean> {
    Boolean isAirplaneModeOn = Boolean.valueOf(false);

    public ObserverAirplaneModeOn(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return Global.getUriFor("airplane_mode_on");
    }

    public Boolean getValue() {
        return this.isAirplaneModeOn;
    }

    public void onChange() {
        boolean z = true;
        try {
            if (Global.getInt(getContentResolve(), "airplane_mode_on") != 1) {
                z = false;
            }
            this.isAirplaneModeOn = Boolean.valueOf(z);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
    }
}
