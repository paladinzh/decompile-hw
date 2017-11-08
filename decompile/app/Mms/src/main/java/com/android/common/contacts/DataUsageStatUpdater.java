package com.android.common.contacts;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.util.Log;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DataUsageStatUpdater {
    private static final String TAG = DataUsageStatUpdater.class.getSimpleName();
    private final ContentResolver mResolver;

    public static final class DataUsageFeedback {
        static final Uri FEEDBACK_URI = Uri.withAppendedPath(Data.CONTENT_URI, "usagefeedback");
    }

    public DataUsageStatUpdater(Context context) {
        this.mResolver = context.getContentResolver();
    }

    public boolean updateWithPhoneNumber(Collection<String> numbers) {
        if (!(numbers == null || numbers.isEmpty())) {
            ArrayList<String> whereArgs = new ArrayList();
            StringBuilder whereBuilder = new StringBuilder();
            String[] questionMarks = new String[numbers.size()];
            whereArgs.addAll(numbers);
            Arrays.fill(questionMarks, "?");
            whereBuilder.append("data1 IN (").append(TextUtils.join(",", questionMarks)).append(")");
            Cursor cursor = this.mResolver.query(Phone.CONTENT_URI, new String[]{"contact_id", "_id"}, whereBuilder.toString(), (String[]) whereArgs.toArray(new String[0]), null);
            if (cursor == null) {
                Log.w(TAG, "Cursor for Phone.CONTENT_URI became null.");
            } else {
                Set<Long> contactIds = new HashSet(cursor.getCount());
                Set<Long> dataIds = new HashSet(cursor.getCount());
                try {
                    cursor.move(-1);
                    while (cursor.moveToNext()) {
                        contactIds.add(Long.valueOf(cursor.getLong(0)));
                        dataIds.add(Long.valueOf(cursor.getLong(1)));
                    }
                    return update(contactIds, dataIds, "short_text");
                } finally {
                    cursor.close();
                }
            }
        }
        return false;
    }

    private boolean update(Collection<Long> contactIds, Collection<Long> dataIds, String type) {
        long currentTimeMillis = System.currentTimeMillis();
        if (VERSION.SDK_INT >= 14) {
            if (!dataIds.isEmpty()) {
                if (this.mResolver.update(DataUsageFeedback.FEEDBACK_URI.buildUpon().appendPath(TextUtils.join(",", dataIds)).appendQueryParameter(NumberInfo.TYPE_KEY, type).build(), new ContentValues(), null, null) > 0) {
                    return true;
                }
                if (!Log.isLoggable(TAG, 3)) {
                    return false;
                }
                Log.d(TAG, "update toward data rows " + dataIds + " failed");
                return false;
            } else if (!Log.isLoggable(TAG, 3)) {
                return false;
            } else {
                Log.d(TAG, "Given list for data IDs is null. Ignoring.");
                return false;
            }
        } else if (!contactIds.isEmpty()) {
            StringBuilder whereBuilder = new StringBuilder();
            ArrayList<String> whereArgs = new ArrayList();
            String[] questionMarks = new String[contactIds.size()];
            for (Long longValue : contactIds) {
                whereArgs.add(String.valueOf(longValue.longValue()));
            }
            Arrays.fill(questionMarks, "?");
            whereBuilder.append("_id IN (").append(TextUtils.join(",", questionMarks)).append(")");
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, "contactId where: " + whereBuilder.toString());
                Log.d(TAG, "contactId selection: " + whereArgs);
            }
            ContentValues values = new ContentValues();
            values.put("last_time_contacted", Long.valueOf(currentTimeMillis));
            if (this.mResolver.update(Contacts.CONTENT_URI, values, whereBuilder.toString(), (String[]) whereArgs.toArray(new String[0])) > 0) {
                return true;
            }
            if (!Log.isLoggable(TAG, 3)) {
                return false;
            }
            Log.d(TAG, "update toward raw contacts " + contactIds + " failed");
            return false;
        } else if (!Log.isLoggable(TAG, 3)) {
            return false;
        } else {
            Log.d(TAG, "Given list for contact IDs is null. Ignoring.");
            return false;
        }
    }
}
