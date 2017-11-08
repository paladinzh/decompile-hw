package com.android.contacts.hap.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import com.android.contacts.ContactSaveService;

public class EraseContactMarkUtils {
    public static Cursor getDataUsageState(Context context, long contact_id) {
        if (context == null) {
            return null;
        }
        ContentResolver resolver = context.getContentResolver();
        Uri frequentUri = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "contacts/data_phone_frequent");
        if (contact_id <= 0) {
            return null;
        }
        return resolver.query(frequentUri, new String[]{"data_id", "times_used", "contact_id"}, "contact_id=?", new String[]{String.valueOf(contact_id)}, "times_used DESC");
    }

    public static boolean eraseContactTimes(Context context, long contactid) {
        if (context == null) {
            return false;
        }
        ContentResolver resolver = context.getContentResolver();
        ContactSaveService.clearPrimary(String.valueOf(contactid), resolver);
        ContentValues cv = new ContentValues();
        cv.put("times_contacted", Integer.valueOf(0));
        return deleteDataUsageAndSetTimesContacted(resolver, String.valueOf(contactid), cv);
    }

    public static boolean deleteDataUsageByDataIds(Context context, long[] dataIds) {
        boolean z = true;
        if (size < 1) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        for (long append : dataIds) {
            sb.append(append).append(",");
        }
        sb.setLength(sb.length() - 1);
        if (context.getContentResolver().update(Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "contacts/data_usage_stat_data").buildUpon().appendPath(sb.toString()).build(), new ContentValues(), null, null) <= 0) {
            z = false;
        }
        return z;
    }

    private static boolean deleteDataUsageAndSetTimesContacted(ContentResolver resolver, String contactIds, ContentValues values) {
        if (resolver.update(Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "contacts/data_phone_frequent").buildUpon().appendPath(contactIds).build(), values, null, null) > 0) {
            return true;
        }
        return false;
    }
}
