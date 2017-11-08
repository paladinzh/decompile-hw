package com.android.settings.accessibility;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.System;

public class PersistentNotificationSettingsObserver extends ContentObserver {
    private static Context mContext;

    private static class SingletonHandler {
        private static final PersistentNotificationSettingsObserver INSTANCE = new PersistentNotificationSettingsObserver(null);

        private SingletonHandler() {
        }
    }

    private PersistentNotificationSettingsObserver(Handler handler) {
        super(handler);
    }

    public static PersistentNotificationSettingsObserver getInstance(Context context) {
        mContext = context;
        return SingletonHandler.INSTANCE;
    }

    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        int persistentNotificationValue = System.getInt(mContext.getContentResolver(), "persistent_notification", -1);
        if (persistentNotificationValue != -1) {
            int persistentNotificationStatus = System.getInt(mContext.getContentResolver(), "persistent_notification_status", 0);
            if (persistentNotificationStatus == 0) {
                PersistentNotificationScheduler.getInstance(mContext).shutdownNow();
            } else if (persistentNotificationStatus == 1) {
                int[] repeatInterval = mContext.getResources().getIntArray(2131362019);
                PersistentNotificationScheduler.getInstance(mContext).shutdownNow();
                PersistentNotificationScheduler.getInstance(mContext).scheduleIndefiniteTimer((long) repeatInterval[persistentNotificationValue]);
            }
        }
    }
}
