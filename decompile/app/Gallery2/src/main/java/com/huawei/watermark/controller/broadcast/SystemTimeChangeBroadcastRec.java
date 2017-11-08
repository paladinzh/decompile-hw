package com.huawei.watermark.controller.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huawei.watermark.controller.callback.WMTimeChangedCallBack;

public class SystemTimeChangeBroadcastRec extends BroadcastReceiver {
    IntentFilter filter = new IntentFilter("android.intent.action.TIME_TICK");
    WMTimeChangedCallBack mTimeChangedCallBack;

    public SystemTimeChangeBroadcastRec(WMTimeChangedCallBack callBack) {
        this.mTimeChangedCallBack = callBack;
    }

    public IntentFilter getFilter() {
        return this.filter;
    }

    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.TIME_TICK".equals(intent.getAction())) {
            this.mTimeChangedCallBack.miniteChanged();
        }
    }
}
