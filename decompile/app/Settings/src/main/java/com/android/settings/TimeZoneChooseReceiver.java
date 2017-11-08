package com.android.settings;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Log;

public class TimeZoneChooseReceiver extends BroadcastReceiver {
    private static final boolean mSupportDT = SystemProperties.getBoolean("ro.config_hw_doubletime", false);

    public void onReceive(Context context, Intent intent) {
        Log.d("TimeZoneChooseReceiver", "receive android.intent.action.ACTION_TIMEZONE_SELECTION and support is " + mSupportDT);
        if (mSupportDT && "android.intent.action.ACTION_TIMEZONE_SELECTION".equals(intent.getAction())) {
            Builder nfBuilder = new Builder(context);
            nfBuilder.setDefaults(-1);
            nfBuilder.setSmallIcon(2130837686);
            String nfTitle = context.getString(2131629065);
            nfBuilder.setTicker(nfTitle);
            nfBuilder.setContentTitle(nfTitle);
            nfBuilder.setContentText(context.getString(2131629066));
            Intent timeZoneSetting = new Intent(context, TimeZoneRecommendActivity.class);
            timeZoneSetting.setAction("android.intent.action.PICK");
            timeZoneSetting.setFlags(872415232);
            timeZoneSetting.putExtra("iso", intent.getStringExtra("iso"));
            nfBuilder.setContentIntent(PendingIntent.getActivity(context, 0, timeZoneSetting, 0));
            nfBuilder.setAutoCancel(true);
            Notification notification = nfBuilder.getNotification();
            NotificationManager nm = (NotificationManager) context.getSystemService("notification");
            if (nm != null) {
                nm.notify(1, notification);
                Log.d("TimeZoneChooseReceiver", "receive android.intent.action.ACTION_TIMEZONE_SELECTION and notify user");
                return;
            }
            Log.e("TimeZoneChooseReceiver", "receive android.intent.action.ACTION_TIMEZONE_SELECTION, but notification manager is null.");
        }
    }
}
