package com.android.contacts;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import com.android.contacts.ContactSaveService.Listener;
import com.android.contacts.activities.TransactionSafeActivity;
import com.android.contacts.test.InjectedServices;

public abstract class ContactsActivity extends TransactionSafeActivity implements Listener {
    private ContentResolver mContentResolver;

    public ContentResolver getContentResolver() {
        if (this.mContentResolver == null) {
            InjectedServices services = ContactsApplication.getInjectedServices();
            if (services != null) {
                this.mContentResolver = services.getContentResolver();
            }
            if (this.mContentResolver == null) {
                this.mContentResolver = super.getContentResolver();
            }
        }
        return this.mContentResolver;
    }

    public SharedPreferences getSharedPreferences(String name, int mode) {
        InjectedServices services = ContactsApplication.getInjectedServices();
        if (services != null) {
            SharedPreferences prefs = services.getSharedPreferences();
            if (prefs != null) {
                return prefs;
            }
        }
        return super.getSharedPreferences(name, mode);
    }

    public Object getSystemService(String name) {
        Object service = super.getSystemService(name);
        if (service != null) {
            return service;
        }
        return getApplicationContext().getSystemService(name);
    }

    protected void onCreate(Bundle savedInstanceState) {
        ContactSaveService.registerListener(this);
        super.onCreate(savedInstanceState);
    }

    protected void onDestroy() {
        ContactSaveService.unregisterListener(this);
        super.onDestroy();
    }

    public void onServiceCompleted(Intent callbackIntent) {
        onNewIntent(callbackIntent);
    }

    public <T extends View> T getView(int id) {
        T result = findViewById(id);
        if (result != null) {
            return result;
        }
        throw new IllegalArgumentException("view 0x" + Integer.toHexString(id) + " doesn't exist");
    }
}
