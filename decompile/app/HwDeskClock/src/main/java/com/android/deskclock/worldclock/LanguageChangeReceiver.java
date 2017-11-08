package com.android.deskclock.worldclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.android.util.Log;

public class LanguageChangeReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.printf("action=%s", action);
        if ("huawei.intent.action.ZONE_PICKER_LOAD_COMPLETED".equals(action)) {
            if (!TimeZoneUtils.getTimeZoneUpdating()) {
                TimeZoneUtils.setTimeZoneUpdating(true);
                Intent intentService = new Intent(context, TimeZoneService.class);
                intentService.setAction("huawei.intent.action.ZONE_PICKER_LOAD_COMPLETED");
                context.startService(intentService);
            } else {
                return;
            }
        }
        if ("com.android.desk.syncData".equals(action)) {
            intentService = new Intent(context, TimeZoneService.class);
            intentService.setAction("com.android.desk.syncData");
            context.startService(intentService);
        }
    }
}
