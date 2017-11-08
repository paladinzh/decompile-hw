package com.huawei.notificationmanager.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.notificationmanager.common.CommonObjects.NotificationCfgInfo;
import com.huawei.notificationmanager.common.ConstValues;
import com.huawei.notificationmanager.common.NotificationBackend;
import com.huawei.notificationmanager.common.NotificationUtils;
import com.huawei.notificationmanager.util.Helper;
import com.huawei.systemmanager.backup.HsmContentProvider;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import com.huawei.systemmanager.util.app.HsmPkgUtils;
import com.huawei.timekeeper.AbsTimeKeeper;
import java.util.ArrayList;
import java.util.List;

public class DBProvider extends HsmContentProvider {
    private static final String AUTH = "com.huawei.systemmanager.NotificationDBProvider";
    private static final String CFG_ANDROID_ORIGIN_BACKUP_URI = "origin_notificationCfg_backup";
    private static final int CFG_ANDROID_ORIGIN_BACKUP_URI_INDICATOR = 6;
    private static final String CFG_BACKUP_URI = "notificationCfg_backup";
    private static final int CFG_BACKUP_URI_INDICATOR = 3;
    private static final String CFG_URI = "notificationCfg";
    private static final int CFG_URI_INDICATOR = 1;
    private static final String COL_APP_NAME = "appname";
    private static final String COL_NOTIFICATION_IS_ENABLE = "isNofiticationEnable";
    private static final String COL_NOTIFICATION_IS_HIGHT_PRORITY = "isNotificationHighPrority";
    private static final String LOG_BACKUP_URI = "notifyLogTable_backup";
    private static final int LOG_BACKUP_URI_INDICATOR = 4;
    private static final String LOG_URI = "notificationLog";
    private static final int LOG_URI_INDICATOR = 2;
    private static final String NOTIFICATION_CFG_TABLE = "tbNotificationMgrCfg";
    private static final String NOTIFICATION_LOG_TABLE = "tbNotificationMgrLog";
    private static final String PREFERENCE_BACKUP_URI = "notification_rulepreference_backup";
    private static final int PREFERENCE_BACKUP_URI_INDICATOR = 5;
    private static final String TAG = "NotificationDBProvider";
    public static final Uri URI_NOTIFICATION_CFG = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.NotificationDBProvider"), "notificationCfg");
    public static final Uri URI_NOTIFICATION_LOG = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.NotificationDBProvider"), LOG_URI);
    private static UriMatcher mUriMatcher = new UriMatcher(-1);
    private DBHelper mDatabaseHelper;

    static {
        mUriMatcher.addURI(AUTH, "notificationCfg", 1);
        mUriMatcher.addURI(AUTH, LOG_URI, 2);
        mUriMatcher.addURI(AUTH, CFG_BACKUP_URI, 3);
        mUriMatcher.addURI(AUTH, LOG_BACKUP_URI, 4);
        mUriMatcher.addURI(AUTH, PREFERENCE_BACKUP_URI, 5);
        mUriMatcher.addURI(AUTH, CFG_ANDROID_ORIGIN_BACKUP_URI, 6);
    }

    public Bundle call(String method, String arg, Bundle extras) {
        if (method == null) {
            HwLog.w(TAG, "Call method is null");
            return null;
        } else if (method.equals(ConstValues.METHOD_NOTIFICATION_LIST_QUERY)) {
            return queryNotificationList(arg);
        } else {
            if (method.equals(ConstValues.METHOD_NOTIFICATION_ITEM_QUERY)) {
                return queryNotificationItem(arg, extras);
            }
            if (method.equals(ConstValues.METHOD_NOTIFICATION_LIMITS_QUERY)) {
                return queryNotificationLimits();
            }
            if (method.equals(ConstValues.METHOD_SET_SOUND_VIBRATE)) {
                return setSoundVibrate(extras);
            }
            return super.call(method, arg, extras);
        }
    }

    private Bundle setSoundVibrate(Bundle extras) {
        if (extras == null) {
            HwLog.w(TAG, "setSoundVibrate bundle is null");
            return null;
        }
        String pkg = extras.getString("package_name", "");
        int soundVibrate = extras.getInt(ConstValues.BUNDLE_KEY_SOUND_VIBRATE_NOTIFICATION_ITEM, -2);
        int uid = extras.getInt("uid", AbsTimeKeeper.USER_NULL);
        if (TextUtils.isEmpty(pkg) || soundVibrate <= -2) {
            HwLog.w(TAG, "setSoundVibrate bundle is invalid");
            return null;
        }
        HwLog.i(TAG, "setSoundVibrate pkg=" + pkg + ", uid=" + uid + ", soundVibrate=" + soundVibrate);
        if (-1 == soundVibrate) {
            if (AbsTimeKeeper.USER_NULL != uid) {
                new NotificationBackend().setNotificationsBanned(pkg, uid, false);
                notifyUI();
            }
            return null;
        }
        try {
            ContentValues cv = new ContentValues();
            cv.put(ConstValues.NOTIFICATION_SOUND_VIBRATE, Integer.valueOf(soundVibrate));
            if (update(URI_NOTIFICATION_CFG, cv, "packageName = ? ", new String[]{pkg}) <= 0) {
                cv = new NotificationCfgInfo(uid, pkg).getAsContentValue();
                cv.put(ConstValues.NOTIFICATION_SOUND_VIBRATE, Integer.valueOf(soundVibrate));
                HwLog.i(TAG, "setSoundVibrate insert cv=" + cv);
                insert(URI_NOTIFICATION_CFG, cv);
            }
            notifyUI();
        } catch (RuntimeException e) {
            HwLog.w(TAG, "setSoundVibrate exception", e);
        }
        return null;
    }

    private void notifyUI() {
        NotificationUtils.notifyCfgChange(getContext(), false);
    }

    private Bundle queryNotificationList(String arg) {
        Cursor cursor = query(URI_NOTIFICATION_CFG, null, null, null, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            HwLog.w(TAG, "queryNotificationList: Fail to get cfg from DB");
            return null;
        } else if (cursor.moveToFirst()) {
            int nColIndexPkgName = cursor.getColumnIndex("packageName");
            Bundle result = new Bundle();
            int nType = Integer.parseInt(arg);
            HwLog.w(TAG, "queryNotificationList: nType = " + nType);
            switch (nType) {
                case 1:
                    result.putParcelableArrayList(ConstValues.BUNDLE_KEY_STATUSBAR_NOTIFICATION_LIST, queryNotificationCfgs(cursor, nColIndexPkgName, cursor.getColumnIndex(ConstValues.NOTIFICATION_STATUSBAR_CFG)));
                    break;
                case 2:
                    result.putParcelableArrayList(ConstValues.BUNDLE_KEY_BANNERS_NOTIFICATION_LIST, queryNotificationCfgs(cursor, nColIndexPkgName, cursor.getColumnIndex(ConstValues.NOTIFICATION_HEADSUP_CFG)));
                    break;
                case 3:
                    result.putParcelableArrayList(ConstValues.BUNDLE_KEY_STATUSBAR_NOTIFICATION_LIST, queryNotificationCfgs(cursor, nColIndexPkgName, cursor.getColumnIndex(ConstValues.NOTIFICATION_STATUSBAR_CFG)));
                    cursor.moveToFirst();
                    result.putParcelableArrayList(ConstValues.BUNDLE_KEY_BANNERS_NOTIFICATION_LIST, queryNotificationCfgs(cursor, nColIndexPkgName, cursor.getColumnIndex(ConstValues.NOTIFICATION_HEADSUP_CFG)));
                    break;
                case 4:
                    result.putParcelableArrayList(ConstValues.BUNDLE_KEY_LOCKSCREEN_NOTIFICATION_LIST, queryNotificationCfgs(cursor, nColIndexPkgName, cursor.getColumnIndex(ConstValues.NOTIFICATION_LOCKSCREEN_CFG)));
                    break;
                case 5:
                    result.putParcelableArrayList(ConstValues.BUNDLE_KEY_SOUND_VIBRATE_NOTIFICATION_LIST, queryNotificationCfgs(cursor, nColIndexPkgName, cursor.getColumnIndex(ConstValues.NOTIFICATION_SOUND_VIBRATE)));
                    break;
                default:
                    HwLog.w(TAG, "queryNotificationList: Invalid arg = " + arg);
                    result = null;
                    break;
            }
            cursor.close();
            return result;
        } else {
            HwLog.w(TAG, "queryNotificationList: Fail to read cursor");
            cursor.close();
            return null;
        }
    }

    private ArrayList<ContentValues> queryNotificationCfgs(Cursor cursor, int nColIndexPkgName, int nColIndexCfg) {
        ArrayList<ContentValues> cfglist = new ArrayList();
        do {
            ContentValues contentvalues = new ContentValues();
            contentvalues.put(cursor.getString(nColIndexPkgName), Integer.valueOf(cursor.getInt(nColIndexCfg)));
            cfglist.add(contentvalues);
        } while (cursor.moveToNext());
        return cfglist;
    }

    private Bundle queryNotificationItem(String arg, Bundle extras) {
        Bundle bundle = new Bundle();
        if (extras == null) {
            HwLog.w(TAG, "queryNotificationItem: Invalid extras");
            return bundle;
        }
        String pkgname = extras.getString("package_name", "");
        HwLog.d(TAG, "queryNotificationItem: pkgname = " + pkgname);
        if (TextUtils.isEmpty(pkgname)) {
            return bundle;
        }
        Cursor cursor = query(URI_NOTIFICATION_CFG, null, "packageName = ? ", new String[]{pkgname}, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            HwLog.w(TAG, "queryNotificationItem: Fail to get cfg from DB");
            return bundle;
        }
        int nColLockscreenCfgIndex = cursor.getColumnIndex(ConstValues.NOTIFICATION_LOCKSCREEN_CFG);
        int nColStatusbarCfgIndex = cursor.getColumnIndex(ConstValues.NOTIFICATION_STATUSBAR_CFG);
        int nColHeadsupCfgIndex = cursor.getColumnIndex(ConstValues.NOTIFICATION_HEADSUP_CFG);
        if (cursor.moveToNext()) {
            int cfgvalue;
            switch (Integer.parseInt(arg)) {
                case 1:
                    cfgvalue = cursor.getInt(nColStatusbarCfgIndex);
                    HwLog.d(TAG, "STATUS_BAR_TYPE  cfgvalue: " + cfgvalue);
                    bundle.putInt(ConstValues.BUNDLE_KEY_STATUSBAR_NOTIFICATION_ITEM, cfgvalue);
                    break;
                case 2:
                    cfgvalue = cursor.getInt(nColHeadsupCfgIndex);
                    HwLog.d(TAG, "BANNERS_TYPE  cfgvalue: " + cfgvalue);
                    bundle.putInt(ConstValues.BUNDLE_KEY_BANNERS_NOTIFICATION_ITEM, cfgvalue);
                    break;
                case 3:
                    cfgvalue = cursor.getInt(nColStatusbarCfgIndex);
                    HwLog.d(TAG, "STATUSBAR_BANNERS_TYPE  status bar  cfgvalue: " + cfgvalue);
                    bundle.putInt(ConstValues.BUNDLE_KEY_STATUSBAR_NOTIFICATION_ITEM, cfgvalue);
                    cfgvalue = cursor.getInt(nColHeadsupCfgIndex);
                    HwLog.d(TAG, "STATUSBAR_BANNERS_TYPE  banners  cfgvalue: " + cfgvalue);
                    bundle.putInt(ConstValues.BUNDLE_KEY_BANNERS_NOTIFICATION_ITEM, cfgvalue);
                    break;
                case 4:
                    cfgvalue = cursor.getInt(nColLockscreenCfgIndex);
                    HwLog.d(TAG, "LOCKSCREEN_TYPE  cfgvalue: " + cfgvalue);
                    bundle.putInt(ConstValues.BUNDLE_KEY_LOCKSCREEN_NOTIFICATION_ITEM, cfgvalue);
                    break;
            }
        }
        cursor.close();
        return bundle;
    }

    private Bundle queryNotificationLimits() {
        Bundle bundle = new Bundle();
        try {
            bundle.putStringArrayList(ConstValues.BUNDLE_KEY_SYSTEMUI_NOTIFICATION_LIST, Helper.getMonitoredPackageName());
        } catch (Exception e) {
            HwLog.i(TAG, "exception in queryNotificationLimits");
        }
        HwLog.i(TAG, "queryNotificationLimits , return bundle");
        return bundle;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int delete(Uri uri, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        if (db == null) {
            HwLog.w(TAG, "delete : fail to get getWritableDatabase, uri = " + uri);
            return 0;
        }
        int nDelRows = 0;
        int matchCode = mUriMatcher.match(uri);
        switch (matchCode) {
            case 1:
                nDelRows = db.delete("tbNotificationMgrCfg", whereClause, whereArgs);
                break;
            case 2:
                nDelRows = db.delete("tbNotificationMgrLog", whereClause, whereArgs);
                break;
            default:
                try {
                    HwLog.i(TAG, "delete : UnSupported uri = " + uri);
                    break;
                } catch (Exception e) {
                    HwLog.e(TAG, "delete exception", e);
                    break;
                }
        }
        if (nDelRows > 0) {
            notifyDBChange(matchCode, uri);
        }
        return nDelRows;
    }

    public String getType(Uri arg0) {
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Uri insert(Uri uri, ContentValues values) {
        if (values == null) {
            HwLog.w(TAG, "insert : Invalid values, uri = " + uri);
            return null;
        }
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        if (db == null) {
            HwLog.w(TAG, "insert : fail to get getWritableDatabase, uri = " + uri);
            return null;
        }
        Uri retUri = uri;
        long newRecordId = -1;
        int matchCode = mUriMatcher.match(uri);
        switch (matchCode) {
            case 1:
                newRecordId = db.replace("tbNotificationMgrCfg", null, values);
                break;
            case 2:
                newRecordId = db.insert("tbNotificationMgrLog", null, values);
                if (newRecordId >= 0) {
                    limitLogCountForApp(db, values.getAsString("packageName"));
                    break;
                }
                break;
            case 3:
            case 4:
            case 5:
                newRecordId = restoreFromBackupData(matchCode, db, values);
                break;
            case 6:
                NotificationUtils.setAndroidNotification(values);
                break;
            default:
                try {
                    HwLog.i(TAG, "insert : Unknown insert uri = " + uri);
                    break;
                } catch (Exception e) {
                    HwLog.e(TAG, "insert exception", e);
                    break;
                }
        }
        if (newRecordId != -1) {
            retUri = Uri.withAppendedPath(uri, String.valueOf(newRecordId));
            notifyDBChange(matchCode, uri);
        } else {
            HwLog.i(TAG, "insert failed! uri = " + uri);
        }
        return retUri;
    }

    private Cursor queryNotificationLog(SQLiteDatabase db, String packageName) {
        return db.query("tbNotificationMgrLog", null, "packageName = ?", new String[]{packageName}, null, null, "logDatetime DESC");
    }

    private void limitLogCountForApp(SQLiteDatabase db, String packageName) {
        Cursor cursor = queryNotificationLog(db, packageName);
        if (cursor != null) {
            if (cursor.getCount() <= 15) {
                cursor.close();
                return;
            }
            int nIndex = 0;
            while (cursor.moveToNext()) {
                nIndex++;
                if (nIndex >= 15) {
                    break;
                }
            }
            int nID = cursor.getInt(cursor.getColumnIndex("_id"));
            cursor.close();
            deleteLogWithSmallerID(db, packageName, nID);
        }
    }

    private int deleteLogWithSmallerID(SQLiteDatabase db, String packageName, int nID) {
        return db.delete("tbNotificationMgrLog", "packageName = ? AND _id < ?", new String[]{packageName, String.valueOf(nID)});
    }

    private long restoreFromBackupData(int uriMatchCode, SQLiteDatabase db, ContentValues values) {
        long newRecordId = -1;
        switch (uriMatchCode) {
            case 3:
                newRecordId = restoreNotificationCfgItem(db, values);
                break;
            case 4:
                newRecordId = restoreNotificationLogItem(db, values);
                break;
            case 5:
                newRecordId = (long) setNotificationCfgPreference(values);
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
        int i = 0;
        if (values == null || values.length <= 0) {
            HwLog.w(TAG, "bulkInsert : Invalid values, uri = " + uri);
            return 0;
        }
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        if (db == null) {
            HwLog.w(TAG, "bulkInsert: Fail to getWritableDatabase, uri = " + uri);
            return 0;
        }
        int nInsertCount = 0;
        int matchCode = mUriMatcher.match(uri);
        switch (matchCode) {
            case 1:
                nInsertCount = (int) doCommonBulkInsert(db, "tbNotificationMgrCfg", values, true);
                break;
            case 2:
                nInsertCount = (int) doCommonBulkInsert(db, "tbNotificationMgrLog", values, false);
                break;
            case 3:
                nInsertCount = (int) doRestoreBulkInsert(db, "tbNotificationMgrCfg", values);
                break;
            case 4:
                nInsertCount = (int) doRestoreBulkInsert(db, "tbNotificationMgrLog", values);
                break;
            case 5:
                int length = values.length;
                while (i < length) {
                    if (-1 != setNotificationCfgPreference(values[i])) {
                        increaseRecoverSucceedCount();
                        nInsertCount++;
                    } else {
                        increaseRecoverFailedCount();
                    }
                    i++;
                }
                return nInsertCount;
            case 6:
                nInsertCount = NotificationUtils.setAndroidNotifications(values);
                break;
            default:
                HwLog.i(TAG, "Unknown bulkInsert uri = " + uri);
                break;
        }
        if (nInsertCount > 0) {
            notifyDBChange(matchCode, uri);
        }
        return nInsertCount;
    }

    private long doCommonBulkInsert(SQLiteDatabase db, String tableName, ContentValues[] values, boolean isReplace) {
        long nInsertCount = 0;
        try {
            db.beginTransaction();
            if (isReplace) {
                for (ContentValues value : values) {
                    if (-1 != db.replace(tableName, null, value)) {
                        nInsertCount++;
                    }
                }
            } else {
                for (ContentValues value2 : values) {
                    if (-1 != db.insert(tableName, null, value2)) {
                        nInsertCount++;
                    }
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            nInsertCount = 0;
            HwLog.e(TAG, "doCommonBulkInsert exception", e);
        } finally {
            db.endTransaction();
        }
        return nInsertCount;
    }

    private long doRestoreBulkInsert(SQLiteDatabase db, String tableName, ContentValues[] values) {
        int nInsertCount = 0;
        ArrayList<ContentValues> validValues = new ArrayList();
        for (ContentValues value : values) {
            String packageName = value.getAsString("packageName");
            if (-1 != HsmPkgUtils.getPackageUid(packageName)) {
                validValues.add(value);
            } else {
                HwLog.i(TAG, "doRestoreBulkInsert does not exist, packageName = " + packageName);
            }
        }
        if (validValues.size() <= 0) {
            HwLog.i(TAG, "doRestoreBulkInsert: No data needs to berecovered");
            return 0;
        }
        try {
            db.beginTransaction();
            for (ContentValues value2 : validValues) {
                if (-1 != db.insert(tableName, null, value2)) {
                    increaseRecoverSucceedCount();
                    nInsertCount++;
                } else {
                    increaseRecoverFailedCount();
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            decreaseRecoverSucceedCount(nInsertCount);
            increaseRecoverFailedCount(nInsertCount);
            nInsertCount = 0;
            HwLog.e(TAG, "doRestoreBulkInsert exception", e);
        } finally {
            db.endTransaction();
        }
        return (long) nInsertCount;
    }

    public boolean onCreate() {
        this.mDatabaseHelper = new DBHelper(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
        SQLiteDatabase db = this.mDatabaseHelper.getReadableDatabase();
        if (db == null) {
            HwLog.w(TAG, "query : fail to get getReadableDatabase");
            return null;
        }
        switch (mUriMatcher.match(uri)) {
            case 1:
            case 3:
                return db.query("tbNotificationMgrCfg", projection, selection, selectionArgs, null, null, orderBy);
            case 2:
            case 4:
                return db.query("tbNotificationMgrLog", projection, selection, selectionArgs, null, null, orderBy);
            case 5:
                return getNotificationPreferenceCursor();
            case 6:
                return NotificationUtils.getAndroidNotificationCursor();
            default:
                try {
                    HwLog.i(TAG, "Unknown query uri = " + uri);
                } catch (Exception e) {
                    HwLog.e(TAG, "query exception", e);
                }
                return null;
        }
    }

    public int update(Uri uri, ContentValues values, String where, String[] selectionArgs) {
        SQLiteDatabase db = this.mDatabaseHelper.getReadableDatabase();
        if (db == null) {
            HwLog.w(TAG, "update : fail to get getReadableDatabase");
            return 0;
        }
        int updatedRow = 0;
        int matchCode = mUriMatcher.match(uri);
        switch (matchCode) {
            case 1:
                updatedRow = db.update("tbNotificationMgrCfg", values, where, selectionArgs);
                break;
            case 2:
                updatedRow = db.update("tbNotificationMgrLog", values, where, selectionArgs);
                break;
            default:
                try {
                    HwLog.i(TAG, "UnSupported update uri :" + uri);
                    break;
                } catch (Exception e) {
                    HwLog.e(TAG, "update exception", e);
                    break;
                }
        }
        if (updatedRow > 0) {
            notifyDBChange(matchCode, uri);
        }
        return updatedRow;
    }

    public Cursor getNotificationPreferenceCursor() {
        List<HsmPkgInfo> installedApps = Helper.getMonitoredAppList(getContext());
        if (Utility.isNullOrEmptyList(installedApps)) {
            return null;
        }
        MatrixCursor cursor = new MatrixCursor(new String[]{"appname", COL_NOTIFICATION_IS_ENABLE, COL_NOTIFICATION_IS_HIGHT_PRORITY});
        for (HsmPkgInfo appInfo : installedApps) {
            Boolean bNotificationCfg = Boolean.valueOf(Helper.areNotificationsEnabledForPackage(appInfo.mPkgName, appInfo.mUid));
            Boolean bNotificationPrority = Boolean.valueOf(Helper.areNotificationHighPriority(appInfo.mPkgName, appInfo.mUid));
            cursor.addRow(new Object[]{appInfo.mPkgName, bNotificationCfg.toString(), bNotificationPrority.toString()});
            HwLog.v(TAG, String.format("getPreferenceCursor:%1$s = %2$s, %3$s", new Object[]{appInfo.mPkgName, bNotificationCfg, bNotificationPrority}));
        }
        HwLog.v(TAG, "getPreferenceCursor : count = " + cursor.getCount());
        return cursor;
    }

    private int setNotificationCfgPreference(ContentValues values) {
        if (values == null) {
            HwLog.w(TAG, "setNotificationRulePreference : Invalid values");
            return -1;
        }
        String appName = values.getAsString("appname");
        Boolean isEnable = values.getAsBoolean(COL_NOTIFICATION_IS_ENABLE);
        Boolean isHighPrority = values.getAsBoolean(COL_NOTIFICATION_IS_HIGHT_PRORITY);
        int appUid = HsmPkgUtils.getPackageUid(appName);
        if (-1 != appUid) {
            Helper.setNotificationsEnabledForPackage(appName, appUid, isEnable);
            Helper.setHighPriority(appName, appUid, isHighPrority.booleanValue());
            HwLog.d(TAG, String.format("setNotificationCfgPreference : %1$s = %2$s,%3$s", new Object[]{appName, Integer.valueOf(appUid), isHighPrority}));
            return 1;
        }
        HwLog.i(TAG, "setNotificationCfgPreference: app does not exist. packageName = " + appName);
        return -2;
    }

    private long restoreNotificationCfgItem(SQLiteDatabase db, ContentValues values) {
        String packageName = values.getAsString("packageName");
        HwLog.d(TAG, "restoreNotificationCfgItem: packageName = " + packageName + ", values = " + values);
        if (-1 != HsmPkgUtils.getPackageUid(packageName)) {
            return db.insert(this.mDatabaseHelper.getRecoverTmpTableMap("tbNotificationMgrCfg"), null, values);
        }
        HwLog.w(TAG, "restoreNotificationCfgItem: App does not exist, packageName = " + packageName);
        return -2;
    }

    private long restoreNotificationLogItem(SQLiteDatabase db, ContentValues values) {
        String packageName = values.getAsString("packageName");
        if (-1 != HsmPkgUtils.getPackageUid(packageName)) {
            return db.insert(this.mDatabaseHelper.getRecoverTmpTableMap("tbNotificationMgrLog"), null, values);
        }
        HwLog.i(TAG, "restoreNotificationLogItem: App does not exist, packageName = " + packageName);
        return -2;
    }

    private void notifyDBChange(int matchCode, Uri uri) {
        switch (matchCode) {
            case 1:
            case 2:
                getContext().getContentResolver().notifyChange(uri, null);
                return;
            default:
                return;
        }
    }

    protected int getDBVersion() {
        return DBHelper.getDBVersion();
    }

    protected ArrayList<String> getBackupSupportedUriList() {
        ArrayList<String> uriList = new ArrayList();
        uriList.add("content://com.huawei.systemmanager.NotificationDBProvider/notificationCfg_backup");
        uriList.add("content://com.huawei.systemmanager.NotificationDBProvider/notifyLogTable_backup");
        uriList.add("content://com.huawei.systemmanager.NotificationDBProvider/notification_rulepreference_backup");
        uriList.add("content://com.huawei.systemmanager.NotificationDBProvider/origin_notificationCfg_backup");
        return uriList;
    }

    protected boolean canRecoverDB(int nRecoverVersion) {
        HwLog.i(TAG, "canRecoverDB: Try to recover from version : " + nRecoverVersion + ", Current version : " + getDBVersion());
        return true;
    }

    protected boolean onRecoverStart(int nRecoverVersion) {
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        if (db != null) {
            return this.mDatabaseHelper.onRecoverStart(db, nRecoverVersion);
        }
        HwLog.w(TAG, "onRecoverStart: Fail to get getWritableDatabase");
        return false;
    }

    public boolean onRecoverComplete(int nRecoverVersion) {
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        if (db == null) {
            HwLog.w(TAG, "onRecoverComplete: Fail to get getWritableDatabase");
            return false;
        } else if (!this.mDatabaseHelper.onRecoverComplete(db, nRecoverVersion)) {
            return false;
        } else {
            HwLog.i(TAG, "onRecoverComplete: Success = " + getRecoverSucceedCount() + ", Failure = " + getRecoverFailedCount());
            Helper.setCfgChangeFlag(getContext(), true);
            Helper.setLogChangeFlag(getContext(), true);
            Helper.setCfgBackupCompleted(getContext(), true);
            ContentResolver resolver = getContext().getContentResolver();
            resolver.notifyChange(URI_NOTIFICATION_CFG, null);
            resolver.notifyChange(URI_NOTIFICATION_LOG, null);
            return true;
        }
    }
}
