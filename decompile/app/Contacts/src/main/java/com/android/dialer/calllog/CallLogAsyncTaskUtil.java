package com.android.dialer.calllog;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import com.android.contacts.calllog.CallLogNotificationsService;
import com.android.contacts.util.AsyncTaskExecutor;
import com.android.contacts.util.AsyncTaskExecutors;
import com.google.common.annotations.VisibleForTesting;

public class CallLogAsyncTaskUtil {
    private static String TAG = CallLogAsyncTaskUtil.class.getSimpleName();
    private static AsyncTaskExecutor sAsyncTaskExecutor;

    public interface CallLogAsyncTaskListener {
        void onDeleteVoicemail();
    }

    public enum Tasks {
        DELETE_VOICEMAIL,
        DELETE_CALL,
        MARK_VOICEMAIL_READ,
        GET_CALL_DETAILS
    }

    private static void initTaskExecutor() {
        sAsyncTaskExecutor = AsyncTaskExecutors.createThreadPoolExecutor();
    }

    public static void markVoicemailAsRead(final Context context, final Uri voicemailUri) {
        if (sAsyncTaskExecutor == null) {
            initTaskExecutor();
        }
        sAsyncTaskExecutor.submit(Tasks.MARK_VOICEMAIL_READ, new AsyncTask<Void, Void, Void>() {
            public Void doInBackground(Void... params) {
                ContentValues values = new ContentValues();
                values.put("is_read", Boolean.valueOf(true));
                context.getContentResolver().update(voicemailUri, values, "is_read = 0", null);
                Intent intent = new Intent(context, CallLogNotificationsService.class);
                intent.setAction("com.android.contacts.calllog.ACTION_MARK_NEW_VOICEMAILS_AS_OLD");
                context.startService(intent);
                return null;
            }
        }, new Void[0]);
    }

    public static void deleteVoicemail(final Context context, final Uri voicemailUri, final CallLogAsyncTaskListener callLogAsyncTaskListener) {
        if (sAsyncTaskExecutor == null) {
            initTaskExecutor();
        }
        sAsyncTaskExecutor.submit(Tasks.DELETE_VOICEMAIL, new AsyncTask<Void, Void, Void>() {
            public Void doInBackground(Void... params) {
                context.getContentResolver().delete(voicemailUri, null, null);
                return null;
            }

            public void onPostExecute(Void result) {
                if (callLogAsyncTaskListener != null) {
                    callLogAsyncTaskListener.onDeleteVoicemail();
                }
            }
        }, new Void[0]);
    }

    @VisibleForTesting
    public static void resetForTest() {
        sAsyncTaskExecutor = null;
    }
}
