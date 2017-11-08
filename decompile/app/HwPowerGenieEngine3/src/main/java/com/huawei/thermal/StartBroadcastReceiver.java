package com.huawei.thermal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartBroadcastReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null) {
            String action = intent.getAction();
            if (action != null && action.equals("huawei.intent.action.START_THERMAL_ACTION")) {
                Log.d("StartBroadcastReceiver", "receiver broadcast START_THERMAL_ACTION");
                Log.d("StartBroadcastReceiver", "Thermal JAR Ver 1.1.9");
                ThermalCoreService.getInstance(context).onStart();
            }
        }
    }
}
