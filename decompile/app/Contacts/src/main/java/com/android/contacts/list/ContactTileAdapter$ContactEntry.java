package com.android.contacts.list;

import android.content.ContentUris;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;

public class ContactTileAdapter$ContactEntry {
    public boolean isPrivate;
    public Uri lookupKey;
    public long mId;
    public String mLookupKeyStr;
    public String name;
    public String phoneLabel;
    public String phoneNumber;
    public long photoId;
    public Uri photoUri;
    public Drawable presenceIcon;
    public String status;

    public Uri getAndUpdateLookupKey() {
        if (this.lookupKey == null) {
            this.lookupKey = ContentUris.withAppendedId(Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, this.mLookupKeyStr), this.mId);
        }
        return this.lookupKey;
    }
}
