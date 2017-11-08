package com.android.contacts.vcard;

import android.app.Application;
import android.content.ContentResolver;
import com.android.contacts.ContactsApplication;
import com.android.vcard.VCardEntry;
import com.android.vcard.VCardEntryCommitter;

public class VCardEntryCommitterCustom extends VCardEntryCommitter {
    private ContactsApplication mContactsApp;

    public VCardEntryCommitterCustom(ContentResolver resolver, Application app) {
        super(resolver);
        this.mContactsApp = (ContactsApplication) app;
    }

    public void onEntryCreated(VCardEntry vcardEntry) {
        this.mContactsApp.waitForLaunch();
        super.onEntryCreated(vcardEntry);
    }
}
