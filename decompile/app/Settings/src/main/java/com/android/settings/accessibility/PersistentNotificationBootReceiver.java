package com.android.settings.accessibility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.settings.HwCustSettingsUtils;

public class PersistentNotificationBootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !"android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Log.e("PersistentNotificationBootReceiver", "Intent is null or action doesn't match android.intent.action.BOOT_COMPLETED, so exiting");
            return;
        }
        if (HwCustSettingsUtils.isFlagPersistentNotificationEnabled()) {
            Intent startIntent = new Intent(context, PersistentNotificationService.class);
            Log.i("PersistentNotificationBootReceiver", "Explicit Intent Fired to start PersistentNotificationService");
            context.startService(startIntent);
        }
    }
}
