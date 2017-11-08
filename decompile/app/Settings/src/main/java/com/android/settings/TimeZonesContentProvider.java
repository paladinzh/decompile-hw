package com.android.settings;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class TimeZonesContentProvider extends ContentProvider {
    public static final Uri CONTENT_URI = Uri.parse("content://com.android.settings.timezonesprovider/timezones");
    private static final String TAG = TimeZonesContentProvider.class.getCanonicalName();
    private static final UriMatcher URL_MATCHER = new UriMatcher(-1);
    private TimeZonesDatabaseHelper mDBHelper;

    static {
        URL_MATCHER.addURI("com.android.settings.timezonesprovider", "timezones", 1);
    }

    public boolean onCreate() {
        this.mDBHelper = TimeZonesDatabaseHelper.getInstance(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        this.mDBHelper = TimeZonesDatabaseHelper.getInstance(getContext());
        switch (URL_MATCHER.match(uri)) {
            case 1:
                queryBuilder.setTables("timezones");
                try {
                    return queryBuilder.query(this.mDBHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            default:
                Log.e(TAG, " query unknown uri " + uri);
                return null;
        }
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
