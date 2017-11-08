package com.android.systemui.observer;

import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Global;
import android.telephony.TelephonyManager;
import com.android.systemui.utils.HwLog;

public class ObserverDataSwitchChanged extends ObserverItem<Boolean> {
    Boolean mDataSwitchEnable;

    public ObserverDataSwitchChanged(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return Global.getUriFor("mobile_data");
    }

    public void onChange() {
        try {
            this.mDataSwitchEnable = Boolean.valueOf(TelephonyManager.from(this.mContext).getDataEnabled());
        } catch (Exception e) {
            HwLog.e(this.TAG, "onChange:: get data enable exception: " + e.getMessage());
        }
    }

    public Boolean getValue() {
        return this.mDataSwitchEnable;
    }
}
