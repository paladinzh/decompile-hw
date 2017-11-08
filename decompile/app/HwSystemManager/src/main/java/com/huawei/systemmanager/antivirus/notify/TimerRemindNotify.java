package com.huawei.systemmanager.antivirus.notify;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.antivirus.ui.AntiVirusGlobalScanActivity;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.NotificationID;

public class TimerRemindNotify {
    public static final String ACTION_VIRUS_TIMING_NOTIFY = "huawei.intent.action.antivirus.globalscan.timernotify";
    private static final int SCHDULE_NOTIFY_PERIOD = 1;
    private static final int TIMING_NOTIFY_MAX = 1000;
    private static final int TIMING_NOTIFY_PERIOD = 30;

    public void schduleTimingNotify(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        long lastTime = AntiVirusTools.getTimerRemindTimeStamp(context);
        if (lastTime <= 0 || lastTime > System.currentTimeMillis()) {
            AntiVirusTools.updateTimerRemindTimeStamp(context);
            lastTime = AntiVirusTools.getTimerRemindTimeStamp(context);
        }
        alarmManager.setRepeating(1, 86400000 + lastTime, 86400000, createTimingNotifyIntent(context));
    }

    public void cancelTimingNotify(Context context) {
        ((AlarmManager) context.getSystemService("alarm")).cancel(createTimingNotifyIntent(context));
    }

    private PendingIntent createTimingNotifyIntent(Context context) {
        Intent intent = new Intent(ACTION_VIRUS_TIMING_NOTIFY);
        intent.setPackage(context.getPackageName());
        return PendingIntent.getService(context, 0, intent, 134217728);
    }

    public void doAction(Context context) {
        int period = (int) ((System.currentTimeMillis() - AntiVirusTools.getTimerRemindTimeStamp(context)) / 86400000);
        if (period > 1000 || period < 0) {
            AntiVirusTools.updateTimerRemindTimeStamp(context);
            cancelTimingNotify(context);
            schduleTimingNotify(context);
        }
        if (period >= 30 && period <= 1000) {
            showNotification(period);
        }
    }

    private void showNotification(int period) {
        Context context = GlobalContext.getContext();
        NotificationManager nm = (NotificationManager) context.getSystemService("notification");
        Intent intent = new Intent(context, AntiVirusGlobalScanActivity.class);
        intent.setFlags(268468224);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, ShareCfg.PERMISSION_MODIFY_CALENDAR);
        intent.setPackage(context.getPackageName());
        Builder builder = new Builder(context);
        builder.setSmallIcon(R.drawable.ic_virus_notification).setContentTitle(context.getString(R.string.virus_timer_notify_title)).setTicker(context.getString(R.string.virus_timer_notify_title)).setContentText(context.getResources().getQuantityString(R.plurals.virus_timer_notify_message, period, new Object[]{Integer.valueOf(period)})).setContentIntent(contentIntent);
        Notification notification = builder.build();
        notification.flags = 16;
        nm.notify(NotificationID.VIRUS, notification);
    }
}
