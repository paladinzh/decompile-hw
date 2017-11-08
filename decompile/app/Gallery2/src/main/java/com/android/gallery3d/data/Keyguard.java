package com.android.gallery3d.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.OperationCanceledException;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import java.io.Closeable;

public final class Keyguard {
    public static final Uri KEYGUARD_UPDATE_URI = new Builder().scheme("content").authority("com.android.huawei.magazineunlock").appendPath("common").build();
    private static String TAG = "Keyguard";
    public static final Uri URI = new Builder().scheme("content").authority("com.android.huawei.magazineunlock").appendPath("pictures").build();

    public static int updateHiddenFlag(ContentResolver resolver, int bucketId, boolean setHidden) {
        int i;
        ContentValues cv = new ContentValues();
        String str = "isHidden";
        if (setHidden) {
            i = 1;
        } else {
            i = 0;
        }
        cv.put(str, Integer.valueOf(i));
        return resolver.update(URI, cv, "bucket_id=?", new String[]{String.valueOf(bucketId)});
    }

    public static int updateKeyguardLikeFlag(ContentResolver resolver, int id, boolean setFavorite) {
        int i = 1;
        try {
            ContentValues cv = new ContentValues();
            String str = "isFavorite";
            if (!setFavorite) {
                i = 0;
            }
            cv.put(str, Integer.valueOf(i));
            return resolver.update(URI, cv, "_id=?", new String[]{String.valueOf(id)});
        } catch (RuntimeException e) {
            GalleryLog.w(TAG, "updateKeyguardLikeFlag failed...");
            return -1;
        }
    }

    public static void updateItemWidthAndHeight(ContentResolver resolver, int id, int width, int height) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("width", Integer.valueOf(width));
            cv.put("height", Integer.valueOf(height));
            resolver.update(URI, cv, "_id=?", new String[]{String.valueOf(id)});
        } catch (RuntimeException e) {
            GalleryLog.w(TAG, "updateItemWidthAndHeight failed...");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void setCheckedNewVersion(ContentResolver resolver, boolean isNew) {
        String where = "key = 'checked_new_version'";
        Closeable closeable = null;
        try {
            closeable = resolver.query(KEYGUARD_UPDATE_URI, null, where, null, "_id");
            if (closeable != null) {
                ContentValues values;
                if (closeable.getCount() == 0) {
                    values = new ContentValues();
                    values.put("key", "checked_new_version");
                    values.put("value", isNew ? "1" : "0");
                    resolver.insert(KEYGUARD_UPDATE_URI, values);
                } else {
                    values = new ContentValues();
                    values.put("value", isNew ? "1" : "0");
                    resolver.update(KEYGUARD_UPDATE_URI, values, where, null);
                }
            }
            Utils.closeSilently(closeable);
        } catch (SQLiteException ex) {
            GalleryLog.w(TAG, "insertCheckedNewVersion ex = " + ex.toString());
        } catch (OperationCanceledException ex2) {
            GalleryLog.w(TAG, "insertCheckedNewVersion ex = " + ex2.toString());
        } catch (Throwable th) {
            Utils.closeSilently(closeable);
        }
    }
}
