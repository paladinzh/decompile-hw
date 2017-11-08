package com.huawei.systemmanager.preventmode.notify;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.service.notification.ZenModeConfig;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Nodisturbe;
import com.huawei.systemmanager.preventmode.util.PreventDataHelper;
import com.huawei.systemmanager.util.HwLog;

public class PreventNotifyManager {
    private static final String TAG = "PreventNotifyManager";
    private Context mContext;
    private NotificationManager mManager = null;

    public PreventNotifyManager(Context context) {
        this.mContext = context;
        this.mManager = (NotificationManager) this.mContext.getSystemService("notification");
    }

    private void cancel() {
        this.mManager.cancel(100);
    }

    private Intent getNotifyIntent() {
        Intent intent = new Intent("android.settings.ZEN_MODE_SETTINGS");
        intent.setPackage(HsmStatConst.SETTING_PACKAGE_NAME);
        intent.setFlags(335544320);
        intent.putExtra(HsmStatConst.KEY_NOTFICATION_EVENT, Nodisturbe.ACTION_CLICK_NOTIFICATION);
        return intent;
    }

    public void updateNotification() {
        int zenMode = PreventDataHelper.getCurrentZenMode(this.mContext);
        HwLog.i(TAG, "updateNotification, zenMode:" + zenMode);
        if (zenMode == 0) {
            cancel();
            return;
        }
        int stringId = getStringId(zenMode);
        PendingIntent contentIntent = PendingIntent.getActivity(this.mContext, 100, getNotifyIntent(), 134217728);
        Builder builder = new Builder(this.mContext);
        builder.setSmallIcon(R.drawable.ic_settings_notification_miandarao).setTicker(this.mContext.getText(R.string.prevent_notofication_title)).setOngoing(true).setShowWhen(true).setPriority(2).setContentTitle(this.mContext.getText(R.string.prevent_notofication_title)).setContentText(this.mContext.getText(stringId));
        if (UserHandle.myUserId() == 0) {
            builder.setContentIntent(contentIntent);
        }
        Notification notification = builder.build();
        notification.flags = 32;
        this.mManager.notify(100, notification);
    }

    private int getStringId(int zenMode) {
        if (2 == zenMode) {
            return R.string.prevent_no_call_and_sms_event;
        }
        if (1 == zenMode) {
            ZenModeConfig config = PreventDataHelper.getCurrentZenModeConfig();
            if (config == null) {
                return R.string.prevent_no_call_and_sms_event;
            }
            if (!config.allowEvents && !config.allowReminders) {
                return R.string.prevent_all_except_alarms;
            }
            if (config.allowReminders && !config.allowEvents) {
                return R.string.prevent_allow_reminders_but_events;
            }
            if (config.allowReminders || !config.allowEvents) {
                return R.string.prevent_allow_reminders_and_events;
            }
            return R.string.prevent_allow_events_but_allows;
        } else if (3 == zenMode) {
            return R.string.prevent_all_except_alarms;
        } else {
            return R.string.prevent_no_call_and_sms_event;
        }
    }
}
