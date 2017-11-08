package com.android.huawei.coverscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.System;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class TimeSubject {
    private Context mContext;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.w("TimeSubject", "onReceive, the intent is null!");
            } else {
                TimeSubject.this.notifyTimeChangeObservers();
            }
        }
    };
    private List<TimeChangeObserver> mTimeChangeObserver = new ArrayList();
    private ContentObserver mTimeFormatChangeObserver;

    private class TimeFormatChangeObserver extends ContentObserver {
        public TimeFormatChangeObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            TimeSubject.this.notifyTimeChangeObservers();
        }
    }

    public TimeSubject(Context context) {
        this.mContext = context;
    }

    public void attachTimeChangeObserver(TimeChangeObserver timeChangeObserver) {
        this.mTimeChangeObserver.add(timeChangeObserver);
    }

    private void notifyTimeChangeObservers() {
        for (TimeChangeObserver observer : this.mTimeChangeObserver) {
            observer.onTimeChange();
        }
    }

    public void registerBroadcastReceiverAndContentObserver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TIME_TICK");
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        if (this.mContext != null) {
            this.mContext.registerReceiver(this.mIntentReceiver, filter, null, null);
        }
        this.mTimeFormatChangeObserver = new TimeFormatChangeObserver(null);
        if (this.mContext != null) {
            this.mContext.getContentResolver().registerContentObserver(System.CONTENT_URI, true, this.mTimeFormatChangeObserver);
        }
    }

    public void unregisterBroadcastReceiverAndContentObserver() {
        if (this.mContext == null) {
            Log.w("TimeSubject", "mContext is null!");
            return;
        }
        if (this.mIntentReceiver != null) {
            this.mContext.unregisterReceiver(this.mIntentReceiver);
        }
        if (this.mTimeFormatChangeObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mTimeFormatChangeObserver);
        }
    }
}
