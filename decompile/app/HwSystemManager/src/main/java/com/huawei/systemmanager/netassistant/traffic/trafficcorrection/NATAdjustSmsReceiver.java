package com.huawei.systemmanager.netassistant.traffic.trafficcorrection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.util.HwLog;

public class NATAdjustSmsReceiver extends BroadcastReceiver {
    public static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = NATAdjustSmsReceiver.class.getSimpleName();

    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && ACTION_SMS_RECEIVED.equals(action)) {
                HwLog.i(TAG, "Traffic auto adjust Receiver action = " + action);
                Intent serviceIntent = new Intent(ShareCfg.AUTO_ADJUST_SMS_ACTION);
                serviceIntent.putExtra(ShareCfg.EXTRA_ADJUST_SERVICE_DATA, intent.getExtras());
                serviceIntent.setClass(context, NATAutoAdjustService.class);
                context.startService(serviceIntent);
            }
        }
    }
}
