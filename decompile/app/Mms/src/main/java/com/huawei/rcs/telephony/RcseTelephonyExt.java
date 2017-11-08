package com.huawei.rcs.telephony;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;

public final class RcseTelephonyExt {
    public static final Uri CONTENT_URI_RECIPIENTIDS = Uri.parse("content://rcsim/recipientids");
    public static final Uri CONTENT_URI_THREADS = Uri.parse("content://rcsim/threads");
    public static final Uri CONTENT_URI_THREADS_ID = Uri.parse("content://rcsim/threads_id");
    private static final String FT_TAG = (TAG + " FileTrans: ");
    private static String TAG = "RcsTelePhonyExt";

    public static final class RcsAttachments implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://rcsim/file_trans");

        public static final Cursor query(ContentResolver aResolver, String[] aProjection, String aWhere) {
            return aResolver.query(CONTENT_URI, aProjection, aWhere, null, null);
        }
    }

    public static Bundle createBundleValues(long threadId, String path, String displayName, long totalSize, String mimeType) {
        Bundle bundle = new Bundle();
        bundle.putString("file_type", mimeType);
        bundle.putString("file_name", displayName);
        bundle.putString("file_content", path);
        bundle.putLong("trans_size", 0);
        bundle.putLong("file_size", totalSize);
        bundle.putLong("date", System.currentTimeMillis());
        bundle.putLong("thread_id", threadId);
        return bundle;
    }
}
