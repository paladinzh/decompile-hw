package com.android.contacts.test;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import java.util.HashMap;

public class InjectedServices {
    private ContentResolver mContentResolver;
    private SharedPreferences mSharedPreferences;
    private HashMap<String, Object> mSystemServices;

    @VisibleForTesting
    public void setContentResolver(ContentResolver contentResolver) {
        this.mContentResolver = contentResolver;
    }

    public ContentResolver getContentResolver() {
        return this.mContentResolver;
    }

    @VisibleForTesting
    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.mSharedPreferences = sharedPreferences;
    }

    public SharedPreferences getSharedPreferences() {
        return this.mSharedPreferences;
    }

    @VisibleForTesting
    public void setSystemService(String name, Object service) {
        if (this.mSystemServices == null) {
            this.mSystemServices = Maps.newHashMap();
        }
        this.mSystemServices.put(name, service);
    }

    public Object getSystemService(String name) {
        if (this.mSystemServices != null) {
            return this.mSystemServices.get(name);
        }
        return null;
    }
}
