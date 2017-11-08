package com.android.settings;

import android.app.IntentService;
import android.content.Intent;

public class TimeZonesService extends IntentService {
    private static final String TAG = TimeZonesService.class.getCanonicalName();

    public TimeZonesService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        if ("android.intent.action.LOCALE_CHANGED".equals(intent.getStringExtra("action_name"))) {
            TimeZonesDatabaseHelper timeZonesDatabaseHelper = TimeZonesDatabaseHelper.getInstance(this);
            timeZonesDatabaseHelper.onOpen(timeZonesDatabaseHelper.getReadableDatabase());
        }
    }
}
