package com.huawei.gallery.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.TraceController;

public class StorageMonitorDBHelper extends SQLiteOpenHelper {
    public final Context mContext;

    public StorageMonitorDBHelper(Context context) {
        super(context, "StorageMonitor.db", null, getDatabaseVersion(context));
        this.mContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        GalleryLog.d("StorageMonitorDBHelper", "StorageMonitorDBHelper onCreate");
        TraceController.traceBegin("StorageMonitorDBHelper.onCreate.updateDatabase");
        updateDatabase(this.mContext, db, 0, getDatabaseVersion(this.mContext));
        TraceController.traceEnd();
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        TraceController.traceBegin("StorageMonitorDBHelper.onUpgrade.updateDatabase");
        updateDatabase(this.mContext, db, oldVersion, newVersion);
        TraceController.traceEnd();
    }

    public int delete(String table, String whereClause, String[] whereArgs) {
        return getWritableDatabase().delete(table, whereClause, whereArgs);
    }

    private static void updateDatabase(Context context, SQLiteDatabase db, int fromVersion, int toVersion) {
        int dbversion = getDatabaseVersion(context);
        if (dbversion >= 0 && fromVersion >= 0 && toVersion >= 0) {
            if (toVersion != dbversion) {
                GalleryLog.e("StorageMonitorDBHelper", "Illegal update request. Got " + toVersion + ", expected " + dbversion);
            } else if (fromVersion > toVersion) {
                GalleryLog.e("StorageMonitorDBHelper", "Illegal update request: can't downgrade from " + fromVersion + " to " + toVersion + ". Did you forget to wipe data?");
            } else {
                GalleryLog.e("StorageMonitorDBHelper", "updateDatabase: from " + fromVersion + " to  " + toVersion);
                if (fromVersion < 1) {
                    db.execSQL("CREATE TABLE IF NOT EXISTS media_file (_id INTEGER PRIMARY KEY AUTOINCREMENT,file_path TEXT UNIQUE NOT NULL,creat_time INTEGER,crc32_data INTEGER,be_reported INTEGER);");
                }
            }
        }
    }

    public static int getDatabaseVersion(Context context) {
        return 1;
    }
}
