package com.android.settings.accessibility;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Secure;

abstract class SettingsContentObserver extends ContentObserver {
    public abstract void onChange(boolean z, Uri uri);

    public SettingsContentObserver(Handler handler) {
        super(handler);
    }

    public void register(ContentResolver contentResolver) {
        contentResolver.registerContentObserver(Secure.getUriFor("accessibility_enabled"), false, this);
        contentResolver.registerContentObserver(Secure.getUriFor("enabled_accessibility_services"), false, this);
    }

    public void unregister(ContentResolver contentResolver) {
        contentResolver.unregisterContentObserver(this);
    }
}
