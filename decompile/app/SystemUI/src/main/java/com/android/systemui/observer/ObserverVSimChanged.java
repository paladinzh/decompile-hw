package com.android.systemui.observer;

import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import com.android.systemui.utils.HwLog;

public class ObserverVSimChanged extends ObserverItem<Integer> {
    Integer mVSimId = Integer.valueOf(-1);

    public ObserverVSimChanged(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return System.getUriFor("vsim_enabled_subid");
    }

    public void onChange() {
        try {
            this.mVSimId = Integer.valueOf(System.getInt(this.mContext.getContentResolver(), "vsim_enabled_subid"));
        } catch (SettingNotFoundException e) {
            HwLog.e(this.TAG, "VSIM_EANBLED_SUBID::SettingNotFoundException::e=" + e);
        }
    }

    public Integer getValue() {
        return this.mVSimId;
    }
}
