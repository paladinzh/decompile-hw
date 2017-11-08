package com.android.systemui.observer;

import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Global;
import com.android.systemui.utils.HwLog;

public class ObserverTrafficData extends ObserverItem<Integer> {
    private int mCurrentCard;

    public ObserverTrafficData(Handler handler) {
        super(handler);
    }

    public Uri getUri() {
        return Global.getUriFor("multi_sim_data_call");
    }

    public void onChange() {
        this.mCurrentCard = Global.getInt(this.mContext.getContentResolver(), "multi_sim_data_call", 0);
        HwLog.i("ObserverTrafficSwitch", "onChange: current card = " + this.mCurrentCard);
    }

    public Integer getValue() {
        return Integer.valueOf(this.mCurrentCard);
    }
}
