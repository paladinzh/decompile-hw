package com.android.deskclock.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.StatFs;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.deskclock.alarmclock.SettingsActivity.PlaceholderFragment;
import com.android.deskclock.worldclock.TimeZoneService;
import com.android.deskclock.worldclock.TimeZoneUtils;
import com.android.util.HwLog;
import com.android.util.Utils;
import java.util.ArrayList;
import java.util.HashMap;

public class HwDeskClockBackupProvider extends ContentProvider {
    public static final Uri LOCATION_CONTENT_URI = Uri.parse("content://com.android.deskclock.backup/pref_items");
    public static final Uri PREF_CONTENT_URI = Uri.parse("content://com.android.deskclock.backup/pref_items");
    private static HashMap<String, String> mLocationProjectionMap = new HashMap();
    private static SparseArray<RecoveryResult> sResultMap = null;
    private static final UriMatcher sURLMatcher = new UriMatcher(-1);
    private boolean mNeedProviderHomeClockData = true;
    private AlarmDatabaseHelper mOpenHelper;
    private String mProviderPrefId = "";
    private boolean mProviderPrefSwitchOn = false;
    private String mProviderPrefTz = "";

    static class RecoveryResult {
        int mFaildInsertCount = 0;
        int mSuccessInsertCount = 0;

        RecoveryResult() {
        }
    }

    static {
        sURLMatcher.addURI("com.android.deskclock.backup", "location_items", 1);
        sURLMatcher.addURI("com.android.deskclock.backup", "pref_items", 2);
        mLocationProjectionMap.put("_id", "_id");
        mLocationProjectionMap.put("sort_order", "sort_order");
        mLocationProjectionMap.put("city_index", "city_index");
        mLocationProjectionMap.put("timezone", "timezone");
        mLocationProjectionMap.put("homecity", "homecity");
    }

    public boolean onCreate() {
        Context storageContext;
        Context context = getContext();
        if (Utils.isNOrLater()) {
            Context deviceContext = context.createDeviceProtectedStorageContext();
            if (!deviceContext.moveDatabaseFrom(context, "alarms.db")) {
                HwLog.i("HwDeskClockBackupProvider", "Failed to migrate database");
            }
            storageContext = deviceContext;
        } else {
            storageContext = context;
        }
        this.mOpenHelper = new AlarmDatabaseHelper(storageContext);
        return true;
    }

    public Cursor query(Uri uri, String[] projectionIn, String selection, String[] selectionArgs, String sort) {
        if (this.mOpenHelper == null) {
            return null;
        }
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sURLMatcher.match(uri)) {
            case 1:
                String orderBy;
                qb.setTables("locations");
                qb.setProjectionMap(mLocationProjectionMap);
                if (TextUtils.isEmpty(sort)) {
                    switch (sURLMatcher.match(uri)) {
                        case 1:
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
                    ret.setNotificationUri(getContext().getContentResolver(), uri);
                }
                return ret;
            case 2:
                return getMatrixCursor();
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    private Cursor getMatrixCursor() {
        MatrixCursor result = new MatrixCursor(new String[]{"alarm_in_silent", "volum_button", "home_clock_checked", "home_clock_name", "home_clock_id", "duration_num", "duration_time", "snooze_min", "snooze_count"});
        Context context = getContext();
        SharedPreferences sp1 = Utils.getSharedPreferences(context, "setting_activity", 0);
        boolean alarmInSlient = System.getInt(context.getContentResolver(), "mode_ringer_streams_affected", 0) == 294;
        int volBtnChoiceNum = sp1.getInt("choice", 1);
        boolean switchOn = sp1.getBoolean("ISCHECKED", false);
        String tz = sp1.getString("home_city_timezone", "");
        String id = sp1.getString("home_time_index", "");
        SharedPreferences sp2 = Utils.getDefaultSharedPreferences(context);
        int durNum = sp2.getInt("bell_duration_choice_num", 1);
        int durTime = sp2.getInt("bell_duration", 5);
        int snoozeMin = sp2.getInt("snooze_duration", 10);
        int snoozeCount = sp2.getInt("snooze_timers", 3);
        result.addRow(new Object[]{Boolean.valueOf(alarmInSlient), Integer.valueOf(volBtnChoiceNum), Boolean.valueOf(switchOn), tz, id, Integer.valueOf(durNum), Integer.valueOf(durTime), Integer.valueOf(snoozeMin), Integer.valueOf(snoozeCount)});
        return result;
    }

    public Uri insert(Uri uri, ContentValues initialValues) {
        if (this.mOpenHelper == null) {
            return null;
        }
        ensureResultMapAndResultValueExits();
        RecoveryResult recoveryResult = (RecoveryResult) sResultMap.get(Binder.getCallingPid());
        if (recoveryResult == null) {
            return null;
        }
        String table = "";
        Uri uri2 = null;
        Context context = getContext();
        switch (sURLMatcher.match(uri)) {
            case 1:
                if (initialValues == null) {
                    return null;
                }
                if (initialValues.containsKey("_id")) {
                    initialValues.remove("_id");
                }
                int ExitedHomeClockId = this.mOpenHelper.getHomeClockExistId();
                int ExistCityId = this.mOpenHelper.getCityIndexExistId(initialValues.getAsString("city_index"));
                if (1 == initialValues.getAsInteger("homecity").intValue()) {
                    if (-1 != ExistCityId) {
                        this.mNeedProviderHomeClockData = false;
                    }
                    String existId = Utils.getSharedPreferences(context, "setting_activity", 0).getString("home_time_index", "");
                    if (!(-1 == ExitedHomeClockId && TextUtils.isEmpty(existId))) {
                        this.mNeedProviderHomeClockData = false;
                        recoveryResult.mSuccessInsertCount++;
                        return ContentUris.withAppendedId(uri, (long) ExitedHomeClockId);
                    }
                }
                if (-1 != ExistCityId) {
                    recoveryResult.mSuccessInsertCount++;
                    return ContentUris.withAppendedId(uri, (long) ExistCityId);
                }
                String nullColumnHack = "city_index";
                table = "locations";
                SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
                long rowId = -1;
                try {
                    db.beginTransaction();
                    rowId = db.insert(table, nullColumnHack, initialValues);
                    db.setTransactionSuccessful();
                    uri2 = ContentUris.withAppendedId(uri, rowId);
                    HwLog.w("HwDeskClockBackupProvider", "insert result: " + rowId);
                    if (rowId > -1) {
                        recoveryResult.mSuccessInsertCount++;
                    } else {
                        recoveryResult.mFaildInsertCount++;
                    }
                    db.endTransaction();
                    if (!diskInternalSpaceAvailable() && rowId < 0) {
                        throw new SQLiteFullException();
                    }
                } catch (Exception e) {
                    HwLog.w("HwDeskClockBackupProvider", "insert : can not insert because of " + e.getMessage());
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
                HwLog.d("HwDeskClockBackupProvider", "insert :  newUrl = " + uri2);
                getContext().getContentResolver().notifyChange(uri2, null);
                return uri2;
            case 2:
                SharedPreferences sp1 = Utils.getSharedPreferences(context, "setting_activity", 0);
                boolean alarmInSlient = initialValues.getAsBoolean("alarm_in_silent").booleanValue();
                PlaceholderFragment.doSwitchAlarmInSlient(context, Boolean.valueOf(alarmInSlient));
                int volBtnChoiceNum = initialValues.getAsInteger("volum_button").intValue();
                Editor editor1 = sp1.edit();
                editor1.putBoolean("alarm_in_silent", alarmInSlient).putInt("choice", volBtnChoiceNum);
                editor1.commit();
                this.mProviderPrefSwitchOn = initialValues.getAsBoolean("home_clock_checked").booleanValue();
                this.mProviderPrefId = initialValues.getAsString("home_clock_id");
                this.mProviderPrefTz = initialValues.getAsString("home_clock_name");
                SharedPreferences sp2 = Utils.getDefaultSharedPreferences(context);
                int durNum = initialValues.getAsInteger("duration_num").intValue();
                int durTime = initialValues.getAsInteger("duration_time").intValue();
                int snoozeMin = initialValues.getAsInteger("snooze_min").intValue();
                sp2.edit().putInt("bell_duration_choice_num", durNum).putInt("bell_duration", durTime).putInt("snooze_duration", snoozeMin).putInt("snooze_timers", initialValues.getAsInteger("snooze_count").intValue()).commit();
                getContext().getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(PREF_CONTENT_URI, 0);
            default:
                throw new IllegalArgumentException("Cannot insert URL: " + uri);
        }
    }

    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        return 0;
    }

    public int delete(Uri arg0, String arg1, String[] arg2) {
        return 0;
    }

    public String getType(Uri arg0) {
        return null;
    }

    public static boolean diskInternalSpaceAvailable() {
        boolean z = false;
        try {
            StatFs fs = new StatFs("data");
            HwLog.d("HwDeskClockBackupProvider", "diskInternalSpaceAvailable : fs.getAvailableBlocks() = " + fs.getAvailableBlocks());
            if (((long) fs.getAvailableBlocks()) * ((long) fs.getBlockSize()) > 524288) {
                z = true;
            }
            return z;
        } catch (IllegalArgumentException e) {
            HwLog.w("HwDeskClockBackupProvider", "diskInternalSpaceAvailable : IllegalArgumentException = " + e.getMessage());
            return false;
        }
    }

    public Bundle call(String method, String arg, Bundle extras) {
        super.call(method, arg, extras);
        HwLog.i("HwDeskClockBackupProvider", "call:" + method);
        if ("backup_query".equals(method)) {
            return backupQuery(arg, extras);
        }
        if ("backup_recover_start".equals(method)) {
            return backupRecoverStart(arg, extras);
        }
        if (!"backup_recover_complete".equals(method)) {
            return null;
        }
        backupRecoverComplete(arg, extras);
        return null;
    }

    private Bundle backupQuery(String arg, Bundle extras) {
        HwLog.i("HwDeskClockBackupProvider", String.format("running in thread: %s, thread name: %s", new Object[]{Long.valueOf(Thread.currentThread().getId()), Thread.currentThread().getName()}));
        ArrayList<String> backupList = new ArrayList();
        backupList.add("content://com.android.deskclock.backup/location_items");
        backupList.add("content://com.android.deskclock.backup/pref_items");
        ArrayList<String> backupListNeedCount = new ArrayList();
        backupListNeedCount.add("content://com.android.deskclock.backup/location_items");
        backupListNeedCount.add("content://com.android.deskclock.backup/pref_items");
        Bundle result = new Bundle();
        result.putInt("version", 1);
        result.putStringArrayList("uri_list", backupList);
        result.putStringArrayList("uri_list_need_count", backupListNeedCount);
        return result;
    }

    private Bundle backupRecoverStart(String arg, Bundle extras) {
        boolean isPermitted = true;
        if (extras == null) {
            HwLog.w("HwDeskClockBackupProvider", "Caller intent to recover app' data, while the extas send is null");
            return null;
        }
        Bundle result = new Bundle();
        ArrayList<String> recoveryList = new ArrayList();
        if (extras.getInt("version", 1) > 1) {
            isPermitted = false;
        }
        if (isPermitted) {
            recoveryList.add("content://com.android.deskclock.backup/location_items");
            recoveryList.add("content://com.android.deskclock.backup/pref_items");
        }
        result.putBoolean("permit", isPermitted);
        result.putStringArrayList("uri_list", recoveryList);
        return result;
    }

    private Bundle backupRecoverComplete(String arg, Bundle extras) {
        Context context = getContext();
        Bundle result = new Bundle();
        if (TimeZoneUtils.getTimeZoneUpdating()) {
            return null;
        }
        if (this.mNeedProviderHomeClockData) {
            Utils.getSharedPreferences(context, "setting_activity", 0).edit().putBoolean("ISCHECKED", this.mProviderPrefSwitchOn).putString("home_city_timezone", this.mProviderPrefTz).putString("home_time_index", this.mProviderPrefId).commit();
        }
        TimeZoneUtils.setTimeZoneUpdating(true);
        Intent intentService = new Intent(context, TimeZoneService.class);
        intentService.putExtra("need_init_tz_pref", true);
        intentService.setAction("huawei.intent.action.ZONE_PICKER_LOAD_COMPLETED");
        context.startService(intentService);
        int[] resultArrays = new int[2];
        if (sResultMap != null) {
            RecoveryResult recovery = (RecoveryResult) sResultMap.get(Binder.getCallingPid());
            if (recovery != null) {
                resultArrays[0] = recovery.mFaildInsertCount;
                resultArrays[1] = recovery.mSuccessInsertCount;
                sResultMap.delete(Binder.getCallingPid());
            } else {
                HwLog.d("HwDeskClockBackupProvider", "recovery complete while the result is null");
            }
        } else {
            HwLog.d("HwDeskClockBackupProvider", "recovery complete while the result map is null");
        }
        HwLog.d("HwDeskClockBackupProvider", "the result array : " + resultArrays[0] + " " + resultArrays[1]);
        result.putInt("fail_count", resultArrays[0]);
        result.putInt("success_count", resultArrays[1]);
        return result;
    }

    private void ensureResultMapAndResultValueExits() {
        if (sResultMap == null) {
            setResultMap(new SparseArray());
        }
        if (((RecoveryResult) sResultMap.get(Binder.getCallingPid())) == null) {
            sResultMap.put(Binder.getCallingPid(), new RecoveryResult());
        }
    }

    public static void setResultMap(SparseArray<RecoveryResult> result) {
        sResultMap = result;
    }
}
