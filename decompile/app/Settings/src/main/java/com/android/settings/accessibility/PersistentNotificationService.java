package com.android.settings.accessibility;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings.System;

public class PersistentNotificationService extends IntentService {
    public static final Uri PERSISTENT_NOTIFICATION_STATUS_URI = System.getUriFor("persistent_notification_status");
    public static final Uri SYSTEM_PERSISTENT_NOTIFICATION_PREVIOUS_VALUE_URI = System.getUriFor("persistent_notification_previous_value");
    public static final Uri SYSTEM_PERSISTENT_NOTIFICATION_URI = System.getUriFor("persistent_notification");

    public PersistentNotificationService() {
        super("PersistentNotificationService");
    }

    protected void onHandleIntent(Intent intent) {
        registerObserver(System.getInt(getContentResolver(), "persistent_notification", -1));
        System.putInt(getContentResolver(), "persistent_notification_status", 0);
    }

    private void registerObserver(int isPersistentNotificationEnabled) {
        if (isPersistentNotificationEnabled != -1) {
            PersistentNotificationSettingsObserver mPersistentNotificationSettingsObserver = PersistentNotificationSettingsObserver.getInstance(this);
            getContentResolver().registerContentObserver(SYSTEM_PERSISTENT_NOTIFICATION_URI, false, mPersistentNotificationSettingsObserver);
            getContentResolver().registerContentObserver(PERSISTENT_NOTIFICATION_STATUS_URI, false, mPersistentNotificationSettingsObserver);
            mPersistentNotificationSettingsObserver.onChange(false);
        }
    }
}
