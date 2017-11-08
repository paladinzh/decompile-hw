package com.huawei.gallery.recycle.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.android.gallery3d.common.Utils;
import com.huawei.gallery.util.MyPrinter;
import java.io.Closeable;

public class GuidCloudRecycleUtils extends CloudRecycleUtils {
    private static MyPrinter LOG = new MyPrinter("Recycle_GuidCloudRecycleUtils");

    public static long moveToRecycleTable(SQLiteDatabase db, String table, ContentValues values) {
        long ret = db.insertWithOnConflict(table, null, values, 5);
        LOG.d("insert info to cloud recycle, ret: " + ret);
        return ret;
    }

    public static int updateRecycleTableByRecover(SQLiteDatabase db, String cloudId, ContentValues values) {
        ContentValues value = new ContentValues();
        value.put("recycleFlag", Integer.valueOf(2));
        int rowCount = db.update("cloud_recycled_file", value, "id=? AND (recycleFlag = ? OR recycleFlag = ?)", new String[]{cloudId, FLAG_RECYCLE_UPLOADED_STRING, FLAG_RECYCLE_WAIT_UPLOAD_STRING});
        if (rowCount > 0) {
            LOG.d("update cloud recycle with CLOUD_RECOVER_WAIT_UPLOAD  result: " + rowCount);
            CloudRecycleUtils.recoverCloudAlbumDeleteFlag(db, values.getAsString("albumId"));
        }
        return rowCount;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void updateRecycleTableByDelete(SQLiteDatabase db, String cloudId) {
        int recycleFlag = 0;
        try {
            SQLiteDatabase sQLiteDatabase = db;
            Closeable cursor = sQLiteDatabase.query("cloud_recycled_file", new String[]{"recycleFlag"}, "id=?", new String[]{cloudId}, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                recycleFlag = cursor.getInt(0);
            }
            Utils.closeSilently(cursor);
        } catch (Exception e) {
            LOG.d("updateRecycleTableByDelete error: " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        ContentValues values = new ContentValues();
        if (recycleFlag == -1) {
            values.put("recycleFlag", Integer.valueOf(-3));
        } else {
            values.put("recycleFlag", Integer.valueOf(-2));
        }
        LOG.d("update cloud recycle with CLOUD_DELETE_WAIT_UPLOAD  result: " + db.update("cloud_recycled_file", values, "id= ? AND (recycleFlag = ? OR recycleFlag = ?)", new String[]{cloudId, FLAG_RECYCLE_UPLOADED_STRING, FLAG_RECYCLE_WAIT_UPLOAD_STRING}));
    }

    public static int updateCloudRecycleFileTable(Context context, SQLiteDatabase db, ContentValues initialValues, String userWhere, String[] whereArgs) {
        return db.update("cloud_recycled_file", initialValues, userWhere, whereArgs);
    }
}
