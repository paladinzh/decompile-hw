package cn.com.xy.sms.sdk.ui.popu.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class XyBroadcastReceiver extends BroadcastReceiver {
    private ReceiverInterface mReceiver;

    public void setReceiver(ReceiverInterface receiver) {
        this.mReceiver = receiver;
    }

    public XyBroadcastReceiver(ReceiverInterface receiver) {
        this.mReceiver = receiver;
    }

    public void onReceive(Context context, Intent intent) {
        if (this.mReceiver != null) {
            this.mReceiver.onReceive(intent);
            this.mReceiver = null;
        }
    }
}
