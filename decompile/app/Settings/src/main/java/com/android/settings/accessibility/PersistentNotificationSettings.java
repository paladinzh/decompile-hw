package com.android.settings.accessibility;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

public class PersistentNotificationSettings extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
    private ContentResolver mContentResolver = null;
    private Context mContext = null;
    private String[] mIntervalArray = null;
    private Preference mIntervalPref = null;
    private boolean mIsAfterOnStart;
    private SwitchPreference mPersistentNotification = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230830);
        this.mContext = getActivity();
        this.mContentResolver = this.mContext.getContentResolver();
        this.mPersistentNotification = (SwitchPreference) findPreference("persistent_notification");
        if (this.mPersistentNotification != null) {
            this.mPersistentNotification.setOnPreferenceChangeListener(this);
        }
        this.mIntervalPref = findPreference("repeat_interval");
        if (this.mIntervalPref != null) {
            this.mIntervalPref.setOnPreferenceClickListener(this);
        }
        this.mIntervalArray = this.mContext.getResources().getStringArray(2131362018);
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    public void onResume() {
        super.onResume();
        int value = System.getInt(this.mContentResolver, "persistent_notification", -1);
        Log.d("PersistentNotificationSettings", "onResume Entry, Persistent Notification Value: " + value);
        updateSettings(value);
    }

    public void onStart() {
        super.onStart();
        Log.d("PersistentNotificationSettings", "onStart called, so setting mIsAfterOnStart to true");
        this.mIsAfterOnStart = true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.mPersistentNotification) {
            Boolean isChecked = (Boolean) newValue;
            int value = -1;
            if (isChecked.booleanValue()) {
                value = System.getInt(this.mContentResolver, "persistent_notification_previous_value", 0);
                Log.d("PersistentNotificationSettings", "Persistent Notification Setting enabled, so restoring from db the previous value: " + value);
            } else {
                int currentValue = System.getInt(this.mContentResolver, "persistent_notification", 0);
                Log.d("PersistentNotificationSettings", "Persistent Notification Setting getting disabled, so storing in db, setting the previous value as current value: " + currentValue);
                System.putInt(this.mContentResolver, "persistent_notification_previous_value", currentValue);
            }
            updateSettings(value);
            System.putInt(this.mContentResolver, "persistent_notification", value);
            registerOrUnregisterObserver(isChecked.booleanValue());
        }
        return false;
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference != this.mIntervalPref) {
            return false;
        }
        startActivityForResult(new Intent("android.intent.action.NOTIFICATION_INTERVAL_PICKER"), 0);
        return true;
    }

    private void updateSettings(int state) {
        boolean isEnabled = -1 != state;
        this.mPersistentNotification.setChecked(isEnabled);
        if (this.mIntervalPref != null) {
            this.mIntervalPref.setEnabled(isEnabled);
        }
        if (isEnabled && this.mIntervalPref != null) {
            this.mIntervalPref.setSummary(this.mIntervalArray[state]);
        } else if (this.mIsAfterOnStart) {
            int previousValue = System.getInt(this.mContentResolver, "persistent_notification_previous_value", 0);
            Log.d("PersistentNotificationSettings", "Call is after onStart, so setting Interval from Previous value: " + previousValue);
            if (this.mIntervalPref != null) {
                this.mIntervalPref.setSummary(this.mIntervalArray[previousValue]);
            }
            this.mIsAfterOnStart = false;
        }
    }

    private void registerOrUnregisterObserver(boolean isPersistentNotificationEnabled) {
        PersistentNotificationSettingsObserver mPersistentNotificationSettingsObserver = PersistentNotificationSettingsObserver.getInstance(this.mContext.getApplicationContext());
        if (isPersistentNotificationEnabled) {
            Log.d("PersistentNotificationSettings", "Persistent Notification enabled, so registering observer for URIs: " + PersistentNotificationService.SYSTEM_PERSISTENT_NOTIFICATION_URI + " & " + PersistentNotificationService.PERSISTENT_NOTIFICATION_STATUS_URI);
            this.mContentResolver.registerContentObserver(PersistentNotificationService.SYSTEM_PERSISTENT_NOTIFICATION_URI, false, mPersistentNotificationSettingsObserver);
            this.mContentResolver.registerContentObserver(PersistentNotificationService.PERSISTENT_NOTIFICATION_STATUS_URI, false, mPersistentNotificationSettingsObserver);
            mPersistentNotificationSettingsObserver.onChange(false);
            return;
        }
        Log.d("PersistentNotificationSettings", "Persistent Notification disabled, so Killing Persistent Notification Scheduler and unregistering Observer");
        PersistentNotificationScheduler.getInstance(this.mContext.getApplicationContext()).shutdownNow();
        this.mContentResolver.unregisterContentObserver(mPersistentNotificationSettingsObserver);
    }
}
