package com.android.settings.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings.Global;
import android.util.Log;

public class ZenModeReceiver extends BroadcastReceiver {
    private ContentResolver mResolver;

    public void onReceive(Context context, Intent intent) {
        this.mResolver = context.getContentResolver();
        if ("com.huawei.android.preventmode.change".equals(intent.getAction())) {
            Log.d("ZenModeReceiver", "Receive zen mode change broad cast.");
            handleSetPreventMode(context, intent);
        }
    }

    private void handleSetPreventMode(Context context, Intent intent) {
        int enable = intent.getIntExtra("PreventModechange", 0);
        boolean isCheck = 1 == Global.getInt(this.mResolver, "zen_mode_change_do_not_ask", 0);
        boolean on = enable == 1;
        String from = intent.getStringExtra("package_name");
        if (on && !isCheck) {
            startConfirmationService(context, from);
        } else if (on && isCheck) {
            NotificationManager.from(context).setZenMode(Global.getInt(this.mResolver, "zen_mode_last_choosen", 3), null, "ZenModeReceiver");
        } else {
            NotificationManager.from(context).setZenMode(0, null, "ZenModeReceiver");
        }
    }

    private void startConfirmationService(Context context, String from) {
        Log.d("ZenModeReceiver", "Start confirmation service, from:" + from);
        Intent intentservice = new Intent();
        intentservice.putExtra("PreventModechange", 1);
        intentservice.setClass(context, ZenModeLightweightService.class);
        if (from != null) {
            intentservice.putExtra("package_name", from);
        }
        context.startService(intentservice);
    }
}
