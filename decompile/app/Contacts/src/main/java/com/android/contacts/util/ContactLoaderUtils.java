package com.android.contacts.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.ContactsContract.RawContacts;
import com.android.contacts.hap.numbermark.YellowPageContactUtil;

public final class ContactLoaderUtils {
    private ContactLoaderUtils() {
    }

    public static Uri ensureIsContactUri(ContentResolver resolver, Uri uri) throws IllegalArgumentException {
        if (uri == null) {
            throw new IllegalArgumentException("uri must not be null");
        }
        String authority = uri.getAuthority();
        if ("com.android.contacts".equals(authority)) {
            String type = resolver.getType(uri);
            if ("vnd.android.cursor.item/contact".equals(type)) {
                return uri;
            }
            if ("vnd.android.cursor.item/raw_contact".equals(type)) {
                return RawContacts.getContactLookupUri(resolver, ContentUris.withAppendedId(RawContacts.CONTENT_URI, ContentUris.parseId(uri)));
            }
            HwLog.e("ContactLoaderUtils", "uri:" + uri);
            throw new IllegalArgumentException("uri format is unknown");
        } else if (YellowPageContactUtil.isYellowPageUri(uri)) {
            return uri;
        } else {
            String OBSOLETE_AUTHORITY = "contacts";
            if ("contacts".equals(authority)) {
                return RawContacts.getContactLookupUri(resolver, ContentUris.withAppendedId(RawContacts.CONTENT_URI, ContentUris.parseId(uri)));
            }
            HwLog.e("ContactLoaderUtils", "uri:" + uri);
            throw new IllegalArgumentException("uri authority is unknown");
        }
    }
}
