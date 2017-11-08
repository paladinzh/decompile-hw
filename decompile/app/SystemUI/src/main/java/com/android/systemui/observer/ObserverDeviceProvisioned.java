package com.android.systemui.observer;

import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Global;

public class ObserverDeviceProvisioned extends ObserverItem<Boolean> {
    private boolean mIsDeviceProvisioned = false;

    public ObserverDeviceProvisioned(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return Global.getUriFor("device_provisioned");
    }

    public void onChange() {
        boolean z = false;
        if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            z = true;
        }
        this.mIsDeviceProvisioned = z;
    }

    public Boolean getValue() {
        return Boolean.valueOf(this.mIsDeviceProvisioned);
    }
}
