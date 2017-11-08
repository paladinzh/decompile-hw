package com.huawei.gallery.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.media.database.MergedMedia;
import com.huawei.gallery.photoshare.utils.PhotoShareNoHwAccount;
import java.util.Locale;

public class GalleryDBHelper extends SQLiteOpenHelper {
    public final Context mContext;

    public GalleryDBHelper(Context context) {
        this(context, "gallery.db");
    }

    public GalleryDBHelper(Context context, String dbName) {
        super(context, dbName, null, getDatabaseVersion(context));
        this.mContext = context;
        setWriteAheadLoggingEnabled(true);
    }

    public void onCreate(SQLiteDatabase db) {
        TraceController.traceBegin("GalleryDBHelper.onCreate.updateDatabase");
        updateDatabase(this.mContext, db, 0, getDatabaseVersion(this.mContext));
        TraceController.traceEnd();
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        TraceController.traceBegin("GalleryDBHelper.onUpgrade.updateDatabase");
        updateDatabase(this.mContext, db, oldVersion, newVersion);
        TraceController.traceEnd();
    }

    public long insert(String tableName, ContentValues values) {
        return getWritableDatabase().insert(tableName, null, values);
    }

    public int update(String tableName, ContentValues values, String whereClause, String[] whereArgs) {
        return getWritableDatabase().update(tableName, values, whereClause, whereArgs);
    }

    public Cursor query(String tableName, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return getReadableDatabase().query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public int delete(String tableName, String whereClause, String[] whereArgs) {
        return getWritableDatabase().delete(tableName, whereClause, whereArgs);
    }

    private static void updateDatabase(Context context, SQLiteDatabase db, int fromVersion, int toVersion) {
        int dbversion = getDatabaseVersion(context);
        if (dbversion >= 0 && fromVersion >= 0 && toVersion >= 0) {
            if (toVersion != dbversion) {
                GalleryLog.e("GalleryDBHelper", "Illegal update request. Got " + toVersion + ", expected " + dbversion);
            } else if (fromVersion > toVersion) {
                GalleryLog.e("GalleryDBHelper", "Illegal update request: can't downgrade from " + fromVersion + " to " + toVersion + ". Did you forget to wipe data?");
            } else {
                if (fromVersion < 1) {
                    db.execSQL("CREATE TABLE IF NOT EXISTS media_file (_id INTEGER PRIMARY KEY AUTOINCREMENT,_data TEXT UNIQUE NOT NULL,is_favorite INTEGER,volume_id INTEGER);");
                    db.execSQL("CREATE TABLE IF NOT EXISTS bucket (_id INTEGER PRIMARY KEY AUTOINCREMENT,bucket_id TEXT NOT NULL,album_sort_index INTEGER,volume_id INTEGER);");
                    db.execSQL("CREATE TABLE IF NOT EXISTS list_control (_id INTEGER PRIMARY KEY AUTOINCREMENT,bucket_id TEXT NOT NULL,is_external_sdcard INTEGER,volume_id INTEGER);");
                }
                if (fromVersion < 40001) {
                    db.execSQL("DROP TABLE IF EXISTS list_control");
                    db.execSQL("CREATE TABLE IF NOT EXISTS bucket2 (_id INTEGER PRIMARY KEY AUTOINCREMENT,bucket_id TEXT NOT NULL,album_sort_index INTEGER,volume_id INTEGER,CONSTRAINT sortIndexUniques UNIQUE (bucket_id, volume_id));");
                    db.execSQL("INSERT OR REPLACE INTO bucket2 SELECT * FROM bucket;");
                    db.execSQL("DROP TABLE bucket;");
                    db.execSQL("ALTER TABLE bucket2 RENAME TO bucket;");
                }
                if (fromVersion < 4000001) {
                    PhotoShareNoHwAccount.createOrUpdateTable(db);
                }
                if (fromVersion < 5000001) {
                    MergedMedia.createOrUpdateTable(db);
                }
                MergedMedia.createOrUpdateTable(db, fromVersion);
                if (fromVersion < 50010001) {
                    db.execSQL("DROP TABLE IF EXISTS t_sort_params");
                }
            }
        }
    }

    public static int getDatabaseVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            GalleryLog.w("GalleryDBHelper", "couldn't get version code for " + context);
            return -1;
        }
    }

    public void addAlbumSortIndex(int bucketId, int volumeId) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("INSERT INTO bucket (bucket_id,album_sort_index,volume_id) values(%d,1+ (SELECT IFNULL(MAX(album_sort_index),0) FROM bucket WHERE volume_id!=0),%d)");
            getWritableDatabase().execSQL(String.format(Locale.US, stringBuilder.toString(), new Object[]{Integer.valueOf(bucketId), Integer.valueOf(volumeId)}));
        } catch (Exception e) {
            GalleryLog.d("GalleryDBHelper", "addAlbumSortIndex err : " + e.toString());
        }
    }

    public void exchangeAlbumSortIndex(int fromBucketId, int toBucketId, String volumeIDs) {
        int from = 0;
        int to = 0;
        String whereClause = String.format(Locale.US, "bucket_id IN (%d,%d) AND (volume_id in (%s))", new Object[]{Integer.valueOf(fromBucketId), Integer.valueOf(toBucketId), volumeIDs});
        Cursor cursor = query("bucket", new String[]{"album_sort_index", "bucket_id"}, whereClause, null, null, null, null, null);
        if (cursor == null || cursor.getCount() != 2) {
            if (cursor != null) {
                cursor.close();
            }
            GalleryLog.w("GalleryDBHelper", "gallery database do not have this two bucket_id");
            return;
        }
        while (cursor.moveToNext()) {
            try {
                if (cursor.getInt(1) == fromBucketId) {
                    from = cursor.getInt(0);
                } else if (cursor.getInt(1) == toBucketId) {
                    to = cursor.getInt(0);
                } else {
                    GalleryLog.w("GalleryDBHelper", "gallery database give wrong bucket_id");
                    return;
                }
            } finally {
                cursor.close();
            }
        }
        cursor.close();
        if (from == to) {
            GalleryLog.w("GalleryDBHelper", "fromBucketId = " + fromBucketId + ";toBucketId = " + toBucketId + " have same album index");
            return;
        }
        StringBuilder stringBuilder;
        if (from < to) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("UPDATE bucket SET album_sort_index = album_sort_index-1 WHERE album_sort_index BETWEEN %d AND %d AND (volume_id in (%s))");
            getWritableDatabase().execSQL(String.format(Locale.US, stringBuilder.toString(), new Object[]{Integer.valueOf(from + 1), Integer.valueOf(to), volumeIDs}));
        } else {
            stringBuilder = new StringBuilder();
            stringBuilder.append("UPDATE bucket SET album_sort_index = album_sort_index+1 WHERE album_sort_index BETWEEN %d AND %d AND (volume_id in (%s))");
            getWritableDatabase().execSQL(String.format(Locale.US, stringBuilder.toString(), new Object[]{Integer.valueOf(to), Integer.valueOf(from - 1), volumeIDs}));
        }
        StringBuilder fromBuilder = new StringBuilder();
        fromBuilder.append("UPDATE bucket SET album_sort_index = %d WHERE bucket_id = %d AND (volume_id in (%s))");
        getWritableDatabase().execSQL(String.format(Locale.US, fromBuilder.toString(), new Object[]{Integer.valueOf(to), Integer.valueOf(fromBucketId), volumeIDs}));
    }
}
