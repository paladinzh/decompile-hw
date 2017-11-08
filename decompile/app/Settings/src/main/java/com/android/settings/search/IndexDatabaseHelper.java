package com.android.settings.search;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build.VERSION;
import android.util.Log;
import java.util.Locale;

public class IndexDatabaseHelper extends SQLiteOpenHelper {
    private static final String INSERT_BUILD_VERSION = ("INSERT INTO meta_index VALUES ('" + VERSION.INCREMENTAL + "');");
    private static IndexDatabaseHelper sSingleton;
    private final Context mContext;

    public static synchronized IndexDatabaseHelper getInstance(Context context) {
        IndexDatabaseHelper indexDatabaseHelper;
        synchronized (IndexDatabaseHelper.class) {
            if (sSingleton == null) {
                sSingleton = new IndexDatabaseHelper(context);
            }
            indexDatabaseHelper = sSingleton;
        }
        return indexDatabaseHelper;
    }

    public IndexDatabaseHelper(Context context) {
        super(context, "search_index.db", null, 115);
        this.mContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        bootstrapDB(db);
    }

    private void bootstrapDB(SQLiteDatabase db) {
        db.execSQL("CREATE VIRTUAL TABLE prefs_index USING fts4(locale, data_rank, data_title, data_title_normalized, data_summary_on, data_summary_on_normalized, data_summary_off, data_summary_off_normalized, data_entries, data_keywords, screen_title, class_name, icon, intent_action, intent_target_package, intent_target_class, enabled, data_key_reference, user_id, tokenize=unicode61);");
        db.execSQL("CREATE TABLE meta_index(build VARCHAR(32) NOT NULL)");
        db.execSQL("CREATE TABLE saved_queries(query VARCHAR(64) NOT NULL, timestamp INTEGER)");
        db.execSQL(INSERT_BUILD_VERSION);
        Log.i("IndexDatabaseHelper", "Bootstrapped database");
    }

    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        Log.i("IndexDatabaseHelper", "Using schema version: " + db.getVersion());
        if (VERSION.INCREMENTAL.equals(getBuildVersion(db))) {
            Log.i("IndexDatabaseHelper", "Index is fine");
            return;
        }
        Log.w("IndexDatabaseHelper", "Index needs to be rebuilt as build-version is not the same");
        reconstruct(db);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 115) {
            Log.w("IndexDatabaseHelper", "Detected schema version '" + oldVersion + "'. " + "Index needs to be rebuilt for schema version '" + newVersion + "'.");
            reconstruct(db);
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("IndexDatabaseHelper", "Detected schema version '" + oldVersion + "'. " + "Index needs to be rebuilt for schema version '" + newVersion + "'.");
        reconstruct(db);
    }

    private void reconstruct(SQLiteDatabase db) {
        dropTables(db);
        bootstrapDB(db);
    }

    private String getBuildVersion(SQLiteDatabase db) {
        String str = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT build FROM meta_index LIMIT 1;", null);
            if (cursor.moveToFirst()) {
                str = cursor.getString(0);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("IndexDatabaseHelper", "Cannot get build version from Index metadata");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return str;
    }

    public static void clearLocalesIndexed(Context context) {
        context.getSharedPreferences("index", 0).edit().clear().commit();
    }

    public static void setLocaleIndexed(Context context, String locale) {
        context.getSharedPreferences("index", 0).edit().putBoolean(locale, true).commit();
    }

    public static boolean isLocaleAlreadyIndexedEx(Context context) {
        return context.getSharedPreferences("indexablestate", 0).getBoolean(Index.INDEXABLE_LANG_BASE_KEY + Locale.getDefault().toString(), false);
    }

    private void dropTables(SQLiteDatabase db) {
        clearLocalesIndexed(this.mContext);
        db.execSQL("DROP TABLE IF EXISTS meta_index");
        db.execSQL("DROP TABLE IF EXISTS prefs_index");
        db.execSQL("DROP TABLE IF EXISTS saved_queries");
    }
}
