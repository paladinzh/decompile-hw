package com.huawei.mms.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Threads;
import android.util.Log;

public class ResetVerifitionFlagThread extends Thread {
    private static final Uri ALL_THREADS_URI = Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();
    private static final Uri THREADS_URI = Threads.CONTENT_URI.buildUpon().appendQueryParameter("update_threads", "true").build();
    private Context context;
    private ContentResolver resolver;

    public ResetVerifitionFlagThread(Context context) {
        super("ResetVerifitionFlagThread");
        this.context = context;
        this.resolver = context.getContentResolver();
    }

    public void run() {
        resetThreadsSecretInfo(this.context);
        resetVerifitionSmsFlag(this.context);
    }

    private void resetThreadsSecretInfo(Context context) {
        Log.d("ResetVerifitionFlagThread", "resetThreadsSecretInfo");
        if (this.resolver == null) {
            Log.e("ResetVerifitionFlagThread", "resolver is null!");
            return;
        }
        Cursor cursor = null;
        try {
            cursor = this.resolver.query(ALL_THREADS_URI, null, "is_secret = ?", new String[]{"1"}, null);
            if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
            }
            do {
                long threadId = cursor.getLong(cursor.getColumnIndex("_id"));
                long messageCount = cursor.getLong(cursor.getColumnIndex("message_count"));
                String snippet = cursor.getString(cursor.getColumnIndex("snippet"));
                ContentValues values = new ContentValues();
                values.put("message_count_secret", Long.valueOf(messageCount));
                values.put("snippet_secret", snippet);
                values.put("is_secret", Integer.valueOf(0));
                String[] threadSelectionArgs = new String[]{String.valueOf(threadId)};
                this.resolver.update(THREADS_URI, values, "_id = ?", threadSelectionArgs);
            } while (cursor.moveToNext());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception ex) {
            Log.e("ResetVerifitionFlagThread", "Exception in resetThreadsSecretInfo, ex = " + ex);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void resetVerifitionSmsFlag(Context context) {
        Log.d("ResetVerifitionFlagThread", "in resetVerifitionSmsFlag");
        if (this.resolver == null) {
            Log.e("ResetVerifitionFlagThread", "resolver is null!");
            return;
        }
        String[] selectionArgs = new String[]{"1"};
        ContentValues values = new ContentValues();
        values.put("is_secret", Integer.valueOf(0));
        this.resolver.update(Sms.CONTENT_URI, values, "is_secret = ?", selectionArgs);
    }
}
