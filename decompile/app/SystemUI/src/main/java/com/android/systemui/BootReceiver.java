package com.android.systemui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings.Global;
import android.util.Log;
import com.android.systemui.linkplus.RoamPlus;

public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        try {
            RoamPlus.resetAlreadyShowRoaming(context);
            if (Global.getInt(context.getContentResolver(), "show_processes", 0) != 0) {
                context.startService(new Intent(context, LoadAverageService.class));
            }
        } catch (Exception e) {
            Log.e("SystemUIBootReceiver", "Can't start load average service", e);
        }
    }
}
