package com.huawei.gallery.media.database;

import android.content.ContentValues;
import android.database.Cursor;

public class CloudTableConstants {
    private static final String[] CLOUD_FILE_PROJECTION = new String[]{"id", "size", "hash", "localThumbPath", "localBigThumbPath", "localRealPath", "fileName", "orientation", "albumId", "fyuseAttach", "duration", "latitude", "longitude", "fileType", "fileId", "source", "videoThumbId", "createTime", "expand", "thumbType", "uniqueId"};
    private static final String[] CLOUD_RECYCLED_FILE_PROJECTION = new String[]{"id", "size", "hash", "localThumbPath", "localBigThumbPath", "localRealPath", "fileName", "orientation", "albumId", "fyuseAttach", "duration", "latitude", "longitude", "fileType", "fileId", "source", "videoThumbId", "createTime", "expand", "thumbType", "uniqueId", "recycleFlag"};

    public static String[] getProjection() {
        return (String[]) CLOUD_FILE_PROJECTION.clone();
    }

    public static String[] getRecycledFileProjection() {
        return (String[]) CLOUD_RECYCLED_FILE_PROJECTION.clone();
    }

    public static ContentValues getContentValuesFromCloudFileCursor(Cursor cursor) {
        ContentValues values = new ContentValues();
        values.put("id", Long.valueOf(cursor.getLong(0)));
        values.put("size", Long.valueOf(cursor.getLong(1)));
        values.put("hash", cursor.getString(2));
        values.put("localThumbPath", cursor.getString(3));
        values.put("localBigThumbPath", cursor.getString(4));
        values.put("localRealPath", cursor.getString(5));
        values.put("fileName", cursor.getString(6));
        values.put("orientation", Integer.valueOf(cursor.getInt(7)));
        values.put("albumId", cursor.getString(8));
        values.put("fyuseAttach", cursor.getString(9));
        values.put("duration", Long.valueOf(cursor.getLong(10)));
        values.put("latitude", Double.valueOf(cursor.getDouble(11)));
        values.put("longitude", Double.valueOf(cursor.getDouble(12)));
        values.put("fileType", Integer.valueOf(cursor.getInt(13)));
        values.put("fileId", cursor.getString(14));
        values.put("source", cursor.getString(15));
        values.put("videoThumbId", cursor.getString(16));
        values.put("createTime", Long.valueOf(cursor.getLong(17)));
        values.put("expand", cursor.getString(18));
        values.put("thumbType", Integer.valueOf(cursor.getInt(19)));
        values.put("uniqueId", cursor.getString(20));
        return values;
    }

    public static ContentValues getContentValuesFromCloudRecycledFileCursor(Cursor cursor) {
        ContentValues values = getContentValuesFromCloudFileCursor(cursor);
        values.put("recycleFlag", cursor.getString(21));
        return values;
    }
}
