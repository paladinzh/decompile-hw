package rcstelephony;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.BaseColumns;
import android.util.Log;
import com.huawei.cspcommon.ex.SqliteWrapper;
import java.util.HashSet;
import java.util.Set;

public final class RcsMessagingConstants$Threads implements BaseColumns {
    public static final Uri CONTENT_URI = Uri.withAppendedPath(RcsMessagingConstants$MmsSms.CONTENT_URI, "conversations");
    private static final String[] ID_PROJECTION = new String[]{"_id"};
    public static final Uri OBSOLETE_THREADS_URI = Uri.withAppendedPath(CONTENT_URI, "obsolete");
    private static final Uri THREAD_ID_CONTENT_URI = Uri.parse("content://mms-sms/threadID");

    private RcsMessagingConstants$Threads() {
    }

    public static long getOrCreateThreadId(Context context, String recipient) {
        Set recipients = new HashSet();
        recipients.add(recipient);
        return getOrCreateThreadId(context, recipients);
    }

    public static long getOrCreateThreadId(Context context, Set<String> recipients) {
        Builder uriBuilder = THREAD_ID_CONTENT_URI.buildUpon();
        for (String recipient : recipients) {
            String recipient2;
            if (RcsMessagingConstants$Mms.isEmailAddress(recipient2)) {
                recipient2 = RcsMessagingConstants$Mms.extractAddrSpec(recipient2);
            }
            uriBuilder.appendQueryParameter("recipient", recipient2);
        }
        Uri uri = uriBuilder.build();
        Log.v("Telephony", "getOrCreateThreadId ");
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(), uri, ID_PROJECTION, null, null, null);
        if (cursor != null) {
            Log.v("Telephony", "getOrCreateThreadId cursor cnt: " + cursor.getCount());
            try {
                if (cursor.moveToFirst()) {
                    long j = cursor.getLong(0);
                    return j;
                }
                Log.w("Telephony", "getOrCreateThreadId returned no rows!");
                cursor.close();
            } finally {
                cursor.close();
            }
        }
        Log.w("Telephony", "getOrCreateThreadId failed with uri " + uri.toString());
        throw new IllegalArgumentException("Unable to find or allocate a thread ID.");
    }
}
