package com.huawei.gallery.media.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.GalleryMediaItem;
import com.huawei.gallery.photoshare.utils.PhotoShareConstants;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.CloudRecycleUtils;
import com.huawei.gallery.util.MyPrinter;
import java.io.Closeable;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

public class CloudRecycleTableOperateHelper {
    private static MyPrinter LOG = new MyPrinter("Recycle_CloudRecycleTableOperateHelper");
    private static CloudTableOperateHelper sCloudTableOperateHelper;

    private static synchronized CloudTableOperateHelper getCloudTableOperateHelper(Context context) {
        CloudTableOperateHelper cloudTableOperateHelper;
        synchronized (CloudRecycleTableOperateHelper.class) {
            if (sCloudTableOperateHelper == null) {
                sCloudTableOperateHelper = new CloudTableOperateHelper(context.getContentResolver());
            }
            cloudTableOperateHelper = sCloudTableOperateHelper;
        }
        return cloudTableOperateHelper;
    }

    public static void moveToRecycleBin(SQLiteDatabase db, long galleryMediaId, long cloudId, Context context) {
        String cloudMediaId = String.valueOf(cloudId);
        LOG.d("moveToRecycleBin cloudId:" + cloudId + " galleryId:" + galleryMediaId);
        Closeable closeable = null;
        try {
            SQLiteDatabase sQLiteDatabase = db;
            closeable = sQLiteDatabase.query("cloud_file", CloudTableConstants.getProjection(), "id=?", new String[]{cloudMediaId}, null, null, null);
            if (closeable != null && closeable.moveToNext()) {
                ContentValues values = CloudTableConstants.getContentValuesFromCloudFileCursor(closeable);
                values = updateRecycleExtraInfo(values, galleryMediaId, (String) values.get("fileName"));
                CloudRecycleUtils.updateRecycleFlag(values, -1);
                CloudRecycleUtils.moveToRecycleTable(db, "cloud_recycled_file", values);
                SQLiteDatabase sQLiteDatabase2 = db;
                LOG.d("delete info form cloud file,  result: " + sQLiteDatabase2.delete("cloud_file", "id=?", new String[]{cloudMediaId}));
            }
            if (!(PhotoShareUtils.getServer() == null || TextUtils.isEmpty(cloudMediaId))) {
                final Context context2 = context;
                final long j = cloudId;
                new Thread() {
                    public void run() {
                        GalleryMediaItem.detachRelativePath(context2, (int) j);
                    }
                }.start();
            }
            Utils.closeSilently(closeable);
        } catch (SQLiteException e) {
            throw new SQLiteException("moveToRecycleBin error: " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(closeable);
        }
    }

    public static void clearCloudBurstPhoto(int bucketId, String burstId, ContentResolver resolver) {
        LOG.d("clearBurstPhotoToRecycleBin bucketId:" + bucketId + "_" + burstId);
        String cloudAlbumId = PhotoShareUtils.getCloudAlbumIdByBucketId(String.valueOf(bucketId));
        String whereClause = "albumId = ? AND fileName LIKE '%'||?||'_BURST%.JPG' ";
        try {
            ContentResolver contentResolver = resolver;
            Closeable cursor = contentResolver.query(PhotoShareConstants.CLOUD_FILE_TABLE_URI, CloudTableConstants.getProjection(), "albumId = ? AND fileName LIKE '%'||?||'_BURST%.JPG' ", new String[]{cloudAlbumId, burstId}, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    ContentValues values = CloudTableConstants.getContentValuesFromCloudFileCursor(cursor);
                    values = updateRecycleExtraInfo(values, 0, (String) values.get("fileName"));
                    CloudRecycleUtils.updateRecycleFlag(values, -3);
                    resolver.insert(CloudRecycleUtils.CLOUD_RECYCLED_FILE_TABLE_URI, values);
                    LOG.d("insert info to cloud recycle with hard delete");
                    LOG.d("delete info form cloud file,  result: " + resolver.delete(PhotoShareConstants.CLOUD_FILE_TABLE_URI, "albumId = ? AND fileName LIKE '%'||?||'_BURST%.JPG' ", new String[]{cloudAlbumId, burstId}));
                }
            }
            Utils.closeSilently(cursor);
        } catch (SQLiteException e) {
            throw new SQLiteException("clearBurstPhotoToRecycleBin error: " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
    }

    public static void moveToRecycleBin(SQLiteDatabase db, int localId, Context context) {
        SQLiteException e;
        Throwable th;
        String localMediaId = String.valueOf(localId);
        LOG.d("moveToRecycleBin localMediaId:" + localMediaId);
        Closeable closeable = null;
        long cloudId;
        try {
            long galleryMediaId;
            SQLiteDatabase sQLiteDatabase = db;
            closeable = sQLiteDatabase.query("gallery_media", new String[]{"cloud_media_id", "_id"}, "local_media_id=?", new String[]{localMediaId}, null, null, null);
            if (closeable == null) {
                galleryMediaId = -1;
                cloudId = -1;
            } else if (closeable.moveToNext()) {
                cloudId = closeable.getLong(0);
                try {
                    galleryMediaId = closeable.getLong(1);
                } catch (SQLiteException e2) {
                    e = e2;
                    try {
                        throw new SQLiteException("moveToRecycleBin error: " + e.getMessage());
                    } catch (Throwable th2) {
                        th = th2;
                        Utils.closeSilently(closeable);
                        throw th;
                    }
                }
            } else {
                galleryMediaId = -1;
                cloudId = -1;
            }
            Utils.closeSilently(closeable);
            if (cloudId == -1) {
                LOG.d("no cloud data with local id " + localMediaId);
            } else {
                moveToRecycleBin(db, galleryMediaId, cloudId, context);
            }
        } catch (SQLiteException e3) {
            e = e3;
            cloudId = -1;
            throw new SQLiteException("moveToRecycleBin error: " + e.getMessage());
        } catch (Throwable th3) {
            th = th3;
            cloudId = -1;
            Utils.closeSilently(closeable);
            throw th;
        }
    }

    public static void recoverFromRecycleBin(SQLiteDatabase db, int cloudId, String sourcePath, Context context) {
        String cloudMediaId = String.valueOf(cloudId);
        LOG.d("recoverFromRecycleBin cloudId:" + cloudId);
        try {
            SQLiteDatabase sQLiteDatabase = db;
            Closeable cursor = sQLiteDatabase.query("cloud_recycled_file", CloudTableConstants.getRecycledFileProjection(), "id=?", new String[]{cloudMediaId}, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                ContentValues values = CloudTableConstants.getContentValuesFromCloudRecycledFileCursor(cursor);
                if (values.getAsInteger("recycleFlag").intValue() == 1) {
                    CloudRecycleUtils.recoverOriginalFileName(values);
                }
                values.remove("id");
                values.remove("recycleFlag");
                if (!TextUtils.isEmpty(sourcePath)) {
                    values.put("localRealPath", sourcePath);
                }
                CloudRecycleUtils.recoverFromBinUpdateValues(values);
                CloudRecycleUtils.updateRecycleTableByRecover(db, cloudMediaId, values);
                getCloudTableOperateHelper(context).insertCloudFileTable(db, values, context, false);
            }
            Utils.closeSilently(cursor);
        } catch (SQLiteException e) {
            throw new SQLiteException("recoverFromRecycleBin error: " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
    }

    public static void deleteFromRecycleBin(SQLiteDatabase db, int cloudId) {
        LOG.d("deleteFromRecycleBin cloudId:" + cloudId);
        CloudRecycleUtils.updateRecycleTableByDelete(db, String.valueOf(cloudId));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static HashMap<String, String> queryGalleryRecycleInfo(SQLiteDatabase db, String where, String[] whereArgs, int offset) {
        String ORDER_BY = "recycledTime DESC";
        String limit = offset + "," + 500;
        HashMap<String, String> uniqueIdDataMap = new HashMap();
        try {
            SQLiteDatabase sQLiteDatabase = db;
            Closeable cursor = sQLiteDatabase.query("gallery_recycled_file", new String[]{"uniqueId", "_data"}, where, whereArgs, null, null, ORDER_BY, limit);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    uniqueIdDataMap.put(cursor.getString(0), cursor.getString(1));
                }
            }
            Utils.closeSilently(cursor);
        } catch (SQLiteException e) {
            LOG.d("error query to be deleted in cloud recycle table: " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        return uniqueIdDataMap;
    }

    public static int deleteCloudRecycleFileTable(SQLiteDatabase db, String table, String where, String[] whereArgs) {
        HashMap<String, String> allRecycleInfo = new HashMap();
        int offset = 0;
        boolean finish = false;
        do {
            HashMap<String, String> recycleInfo = queryGalleryRecycleInfo(db, where, whereArgs, offset);
            allRecycleInfo.putAll(recycleInfo);
            offset += recycleInfo.size();
            if (recycleInfo.size() < 500) {
                finish = true;
                continue;
            }
        } while (!finish);
        int count = db.delete(table, where, whereArgs);
        if (allRecycleInfo.size() > 0) {
            Object[] allUniqueId = allRecycleInfo.keySet().toArray();
            String[] partUniqueId = new String[allUniqueId.length];
            int index = 0;
            while (index < allUniqueId.length) {
                for (int i = 0; i < 500 && index < allUniqueId.length; i++) {
                    partUniqueId[i] = (String) allUniqueId[index];
                    index++;
                }
                db.delete("local_recycled_file", "uniqueId IN (" + TextUtils.join(",", Collections.nCopies(partUniqueId.length, "?")) + " )", partUniqueId);
            }
            for (Entry entry : allRecycleInfo.entrySet()) {
                String path = (String) entry.getValue();
                if (!TextUtils.isEmpty(path)) {
                    File file = new File(path);
                    if (!file.delete()) {
                        LOG.d("delete file failed: " + file.getName());
                    }
                }
            }
        }
        return count;
    }

    private static ContentValues updateRecycleExtraInfo(ContentValues values, long galleryId, String fileName) {
        values.put("recycledTime", Long.valueOf(System.currentTimeMillis()));
        values.put("galleryId", Long.valueOf(galleryId));
        values.put("sourceFileName", fileName);
        values.remove("id");
        return values;
    }
}
