package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UpdateTimeZonesReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent != null && "android.intent.action.LOCALE_CHANGED".equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, TimeZonesService.class);
            serviceIntent.putExtra("action_name", "android.intent.action.LOCALE_CHANGED");
            context.startService(serviceIntent);
        }
    }
}
