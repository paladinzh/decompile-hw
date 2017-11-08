package com.android.mms.transaction;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.provider.Settings.Global;
import com.android.mms.ui.ConversationList;
import com.google.android.gms.R;

public class SmsRejectedReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (MessagingNotification.isMessageNotificationEnabled(context) && Global.getInt(context.getContentResolver(), "device_provisioned", 0) == 1 && "android.provider.Telephony.SMS_REJECTED".equals(intent.getAction())) {
            if (intent.getIntExtra("result", -1) == 3) {
                NotificationManager nm = (NotificationManager) context.getSystemService("notification");
                Intent viewConvIntent = new Intent(context, ConversationList.class);
                viewConvIntent.setAction("android.intent.action.MAIN");
                viewConvIntent.setFlags(872415232);
                PendingIntent pendingIntent = PendingIntent.getActivityAsUser(context, 0, viewConvIntent, 0, null, UserHandle.CURRENT);
                Notification notification = new Notification();
                notification.icon = R.drawable.stat_notify_sms;
                notification.largeIcon = MessagingNotification.getSmsAppBitmap(context);
                notification.tickerText = context.getString(R.string.sms_full_title);
                notification.defaults = -1;
                notification.setLatestEventInfo(context, context.getString(R.string.sms_full_title), context.getString(R.string.sms_full_body), pendingIntent);
                nm.notifyAsUser(null, 239, notification, UserHandle.CURRENT);
            }
        }
    }
}
