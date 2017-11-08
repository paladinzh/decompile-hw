package com.android.systemui.usb;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import com.android.systemui.utils.HwLog;

public class HwSDCardLockProvider extends ContentProvider {
    public static final Uri CONTENT_URI = Uri.parse("content://com.android.systemui.usb.HwSDCardLockProvider");
    private DatabaseHelper mOpenHelper;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, "SDCardLock.db", null, 1);
        }

        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL("CREATE TABLE sdcard_id_List (sdcard_id INTEGER PRIMARY KEY);");
            } catch (SQLException e) {
                Log.e("HwSDCardLockProvider", "DatabaseHelper->onCreate : SQLException = " + e.getMessage());
            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS sdcard_id_List");
            onCreate(db);
        }
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deleteNum = 0;
        SQLiteDatabase sQLiteDatabase = null;
        try {
            sQLiteDatabase = this.mOpenHelper.getWritableDatabase();
            deleteNum = sQLiteDatabase.delete("sdcard_id_List", selection, selectionArgs);
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
        } catch (SQLException e) {
            Log.i("HwSDCardLockProvider", "delete : SQLException = " + e.getMessage());
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
        } catch (Throwable th) {
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
        }
        return deleteNum;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase sQLiteDatabase = null;
        try {
            sQLiteDatabase = this.mOpenHelper.getWritableDatabase();
            sQLiteDatabase.insert("sdcard_id_List", null, values);
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
        } catch (SQLException e) {
            Log.i("HwSDCardLockProvider", "insert : SQLException = " + e.getMessage());
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
        } catch (Throwable th) {
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
        }
        return null;
    }

    public void shutdown() {
        if (this.mOpenHelper != null) {
            this.mOpenHelper.close();
            this.mOpenHelper = null;
        }
        super.shutdown();
    }

    public boolean onCreate() {
        this.mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        try {
            SQLiteDatabase db = this.mOpenHelper.getReadableDatabase();
            if (db != null) {
                return db.query("sdcard_id_List", projection, selection, selectionArgs, null, null, sortOrder);
            }
            HwLog.d("HwSDCardLockProvider", "db == null return null");
            return null;
        } catch (SQLException e) {
            Log.i("HwSDCardLockProvider", "query : SQLException = " + e.getMessage());
            return null;
        }
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        SQLiteDatabase sQLiteDatabase = null;
        try {
            sQLiteDatabase = this.mOpenHelper.getWritableDatabase();
            count = sQLiteDatabase.update("sdcard_id_List", values, selection, selectionArgs);
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
        } catch (SQLException e) {
            Log.i("HwSDCardLockProvider", "update : SQLException = " + e.getMessage());
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
        } catch (Throwable th) {
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
        }
        return count;
    }
}
