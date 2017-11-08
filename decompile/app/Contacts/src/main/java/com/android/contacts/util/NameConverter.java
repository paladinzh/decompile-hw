package com.android.contacts.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.ContactsContract;
import android.text.TextUtils;
import com.android.contacts.model.ValuesDelta;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class NameConverter {
    private static final String[] STRUCTURED_NAME_FIELDS = new String[]{"data4", "data2", "data5", "data3", "data6"};

    public static String structuredNameToDisplayName(Context context, Map<String, String> structuredName) {
        Builder builder = ContactsContract.AUTHORITY_URI.buildUpon().appendPath("complete_name");
        for (String key : STRUCTURED_NAME_FIELDS) {
            if (structuredName.containsKey(key)) {
                appendQueryParameter(builder, key, (String) structuredName.get(key));
            }
        }
        return fetchDisplayName(context, builder.build());
    }

    public static String structuredNameToDisplayName(Context context, ContentValues values) {
        Builder builder = ContactsContract.AUTHORITY_URI.buildUpon().appendPath("complete_name");
        for (String key : STRUCTURED_NAME_FIELDS) {
            if (values.containsKey(key)) {
                appendQueryParameter(builder, key, values.getAsString(key));
            }
        }
        return fetchDisplayName(context, builder.build());
    }

    private static String fetchDisplayName(Context context, Uri uri) {
        String str = null;
        Cursor cursor = context.getContentResolver().query(uri, new String[]{"data1"}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    str = cursor.getString(0);
                }
                cursor.close();
            } catch (Throwable th) {
                cursor.close();
            }
        }
        return str;
    }

    public static Map<String, String> displayNameToStructuredName(Context context, String displayName) {
        Map<String, String> structuredName = new TreeMap();
        Builder builder = ContactsContract.AUTHORITY_URI.buildUpon().appendPath("complete_name");
        appendQueryParameter(builder, "data1", displayName);
        Cursor cursor = context.getContentResolver().query(builder.build(), STRUCTURED_NAME_FIELDS, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    for (int i = 0; i < STRUCTURED_NAME_FIELDS.length; i++) {
                        structuredName.put(STRUCTURED_NAME_FIELDS[i], cursor.getString(i));
                    }
                }
                cursor.close();
            } catch (Throwable th) {
                cursor.close();
            }
        }
        return structuredName;
    }

    public static ContentValues displayNameToStructuredName(Context context, String displayName, ContentValues contentValues) {
        if (contentValues == null) {
            contentValues = new ContentValues();
        }
        for (Entry<String, String> entry : displayNameToStructuredName(context, displayName).entrySet()) {
            contentValues.put((String) entry.getKey(), (String) entry.getValue());
        }
        return contentValues;
    }

    private static void appendQueryParameter(Builder builder, String field, String value) {
        if (!TextUtils.isEmpty(value)) {
            builder.appendQueryParameter(field, value);
        }
    }

    public static String[] getStructuredNameFields() {
        return (String[]) STRUCTURED_NAME_FIELDS.clone();
    }

    public static Map<String, String> valuesToStructuredNameMap(ValuesDelta values) {
        Map<String, String> structuredNameMap = new HashMap();
        for (String key : getStructuredNameFields()) {
            structuredNameMap.put(key, values.getAsString(key));
        }
        return structuredNameMap;
    }

    public static String getDisplayNameFromValuesDelta(Context context, ValuesDelta values) {
        return structuredNameToDisplayName(context, valuesToStructuredNameMap(values));
    }
}
