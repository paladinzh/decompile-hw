package com.huawei.harassmentinterception.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import com.huawei.harassmentinterception.blackwhitelist.BlackWhiteDBDataUpdater;
import com.huawei.harassmentinterception.blackwhitelist.GoogleBlackListContract;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.common.Tables.TbNumberLocation;
import com.huawei.harassmentinterception.strategy.StrategyConfigs.StrategyId;
import com.huawei.harassmentinterception.update.UpdateHelper;
import com.huawei.harassmentinterception.util.PreferenceHelper;
import com.huawei.systemmanager.backup.BackupConst;
import com.huawei.systemmanager.backup.BackupUtil;
import com.huawei.systemmanager.backup.HsmContentProvider;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.valueprefer.ValuePrefer;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.numberlocation.NumberLocationHelper;
import com.huawei.systemmanager.util.numberlocation.NumberLocationInfo;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DBProvider extends HsmContentProvider {
    public static final String AUTH = "com.huawei.systemmanager.HarassmentInterceptionDBProvider";
    static final Uri BACKUP_END_RUI = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), "BACKUP_END");
    private static final String BLACKLIST_TABLE = "interception_blacklist";
    private static final String BLACKLIST_TABLE_BACKUP = "interception_blacklist_backup";
    private static final String BLACKLIST_VIEW = "vBlacklist";
    private static final String CALLS_TABLE = "interception_calls";
    private static final String CALLS_TABLE_BACKUP = "interception_calls_backup";
    private static final String CALLS_VIEW = "vCalls";
    private static final int INDICATOR_BACKUP_PERFER_RULES = 28;
    private static final int INDICATOR_BLACKLIST_TABLE_ALL = 1;
    private static final int INDICATOR_BLACKLIST_TABLE_BACKUP_ALL = 9;
    private static final int INDICATOR_BLACKLIST_TABLE_ONE = 2;
    private static final int INDICATOR_BLACKLIST_VIEW = 18;
    private static final int INDICATOR_CALLS_TABLE_ALL = 5;
    private static final int INDICATOR_CALLS_TABLE_BACKUP_ALL = 11;
    private static final int INDICATOR_CALLS_TABLE_ONE = 6;
    private static final int INDICATOR_CALLS_VIEW = 19;
    private static final int INDICATOR_CALL_INTELL_BACKUP_ALL = 27;
    private static final int INDICATOR_CALL_RULES_BACKUP_ALL = 26;
    private static final int INDICATOR_KEYWORD_TABLE_ALL = 22;
    private static final int INDICATOR_KEYWORD_TABLE_BACKUP_ALL = 24;
    private static final int INDICATOR_KEYWORD_TABLE_ONE = 23;
    private static final int INDICATOR_MESSAGES_TABLE_ALL = 3;
    private static final int INDICATOR_MESSAGES_TABLE_BACKUP_ALL = 10;
    private static final int INDICATOR_MESSAGES_TABLE_ONE = 4;
    private static final int INDICATOR_MESSAGES_VIEW = 20;
    private static final int INDICATOR_MSG_RULES_BACKUP_ALL = 25;
    private static final int INDICATOR_NUMBERLOCATION_TABLE = 17;
    private static final int INDICATOR_PREFERENCE_BACKUP_ALL = 13;
    private static final int INDICATOR_RULES_TABLE_ALL = 7;
    private static final int INDICATOR_RULES_TABLE_BACKUP_ALL = 12;
    private static final int INDICATOR_RULES_TABLE_ONE = 8;
    private static final int INDICATOR_WHITELIST_BACKUP_ALL = 16;
    private static final int INDICATOR_WHITELIST_TABLE_ALL = 14;
    private static final int INDICATOR_WHITELIST_TABLE_ONE = 15;
    private static final int INDICATOR_WHITELIST_VIEW = 21;
    private static final String KEYWORDS_TABLE = "tbKeywordsTable";
    private static final String KEYWORDS_TABLE_BACKUP = "keyword_backup";
    private static final String MESSAGES_TABLE = "interception_messages";
    private static final String MESSAGES_TABLE_BACKUP = "interception_messages_backup";
    private static final String MESSAGES_VIEW = "vMessages";
    private static final String NUMBERLOCATION_TABLE = "tbNumberLocation";
    private static final String PREFERENCE_BACKUP = "interception_preference_backup";
    private static final String RULES_PREFER_BACKUP = "interception_rules_prefer";
    private static final String RULES_TABLE = "interception_rules";
    private static final String RULES_TABLE_BACKUP = "interception_rules_backup";
    private static final String TAG = "HarassmentInterceptionDBProvider";
    private static final String WHITELIST_TABLE = "tbWhitelist";
    private static final String WHITELIST_TABLE_BACKUP = "whitelist_backup";
    private static final String WHITELIST_VIEW = "vWhitelist";
    public static final Uri blacklist_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), "interception_blacklist");
    public static final Uri calls_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), "interception_calls");
    public static final Uri keywords_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), "tbKeywordsTable");
    private static UriMatcher mUriMatcher = new UriMatcher(-1);
    public static final Uri messages_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), "interception_messages");
    public static final Uri rules_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), "interception_rules");
    public static final Uri whitelist_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), "tbWhitelist");
    private DBHelper mDatabaseHelper = null;

    static {
        mUriMatcher.addURI(AUTH, "interception_blacklist", 1);
        mUriMatcher.addURI(AUTH, "interception_blacklist/#", 2);
        mUriMatcher.addURI(AUTH, "interception_messages", 3);
        mUriMatcher.addURI(AUTH, "interception_messages/#", 4);
        mUriMatcher.addURI(AUTH, "interception_calls", 5);
        mUriMatcher.addURI(AUTH, "interception_calls/#", 6);
        mUriMatcher.addURI(AUTH, "interception_rules", 7);
        mUriMatcher.addURI(AUTH, "interception_rules/#", 8);
        mUriMatcher.addURI(AUTH, "tbWhitelist", 14);
        mUriMatcher.addURI(AUTH, "tbWhitelist/#", 15);
        mUriMatcher.addURI(AUTH, "tbNumberLocation", 17);
        mUriMatcher.addURI(AUTH, "tbKeywordsTable", 22);
        mUriMatcher.addURI(AUTH, "tbKeywordsTable/#", 23);
        mUriMatcher.addURI(AUTH, "vBlacklist", 18);
        mUriMatcher.addURI(AUTH, "vCalls", 19);
        mUriMatcher.addURI(AUTH, "vMessages", 20);
        mUriMatcher.addURI(AUTH, "vWhitelist", 21);
        mUriMatcher.addURI(AUTH, BLACKLIST_TABLE_BACKUP, 9);
        mUriMatcher.addURI(AUTH, MESSAGES_TABLE_BACKUP, 10);
        mUriMatcher.addURI(AUTH, CALLS_TABLE_BACKUP, 11);
        mUriMatcher.addURI(AUTH, RULES_TABLE_BACKUP, 12);
        mUriMatcher.addURI(AUTH, PREFERENCE_BACKUP, 13);
        mUriMatcher.addURI(AUTH, WHITELIST_TABLE_BACKUP, 16);
        mUriMatcher.addURI(AUTH, KEYWORDS_TABLE_BACKUP, 24);
        mUriMatcher.addURI(AUTH, "message_rules_backup", 25);
        mUriMatcher.addURI(AUTH, "call_rules_backup", 26);
        mUriMatcher.addURI(AUTH, "call_intelligent_rules_backup", 27);
        mUriMatcher.addURI(AUTH, RULES_PREFER_BACKUP, 28);
    }

    public int delete(Uri uri, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        if (db == null) {
            HwLog.w(TAG, "delete: Failed to getWritableDatabase, uri = " + uri);
            return 0;
        }
        Uri notifyUri = uri;
        int deleteRowNumber = 0;
        switch (mUriMatcher.match(uri)) {
            case 1:
                deleteRowNumber = db.delete("interception_blacklist", whereClause, whereArgs);
                break;
            case 3:
                deleteRowNumber = db.delete("interception_messages", whereClause, whereArgs);
                break;
            case 5:
                deleteRowNumber = db.delete("interception_calls", whereClause, whereArgs);
                break;
            case 7:
                deleteRowNumber = db.delete("interception_rules", whereClause, whereArgs);
                break;
            case 14:
                deleteRowNumber = db.delete("tbWhitelist", whereClause, whereArgs);
                break;
            case 17:
                deleteRowNumber = db.delete("tbNumberLocation", whereClause, whereArgs);
                break;
            case 18:
            case 19:
            case 20:
            case 21:
                HwLog.w(TAG, "delete: View is readonly. uri :" + uri);
                break;
            case 22:
                deleteRowNumber = db.delete("tbKeywordsTable", whereClause, whereArgs);
                break;
            default:
                HwLog.e(TAG, "delete: Unsupported delete uri :" + uri);
                break;
        }
        if (deleteRowNumber > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deleteRowNumber;
    }

    public String getType(Uri arg0) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        if (values == null) {
            HwLog.w(TAG, "insert : Invalid values, uri = " + uri);
            return null;
        }
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        if (db == null) {
            HwLog.w(TAG, "insert: Failed to getWritableDatabase, uri = " + uri);
            return null;
        }
        Uri retUri = uri;
        long newRecordId = -1;
        int matchCode = mUriMatcher.match(uri);
        switch (matchCode) {
            case 1:
                newRecordId = db.insert("interception_blacklist", null, values);
                break;
            case 3:
                newRecordId = db.insert("interception_messages", null, values);
                this.mDatabaseHelper.checkRecordsCount(db, "interception_messages");
                break;
            case 5:
                newRecordId = db.insert("interception_calls", null, values);
                this.mDatabaseHelper.checkRecordsCount(db, "interception_calls");
                break;
            case 7:
                newRecordId = db.insert("interception_rules", null, values);
                break;
            case 14:
                newRecordId = db.insert("tbWhitelist", null, values);
                break;
            case 17:
                newRecordId = db.replace("tbNumberLocation", null, values);
                break;
            case 18:
            case 19:
            case 20:
            case 21:
                HwLog.w(TAG, "insert: View is readonly. uri :" + uri);
                break;
            case 22:
                newRecordId = db.insert("tbKeywordsTable", null, values);
                break;
            default:
                newRecordId = handleInsertBackup(uri, db, values);
                break;
        }
        if (-1 != newRecordId) {
            retUri = Uri.withAppendedPath(uri, String.valueOf(newRecordId));
            updateNumberLocationCache(db, matchCode, values);
            if (!isBackupOrRecoverOperation(matchCode)) {
                notifiChanged(retUri);
            }
        } else {
            HwLog.i(TAG, "insert failed! uri : " + uri);
        }
        return retUri;
    }

    private long handleInsertBackup(Uri uri, SQLiteDatabase db, ContentValues values) {
        long newRecordId = -1;
        switch (mUriMatcher.match(uri)) {
            case 9:
                newRecordId = db.insert(this.mDatabaseHelper.getRecoverTmpTableMap("interception_blacklist"), null, values);
                break;
            case 10:
                newRecordId = db.insert(this.mDatabaseHelper.getRecoverTmpTableMap("interception_messages"), null, values);
                break;
            case 11:
                newRecordId = db.insert(this.mDatabaseHelper.getRecoverTmpTableMap("interception_calls"), null, values);
                break;
            case 12:
                newRecordId = db.insert(this.mDatabaseHelper.getRecoverTmpTableMap("interception_rules"), null, values);
                break;
            case 13:
                newRecordId = (long) setIntereptionPreference(values);
                break;
            case 16:
                newRecordId = db.insert(this.mDatabaseHelper.getRecoverTmpTableMap("tbWhitelist"), null, values);
                break;
            case 24:
                newRecordId = db.insert(this.mDatabaseHelper.getRecoverTmpTableMap("tbKeywordsTable"), null, values);
                break;
            case 25:
            case 26:
            case 27:
                newRecordId = 1;
                break;
            case 28:
                recoverBackupPrefers(getContext(), values);
                newRecordId = 1;
                break;
            default:
                HwLog.i(TAG, "insert : Unknown insert uri = " + uri);
                break;
        }
        if (newRecordId > 0) {
            increaseRecoverSucceedCount();
        } else {
            increaseRecoverFailedCount();
        }
        return newRecordId;
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        if (values == null || values.length <= 0) {
            HwLog.w(TAG, "bulkInsert : Invalid values, uri = " + uri);
            return 0;
        }
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        if (db == null) {
            HwLog.w(TAG, "bulkInsert: Fail to getWritableDatabase, uri = " + uri);
            return 0;
        }
        int matchCode = mUriMatcher.match(uri);
        String tableNameString = "";
        int nInsertCount = 0;
        switch (matchCode) {
            case 1:
            case 9:
                tableNameString = "interception_blacklist";
                break;
            case 3:
            case 10:
                tableNameString = "interception_messages";
                break;
            case 5:
            case 11:
                tableNameString = "interception_calls";
                break;
            case 7:
            case 12:
                tableNameString = "interception_rules";
                break;
            case 13:
                for (ContentValues value : values) {
                    if (((long) setIntereptionPreference(value)) > 0) {
                        increaseRecoverSucceedCount();
                    } else {
                        increaseRecoverFailedCount();
                    }
                }
                return 0;
            case 14:
            case 16:
                tableNameString = "tbWhitelist";
                break;
            case 17:
                tableNameString = "tbNumberLocation";
                break;
            case 18:
            case 19:
            case 20:
            case 21:
                HwLog.w(TAG, "bulkInsert: View is readonly. uri :" + uri);
                break;
            case 22:
            case 24:
                tableNameString = "tbKeywordsTable";
                break;
            default:
                HwLog.i(TAG, "Unknown bulkInsert uri = " + uri);
                break;
        }
        if (tableNameString.length() <= 0) {
            return 0;
        }
        try {
            db.beginTransaction();
            if (isBackupOrRecoverOperation(matchCode)) {
                for (ContentValues value2 : values) {
                    if (0 < db.insert(this.mDatabaseHelper.getRecoverTmpTableMap(tableNameString), null, value2)) {
                        increaseRecoverSucceedCount();
                        nInsertCount++;
                    } else {
                        increaseRecoverFailedCount();
                    }
                }
            } else {
                for (ContentValues value22 : values) {
                    if (0 < db.insert(tableNameString, null, value22)) {
                        nInsertCount++;
                    }
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            if (isBackupOrRecoverOperation(matchCode)) {
                resetRecoverStats();
            }
            nInsertCount = 0;
            HwLog.e(TAG, "bulkInsert Exception", e);
        } finally {
            db.endTransaction();
        }
        if (nInsertCount > 0) {
            bulkUpdateNumberLocationCache(db, matchCode, values);
            if (!isBackupOrRecoverOperation(matchCode)) {
                switch (matchCode) {
                    case 3:
                        this.mDatabaseHelper.checkRecordsCount(db, "interception_messages");
                        break;
                    case 5:
                        this.mDatabaseHelper.checkRecordsCount(db, "interception_calls");
                        break;
                }
                notifiChanged(uri);
            }
        }
        return nInsertCount;
    }

    public boolean onCreate() {
        if (this.mDatabaseHelper == null) {
            this.mDatabaseHelper = new DBHelper(getContext());
        }
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
        SQLiteDatabase db = this.mDatabaseHelper.getReadableDatabase();
        if (db == null) {
            HwLog.w(TAG, "query : Fail to getReadableDatabase, uri = " + uri);
            return null;
        }
        try {
            switch (mUriMatcher.match(uri)) {
                case 1:
                case 9:
                    return db.query("interception_blacklist", projection, selection, selectionArgs, null, null, orderBy);
                case 3:
                case 10:
                    HwLog.i(TAG, "URL= " + uri.toString());
                    Cursor cursor = db.query("interception_messages", projection, selection, selectionArgs, null, null, orderBy);
                    if (cursor == null) {
                        HwLog.e(TAG, "cursor is null");
                    } else {
                        HwLog.i(TAG, "cursor count= " + cursor.getCount());
                    }
                    return cursor;
                case 5:
                case 11:
                    return db.query("interception_calls", projection, selection, selectionArgs, null, null, orderBy);
                case 7:
                case 12:
                    return db.query("interception_rules", projection, selection, selectionArgs, null, null, orderBy);
                case 14:
                case 16:
                    return db.query("tbWhitelist", projection, selection, selectionArgs, null, null, orderBy);
                case 17:
                    return db.query("tbNumberLocation", projection, selection, selectionArgs, null, null, orderBy);
                case 18:
                    return db.query("vBlacklist", projection, selection, selectionArgs, null, null, orderBy);
                case 19:
                    return db.query("vCalls", projection, selection, selectionArgs, null, null, orderBy);
                case 20:
                    return db.query("vMessages", projection, selection, selectionArgs, null, null, orderBy);
                case 21:
                    return db.query("vWhitelist", projection, selection, selectionArgs, null, null, orderBy);
                case 22:
                case 24:
                    return db.query("tbKeywordsTable", projection, selection, selectionArgs, null, null, orderBy);
                case 28:
                    return queryBackupPrefers(getContext());
                default:
                    HwLog.i(TAG, "query: Unknown query uri " + uri);
                    break;
            }
        } catch (Exception e) {
            HwLog.e(TAG, "query: Exception", e);
        }
        return null;
    }

    public int update(Uri uri, ContentValues values, String where, String[] selectionArgs) {
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        if (db == null) {
            HwLog.w(TAG, "update : Fail to getReadableDatabase, uri = " + uri);
            return 0;
        }
        int updateRow = 0;
        int matchCode = mUriMatcher.match(uri);
        switch (matchCode) {
            case 1:
                updateRow = db.update("interception_blacklist", values, where, selectionArgs);
                break;
            case 3:
                updateRow = db.update("interception_messages", values, where, selectionArgs);
                break;
            case 5:
                updateRow = db.update("interception_calls", values, where, selectionArgs);
                break;
            case 7:
                updateRow = db.update("interception_rules", values, where, selectionArgs);
                break;
            case 14:
                updateRow = db.update("tbWhitelist", values, where, selectionArgs);
                break;
            case 17:
                updateRow = db.update("tbNumberLocation", values, where, selectionArgs);
                break;
            case 18:
            case 19:
            case 20:
            case 21:
                HwLog.w(TAG, "update: View is readonly. uri :" + uri);
                break;
            case 22:
                updateRow = db.update("tbKeywordsTable", values, where, selectionArgs);
                break;
            default:
                try {
                    HwLog.i(TAG, "update: Unsupported update uri :" + uri);
                    break;
                } catch (RuntimeException e) {
                    HwLog.e(TAG, "update: Exception", e);
                    break;
                } catch (Exception e2) {
                    HwLog.e(TAG, "update: Exception", e2);
                    break;
                }
        }
        if (updateRow > 0) {
            updateNumberLocationCache(db, matchCode, values);
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateRow;
    }

    public Bundle call(String method, String arg, Bundle extras) {
        if (method == null) {
            HwLog.w(TAG, "Call method is null");
            return null;
        } else if (method.equals(ConstValues.METHOD_QUERY_UNREAD_COUNT)) {
            return queryUnReadCount(arg, extras);
        } else {
            return super.call(method, arg, extras);
        }
    }

    public Bundle queryUnReadCount(String arg, Bundle extras) {
        if (extras == null || extras.isEmpty()) {
            HwLog.e(TAG, "queryUnReadCount: Invalid or empty extras");
            return null;
        }
        int count;
        Cursor cursor;
        Bundle result = new Bundle();
        if (extras.containsKey(ConstValues.KEY_UNREAD_COUNT_SMS)) {
            count = 0;
            long lastTime = PreferenceHelper.getLastWatchMessageTime(GlobalContext.getContext());
            cursor = query(Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), "interception_messages"), new String[]{"date"}, "date>?", new String[]{"" + lastTime}, null);
            if (!Utility.isNullOrEmptyCursor(cursor, true)) {
                count = cursor.getCount();
                cursor.close();
            }
            result.putInt(ConstValues.KEY_UNREAD_COUNT_SMS, count);
            HwLog.i(TAG, "queryUnReadCount: unread intercepted message count = " + count);
        }
        if (extras.containsKey(ConstValues.KEY_UNREAD_COUNT_CALL)) {
            count = 0;
            lastTime = PreferenceHelper.getLastWatchCallTime(GlobalContext.getContext());
            cursor = query(Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), "interception_calls"), new String[]{"date"}, "date>?", new String[]{"" + lastTime}, null);
            if (!Utility.isNullOrEmptyCursor(cursor, true)) {
                count = cursor.getCount();
                cursor.close();
            }
            result.putInt(ConstValues.KEY_UNREAD_COUNT_CALL, count);
            HwLog.i(TAG, "queryUnReadCount: unread intercepted call count = " + count);
        }
        return result;
    }

    private int setIntereptionPreference(ContentValues values) {
        if (values == null) {
            HwLog.w(TAG, "setIntereptionPreference : Invalid content values");
            return -1;
        }
        Context context = getContext();
        int nUpdateCount = -1;
        String keyString = values.getAsString(BackupConst.PREFERENCE_KEY);
        String valueString = values.getAsString(BackupConst.PREFERENCE_VALUE);
        HwLog.v(TAG, String.format("setIntereptionPreference : %1$s = %2$s", new Object[]{keyString, valueString}));
        if ("harassment_auto_update_state".equalsIgnoreCase(keyString)) {
            Boolean bAutoUpdate = Boolean.valueOf(Boolean.parseBoolean(valueString));
            HwLog.i(TAG, "setIntereptionPreference, set auto update:" + bAutoUpdate);
            UpdateHelper.setAutoUpdateStrategy(context, bAutoUpdate.booleanValue() ? 3 : 1);
            nUpdateCount = 1;
        } else if (PreferenceHelper.KEY_ONLY_WIFI_UPDATE_STATE.equalsIgnoreCase(keyString)) {
            Boolean bUpdateOnylWifi = Boolean.valueOf(Boolean.parseBoolean(valueString));
            int updateStrategy = UpdateHelper.getAutoUpdateStrategy(context);
            HwLog.i(TAG, "setIntereptionPreference, set onlywifistete:" + bUpdateOnylWifi + ", curStrategy:" + updateStrategy);
            if (updateStrategy == 3 && bUpdateOnylWifi.booleanValue()) {
                UpdateHelper.setAutoUpdateStrategy(context, 2);
            }
            nUpdateCount = 1;
        } else if (PreferenceHelper.KEY_RULE.equals(keyString)) {
            int strategy;
            if (1 == Integer.parseInt(valueString)) {
                strategy = StrategyId.BLOCK_INTELLIGENT.getValue();
            } else {
                strategy = StrategyId.BLOCK_BLACKLIST.getValue();
            }
            this.mDatabaseHelper.recoverRulesFrom8(strategy);
            nUpdateCount = 1;
        } else if (PreferenceHelper.KEY_STRATEGY_CONFIG.equals(keyString)) {
            this.mDatabaseHelper.recoverRulesFrom8(Integer.parseInt(valueString));
            nUpdateCount = 1;
        }
        return nUpdateCount;
    }

    private void updateNumberLocationCache(SQLiteDatabase db, int nIndicator, ContentValues value) {
        if (shouldUpdateNumberLocation(nIndicator)) {
            String phone = value.getAsString("phone");
            if (phone != null) {
                NumberLocationInfo location = NumberLocationHelper.queryNumberLocation(getContext(), phone);
                ContentValues locationValue = new ContentValues();
                locationValue.put("phone", phone);
                locationValue.put("location", location.getLocation());
                locationValue.put(TbNumberLocation.OPERATOR, location.getOperator());
                db.replace("tbNumberLocation", null, locationValue);
            }
        }
    }

    private void bulkUpdateNumberLocationCache(SQLiteDatabase db, int nIndicator, ContentValues[] values) {
        if (shouldUpdateNumberLocation(nIndicator)) {
            Set<String> phoneNumberSet = new HashSet();
            for (ContentValues value : values) {
                String phone = value.getAsString("phone");
                if (phone != null) {
                    phoneNumberSet.add(phone);
                }
            }
            this.mDatabaseHelper.updateNumberLocationCache(db, phoneNumberSet);
        }
    }

    private boolean isBackupOrRecoverOperation(int nIndicator) {
        switch (nIndicator) {
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 16:
            case 24:
                return true;
            default:
                return false;
        }
    }

    private boolean shouldUpdateNumberLocation(int nIndicator) {
        switch (nIndicator) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 14:
            case 15:
                return true;
            default:
                return false;
        }
    }

    protected int getDBVersion() {
        return DBHelper.getDBVersion();
    }

    protected ArrayList<String> getBackupSupportedUriList() {
        ArrayList<String> uriList = new ArrayList();
        uriList.add("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider/interception_blacklist_backup");
        uriList.add("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider/interception_messages_backup");
        uriList.add("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider/interception_calls_backup");
        uriList.add("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider/interception_rules_backup");
        uriList.add("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider/whitelist_backup");
        uriList.add("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider/keyword_backup");
        uriList.add("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider/interception_rules_prefer");
        return uriList;
    }

    protected boolean canRecoverDB(int nRecoverVersion) {
        HwLog.d(TAG, "canRecoverDB: Current version = " + getDBVersion() + ", nRecoverVersion = " + nRecoverVersion);
        return true;
    }

    protected boolean onRecoverStart(int nRecoverVersion) {
        HwLog.i(TAG, "onRecoverStart");
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        if (db != null) {
            return this.mDatabaseHelper.onRecoverStart(db, nRecoverVersion);
        }
        HwLog.w(TAG, "onRecoverStart: Fail to get getWritableDatabase");
        return false;
    }

    protected boolean onRecoverComplete(int nRecoverVersion) {
        HwLog.i(TAG, "onRecoverComplete");
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        if (db == null) {
            HwLog.w(TAG, "onRecoverComplete: Fail to get getWritableDatabase");
            return false;
        } else if (!this.mDatabaseHelper.onRecoverComplete(db, nRecoverVersion)) {
            return false;
        } else {
            notifiChanged(blacklist_uri);
            notifiChanged(whitelist_uri);
            notifiChanged(calls_uri);
            notifiChanged(messages_uri);
            notifiChanged(rules_uri);
            notifiChanged(keywords_uri);
            notifiChanged(BACKUP_END_RUI);
            for (String phone : DBAdapter.getBlacklistedPhones(GlobalContext.getContext())) {
                GoogleBlackListContract.addBlockedNumber(phone);
            }
            BlackWhiteDBDataUpdater.getInstance(GlobalContext.getContext(), null, 0).triggleUpdate();
            HwLog.i(TAG, "onRecoverComplete: Success = " + getRecoverSucceedCount() + ", Failure = " + getRecoverFailedCount());
            return true;
        }
    }

    private Cursor queryBackupPrefers(Context ctx) {
        ContentValues values = RulesOps.getAllRules(ctx);
        values.put("harassment_auto_update_state", Integer.valueOf(UpdateHelper.getAutoUpdateStrategy(ctx)));
        return BackupUtil.getPreferenceCursor(values);
    }

    private void recoverBackupPrefers(Context ctx, ContentValues values) {
        String keyString = values.getAsString(BackupConst.PREFERENCE_KEY);
        if ("harassment_auto_update_state".equals(keyString)) {
            int strategy = values.getAsInteger(BackupConst.PREFERENCE_VALUE).intValue();
            UpdateHelper.setAutoUpdateStrategy(ctx, strategy);
            HwLog.i(TAG, "recove auto update strategy, strategy:" + strategy);
            return;
        }
        String valueString = values.getAsString(BackupConst.PREFERENCE_VALUE);
        ValuePrefer.putValueString(ctx, keyString, valueString);
        HwLog.i(TAG, "recoverBackupPrefers, key:" + keyString + ", value:" + valueString);
    }
}
