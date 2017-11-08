package cn.com.xy.sms.sdk.ui.popu.util;

import android.content.BroadcastReceiver;
import android.content.Intent;

public interface ReceiverInterface {
    BroadcastReceiver getReceiver();

    boolean onReceive(Intent intent);
}
