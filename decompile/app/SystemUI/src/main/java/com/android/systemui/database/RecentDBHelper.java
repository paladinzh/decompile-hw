package com.android.systemui.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.android.systemui.utils.HwLog;

public class RecentDBHelper extends AbsDBOpenHelper {
    private static RecentDBHelper sInstance = null;

    public static synchronized RecentDBHelper getInstance(Context context) {
        RecentDBHelper recentDBHelper;
        synchronized (RecentDBHelper.class) {
            if (sInstance == null) {
                sInstance = new RecentDBHelper(context.getApplicationContext());
            }
            recentDBHelper = sInstance;
        }
        return recentDBHelper;
    }

    private RecentDBHelper(Context context) {
        super(context, "RecentLock.db", null, 2);
    }

    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE recent_lock_List (recent_lock_id INTEGER PRIMARY KEY,recent_lock_state INTEGER,recent_lock_pkgname TEXT);");
        } catch (SQLException e) {
            Log.e("RecentDBHelper", "DatabaseHelper->onCreate : SQLException = " + e.getMessage());
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS recent_lock_List");
        onCreate(db);
    }

    void clearDataBeforeRestore() {
        HwLog.i("RecentDBHelper", "clearDataBeforeRestore");
        deleteInner("recent_lock_List", null, null);
    }
}
