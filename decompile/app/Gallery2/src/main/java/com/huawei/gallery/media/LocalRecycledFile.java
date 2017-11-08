package com.huawei.gallery.media;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.media.database.CloudTableOperateHelper;
import com.huawei.gallery.media.database.MergedMedia;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.provider.ExternalUniqueDBHelper;
import com.huawei.gallery.recycle.utils.NoGuidCloudRecycleUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import com.huawei.gallery.storage.GalleryStorage;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class LocalRecycledFile {
    public static final Uri URI = MergedMedia.URI.buildUpon().appendPath("local_recycled_file").build();

    public static void insert(SQLiteDatabase db, ContentResolver resolver, int galleryId, ContentValues values, Bundle data) {
        int localMediaId = ((Integer) values.get("_id")).intValue();
        String recycleFileName = data.getString("recycle_file_name", null);
        long recycleTime = data.getLong("recycle_time", -1);
        String title = data.getString("title", (String) values.get("title"));
        values.put("galleryId", Integer.valueOf(galleryId));
        values.put("recycledTime", Long.valueOf(recycleTime));
        values.put("title", title);
        String sourcePath = values.get("_data").toString();
        values.put("sourcePath", sourcePath);
        values.put("_data", RecycleUtils.getGalleryRecycleBinDir(sourcePath) + recycleFileName);
        if (4 == ((Integer) values.get("media_type")).intValue()) {
            values.remove(String.format("strftime('%%Y%%m', %s / 1000, 'unixepoch') AS normalized_date", new Object[]{"datetaken"}));
        }
        replaceMediaType(values);
        GalleryLog.d("local_recycled_file", "insert localMediaId:" + localMediaId + ", recycleFileName path: " + recycleFileName);
        if (db == null) {
            resolver.insert(URI, values);
            return;
        }
        db.insert("local_recycled_file", null, values);
        resolver.notifyChange(URI, null);
    }

    private static void replaceMediaType(ContentValues values) {
        int mediaObjectType = ((Integer) values.get("media_type")).intValue();
        if (mediaObjectType == 2) {
            values.put("media_type", Integer.valueOf(1));
        } else if (mediaObjectType == 4) {
            values.put("media_type", Integer.valueOf(3));
        }
    }

    public static void delete(SQLiteDatabase db, ContentResolver resolver, int id) {
        if (RecycleUtils.supportRecycle() && !PhotoShareUtils.isGUIDSupport()) {
            String data = NoGuidCloudRecycleUtils.getRecycleFilePathByID(resolver, "_id", id);
            if (data != null) {
                ExternalUniqueDBHelper.deleteUniqueID(resolver, data.substring(data.lastIndexOf("/") + 1, data.length()));
            }
        }
        db.delete("local_recycled_file", "_id = ? ", new String[]{Integer.toString(id)});
        resolver.notifyChange(URI, null);
    }

    public static String querySourcePath(SQLiteDatabase db, ContentResolver resolver, int id) {
        Closeable closeable = null;
        try {
            SQLiteDatabase sQLiteDatabase = db;
            closeable = sQLiteDatabase.query("local_recycled_file", new String[]{"sourcePath"}, "_id = ? ", new String[]{Integer.toString(id)}, null, null, null);
            if (closeable == null || !closeable.moveToNext()) {
                Utils.closeSilently(closeable);
                return "";
            }
            resolver.notifyChange(URI, null);
            String string = closeable.getString(0);
            return string;
        } catch (SQLiteException e) {
            throw new SQLiteException("query SourcePath from local recycle table " + e.getMessage());
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public static void update(SQLiteDatabase db, ContentResolver resolver, int galleryId, ContentValues values) {
        try {
            db.update("local_recycled_file", values, "galleryId = ? ", new String[]{Integer.toString(galleryId)});
            resolver.notifyChange(URI, null);
        } catch (SQLiteException e) {
            throw new SQLiteException("update local recycle table failed " + e.getMessage());
        }
    }

    public static void selfRecoverInsert(ContentResolver resolver, ArrayList<LinkedHashMap> infos, GalleryStorage storage) {
        List<ContentValues> insertBulk = new ArrayList();
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            String uniqueId;
            ContentValues values = new ContentValues();
            int orientation = ((Integer) ((LinkedHashMap) infos.get(i)).get(Integer.valueOf(1))).intValue();
            long recycleTime = ((Long) ((LinkedHashMap) infos.get(i)).get(Integer.valueOf(3))).longValue();
            String title = (String) ((LinkedHashMap) infos.get(i)).get(Integer.valueOf(4));
            int mediaType = ((Integer) ((LinkedHashMap) infos.get(i)).get(Integer.valueOf(0))).intValue();
            String surfix = (String) ((LinkedHashMap) infos.get(i)).get(Integer.valueOf(5));
            if (".gallery".equalsIgnoreCase(surfix)) {
                GalleryLog.d("Recycle_local_recycled_file", "this file has no mediatype suffix. title = " + title);
                surfix = "";
            }
            int localMediaId = ((Integer) ((LinkedHashMap) infos.get(i)).get(Integer.valueOf(2))).intValue();
            String mimeType = (String) ((LinkedHashMap) infos.get(i)).get(Integer.valueOf(6));
            String fileName = RecycleUtils.getRecycleNameFromMap((LinkedHashMap) infos.get(i));
            values.put("orientation", Integer.valueOf(orientation));
            values.put("recycledTime", Long.valueOf(recycleTime));
            values.put("title", title);
            values.put("_display_name", title + surfix);
            values.put("_data", storage.getPath() + "/Pictures/.Gallery2/recycle/" + fileName);
            values.put("media_type", Integer.valueOf(mediaType));
            values.put("sourcePath", storage.getPath() + "/Pictures/Recover/" + title + surfix);
            values.put("_id", Integer.valueOf(localMediaId));
            values.put("mime_type", mimeType);
            values.put("is_hw_burst", Integer.valueOf(CloudTableOperateHelper.matchBurstCover(new StringBuilder().append(title).append(surfix).toString()) ? 1 : 0));
            if (PhotoShareUtils.isGUIDSupport()) {
                uniqueId = (String) ((LinkedHashMap) infos.get(i)).get(Integer.valueOf(7));
            } else {
                uniqueId = ExternalUniqueDBHelper.getUniqueID(resolver, fileName);
            }
            if (RecycleUtils.isInvalidUniqueId(uniqueId)) {
                GalleryLog.w("Recycle_local_recycled_file", fileName + " not find uniqueId");
            } else {
                values.put("uniqueId", uniqueId);
            }
            insertBulk.add(values);
        }
        ContentValues[] values2 = new ContentValues[insertBulk.size()];
        insertBulk.toArray(values2);
        resolver.bulkInsert(URI, values2);
        GalleryLog.d("Recycle_local_recycled_file", "auto recover " + values2.length + " files to Recycle");
    }

    private static void selfRecoverDeleteReal(ContentResolver resolver, List<String> recycledTimesAndNamesInTable) {
        int size = recycledTimesAndNamesInTable.size();
        if (size != 0) {
            String[] recycleTimeSelectArgs = new String[size];
            String[] recycleNameSelectArgs = new String[size];
            for (int i = 0; i < size; i++) {
                String singleInfo = (String) recycledTimesAndNamesInTable.get(i);
                int separatorIndex = singleInfo.indexOf("|");
                String singleRecycleTime = singleInfo.substring(0, separatorIndex);
                String singleRecycleName = singleInfo.substring(separatorIndex + 1);
                recycleTimeSelectArgs[i] = singleRecycleTime;
                recycleNameSelectArgs[i] = singleRecycleName;
            }
            resolver.delete(URI, "recycledTime IN (" + TextUtils.join(",", Collections.nCopies(size, "?")) + ") AND " + "title" + " IN (" + TextUtils.join(",", Collections.nCopies(size, "?")) + ")", (String[]) GalleryUtils.arraysCombine(recycleTimeSelectArgs, recycleNameSelectArgs));
        }
    }

    public static void selfRecoverDelete(ContentResolver resolver, ArrayList<String> recycledTimesAndNamesInTable) {
        int size = recycledTimesAndNamesInTable.size();
        if (size != 0) {
            int i = 0;
            while (i < size) {
                selfRecoverDeleteReal(resolver, recycledTimesAndNamesInTable.subList(i, i + 250 > size ? size : i + 250));
                i += 250;
            }
            GalleryLog.d("Recycle_local_recycled_file", "auto recover delete " + size + " files from Recycle");
        }
    }

    public static ArrayList<String> getFileInfosInTable(ContentResolver resolver) {
        ArrayList<String> infosInTable = new ArrayList();
        Closeable closeable = null;
        try {
            closeable = resolver.query(URI, new String[]{"recycledTime", "title"}, null, null, null);
            if (closeable == null) {
                Utils.closeSilently(closeable);
                return infosInTable;
            }
            while (closeable.moveToNext()) {
                infosInTable.add(String.valueOf(closeable.getLong(0)) + "|" + closeable.getString(1));
            }
            Utils.closeSilently(closeable);
            return infosInTable;
        } catch (Exception e) {
            GalleryLog.d("local_recycled_file", "get galleryIds failed. " + e.getMessage());
            Utils.closeSilently(closeable);
            return infosInTable;
        } catch (Throwable th) {
            Utils.closeSilently(closeable);
            return infosInTable;
        }
    }
}
