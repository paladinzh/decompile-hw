package com.android.mms.ui;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.android.mms.transaction.MessagingNotification;
import com.google.android.gms.R;

public class SmsStorageMonitor extends BroadcastReceiver {
    private static NotificationManager mNotificationManager;
    private Context mContext;

    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        }
        if ("android.intent.action.DEVICE_STORAGE_FULL".equals(intent.getAction())) {
            notifyReachStorageLimited();
        } else if ("android.intent.action.DEVICE_STORAGE_NOT_FULL".equals(intent.getAction())) {
            cancelStorageLimitedWarning();
        }
    }

    private void notifyReachStorageLimited() {
        Builder mBuilder = new Builder(this.mContext).setSmallIcon(R.drawable.stat_notify_sms).setContentTitle(this.mContext.getString(R.string.storage_warning_title)).setContentText(this.mContext.getString(R.string.storage_warning_content)).setOngoing(true);
        mBuilder.setLargeIcon(MessagingNotification.getSmsAppBitmap(this.mContext));
        mNotificationManager.notify(-1, mBuilder.build());
    }

    private void cancelStorageLimitedWarning() {
        mNotificationManager.cancel(-1);
    }
}
