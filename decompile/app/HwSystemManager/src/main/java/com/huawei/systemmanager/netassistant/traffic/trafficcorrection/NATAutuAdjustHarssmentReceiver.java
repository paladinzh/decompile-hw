package com.huawei.systemmanager.netassistant.traffic.trafficcorrection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.util.HwLog;

public class NATAutuAdjustHarssmentReceiver extends BroadcastReceiver {
    public static final String ACTION_HARASSMENT_SMS = "com.huawei.dianxinos.optimizer.action.HARASSMENT_SMS";
    private static final String TAG = NATAutuAdjustHarssmentReceiver.class.getSimpleName();

    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && "com.huawei.dianxinos.optimizer.action.HARASSMENT_SMS".equals(action)) {
                HwLog.i(TAG, "Traffic auto adjust Receiver action = " + action);
                Intent serviceIntent = new Intent(ShareCfg.AUTO_ADJUST_SMS_ACTION);
                serviceIntent.putExtra(ShareCfg.EXTRA_ADJUST_SERVICE_DATA, intent.getExtras());
                serviceIntent.setClass(context, NATAutoAdjustService.class);
                context.startService(serviceIntent);
            }
        }
    }
}
