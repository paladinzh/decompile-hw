package com.huawei.gallery.recycle.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import com.android.gallery3d.common.Utils;
import com.huawei.gallery.media.LocalRecycledFile;
import com.huawei.gallery.provider.ExternalUniqueDBHelper;
import com.huawei.gallery.util.MyPrinter;
import java.io.Closeable;
import java.io.File;

public class NoGuidCloudRecycleUtils extends CloudRecycleUtils {
    private static MyPrinter LOG = new MyPrinter("Recycle_NoGuidCloudRecycleUtils");

    private static class CloudRenameToTools {
        private CloudRenameToTools() {
        }

        private static boolean renameFile(String oldPath, String newPath) {
            if (oldPath == null || newPath == null) {
                NoGuidCloudRecycleUtils.LOG.d("renameFile has null path!");
                return false;
            }
            File file = new File(oldPath);
            File newFile = new File(newPath);
            if (!file.exists() || newFile.exists()) {
                return false;
            }
            return file.renameTo(newFile);
        }

        private static String moveToBinUpdatePath(String path, String hash) {
            if (path == null) {
                return null;
            }
            return path + "@" + hash;
        }

        private static String recoverFromBinUpdatePath(String path) {
            if (path == null) {
                return null;
            }
            if (path.matches(".+@[A-Za-z0-9]+$")) {
                return path.substring(0, path.lastIndexOf("@"));
            }
            return path;
        }

        private static void moveToBinUpdateOneValues(ContentValues values, String key, String hash) {
            String oldPath = (String) values.get(key);
            String newPath = moveToBinUpdatePath(oldPath, hash);
            if (renameFile(oldPath, newPath)) {
                values.put(key, newPath);
            }
        }

        private static void recoverFromBinUpdateOneValues(ContentValues values, String key) {
            String oldPath = (String) values.get(key);
            String newPath = recoverFromBinUpdatePath(oldPath);
            if (renameFile(oldPath, newPath)) {
                values.put(key, newPath);
            }
        }

        public static void moveToBinUpdateValues(ContentValues values) {
            moveToBinUpdateOneValues(values, "localThumbPath", (String) values.get("hash"));
            moveToBinUpdateOneValues(values, "localBigThumbPath", (String) values.get("hash"));
        }

        public static void recoverFromBinUpdateValues(ContentValues values) {
            recoverFromBinUpdateOneValues(values, "localThumbPath");
            recoverFromBinUpdateOneValues(values, "localBigThumbPath");
        }
    }

    public static long moveToRecycleTable(SQLiteDatabase db, String table, ContentValues values) {
        CloudRenameToTools.moveToBinUpdateValues(values);
        if (TextUtils.isEmpty((String) values.get("uniqueId"))) {
            values.put("recycleFlag", Integer.valueOf(-1));
            values.remove("uniqueId");
            long ret = db.insertWithOnConflict("cloud_recycled_file", null, values, 0);
            LOG.d("insert info to CLOUD_RECYCLED_FILE_TABLE  ret: " + ret);
            return ret;
        }
        values.put("recycleFlag", Integer.valueOf(1));
        SQLiteDatabase sQLiteDatabase = db;
        ContentValues contentValues = values;
        ret = (long) sQLiteDatabase.updateWithOnConflict("cloud_recycled_file", contentValues, "uniqueId=?", new String[]{uniqueId}, 0);
        LOG.d("update info to CLOUD_RECYCLED_FILE_TABLE  ret: " + ret);
        return ret;
    }

    public static int updateRecycleTableByRecover(SQLiteDatabase db, String cloudId, ContentValues values) {
        int recycleFlag = readRecycleFlag(db, cloudId);
        int ret;
        if (recycleFlag == -1) {
            ret = db.delete("cloud_recycled_file", "id=?", new String[]{cloudId});
            LOG.d("delete info form CLOUD_RECYCLED_FILE_TABLE  result: " + ret);
            CloudRecycleUtils.recoverCloudAlbumDeleteFlag(db, values.getAsString("albumId"));
            return ret;
        } else if (recycleFlag == 1) {
            ContentValues value = new ContentValues();
            value.put("recycleFlag", Integer.valueOf(2));
            ret = db.update("cloud_recycled_file", value, "id=?", new String[]{cloudId});
            LOG.d("update CLOUD_RECYCLED_FILE_TABLE with CLOUD_RECOVER_WAIT_UPLOAD  result: " + ret);
            return ret;
        } else {
            LOG.w("Recover a file with recycleFlag: " + recycleFlag + " cloudId: " + cloudId);
            return 0;
        }
    }

    public static void updateRecycleTableByDelete(SQLiteDatabase db, String cloudId) {
        int recycleFlag = readRecycleFlag(db, cloudId);
        LOG.d("deleteFromRecycleBin cloudId:" + cloudId + ", recycleFlag:" + recycleFlag);
        ContentValues values = new ContentValues();
        if (recycleFlag == 1 || recycleFlag == -1) {
            values.put("recycleFlag", Integer.valueOf(-2));
            LOG.d("update CLOUD_RECYCLED_FILE_TABLE with CLOUD_DELETE_WAIT_UPLOAD  result: " + db.update("cloud_recycled_file", values, "id= ?", new String[]{cloudId}));
            return;
        }
        LOG.w("Delete a file with recycleFlag: " + recycleFlag + " cloudId: " + cloudId);
    }

    public static void recoverFromBinUpdateValues(ContentValues values) {
        CloudRenameToTools.recoverFromBinUpdateValues(values);
    }

    private static int readRecycleFlag(SQLiteDatabase db, String cloudMediaId) {
        try {
            SQLiteDatabase sQLiteDatabase = db;
            Closeable cursor = sQLiteDatabase.query("cloud_recycled_file", new String[]{"recycleFlag"}, "id=?", new String[]{cloudMediaId}, null, null, null);
            if (cursor == null || !cursor.moveToNext()) {
                Utils.closeSilently(cursor);
                LOG.d("read recycleFlag fail, return default value");
                return 1;
            }
            int i = cursor.getInt(0);
            Utils.closeSilently(cursor);
            return i;
        } catch (SQLiteException e) {
            throw new SQLiteException("read recycleFlag error " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int updateCloudRecycleFileTable(Context context, SQLiteDatabase db, ContentValues initialValues, String userWhere, String[] whereArgs) {
        int count = db.update("cloud_recycled_file", initialValues, userWhere, whereArgs);
        String uniqueId = (String) initialValues.get("uniqueId");
        if (TextUtils.isEmpty(uniqueId)) {
            LOG.d("update cloud recycle table with empty unique id");
            return count;
        }
        int galleryId = -1;
        String localRealPath = null;
        try {
            SQLiteDatabase sQLiteDatabase = db;
            Closeable cursor = sQLiteDatabase.query("cloud_recycled_file", new String[]{"galleryId", "localRealPath"}, "uniqueId=?", new String[]{uniqueId}, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                galleryId = cursor.getInt(0);
                localRealPath = cursor.getString(1);
            }
            Utils.closeSilently(cursor);
        } catch (Exception e) {
            LOG.d("updateCloudRecycleFileTable error: " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        if (galleryId == -1) {
            LOG.d("cloud recycle table with unique id:" + uniqueId + " not found");
            return count;
        }
        if (!TextUtils.isEmpty(localRealPath)) {
            updateLocalRecycleFileUniqueId(db, galleryId, uniqueId, context.getContentResolver());
        }
        return count;
    }

    private static void updateLocalRecycleFileUniqueId(SQLiteDatabase db, int galleryId, String uniqueId, ContentResolver contentResolver) {
        ContentValues values = new ContentValues();
        values.put("uniqueId", uniqueId);
        LOG.d("update local recycle table galleryId " + galleryId + " uniqueId " + uniqueId);
        LocalRecycledFile.update(db, contentResolver, galleryId, values);
        String data = getRecycleFilePathByID(contentResolver, "galleryId", galleryId);
        if (data != null) {
            ExternalUniqueDBHelper.insertUniqueID(contentResolver, data.substring(data.lastIndexOf("/") + 1, data.length()), uniqueId);
        }
    }

    public static String getRecycleFilePathByID(ContentResolver resolver, String whereColumn, int id) {
        String path = null;
        try {
            Closeable cursor = resolver.query(LocalRecycledFile.URI, new String[]{"_data"}, whereColumn + " = ? ", new String[]{Integer.toString(id)}, null);
            if (cursor != null && cursor.moveToNext()) {
                path = cursor.getString(0);
            }
            Utils.closeSilently(cursor);
            return path;
        } catch (Exception e) {
            LOG.d("getRecycleFilePathByID error: " + e.getMessage());
            Utils.closeSilently(null);
            return null;
        } catch (Throwable th) {
            Utils.closeSilently(null);
            return null;
        }
    }
}
