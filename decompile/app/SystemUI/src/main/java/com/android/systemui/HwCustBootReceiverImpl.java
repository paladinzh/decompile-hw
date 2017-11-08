package com.android.systemui;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.util.Slog;

public class HwCustBootReceiverImpl extends BroadcastReceiver {
    private static final String TAG = "SystemUIBootReceiver";
    private Runnable mBetaInfoDisplay;

    public void onReceive(final Context context, Intent intent) {
        try {
            if (Global.getInt(context.getContentResolver(), "show_processes", 0) != 0) {
                context.startService(new Intent(context, LoadAverageService.class));
            }
        } catch (SecurityException se) {
            Slog.e(TAG, "Can't start load average service", se);
        } catch (RuntimeException re) {
            Slog.e(TAG, "Failure from system", re);
        }
        if (SystemProperties.getBoolean("ro.config.hw_showTestInfo", false)) {
            this.mBetaInfoDisplay = new Runnable() {
                public void run() {
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
                    if (notificationManager != null) {
                        notificationManager.notify(R.drawable.stat_notify_test, new Builder(context).setContentTitle(context.getText(R.string.betainfo_title)).setContentText(context.getText(R.string.betainfo_text)).setSubText(context.getText(R.string.betainfo_sub_text)).setSmallIcon(R.drawable.stat_notify_test).build());
                    }
                }
            };
            this.mBetaInfoDisplay.run();
        }
    }
}
