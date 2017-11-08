package com.android.mms.transaction;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.provider.Settings.Global;
import com.android.mms.ui.ManageSimMessages;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;

public class SimFullReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (MessagingNotification.isMessageNotificationEnabled(context) && Global.getInt(context.getContentResolver(), "device_provisioned", 0) == 1 && "android.provider.Telephony.SIM_FULL".equals(intent.getAction())) {
            NotificationManager nm = (NotificationManager) context.getSystemService("notification");
            Intent viewSimIntent = new Intent(context, ManageSimMessages.class);
            viewSimIntent.setAction("android.intent.action.VIEW");
            viewSimIntent.setFlags(268435456);
            int subID = intent.getIntExtra("slot", 0);
            MLog.v("SimFullReceiver", "onReceive::the sub ID is: " + subID);
            PendingIntent pendingIntent = PendingIntent.getActivityAsUser(context, 0, MessageUtils.setSimIdToIntent(viewSimIntent, subID), 134217728, null, UserHandle.CURRENT);
            Notification notification = new Notification();
            notification.icon = R.drawable.stat_notify_sim;
            int id = R.string.sim_full_title;
            if (MessageUtils.isMultiSimEnabled()) {
                id = subID == 0 ? R.string.card1_full_title : R.string.card2_full_title;
            }
            notification.tickerText = context.getString(id);
            notification.defaults = -1;
            nm.notifyAsUser(null, 234, setLatestEventInfo(context, notification, pendingIntent), UserHandle.CURRENT);
        }
    }

    private static Notification setLatestEventInfo(Context context, Notification notification, PendingIntent contentIntent) {
        Builder builder = new Builder(context);
        builder.setWhen(notification.when);
        builder.setSmallIcon(notification.icon);
        builder.setPriority(1);
        builder.setTicker(notification.tickerText);
        builder.setContentTitle(notification.tickerText);
        builder.setContentText(context.getString(R.string.sim_full_body_Toast));
        builder.setNumber(notification.number);
        builder.setSound(notification.sound, notification.audioStreamType);
        builder.setDefaults(notification.defaults);
        builder.setVibrate(notification.vibrate);
        if (notification.largeIcon != null) {
            builder.setLargeIcon(notification.largeIcon);
        }
        builder.setContentIntent(contentIntent);
        return builder.getNotification();
    }
}
