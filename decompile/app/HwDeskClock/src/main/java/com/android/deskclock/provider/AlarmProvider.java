package com.android.deskclock.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.os.StatFs;
import android.text.TextUtils;
import com.android.deskclock.alarmclock.Alarms;
import com.android.deskclock.alarmclock.MetaballPath;
import com.android.deskclock.smartcover.HwCustCoverAdapter;
import com.android.util.HwLog;
import com.android.util.Log;
import com.android.util.Utils;
import java.util.HashMap;

public class AlarmProvider extends ContentProvider {
    private static HashMap<String, String> mLocationProjectionMap = new HashMap();
    private static HashMap<String, String> mWidgetProjectionMap = new HashMap();
    private static final UriMatcher sURLMatcher = new UriMatcher(-1);
    private AlarmDatabaseHelper mOpenHelper;

    static {
        sURLMatcher.addURI(HwCustCoverAdapter.APP_PACKEGE, "alarm", 1);
        sURLMatcher.addURI(HwCustCoverAdapter.APP_PACKEGE, "alarm/#", 2);
        sURLMatcher.addURI(HwCustCoverAdapter.APP_PACKEGE, "locations", 5);
        sURLMatcher.addURI(HwCustCoverAdapter.APP_PACKEGE, "locations/#", 6);
        sURLMatcher.addURI(HwCustCoverAdapter.APP_PACKEGE, "widgets", 3);
        sURLMatcher.addURI(HwCustCoverAdapter.APP_PACKEGE, "widgets/#", 4);
        mLocationProjectionMap.put("_id", "_id");
        mLocationProjectionMap.put("sort_order", "sort_order");
        mLocationProjectionMap.put("city_index", "city_index");
        mLocationProjectionMap.put("timezone", "timezone");
        mLocationProjectionMap.put("homecity", "homecity");
        mWidgetProjectionMap.put("_id", "_id");
        mWidgetProjectionMap.put("cityname", "cityname");
        mWidgetProjectionMap.put("timezone", "timezone");
        mWidgetProjectionMap.put("widget_id", "widget_id");
        mWidgetProjectionMap.put("first_timezone", "first_timezone");
        mWidgetProjectionMap.put("second_timezone", "second_timezone");
        mWidgetProjectionMap.put("first_index", "first_index");
        mWidgetProjectionMap.put("second_index", "second_index");
    }

    public boolean onCreate() {
        Context storageContext;
        Context context = getContext();
        if (Utils.isNOrLater()) {
            Utils.clearTimerSharedPref(context);
            Utils.clearStopWatchPref(context);
            Context deviceContext = context.createDeviceProtectedStorageContext();
            if (!deviceContext.moveDatabaseFrom(context, "alarms.db")) {
                HwLog.i("AlarmProvider", "Failed to migrate database");
            }
            if (!deviceContext.moveSharedPreferencesFrom(context, "setting_activity")) {
                HwLog.i("AlarmProvider", "Failed to migrate shared preferences");
            }
            if (!deviceContext.moveSharedPreferencesFrom(context, "timeZone.cfg")) {
                HwLog.i("AlarmProvider", "Failed to migrate shared preferences");
            }
            storageContext = deviceContext;
        } else {
            storageContext = context;
        }
        this.mOpenHelper = new AlarmDatabaseHelper(storageContext);
        return true;
    }

    public Cursor query(Uri url, String[] projectionIn, String selection, String[] selectionArgs, String sort) {
        String orderBy;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sURLMatcher.match(url)) {
            case 1:
                qb.setTables("alarms");
                break;
            case 2:
                qb.setTables("alarms");
                qb.appendWhere("_id=");
                qb.appendWhere((CharSequence) url.getPathSegments().get(1));
                break;
            case 3:
                qb.setTables("widgets");
                qb.setProjectionMap(mWidgetProjectionMap);
                break;
            case MetaballPath.POINT_NUM /*4*/:
                qb.setTables("widgets");
                qb.setProjectionMap(mWidgetProjectionMap);
                qb.appendWhere("_id=" + ((String) url.getPathSegments().get(1)));
                break;
            case 5:
                qb.setTables("locations");
                qb.setProjectionMap(mLocationProjectionMap);
                break;
            case 6:
                qb.setTables("locations");
                qb.setProjectionMap(mLocationProjectionMap);
                qb.appendWhere("_id=" + ((String) url.getPathSegments().get(1)));
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }
        if (TextUtils.isEmpty(sort)) {
            switch (sURLMatcher.match(url)) {
                case 3:
                case MetaballPath.POINT_NUM /*4*/:
                    orderBy = "_ID ASC";
                    break;
                case 5:
                case 6:
                    orderBy = "SORT_ORDER ASC";
                    break;
                default:
                    orderBy = sort;
                    break;
            }
        }
        orderBy = sort;
        Cursor ret = qb.query(this.mOpenHelper.getReadableDatabase(), projectionIn, selection, selectionArgs, null, null, orderBy);
        if (ret != null) {
            ret.setNotificationUri(getContext().getContentResolver(), url);
        }
        return ret;
    }

    public String getType(Uri url) {
        switch (sURLMatcher.match(url)) {
            case 1:
                return "vnd.android.cursor.dir/alarms";
            case 2:
                return "vnd.android.cursor.item/alarms";
            case 3:
                return "vnd.android.cursor.dir/vnd.AlarmProvider.widgets";
            case MetaballPath.POINT_NUM /*4*/:
                return "vnd.android.cursor.item/vnd.AlarmProvider.widgets";
            case 5:
                return "vnd.android.cursor.dir/vnd.AlarmProvider.locations";
            case 6:
                return "vnd.android.cursor.item/vnd.AlarmProvider.locations";
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }

    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        int count = 0;
        String table = (String) url.getPathSegments().get(0);
        switch (sURLMatcher.match(url)) {
            case 1:
                table = "alarms";
                break;
            case 2:
                table = "alarms";
                where = "_id=" + ((String) url.getPathSegments().get(1)) + (!TextUtils.isEmpty(where) ? "AND (" + where + ")" : "");
                break;
            case 3:
            case 5:
                break;
            case MetaballPath.POINT_NUM /*4*/:
            case 6:
                where = "_id=" + ((String) url.getPathSegments().get(1)) + (!TextUtils.isEmpty(where) ? "AND (" + where + ")" : "");
                break;
            default:
                throw new UnsupportedOperationException("Cannot update URL: " + url);
        }
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            count = db.update(table, values, where, whereArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.w("AlarmProvider", "update : can not update because of " + e.getMessage());
        } finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }

    public Uri insert(Uri url, ContentValues initialValues) {
        if (sURLMatcher.match(url) == 1 || sURLMatcher.match(url) == 5 || sURLMatcher.match(url) == 3) {
            String nullColumnHack;
            String table = (String) url.getPathSegments().get(0);
            Uri uri = null;
            switch (sURLMatcher.match(url)) {
                case 1:
                    table = "alarms";
                    nullColumnHack = "message";
                    if (!(initialValues == null || initialValues.containsKey("daysofweektype"))) {
                        correctBkpData(initialValues);
                        break;
                    }
                case 3:
                    nullColumnHack = "cityname";
                    break;
                case 5:
                    nullColumnHack = "city_index";
                    break;
                default:
                    throw new IllegalArgumentException("Cannot insert URL: " + url);
            }
            SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
            long rowId = -1;
            try {
                db.beginTransaction();
                rowId = db.insert(table, nullColumnHack, initialValues);
                db.setTransactionSuccessful();
                uri = ContentUris.withAppendedId(url, rowId);
                db.endTransaction();
                if (!diskInternalSpaceAvailable() && rowId < 0) {
                    throw new SQLiteFullException();
                }
            } catch (Exception e) {
                Log.w("AlarmProvider", "insert : can not insert because of " + e.getMessage());
                db.endTransaction();
                if (!diskInternalSpaceAvailable() && rowId < 0) {
                    throw new SQLiteFullException();
                }
            } catch (Throwable th) {
                db.endTransaction();
                if (!diskInternalSpaceAvailable() && rowId < 0) {
                    SQLiteFullException sQLiteFullException = new SQLiteFullException();
                }
            }
            Log.d("AlarmProvider", "insert :  newUrl = " + uri);
            getContext().getContentResolver().notifyChange(uri, null);
            return uri;
        }
        throw new IllegalArgumentException("Cannot insert into URL: " + url);
    }

    private void correctBkpData(ContentValues initialValues) {
        Integer backWeekCode = initialValues.getAsInteger("daysofweek");
        int dayofweek = backWeekCode == null ? 0 : backWeekCode.intValue();
        Integer backType = initialValues.getAsInteger("daysofweektype");
        int dayofweekType = backType == null ? 0 : backType.intValue();
        if (dayofweek > 0 && dayofweekType == 0) {
            dayofweekType = 3;
        }
        if (dayofweekType == 3) {
            if (dayofweek == 31) {
                dayofweekType = 1;
            } else if (dayofweek == 127) {
                dayofweekType = 2;
            }
            initialValues.put("daysofweektype", Integer.valueOf(dayofweekType));
        }
        Log.d("AlarmProvider", "dayofweek = " + dayofweek + "  dayofweekType = " + dayofweekType);
    }

    public int delete(Uri url, String where, String[] whereArgs) {
        String table = (String) url.getPathSegments().get(0);
        int count = 0;
        switch (sURLMatcher.match(url)) {
            case 1:
                table = "alarms";
                break;
            case 2:
                table = "alarms";
                where = "_id==" + ((String) url.getPathSegments().get(1)) + (!TextUtils.isEmpty(where) ? "AND (" + where + ")" : "");
                break;
            case 3:
            case 5:
                break;
            case MetaballPath.POINT_NUM /*4*/:
            case 6:
                where = "_id=" + ((String) url.getPathSegments().get(1)) + (!TextUtils.isEmpty(where) ? "AND (" + where + ")" : "");
                break;
            default:
                throw new IllegalArgumentException("Cannot delete from URL: " + url);
        }
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            count = db.delete(table, where, whereArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.w("AlarmProvider", "delete : can not delete because of " + e.getMessage());
        } finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }

    public static boolean diskInternalSpaceAvailable() {
        boolean z = false;
        try {
            StatFs fs = new StatFs("data");
            Log.d("AlarmProvider", "diskInternalSpaceAvailable : fs.getAvailableBlocks() = " + fs.getAvailableBlocks());
            if (((long) fs.getAvailableBlocks()) * ((long) fs.getBlockSize()) > 524288) {
                z = true;
            }
            return z;
        } catch (IllegalArgumentException e) {
            Log.w("AlarmProvider", "diskInternalSpaceAvailable : IllegalArgumentException = " + e.getMessage());
            return false;
        }
    }

    public Bundle call(String method, String arg, Bundle extras) {
        super.call(method, arg, extras);
        Bundle bundle = new Bundle();
        if ("isLockAlarm".equals(method)) {
            boolean isAlert = Utils.getDefaultSharedPreferences(getContext()).getBoolean("isAlerting", false);
            if (isAlert) {
                isAlert = Alarms.isServiceRunning(getContext(), "com.android.deskclock.alarmclock.AlarmKlaxon");
            }
            bundle.putBoolean("isAlerting", isAlert);
        }
        return bundle;
    }
}
