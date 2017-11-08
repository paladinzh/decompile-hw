package com.huawei.gallery.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import com.android.gallery3d.util.GalleryLog;

public class StorageMonitorProvider extends ContentProvider {
    public static final Uri BASE_URI = Uri.parse("content://com.huawei.gallery.provider2/");
    private StorageMonitorDBHelper mDataBase;

    public boolean onCreate() {
        GalleryLog.d("StorageMonitorProvider", "StorageMonitorProvider onCreate");
        this.mDataBase = new StorageMonitorDBHelper(getContext());
        return true;
    }

    public int delete(Uri uri, String userWhere, String[] whereArgs) {
        GalleryLog.d("StorageMonitorProvider", "delete: " + userWhere);
        int count = this.mDataBase.delete("media_file", userWhere, whereArgs);
        if (count > 0) {
            getContext().getContentResolver().notifyChange(BASE_URI, null);
        }
        return count;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues initialValues) {
        GalleryLog.d("StorageMonitorProvider", "insert: " + uri);
        checkIfNeedDelete();
        SQLiteDatabase db = this.mDataBase.getWritableDatabase();
        if (db == null) {
            return null;
        }
        Uri newUri = insertIntoTable(uri, db, "media_file", "_id", initialValues);
        if (newUri != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        GalleryLog.d("StorageMonitorProvider", "insert: " + newUri);
        return newUri;
    }

    private Uri insertIntoTable(Uri uri, SQLiteDatabase db, String table, String nullColumnHack, ContentValues initialValues) {
        long rowId = db.insert(table, nullColumnHack, initialValues);
        if (rowId > 0) {
            return ContentUris.withAppendedId(uri, rowId);
        }
        return uri;
    }

    public Cursor query(Uri uri, String[] projectionIn, String selection, String[] selectionArgs, String sort) {
        GalleryLog.d("StorageMonitorProvider", "query: " + selection);
        SQLiteDatabase db = this.mDataBase.getReadableDatabase();
        if (db == null) {
            return null;
        }
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables("media_file");
        Cursor c = qb.query(db, projectionIn, selection, selectionArgs, null, null, sort, null);
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;
    }

    public int update(Uri uri, ContentValues initialValues, String userWhere, String[] whereArgs) {
        GalleryLog.d("StorageMonitorProvider", "update:" + uri);
        SQLiteDatabase db = this.mDataBase.getWritableDatabase();
        int count = db.update("media_file", initialValues, userWhere, whereArgs);
        if (count > 0 && !db.inTransaction()) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    private void checkIfNeedDelete() {
        if (fetchRecordCount() >= 5000) {
            deleteFirstRecord();
        }
    }

    private long fetchRecordCount() {
        long count = 0;
        SQLiteDatabase db = this.mDataBase.getReadableDatabase();
        if (db == null) {
            return 0;
        }
        try {
            count = db.compileStatement("SELECT COUNT(*) FROM media_file").simpleQueryForLong();
        } catch (Exception e) {
            GalleryLog.d("StorageMonitorProvider", "fetchRecordCount got exception: " + e);
        }
        GalleryLog.d("StorageMonitorProvider", "fetchRecordCount: " + count);
        return count;
    }

    private void deleteFirstRecord() {
        SQLiteDatabase db = this.mDataBase.getWritableDatabase();
        if (db != null) {
            GalleryLog.d("StorageMonitorProvider", "deleteFirstRecord");
            try {
                db.execSQL("delete from media_file where _id in (select _id from media_file order by creat_time LIMIT 1000)");
            } catch (Exception e) {
                GalleryLog.d("StorageMonitorProvider", "deleteFirstRecord got exception: " + e);
            }
            GalleryLog.d("StorageMonitorProvider", "deleteFirstRecord end");
        }
    }
}
