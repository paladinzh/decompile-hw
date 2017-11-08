package com.android.contacts.calllog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.contacts.util.LogConfig;

public class MissedCallNotificationReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (LogConfig.HWFLOW) {
            Log.i("MissedCallNotificationReceiver", "onReceive,action:" + action);
        }
        if ("android.telecom.action.SHOW_MISSED_CALLS_NOTIFICATION".equals(action)) {
            CallLogNotificationsService.updateMissedCallNotifications(context, intent.getIntExtra("android.telecom.extra.NOTIFICATION_COUNT", -1), intent.getStringExtra("android.telecom.extra.NOTIFICATION_PHONE_NUMBER"));
        }
    }
}
