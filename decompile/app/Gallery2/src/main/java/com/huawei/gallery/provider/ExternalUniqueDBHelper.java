package com.huawei.gallery.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import java.io.Closeable;

public class ExternalUniqueDBHelper extends SQLiteOpenHelper {
    private static final Uri ADD_RECYCLE_FILE_UNIQUE_URI = Uri.withAppendedPath(ExternalUnigueProvider.BASE_URI, "add_recycle_file_unique_id");
    private static final Uri DEL_RECYCLE_FILE_UNIQUE_URI = Uri.withAppendedPath(ExternalUnigueProvider.BASE_URI, "del_recycle_file_unique_id");
    private static final Uri GET_RECYCLE_FILE_UNIQUE_URI = Uri.withAppendedPath(ExternalUnigueProvider.BASE_URI, "get_recycle_file_unique_id");
    private final Context mContext;

    public ExternalUniqueDBHelper(Context context, String dir, String dbName) {
        super(new ExternalDataBaseContext(context, dir), dbName, null, 1);
        this.mContext = context;
        setWriteAheadLoggingEnabled(true);
    }

    public void onCreate(SQLiteDatabase db) {
        updateDatabase(this.mContext, db, 0, 1);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateDatabase(this.mContext, db, oldVersion, newVersion);
    }

    private static void updateDatabase(Context context, SQLiteDatabase db, int fromVersion, int toVersion) {
        if (fromVersion >= 0 && toVersion >= 0) {
            if (fromVersion > toVersion) {
                GalleryLog.e("ExternalUniqueDBHelper", "Illegal update request: can't downgrade from " + fromVersion + " to " + toVersion + ". Did you forget to wipe data?");
                return;
            }
            if (fromVersion < 1) {
                db.execSQL("CREATE TABLE IF NOT EXISTS recycle_file_unique_id (_id INTEGER PRIMARY KEY AUTOINCREMENT,_name TEXT UNIQUE NOT NULL,_unique_id TEXT);");
            }
        }
    }

    public static String getUniqueID(ContentResolver contentResolver, String name) {
        Closeable closeable = null;
        try {
            ContentResolver contentResolver2 = contentResolver;
            closeable = contentResolver2.query(GET_RECYCLE_FILE_UNIQUE_URI, new String[]{"_unique_id"}, "_name = ?", new String[]{name}, null);
            if (closeable == null || !closeable.moveToNext()) {
                Utils.closeSilently(closeable);
                return null;
            }
            String string = closeable.getString(0);
            return string;
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public static Uri insertUniqueID(ContentResolver contentResolver, String name, String uniqueID) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("_name", name);
        contentValues.put("_unique_id", uniqueID);
        return contentResolver.insert(ADD_RECYCLE_FILE_UNIQUE_URI, contentValues);
    }

    public static long deleteUniqueID(ContentResolver contentResolver, String name) {
        return (long) contentResolver.delete(DEL_RECYCLE_FILE_UNIQUE_URI, "_name = ?", new String[]{name});
    }
}
