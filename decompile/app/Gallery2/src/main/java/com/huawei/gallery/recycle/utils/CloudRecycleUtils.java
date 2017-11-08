package com.huawei.gallery.recycle.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.text.TextUtils;
import com.android.gallery3d.common.Utils;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.provider.GalleryProvider;
import com.huawei.gallery.util.MyPrinter;
import java.io.Closeable;
import java.util.Map.Entry;

public class CloudRecycleUtils {
    public static final Uri CLOUD_RECYCLED_FILE_TABLE_URI = Uri.withAppendedPath(GalleryProvider.BASE_URI, "cloud_recycled_file");
    protected static final String FLAG_HARD_DELETE_WAIT_UPLOAD_STRING = String.valueOf(-3);
    protected static final String FLAG_RECYCLE_UPLOADED_STRING = String.valueOf(1);
    protected static final String FLAG_RECYCLE_WAIT_UPLOAD_STRING = String.valueOf(-1);
    private static MyPrinter LOG = new MyPrinter("Recycle_CloudRecycleUtils");

    public static long moveToRecycleTable(SQLiteDatabase db, String table, ContentValues values) {
        if (PhotoShareUtils.isGUIDSupport()) {
            return GuidCloudRecycleUtils.moveToRecycleTable(db, table, values);
        }
        return NoGuidCloudRecycleUtils.moveToRecycleTable(db, table, values);
    }

    public static int updateRecycleTableByRecover(SQLiteDatabase db, String table, ContentValues values) {
        if (PhotoShareUtils.isGUIDSupport()) {
            return GuidCloudRecycleUtils.updateRecycleTableByRecover(db, table, values);
        }
        return NoGuidCloudRecycleUtils.updateRecycleTableByRecover(db, table, values);
    }

    public static void updateRecycleTableByDelete(SQLiteDatabase db, String cloudId) {
        if (PhotoShareUtils.isGUIDSupport()) {
            GuidCloudRecycleUtils.updateRecycleTableByDelete(db, cloudId);
        } else {
            NoGuidCloudRecycleUtils.updateRecycleTableByDelete(db, cloudId);
        }
    }

    public static void recoverFromBinUpdateValues(ContentValues values) {
        if (!PhotoShareUtils.isGUIDSupport()) {
            NoGuidCloudRecycleUtils.recoverFromBinUpdateValues(values);
        }
    }

    public static void updateRecycleFlag(ContentValues values, int flag) {
        if (PhotoShareUtils.isGUIDSupport()) {
            values.put("recycleFlag", Integer.valueOf(flag));
        }
    }

    public static int updateCloudRecycleFileTable(Context context, SQLiteDatabase db, ContentValues initialValues, String userWhere, String[] whereArgs) {
        if (PhotoShareUtils.isGUIDSupport()) {
            return GuidCloudRecycleUtils.updateCloudRecycleFileTable(context, db, initialValues, userWhere, whereArgs);
        }
        return NoGuidCloudRecycleUtils.updateCloudRecycleFileTable(context, db, initialValues, userWhere, whereArgs);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean queryInsertPermission(SQLiteDatabase db, String uniqueId) {
        if (!PhotoShareUtils.isGUIDSupport()) {
            return true;
        }
        if (TextUtils.isEmpty(uniqueId)) {
            LOG.w("insert no uniqueId value on guid solution");
            return true;
        }
        try {
            Closeable cursor = db.query("cloud_recycled_file", new String[]{"COUNT(1)"}, "uniqueId=? AND (recycleFlag=? OR recycleFlag=?)", new String[]{uniqueId, FLAG_RECYCLE_WAIT_UPLOAD_STRING, FLAG_HARD_DELETE_WAIT_UPLOAD_STRING}, null, null, null);
            if (cursor == null || !cursor.moveToNext()) {
                Utils.closeSilently(cursor);
                return true;
            }
            boolean z = cursor.getInt(0) == 0;
            Utils.closeSilently(cursor);
            return z;
        } catch (SQLiteException e) {
            LOG.w("query cloud recycle with unique id and recycle flag error " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
    }

    protected static void recoverCloudAlbumDeleteFlag(SQLiteDatabase db, String albumId) {
        String DELETE_FLAG = "1";
        ContentValues values = new ContentValues();
        values.put("deleteFlag", Integer.valueOf(0));
        if (db.update("cloud_album", values, "albumId=? AND deleteFlag=?", new String[]{albumId, "1"}) > 0) {
            LOG.d("recover cloud album delete flag for album " + albumId);
        }
    }

    public static void recoverOriginalFileName(ContentValues values) {
        String fileName = values.getAsString("fileName");
        if (!TextUtils.isEmpty(fileName) && fileName.length() >= 4) {
            values.put("fileName", fileName.substring(4));
        }
    }

    public static String queryBucketIdByCloudAlbumId(String cloudAlbumId) {
        if (!RecycleUtils.supportRecycle() || TextUtils.isEmpty(cloudAlbumId)) {
            return "";
        }
        for (Entry<String, String> bucketCloudAlbumId : RecycleUtils.getAllPreferenceValue(RecycleUtils.CLOUD_BUCKET_ALBUM_ID).entrySet()) {
            if (cloudAlbumId.equalsIgnoreCase((String) bucketCloudAlbumId.getValue())) {
                return (String) bucketCloudAlbumId.getKey();
            }
        }
        return "";
    }
}
