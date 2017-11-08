package com.huawei.permissionmanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.huawei.notificationmanager.common.ConstValues;
import com.huawei.notificationmanager.db.DBProvider;
import com.huawei.notificationmanager.util.Helper;
import com.huawei.permissionmanager.utils.CommonFunctionUtil;
import com.huawei.permissionmanager.utils.SettingsDbUtils;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.permissionmanager.utils.SharedPrefUtils;
import com.huawei.systemmanager.addviewmonitor.AddViewAppManager;
import com.huawei.systemmanager.backup.BackupUtil;
import com.huawei.systemmanager.backup.HsmSQLiteOpenHelper;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.TimeUtil;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.rainbow.db.CloudDBAdapter;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

public class DBHelper extends HsmSQLiteOpenHelper {
    public static final int ACTION_ALLOW = 11;
    public static final int ACTION_REJECT = 10;
    public static final int APP_TRUST_DEFAULT_VALUE = 10;
    public static final int APP_TRUST_VALUE = 1;
    public static final String APP_UID = "uid";
    public static final int APP_UNTRUST_VALUE = 0;
    public static final int AWAKED_APP_NOTIFY_SWITCH_KEY = 20160906;
    public static final int AWAKED_APP_NOTIFY_SWITCH_OFF = 1;
    public static final int AWAKED_APP_NOTIFY_SWITCH_ON = 0;
    public static final String BLOCK_TABLE_NAME = "permissionCfg";
    public static final Uri BLOCK_TABLE_NAME_URI = Uri.parse("content://com.huawei.permissionmanager.provider.PermissionDataProvider/permission");
    public static final String COLUMN_ACTION = "action";
    public static final String COLUMN_COUNT = "count";
    public static final String COLUMN_DATE_START_TIME = "date_start_time";
    public static final String COLUMN_PERMISSION_TYPE = "permission_type";
    public static final String COLUMN_TIME_STAMP = "time_stamp";
    public static final String COMMON_TABLE_NAME = "commonTable";
    public static final int DATABASE_VERSION = 16;
    public static final String DB_NAME = "permission.db";
    public static final long HISTORY_MAX_SIZE = 10000;
    public static final Uri HISTORY_URI = Uri.parse("content://com.huawei.permissionmanager.provider.PermissionDataProvider/history_table");
    public static final int HOLD_DIALOG_CFG = 20121109;
    public static final int HOLD_DIALOG_DEFAULT = 0;
    public static final int HOLD_DIALOG_OFF = 2;
    public static final int HOLD_DIALOG_ON = 1;
    private static final String HOTA_TOVERSION5_THREAD = "init_toversion5_thread";
    public static final String ID = "_id";
    private static final String INIT_PHONEID_THREAD = "init_phoneid_thread";
    private static final String INSERT_HISTORY_SQL_1 = "packageName=? AND date_start_time=? AND permission_type=? AND action=?";
    public static final String KEY = "key";
    public static final String KEY_CONTENT_VALUE = "content_value";
    public static final String KEY_TRUST_APP = "trust_app";
    public static final String LABEL_TRUST = "true";
    public static final String LOG_ALLOW = "allow";
    public static final String LOG_DATE_TIME = "logDatetime";
    public static final String LOG_TABLE_NAME = "logRecord";
    private static final String LOG_TAG = "PermissionDbHelper";
    private static final long MAX_COUNT = 99999;
    private static final int MAX_RECORD_DAYS = 30;
    public static final String METHOD_RECORD_HISTORY = "method_record_history";
    public static final String PACKAGE_NAME = "packageName";
    public static final String PERMISSION_CFG = "permissionCfg";
    public static final String PERMISSION_CODE = "permissionCode";
    public static final Uri PERM_FLAG_URI = Uri.parse("content://com.huawei.permissionmanager.provider.PermissionDataProvider/perm_group_flag");
    public static final String PERM_GROUP = "permissionGroup";
    public static final String PERM_GROUP_FLAG = "flag";
    public static final String PERM_TYPE = "permissionType";
    public static final Uri PREPERMISSION_URI = Uri.parse("content://com.huawei.permissionmanager.provider.PermissionDataProvider/prePermission");
    public static final String PRE_PERMISSION_CFG = "prePermissionCfg";
    public static final String PRE_TRUST_CFG = "trust";
    public static final String PRE_TYPE_CFG = "type";
    private static final String QUERY_HISTORY_SQL_1 = "select time_stamp FROM history_table ORDER BY time_stamp DESC LIMIT 1 OFFSET 5000";
    private static final int RECORD_DELETE_LIMIT = 5000;
    public static final String RUNTIME_PERMS_TABLE_NAME = "runtimePermissions";
    public static final Uri RUNTIME_TABLE_URI = Uri.parse("content://com.huawei.permissionmanager.provider.PermissionDataProvider/runtimePermissions");
    public static final String TABLE_HISTORY = "history_table";
    public static final String TABLE_PERMISSIO_GROUP_FLAG = "perm_group_flag";
    public static final int TOAST_SWITCH_KEY = 20140626;
    public static final int TOAST_SWITCH_OFF = 1;
    public static final int TOAST_SWITCH_ON = 0;
    public static final String TRUST = "trust";
    public static final String VALUE = "value";
    public static final int VERSION_M = 15;
    private static Object syncObj = new Object();
    private Context mContext = null;
    private SQLiteDatabase mDatabase;
    private int mRestoreVersion = -1;
    private HashSet<String> restoredApps = new HashSet();
    private HashSet<String> restoredTrustedApps = new HashSet();

    class DBUpgradeFrom8To9Helper {
        DBUpgradeFrom8To9Helper() {
        }

        public void doUpgrade(SQLiteDatabase db) {
            HwLog.i(DBHelper.LOG_TAG, "upgradeDatabase8ToVersion9 starts");
            Cursor cursorNotifificationCfg = db.query("notificationCfg", null, null, null, null, null, null);
            if (!Utility.isNullOrEmptyCursor(cursorNotifificationCfg, true)) {
                restoreNotificationMgrCfgs(cursorNotifificationCfg);
                cursorNotifificationCfg.close();
            }
            Cursor cursorNotifificationLog = db.query(ConstValues.OLDVER_TB_NOTIFICATIONMGR_LOG, null, null, null, null, null, null);
            if (!Utility.isNullOrEmptyCursor(cursorNotifificationLog, true)) {
                restoreNotificationMgrLogs(cursorNotifificationLog);
                cursorNotifificationLog.close();
            }
            db.execSQL("DROP TABLE IF EXISTS notificationCfg");
            db.execSQL("DROP TABLE IF EXISTS notifyLogTable");
            HwLog.i(DBHelper.LOG_TAG, "upgradeDatabase8ToVersion9 ends");
        }

        private void restoreNotificationMgrCfgs(Cursor cursor) {
            ContentValues[] values = new ContentValues[cursor.getCount()];
            int nIndex = 0;
            int nColIndexPkgName = cursor.getColumnIndex("packageName");
            int nColIndexPermissionCfg = cursor.getColumnIndex("permissionCfg");
            int nColIndexPermissionCode = cursor.getColumnIndex("permissionCode");
            while (cursor.moveToNext()) {
                String packageName = cursor.getString(nColIndexPkgName);
                int nNotificationCfg = Helper.getNotificationCfgFromOldMask(cursor.getInt(nColIndexPermissionCfg), cursor.getInt(nColIndexPermissionCode));
                ContentValues value = new ContentValues();
                value.put("packageName", packageName);
                value.put(ConstValues.NOTIFICATION_CFG, Integer.valueOf(nNotificationCfg));
                int nIndex2 = nIndex + 1;
                values[nIndex] = value;
                nIndex = nIndex2;
            }
            HwLog.i(DBHelper.LOG_TAG, "restoreNotificationMgrCfgs: total count = " + nIndex + ", nRestoreCount count = " + DBHelper.this.mContext.getContentResolver().bulkInsert(DBProvider.URI_NOTIFICATION_CFG, values));
        }

        private void restoreNotificationMgrLogs(Cursor cursor) {
            ContentValues[] values = new ContentValues[cursor.getCount()];
            int nIndex = 0;
            int nColIndexPkgName = cursor.getColumnIndex("packageName");
            int nColIndexLogTime = cursor.getColumnIndex("logDatetime");
            int nColIndexLogTitle = cursor.getColumnIndex("logTitle");
            int nColIndexLogText = cursor.getColumnIndex("logText");
            while (cursor.moveToNext()) {
                String packageName = cursor.getString(nColIndexPkgName);
                long lLogTime = cursor.getLong(nColIndexLogTime);
                String strLogTitle = cursor.getString(nColIndexLogTitle);
                String strLogText = cursor.getString(nColIndexLogText);
                ContentValues value = new ContentValues();
                value.put("packageName", packageName);
                value.put("logDatetime", Long.valueOf(lLogTime));
                value.put("logTitle", strLogTitle);
                value.put("logText", strLogText);
                int nIndex2 = nIndex + 1;
                values[nIndex] = value;
                nIndex = nIndex2;
            }
            HwLog.i(DBHelper.LOG_TAG, "restoreNotificationMgrLogs: total count = " + nIndex + ", import count = " + DBHelper.this.mContext.getContentResolver().bulkInsert(DBProvider.URI_NOTIFICATION_LOG, values));
        }
    }

    private boolean recoverCfgTableFromVersion9(android.database.sqlite.SQLiteDatabase r20, int r21) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:27:? in {3, 7, 14, 19, 23, 24, 26, 28, 29} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r19 = this;
        r10 = "permissionCfg";
        r0 = r19;
        r3 = r0.getRecoverTmpTableMap(r10);
        r2 = android.text.TextUtils.isEmpty(r3);
        if (r2 == 0) goto L_0x001a;
    L_0x000f:
        r2 = "PermissionDbHelper";
        r5 = "recoverPermissionCfgFromVersion9: Fail to get backup tmp table name from map";
        com.huawei.systemmanager.util.HwLog.w(r2, r5);
        r2 = 0;
        return r2;
    L_0x001a:
        r2 = 4;
        r4 = new java.lang.String[r2];
        r2 = "packageName";
        r5 = 0;
        r4[r5] = r2;
        r2 = "permissionCode";
        r5 = 1;
        r4[r5] = r2;
        r2 = "trust";
        r5 = 2;
        r4[r5] = r2;
        r2 = "permissionCfg";
        r5 = 3;
        r4[r5] = r2;
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r9 = 0;
        r2 = r20;
        r11 = r2.query(r3, r4, r5, r6, r7, r8, r9);
        r2 = 1;
        r2 = com.huawei.systemmanager.comm.misc.Utility.isNullOrEmptyCursor(r11, r2);
        if (r2 == 0) goto L_0x0052;
    L_0x0047:
        r2 = "PermissionDbHelper";
        r5 = "recoverCfgTableFromVersion9: Empty backup data";
        com.huawei.systemmanager.util.HwLog.i(r2, r5);
        r2 = 1;
        return r2;
    L_0x0052:
        r2 = "packageName";
        r15 = r11.getColumnIndex(r2);
        r2 = "permissionCode";
        r14 = r11.getColumnIndex(r2);
        r2 = "trust";
        r16 = r11.getColumnIndex(r2);
        r2 = "permissionCfg";
        r13 = r11.getColumnIndex(r2);
        r20.beginTransaction();	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
    L_0x0071:
        r2 = r11.moveToNext();	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        if (r2 == 0) goto L_0x00d5;	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
    L_0x0077:
        r17 = r11.getString(r15);	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r18 = new android.content.ContentValues;	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r18.<init>();	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r2 = "permissionCode";	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r5 = r11.getInt(r14);	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r5 = java.lang.Integer.valueOf(r5);	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r0 = r18;	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r0.put(r2, r5);	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r2 = "trust";	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r0 = r16;	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r5 = r11.getInt(r0);	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r5 = java.lang.Integer.valueOf(r5);	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r0 = r18;	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r0.put(r2, r5);	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r2 = "permissionCfg";	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r5 = r11.getInt(r13);	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r5 = java.lang.Integer.valueOf(r5);	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r0 = r18;	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r0.put(r2, r5);	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r2 = "packageName=?";	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r5 = 1;	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r5 = new java.lang.String[r5];	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r6 = 0;	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r5[r6] = r17;	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r0 = r20;	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r1 = r18;	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r0.update(r10, r1, r2, r5);	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        goto L_0x0071;
    L_0x00c3:
        r12 = move-exception;
        r2 = "PermissionDbHelper";	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r5 = "recoverCfgTableFromVersion9: Exception";	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        com.huawei.systemmanager.util.HwLog.e(r2, r5, r12);	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r2 = 0;
        r20.endTransaction();
        r11.close();
        return r2;
    L_0x00d5:
        r20.setTransactionSuccessful();	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r2 = "PermissionDbHelper";	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r5 = "recoverCfgTableFromVersion9: Successfully";	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        com.huawei.systemmanager.util.HwLog.i(r2, r5);	 Catch:{ Exception -> 0x00c3, all -> 0x00e9 }
        r2 = 1;
        r20.endTransaction();
        r11.close();
        return r2;
    L_0x00e9:
        r2 = move-exception;
        r20.endTransaction();
        r11.close();
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.db.DBHelper.recoverCfgTableFromVersion9(android.database.sqlite.SQLiteDatabase, int):boolean");
    }

    private boolean recoverCommonTableFromVersion9(android.database.sqlite.SQLiteDatabase r21, int r22) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:32:? in {3, 7, 21, 25, 26, 28, 29, 30, 31, 33, 34} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r20 = this;
        r10 = "commonTable";
        r0 = r20;
        r3 = r0.getRecoverTmpTableMap(r10);
        r2 = android.text.TextUtils.isEmpty(r3);
        if (r2 == 0) goto L_0x001a;
    L_0x000f:
        r2 = "PermissionDbHelper";
        r5 = "recoverCommonTableFromVersion9: Fail to get backup tmp table name from map";
        com.huawei.systemmanager.util.HwLog.w(r2, r5);
        r2 = 0;
        return r2;
    L_0x001a:
        r2 = 2;
        r4 = new java.lang.String[r2];
        r2 = "key";
        r5 = 0;
        r4[r5] = r2;
        r2 = "value";
        r5 = 1;
        r4[r5] = r2;
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r9 = 0;
        r2 = r21;
        r11 = r2.query(r3, r4, r5, r6, r7, r8, r9);
        r2 = 1;
        r2 = com.huawei.systemmanager.comm.misc.Utility.isNullOrEmptyCursor(r11, r2);
        if (r2 == 0) goto L_0x0046;
    L_0x003b:
        r2 = "PermissionDbHelper";
        r5 = "recoverCommonTableFromVersion9: Empty backup data";
        com.huawei.systemmanager.util.HwLog.i(r2, r5);
        r2 = 1;
        return r2;
    L_0x0046:
        r2 = "PermissionDbHelper";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "recoverCommonTableFromVersion9: cursor=";
        r5 = r5.append(r6);
        r6 = r11.getCount();
        r5 = r5.append(r6);
        r5 = r5.toString();
        com.huawei.systemmanager.util.HwLog.i(r2, r5);
        r2 = "key";
        r14 = r11.getColumnIndex(r2);
        r2 = "value";
        r15 = r11.getColumnIndex(r2);
        r21.beginTransaction();	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
    L_0x0075:
        r2 = r11.moveToNext();	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        if (r2 == 0) goto L_0x010c;	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
    L_0x007b:
        r13 = r11.getInt(r14);	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r19 = new android.content.ContentValues;	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r19.<init>();	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r2 = "value";	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r5 = r11.getInt(r15);	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r5 = java.lang.Integer.valueOf(r5);	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r0 = r19;	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r0.put(r2, r5);	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r2 = "key=?";	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r5 = 1;	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r5 = new java.lang.String[r5];	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r6 = java.lang.String.valueOf(r13);	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r7 = 0;	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r5[r7] = r6;	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r0 = r21;	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r1 = r19;	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r18 = r0.update(r10, r1, r2, r5);	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r2 = "PermissionDbHelper";	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r5 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r5.<init>();	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r6 = "recoverCommonTableFromVersion9: update result=";	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r0 = r18;	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r5 = r5.append(r0);	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r5 = r5.toString();	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        com.huawei.systemmanager.util.HwLog.i(r2, r5);	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        if (r18 > 0) goto L_0x0075;	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
    L_0x00c7:
        r2 = "key";	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r5 = java.lang.String.valueOf(r13);	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r0 = r19;	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r0.put(r2, r5);	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r2 = 0;	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r0 = r21;	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r1 = r19;	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r16 = r0.insert(r10, r2, r1);	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r2 = "PermissionDbHelper";	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r5 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r5.<init>();	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r6 = "recoverCommonTableFromVersion9: insert result=";	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r0 = r16;	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r5 = r5.append(r0);	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r5 = r5.toString();	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        com.huawei.systemmanager.util.HwLog.i(r2, r5);	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        goto L_0x0075;
    L_0x00fa:
        r12 = move-exception;
        r2 = "PermissionDbHelper";	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r5 = "recoverCommonTableFromVersion9: Exception";	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        com.huawei.systemmanager.util.HwLog.e(r2, r5, r12);	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r2 = 0;
        r21.endTransaction();
        r11.close();
        return r2;
    L_0x010c:
        r21.setTransactionSuccessful();	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r2 = "PermissionDbHelper";	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r5 = "recoverCommonTableFromVersion9: Successfully";	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        com.huawei.systemmanager.util.HwLog.i(r2, r5);	 Catch:{ Exception -> 0x00fa, all -> 0x0120 }
        r2 = 1;
        r21.endTransaction();
        r11.close();
        return r2;
    L_0x0120:
        r2 = move-exception;
        r21.endTransaction();
        r11.close();
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.db.DBHelper.recoverCommonTableFromVersion9(android.database.sqlite.SQLiteDatabase, int):boolean");
    }

    private void upgradeDatabase2ToVersion3(android.database.sqlite.SQLiteDatabase r21) {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r20 = this;
        r13 = 0;
        r3 = "permissionCfg";	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r4 = 0;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r5 = 0;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r6 = 0;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r7 = 0;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r8 = 0;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r9 = 0;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r2 = r21;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r13 = r2.query(r3, r4, r5, r6, r7, r8, r9);	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        if (r13 == 0) goto L_0x00a9;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
    L_0x0012:
        r2 = r13.getCount();	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        if (r2 <= 0) goto L_0x00a9;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
    L_0x0018:
        r17 = 0;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r10 = 0;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r13.moveToFirst();	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r0 = r20;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r2 = r0.mContext;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r18 = r2.getPackageManager();	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
    L_0x0026:
        r2 = r13.isAfterLast();	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        if (r2 != 0) goto L_0x00a9;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
    L_0x002c:
        r2 = "packageName";	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r12 = r13.getColumnIndex(r2);	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r17 = r13.getString(r12);	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r2 = 8192; // 0x2000 float:1.14794E-41 double:4.0474E-320;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r0 = r18;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r1 = r17;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r10 = r0.getApplicationInfo(r1, r2);	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r2 = r10.sourceDir;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r3 = "/data/cust";	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r2 = r2.contains(r3);	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        if (r2 != 0) goto L_0x0057;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
    L_0x004c:
        r2 = r10.sourceDir;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r3 = "/system/app";	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r2 = r2.contains(r3);	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        if (r2 == 0) goto L_0x0092;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
    L_0x0057:
        r2 = 1;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r11 = new java.lang.String[r2];	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r2 = 0;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r11[r2] = r17;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r2 = "permissionCfg";	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r3 = "packageName = ?";	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r0 = r21;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r19 = r0.delete(r2, r3, r11);	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r2 = "PermissionDbHelper";	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r3 = new java.lang.StringBuilder;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r3.<init>();	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r4 = "xxxx deleted row=";	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r3 = r3.append(r4);	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r0 = r19;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r3 = r3.append(r0);	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r4 = " package=";	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r3 = r3.append(r4);	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r0 = r17;	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r3 = r3.append(r0);	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r3 = r3.toString();	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        com.huawei.systemmanager.util.HwLog.d(r2, r3);	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
    L_0x0092:
        r13.moveToNext();	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        goto L_0x0026;
    L_0x0096:
        r14 = move-exception;
        r14.printStackTrace();	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r2 = "PermissionDbHelper";	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r3 = "upgradeDatabase2ToVersion3 NameNotFoundException";	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        com.huawei.systemmanager.util.HwLog.e(r2, r3);	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        if (r13 == 0) goto L_0x00a8;
    L_0x00a5:
        r13.close();
    L_0x00a8:
        return;
    L_0x00a9:
        if (r13 == 0) goto L_0x00a8;
    L_0x00ab:
        r13.close();
        goto L_0x00a8;
    L_0x00af:
        r15 = move-exception;
        r15.printStackTrace();	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r2 = "PermissionDbHelper";	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r3 = "upgradeDatabase2ToVersion3 exception!";	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        com.huawei.systemmanager.util.HwLog.e(r2, r3);	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        if (r13 == 0) goto L_0x00a8;
    L_0x00be:
        r13.close();
        goto L_0x00a8;
    L_0x00c2:
        r16 = move-exception;
        r16.printStackTrace();	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r2 = "PermissionDbHelper";	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        r3 = "upgradeDatabase2ToVersion3 NullPointerException";	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        com.huawei.systemmanager.util.HwLog.e(r2, r3);	 Catch:{ NameNotFoundException -> 0x0096, NullPointerException -> 0x00c2, Exception -> 0x00af, all -> 0x00d5 }
        if (r13 == 0) goto L_0x00a8;
    L_0x00d1:
        r13.close();
        goto L_0x00a8;
    L_0x00d5:
        r2 = move-exception;
        if (r13 == 0) goto L_0x00db;
    L_0x00d8:
        r13.close();
    L_0x00db:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.db.DBHelper.upgradeDatabase2ToVersion3(android.database.sqlite.SQLiteDatabase):void");
    }

    public static int getDBVersion() {
        return 16;
    }

    private SQLiteDatabase openDatabase() {
        synchronized (syncObj) {
            SQLiteDatabase sQLiteDatabase;
            if (!(this.mDatabase == null || new File(this.mDatabase.getPath()).exists())) {
                HwLog.i(LOG_TAG, " db file is not exist, close db ");
                closeDB();
            }
            if (this.mDatabase == null) {
                this.mDatabase = getWritableDatabase();
                HwLog.i(LOG_TAG, "DBHelper open Database:" + this.mDatabase);
                if (this.mDatabase == null) {
                    HwLog.e(LOG_TAG, "mDatabase is null ");
                    sQLiteDatabase = this.mDatabase;
                    return sQLiteDatabase;
                }
            }
            sQLiteDatabase = this.mDatabase;
            return sQLiteDatabase;
        }
    }

    public DBHelper(Context context) {
        super(context, DB_NAME, null, 16);
        this.mContext = context;
    }

    private void closeDB() {
        synchronized (syncObj) {
            if (this.mDatabase != null) {
                HwLog.e(LOG_TAG, " ********* DBHelper Close Database! ******** - : " + this.mDatabase);
                this.mDatabase.close();
                this.mDatabase = null;
            }
        }
    }

    public void onCreate(SQLiteDatabase db) {
        checkApplicationContext();
        createDBTables(db);
        checkDataCleared();
    }

    private void checkDataCleared() {
        if (!SettingsDbUtils.isFirstBootForPermissionInit(this.mContext)) {
            SharedPrefUtils.setDataClearedFlag(this.mContext, true);
        }
    }

    private void createDBTables(SQLiteDatabase db) {
        createPermissionCfgTable(db, false);
        createRuntimePermissionsTable(db);
        db.execSQL("create table if not exists prePermissionCfg ( packageName text primary key, type text, trust text, permissionCode int  DEFAULT (0),permissionCfg int  DEFAULT (0));");
        db.execSQL("create table if not exists logRecord ( _id integer primary key autoincrement, logDatetime long, uid int, permissionCode int, allow int);");
        createCommonTable(db, false);
        createKeyIndex(db);
        createHistoryTable(db);
        createGroupPermissionFlagTable(db);
    }

    private void createPermissionCfgTable(SQLiteDatabase db, boolean isTmpTableForRecover) {
        String tableName = "permissionCfg";
        if (isTmpTableForRecover) {
            tableName = tableName + "_tmpbak";
            db.execSQL("DROP TABLE IF EXISTS " + tableName);
            putRecoverTmpTableMap("permissionCfg", tableName);
        }
        db.execSQL("create table if not exists " + tableName + " ( " + "_id" + " integer primary key autoincrement, " + "packageName" + " text, " + "uid" + " int, " + "permissionCode" + " int, " + "trust" + " int, " + "permissionCfg" + " int " + " DEFAULT (0)" + ");");
    }

    private void createRuntimePermissionsTable(SQLiteDatabase db) {
        String tableName = RUNTIME_PERMS_TABLE_NAME;
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        db.execSQL("create table if not exists " + tableName + " ( " + "_id" + " integer primary key autoincrement, " + "permissionType" + " int, " + "packageName" + " text, " + "uid" + " int" + ");");
    }

    private void createCommonTable(SQLiteDatabase db, boolean isTmpTableForRecover) {
        String tableName = COMMON_TABLE_NAME;
        if (isTmpTableForRecover) {
            tableName = tableName + "_tmpbak";
            db.execSQL("DROP TABLE IF EXISTS " + tableName);
            putRecoverTmpTableMap(COMMON_TABLE_NAME, tableName);
        }
        db.execSQL("create table if not exists " + tableName + " ( " + "_id" + " integer primary key autoincrement, " + "key" + " int, " + VALUE + " int " + " DEFAULT (0)" + ");");
    }

    private void createKeyIndex(SQLiteDatabase db) {
        db.execSQL("create unique index if not exists packageName_idx ON permissionCfg(packageName)");
    }

    private void setPhoneIdPermission() {
        new Thread(INIT_PHONEID_THREAD) {
            public void run() {
                List<PackageInfo> pkgList = PackageManagerWrapper.getInstalledPackages(DBHelper.this.mContext.getPackageManager(), 12288);
                ArrayList<ContentValues> contValuesList = new ArrayList();
                ContentValues contentValues = new ContentValues();
                for (PackageInfo pkgInfo : pkgList) {
                    String pkgName = pkgInfo.packageName;
                    if (DBHelper.shouldMonitor(DBHelper.this.mContext, pkgName)) {
                        boolean hasPhoneStatePer = CommonFunctionUtil.isPackageHasPhoneStatePermission(pkgInfo);
                        boolean hasShortCutPer = CommonFunctionUtil.isPackageHasShortCutPermission(pkgInfo);
                        if (!DBHelper.this.isPackageBeenInitedAlready(pkgName) && (hasPhoneStatePer || hasShortCutPer)) {
                            int perCode = 0;
                            int perCfg = 0;
                            contentValues.put("packageName", pkgName);
                            contentValues.put("uid", Integer.valueOf(pkgInfo.applicationInfo.uid));
                            if (hasPhoneStatePer) {
                                perCode = 16;
                            }
                            if (hasShortCutPer) {
                                perCode |= 16777216;
                                perCfg = 16777216;
                            }
                            contentValues.put("permissionCode", Integer.valueOf(perCode));
                            contentValues.put("permissionCfg", Integer.valueOf(perCfg));
                            contValuesList.add(new ContentValues(contentValues));
                            contentValues.clear();
                        }
                    }
                }
                HwLog.i(DBHelper.LOG_TAG, "Init the Phone_State_Permission for pre_installed apps.");
                if (contValuesList.size() > 0) {
                    DBHelper.this.bulkInsert("permissionCfg", (ContentValues[]) contValuesList.toArray(new ContentValues[contValuesList.size()]));
                }
            }
        }.start();
    }

    private static boolean shouldMonitor(Context context, String pkgName) {
        return GRuleManager.getInstance().shouldMonitor(context, MonitorScenario.SCENARIO_PERMISSION, pkgName);
    }

    private boolean isPackageBeenInitedAlready(String pkgName) {
        Cursor cursor = query("permissionCfg", null, "packageName = ?", new String[]{pkgName}, null, null, null);
        if (cursor == null || cursor.getCount() <= 0) {
            if (cursor != null) {
                cursor.close();
            }
            return false;
        }
        cursor.close();
        return true;
    }

    private void upgradeDatabaseToVersion5Thread(final SQLiteDatabase db) {
        HwLog.d(LOG_TAG, "upgradeDatabaseToVersion5Thread in");
        new Thread(HOTA_TOVERSION5_THREAD) {
            public void run() {
                SharedPrefUtils.setPermissionEmendationFlag(DBHelper.this.mContext, true);
                DBHelper.this.upgradeDatabaseToVersion5Inner(db);
            }
        }.start();
    }

    private void upgradeDatabaseToVersion13Thread(final SQLiteDatabase db) {
        HwLog.d(LOG_TAG, "upgradeDatabaseToVersion13Thread in");
        new Thread(HOTA_TOVERSION5_THREAD) {
            public void run() {
                DBHelper.this.upgradeDatabaseToVersion13Inner(db);
            }
        }.start();
    }

    private void upgradeDatabaseToVersion5Inner(SQLiteDatabase db) {
        HwLog.d(LOG_TAG, "upgradeDatabaseToVersion5Inner in time:" + System.currentTimeMillis());
        List<PackageInfo> pkgList = PackageManagerWrapper.getInstalledPackages(this.mContext.getPackageManager(), 12288);
        ArrayList<ContentValues> insertCVs = new ArrayList();
        ArrayList<ContentValues> updateCVs = new ArrayList();
        ContentValues cv = new ContentValues();
        for (PackageInfo pkgInfo : pkgList) {
            cv.clear();
            String pkgName = pkgInfo.packageName;
            if (CommonFunctionUtil.isPackageHasPhoneStatePermission(pkgInfo) && (pkgInfo.applicationInfo.flags & 1) != 0 && shouldMonitor(this.mContext, pkgName)) {
                cv.put("packageName", pkgName);
                cv.put("uid", Integer.valueOf(pkgInfo.applicationInfo.uid));
                if (!checkAndGetUpgradeContent(db, pkgName, cv)) {
                    HwLog.d(LOG_TAG, "upgradeDatabaseToVersion5Inner insert " + pkgName);
                    insertCVs.add(new ContentValues(cv));
                } else if (cv.containsKey("permissionCode") && cv.containsKey("permissionCfg")) {
                    HwLog.d(LOG_TAG, "upgradeDatabaseToVersion5Inner update " + pkgName);
                    updateCVs.add(new ContentValues(cv));
                } else {
                    HwLog.i(LOG_TAG, "upgradeDatabaseToVersion5Inner " + pkgName + " already set, no need update");
                }
            }
        }
        HwLog.d(LOG_TAG, "upgradeDatabaseToVersion5Inner out before db time:" + System.currentTimeMillis());
        upgradeDatabaseToVersionDB(db, insertCVs, updateCVs, "upgradeDatabaseToVersion5DB");
        HwLog.d(LOG_TAG, "upgradeDatabaseToVersion5Inner out time:" + System.currentTimeMillis());
    }

    private void upgradeDatabaseToVersion13Inner(SQLiteDatabase db) {
        HwLog.d(LOG_TAG, "upgradeDatabaseToVersion13Inner in time:" + System.currentTimeMillis());
        List<PackageInfo> pkgList = PackageManagerWrapper.getInstalledPackages(this.mContext.getPackageManager(), 12288);
        ArrayList<ContentValues> insertCVs = new ArrayList();
        ArrayList<ContentValues> updateCVs = new ArrayList();
        ContentValues cv = new ContentValues();
        for (PackageInfo pkgInfo : pkgList) {
            cv.clear();
            String pkgName = pkgInfo.packageName;
            if (CommonFunctionUtil.isPackageHasShortCutPermission(pkgInfo) && (pkgInfo.applicationInfo.flags & 1) != 0 && shouldMonitor(this.mContext, pkgName)) {
                cv.put("packageName", pkgName);
                cv.put("uid", Integer.valueOf(pkgInfo.applicationInfo.uid));
                if (!checkAndGetUpgrade13DB(db, pkgName, cv)) {
                    HwLog.d(LOG_TAG, "upgradeDatabaseToVersion13Inner insert " + pkgName);
                    insertCVs.add(new ContentValues(cv));
                } else if (cv.containsKey("permissionCode") && cv.containsKey("permissionCfg")) {
                    HwLog.d(LOG_TAG, "upgradeDatabaseToVersion13Inner update " + pkgName);
                    updateCVs.add(new ContentValues(cv));
                } else {
                    HwLog.i(LOG_TAG, "upgradeDatabaseToVersion13Inner " + pkgName + " already set, no need update");
                }
            }
        }
        HwLog.d(LOG_TAG, "upgradeDatabaseToVersion13Inner out before db time:" + System.currentTimeMillis());
        upgradeDatabaseToVersionDB(db, insertCVs, updateCVs, "upgradeDatabaseToVersion13DB");
        HwLog.d(LOG_TAG, "upgradeDatabaseToVersion13Inner out time:" + System.currentTimeMillis());
    }

    private boolean checkAndGetUpgradeContent(SQLiteDatabase db, String pkgName, ContentValues cv) {
        Cursor cursor = null;
        try {
            SQLiteDatabase sQLiteDatabase = db;
            cursor = sQLiteDatabase.query("permissionCfg", null, "packageName= ?", new String[]{pkgName}, null, null, null);
            if (cursor == null || cursor.getCount() <= 0) {
                cv.put("permissionCode", Integer.valueOf(16));
                cv.put("permissionCfg", Integer.valueOf(0));
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            cursor.moveToFirst();
            int codeIdx = cursor.getColumnIndex("permissionCode");
            int cfgIdx = cursor.getColumnIndex("permissionCfg");
            if (!(-1 == codeIdx || -1 == cfgIdx || (cursor.getInt(codeIdx) & 16) != 0)) {
                HwLog.i(LOG_TAG, "checkAndGetUpgradeContent codeIdx:" + codeIdx + ", cfgIdx:" + cfgIdx);
                cv.put("permissionCode", Integer.valueOf(cursor.getInt(codeIdx) | 16));
                cv.put("permissionCfg", Integer.valueOf(cursor.getInt(cfgIdx)));
            }
            return true;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private boolean checkAndGetUpgrade13DB(SQLiteDatabase db, String pkgName, ContentValues cv) {
        Cursor cursor = null;
        try {
            SQLiteDatabase sQLiteDatabase = db;
            cursor = sQLiteDatabase.query("permissionCfg", null, "packageName= ?", new String[]{pkgName}, null, null, null);
            if (cursor == null || cursor.getCount() <= 0) {
                cv.put("permissionCode", Integer.valueOf(16777216));
                cv.put("permissionCfg", Integer.valueOf(16777216));
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            cursor.moveToFirst();
            int codeIdx = cursor.getColumnIndex("permissionCode");
            int cfgIdx = cursor.getColumnIndex("permissionCfg");
            int perCode = cursor.getInt(codeIdx);
            int perCfg = cursor.getInt(cfgIdx);
            if (!(-1 == codeIdx || -1 == cfgIdx)) {
                HwLog.i(LOG_TAG, "checkAndGetUpgrade13DB codeIdx:" + codeIdx + ", cfgIdx:" + cfgIdx);
                if ((cursor.getInt(codeIdx) & 16777216) == 0) {
                    perCfg |= 16777216;
                    cv.put("permissionCode", Integer.valueOf(perCode | 16777216));
                    cv.put("permissionCfg", Integer.valueOf(perCfg));
                }
            }
            return true;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void upgradeDatabaseToVersionDB(SQLiteDatabase db, ArrayList<ContentValues> insertCVs, ArrayList<ContentValues> updateCVs, String type) {
        if (insertCVs.isEmpty() && updateCVs.isEmpty()) {
            HwLog.i(LOG_TAG, type + " no data need upgrade");
        }
        try {
            db.beginTransaction();
            for (ContentValues cv : insertCVs) {
                db.insert("permissionCfg", null, cv);
            }
            for (ContentValues cv2 : updateCVs) {
                int appId = cv2.getAsInteger("uid").intValue();
                cv2.remove("uid");
                db.update("permissionCfg", cv2, "uid=" + appId, null);
            }
            db.setTransactionSuccessful();
        } catch (SQLiteException ex) {
            HwLog.e(LOG_TAG, type + " get SQLiteException");
            ex.printStackTrace();
        } catch (Exception ex2) {
            HwLog.e(LOG_TAG, type + " get Exception");
            ex2.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        HwLog.i(LOG_TAG, "onUpgrade oldVersion:" + oldVersion + " newVersion " + newVersion);
        checkApplicationContext();
        switch (oldVersion) {
            case 1:
                if (newVersion > 1) {
                    db.beginTransaction();
                    try {
                        upgradeDatabase1or2ToVersion3(db);
                        db.setTransactionSuccessful();
                    } catch (Throwable ex) {
                        HwLog.e(LOG_TAG, ex.getMessage());
                        break;
                    } finally {
                        db.endTransaction();
                    }
                } else {
                    return;
                }
            case 2:
                if (newVersion > 2) {
                    db.beginTransaction();
                    try {
                        upgradeDatabase1or2ToVersion3(db);
                        upgradeDatabase2ToVersion3(db);
                        db.setTransactionSuccessful();
                    } catch (Throwable ex2) {
                        HwLog.e(LOG_TAG, ex2.getMessage());
                        break;
                    } finally {
                        db.endTransaction();
                    }
                } else {
                    return;
                }
            case 3:
                if (newVersion > 3) {
                    if (4 == newVersion) {
                        try {
                            setPhoneIdPermission();
                        } catch (Throwable ex22) {
                            HwLog.e(LOG_TAG, "onUpgrade case 3 " + ex22.getMessage());
                            break;
                        }
                    }
                }
                return;
            case 4:
                if (newVersion > 4) {
                    try {
                        upgradeDatabase3or4ToVersion5(db);
                    } catch (Throwable ex222) {
                        HwLog.e(LOG_TAG, "onUpgrade case 4 " + ex222.getMessage());
                        break;
                    }
                }
                return;
            case 5:
                if (newVersion > 5) {
                    db.beginTransaction();
                    try {
                        upgradeDatabase5ToVersion6(db);
                        db.setTransactionSuccessful();
                    } catch (Throwable ex2222) {
                        HwLog.e(LOG_TAG, ex2222.getMessage());
                        break;
                    } finally {
                        db.endTransaction();
                    }
                } else {
                    return;
                }
            case 6:
                if (newVersion > 6) {
                    db.beginTransaction();
                    try {
                        upgradeDatabase6ToVersion7(db);
                        db.setTransactionSuccessful();
                    } catch (Throwable ex22222) {
                        HwLog.e(LOG_TAG, ex22222.getMessage());
                        break;
                    } finally {
                        db.endTransaction();
                    }
                } else {
                    return;
                }
            case 7:
                if (newVersion > 7) {
                    db.beginTransaction();
                    try {
                        upgradeDatabase7ToVersion8(db);
                        db.setTransactionSuccessful();
                    } catch (Throwable ex222222) {
                        HwLog.e(LOG_TAG, ex222222.getMessage());
                        break;
                    } finally {
                        db.endTransaction();
                    }
                } else {
                    return;
                }
            case 8:
                if (newVersion > 8) {
                    db.beginTransaction();
                    try {
                        upgradeDatabase8ToVersion9(db);
                        db.setTransactionSuccessful();
                    } catch (Throwable ex2222222) {
                        HwLog.e(LOG_TAG, ex2222222.getMessage());
                        break;
                    } finally {
                        db.endTransaction();
                    }
                } else {
                    return;
                }
            case 9:
                if (newVersion > 9) {
                    try {
                        upgradeDatabase9ToVersion10(db);
                    } catch (Throwable ex22222222) {
                        HwLog.e(LOG_TAG, "onUpgrade case 9 " + ex22222222.getMessage());
                        break;
                    }
                }
                return;
            case 10:
                if (newVersion > 10) {
                    try {
                        upgradeDataBase10ToVersion11(db);
                    } catch (Throwable ex222222222) {
                        HwLog.e(LOG_TAG, "onUpgrade case 10 " + ex222222222.getMessage());
                        break;
                    }
                }
                return;
            case 11:
                if (newVersion > 11) {
                    try {
                        upgradeDataBase11ToVersion12(db);
                    } catch (Throwable ex2222222222) {
                        HwLog.e(LOG_TAG, "onUpgrade case 11 " + ex2222222222.getMessage());
                        break;
                    }
                }
                return;
            case 12:
                if (newVersion > 12) {
                    try {
                        HwLog.i(LOG_TAG, "upgradeDataBase12ToVersion13:");
                        upgradeDataBase12ToVersion13(db);
                        break;
                    } catch (Throwable ex22222222222) {
                        HwLog.e(LOG_TAG, "onUpgrade case 12 " + ex22222222222.getMessage());
                        break;
                    }
                }
                return;
            default:
                HwLog.e(LOG_TAG, "onUpgrade should not be run here");
                break;
        }
        from13to14(db, oldVersion, newVersion);
        from14to15(oldVersion, newVersion);
        from15to16(oldVersion, newVersion);
    }

    private void from14to15(int oldVersion, int newVersion) {
        if (oldVersion <= 14 && newVersion > 14) {
            try {
                SharedPrefUtils.setMPermissionUpgradeFlag(this.mContext, true);
            } catch (Throwable ex) {
                HwLog.e(LOG_TAG, "onUpgrade case 14" + ex.getMessage());
            }
        }
    }

    private void from15to16(int oldVersion, int newVersion) {
        if (oldVersion <= 15 && newVersion > 15) {
            try {
                HwLog.i(LOG_TAG, "onUpgrade to version 16. old:" + oldVersion);
                SharedPrefUtils.setMPermissionUpgradeFlag(this.mContext, true);
            } catch (Throwable ex) {
                HwLog.e(LOG_TAG, "onUpgrade case 15" + ex.getMessage());
            }
        }
    }

    private void from13to14(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 13 && newVersion > 13) {
            try {
                createGroupPermissionFlagTable(db);
            } catch (Throwable ex) {
                HwLog.e(LOG_TAG, "onUpgrade case 13" + ex.getMessage());
            }
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS permissionCfg");
        db.execSQL("DROP TABLE IF EXISTS prePermissionCfg");
        db.execSQL("DROP TABLE IF EXISTS logRecord");
        db.execSQL("DROP TABLE IF EXISTS runtimePermissions");
        db.execSQL("DROP TABLE IF EXISTS commonTable");
        db.execSQL("DROP TABLE IF EXISTS history_table");
        db.execSQL("DROP TABLE IF EXISTS perm_group_flag");
        onCreate(db);
    }

    private void upgradeDatabase1or2ToVersion3(SQLiteDatabase db) {
        db.execSQL("create table if not exists prePermissionCfg ( packageName text primary key, type text, trust text, permissionCode int  DEFAULT (0),permissionCfg int  DEFAULT (0));");
    }

    private void upgradeDatabase3or4ToVersion5(SQLiteDatabase db) {
        upgradeDatabaseToVersion5Thread(db);
    }

    private void upgradeDatabase5ToVersion6(SQLiteDatabase db) {
        db.delete("prePermissionCfg", null, null);
        HwLog.i(LOG_TAG, "upgradeDatabase5ToVersion6 finished!");
    }

    private void upgradeDatabase6ToVersion7(SQLiteDatabase db) {
        db.execSQL("delete from permissionCfg where _id in (select _id from permissionCfg a where exists (select 1 from permissionCfg b where a.packageName= b.packageName and a._id< b._id))");
        createKeyIndex(db);
    }

    private void upgradeDatabase7ToVersion8(SQLiteDatabase db) {
        db.execSQL("update permissionCfg set trust=10");
    }

    private void upgradeDatabase8ToVersion9(SQLiteDatabase db) {
        new DBUpgradeFrom8To9Helper().doUpgrade(db);
    }

    private void checkApplicationContext() {
        if (GlobalContext.getContext() == null) {
            GlobalContext.setContext(this.mContext);
        }
    }

    public int update(String tableName, ContentValues values, String whereClause, String[] whereArgs) {
        int update;
        synchronized (syncObj) {
            openDatabase();
            update = this.mDatabase.update(tableName, values, whereClause, whereArgs);
        }
        return update;
    }

    public Cursor query(String tableName, String[] projection, String selection, String[] selectionArgs, Object object, Object object2, String sortOrder) {
        Cursor cursor;
        synchronized (syncObj) {
            SQLiteDatabase db = getReadableDatabase();
            cursor = null;
            if (db != null) {
                cursor = db.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
            }
        }
        return cursor;
    }

    public long insert(ContentValues values, String tableName) {
        long insert;
        synchronized (syncObj) {
            openDatabase();
            insert = this.mDatabase.insert(tableName, null, values);
        }
        return insert;
    }

    public long replace(ContentValues value, String table) {
        long replace;
        synchronized (syncObj) {
            openDatabase();
            replace = this.mDatabase.replace(table, null, value);
        }
        return replace;
    }

    public boolean isTrustendWhenRestore(String pkg) {
        return this.restoredTrustedApps.contains(pkg);
    }

    public void clearTrustAppsCache() {
        this.restoredTrustedApps.clear();
    }

    public boolean isRestored(String pkg) {
        return this.restoredApps.contains(pkg);
    }

    public void clearRestoredAppsCache() {
        this.restoredApps.clear();
    }

    public long insertForRestorePermissionCfg(Context context, ContentValues values, String tableName) {
        if (context == null || values == null) {
            return -1;
        }
        String pkgName = values.getAsString("packageName");
        HwLog.i(LOG_TAG, "backup_restore: insertForRestorePermissionCfg, pkg:" + pkgName + ", values:" + values);
        if (GRuleManager.getInstance().shouldMonitor(this.mContext, MonitorScenario.SCENARIO_PERMISSION, pkgName)) {
            int uId = BackupUtil.getPackageUid(context, pkgName);
            if (-1 == uId) {
                HwLog.e(LOG_TAG, "backup_restore: The given pkgName's uid is not exists: " + pkgName);
                return -2;
            }
            long insert;
            values.put("uid", Integer.valueOf(uId));
            int compareCode = AppInfo.getComparePermissionCode(context, pkgName);
            int permissionCode = values.getAsInteger("permissionCode").intValue();
            int permissionCfg = values.getAsInteger("permissionCfg").intValue();
            Integer trustInteger = values.getAsInteger("trust");
            if ((trustInteger != null ? trustInteger.intValue() : 0) == 1) {
                permissionCode = compareCode;
                permissionCfg = 0;
                this.restoredTrustedApps.add(pkgName);
                AddViewAppManager.trust(this.mContext, uId, pkgName);
                HwLog.i(LOG_TAG, "backup_restore: insertForRestorePermissionCfg, trust app:" + pkgName);
            } else {
                int[] res = CloudDBAdapter.applyDefaultPolicy(context, compareCode, pkgName, permissionCode, permissionCfg);
                HwLog.i(LOG_TAG, "backup_restore: insertForRestorePermissionCfg for " + pkgName + ", previous:" + permissionCode + SqlMarker.COMMA_SEPARATE + permissionCfg + ", after:" + res[0] + com.huawei.harassmentinterception.common.ConstValues.SEPARATOR_KEYWORDS_EN + res[1]);
                permissionCode = res[0];
                permissionCfg = res[1];
                if (this.mRestoreVersion <= 15) {
                    res = trimEmui5NewPermissions(pkgName, permissionCode, permissionCfg);
                    permissionCode = res[0];
                    permissionCfg = res[1];
                }
            }
            values.put("permissionCode", Integer.valueOf(permissionCode));
            values.put("permissionCfg", Integer.valueOf(permissionCfg));
            this.restoredApps.add(pkgName);
            synchronized (syncObj) {
                openDatabase();
                insert = this.mDatabase.insert(tableName, null, values);
            }
            return insert;
        }
        HwLog.w(LOG_TAG, "backup_restore: The given pkgName's uid is not monitored: " + pkgName);
        return -2;
    }

    private int[] trimEmui5NewPermissions(String pkgName, int permissionCode, int permissionCfg) {
        Closeable closeable = null;
        try {
            synchronized (syncObj) {
                openDatabase();
                closeable = this.mDatabase.query("permissionCfg", null, "packageName = ?", new String[]{String.valueOf(pkgName)}, null, null, null);
            }
            if (closeable != null && closeable.getCount() > 0) {
                closeable.moveToFirst();
                permissionCode = (permissionCode & -1074790401) | (closeable.getInt(closeable.getColumnIndex("permissionCode")) & ShareCfg.EMUI5_NEW_TYPES);
                permissionCfg = (permissionCfg & -1074790401) | (closeable.getInt(closeable.getColumnIndex("permissionCfg")) & ShareCfg.EMUI5_NEW_TYPES);
            }
            Closeables.close(closeable);
        } catch (NullPointerException e) {
            try {
                HwLog.w(LOG_TAG, "trim permission fail.", e);
            } finally {
                Closeables.close(closeable);
            }
        } catch (Exception e2) {
            HwLog.w(LOG_TAG, "trim permission fail.", e2);
            Closeables.close(closeable);
        }
        return new int[]{permissionCode, permissionCfg};
    }

    public int deleteAllData(String tableName) {
        int delete;
        synchronized (syncObj) {
            openDatabase();
            delete = this.mDatabase.delete(tableName, null, null);
        }
        return delete;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int bulkInsert(String tableName, ContentValues[] values) {
        int iInsertCount;
        synchronized (syncObj) {
            iInsertCount = 0;
            openDatabase();
            try {
                this.mDatabase.beginTransaction();
                for (ContentValues insert : values) {
                    if (-1 != this.mDatabase.insert(tableName, null, insert)) {
                        iInsertCount++;
                    }
                }
                this.mDatabase.setTransactionSuccessful();
                try {
                    this.mDatabase.endTransaction();
                } catch (Exception e) {
                    HwLog.e(LOG_TAG, "bulkInsert endTransaction catch exception");
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                HwLog.e(LOG_TAG, "bulkInsert cat exception");
            } catch (Throwable th) {
                try {
                    this.mDatabase.endTransaction();
                } catch (Exception e3) {
                    HwLog.e(LOG_TAG, "bulkInsert endTransaction catch exception");
                }
            }
            HwLog.i(LOG_TAG, "bulkInsert input size:" + values.length + ", InsertedNum:" + iInsertCount);
        }
        return iInsertCount;
    }

    public int bulkReplace(String tableName, ContentValues[] values) {
        int nUpdate;
        synchronized (syncObj) {
            nUpdate = 0;
            openDatabase();
            try {
                this.mDatabase.beginTransaction();
                for (ContentValues replace : values) {
                    if (-1 != this.mDatabase.replace(tableName, null, replace)) {
                        nUpdate++;
                    }
                }
                this.mDatabase.setTransactionSuccessful();
                if (this.mDatabase != null) {
                    this.mDatabase.endTransaction();
                }
            } catch (Exception e) {
                nUpdate = 0;
                e.printStackTrace();
                HwLog.e(LOG_TAG, "updatePermissions Exception: " + e.getMessage());
                if (this.mDatabase != null) {
                    this.mDatabase.endTransaction();
                }
            } catch (Throwable th) {
                if (this.mDatabase != null) {
                    this.mDatabase.endTransaction();
                }
            }
        }
        return nUpdate;
    }

    private void upgradeDatabase9ToVersion10(SQLiteDatabase db) {
        Cursor cursor = null;
        String[] columns = new String[]{"packageName"};
        ArrayList<String> pkgNameList = new ArrayList();
        db.beginTransaction();
        try {
            cursor = db.query("permissionCfg", columns, null, null, null, null, null);
            db.setTransactionSuccessful();
        } catch (Throwable ex) {
            HwLog.e(LOG_TAG, ex.getMessage());
        } finally {
            db.endTransaction();
        }
        if (cursor != null && cursor.getCount() > 0) {
            int pkgNameIndex = cursor.getColumnIndex("packageName");
            while (cursor.moveToNext()) {
                pkgNameList.add(cursor.getString(pkgNameIndex));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        AddViewAppManager.getInstance(this.mContext).saveInitedPkgIntoFileCheck(pkgNameList);
    }

    private void upgradeDataBase10ToVersion11(SQLiteDatabase db) {
        createHistoryTable(db);
    }

    private void upgradeDataBase11ToVersion12(SQLiteDatabase db) {
        createHistoryTable(db);
    }

    private void upgradeDataBase12ToVersion13(SQLiteDatabase db) {
        createRuntimePermissionsTable(db);
        upgradeDatabaseToVersion13Thread(db);
    }

    private void createHistoryTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS history_table");
        db.execSQL("CREATE TABLE IF NOT EXISTS history_table ( _id INTEGER PRIMARY KEY AUTOINCREMENT, packageName TEXT, permission_type TEXT, action INTEGER, count INTEGER, date_start_time INTEGER, time_stamp INTEGER);");
    }

    private void createGroupPermissionFlagTable(SQLiteDatabase db) {
        HwLog.i(LOG_TAG, "createGroupPermissionFlagTable.");
        db.execSQL("DROP TABLE IF EXISTS perm_group_flag");
        db.execSQL("CREATE TABLE IF NOT EXISTS perm_group_flag ( _id INTEGER PRIMARY KEY AUTOINCREMENT, packageName TEXT, uid INTEGER, permissionGroup INTEGER, flag INTEGER);");
    }

    public int delete(String table, String whereClause, String[] whereArgs) {
        int delete;
        synchronized (syncObj) {
            openDatabase();
            delete = this.mDatabase.delete(table, whereClause, whereArgs);
        }
        return delete;
    }

    private void checkHistoryRecordLimit() {
        synchronized (syncObj) {
            Calendar calendar = TimeUtil.getTodayStartCalendar();
            calendar.add(5, -30);
            long timeStart = calendar.getTimeInMillis();
            long totalCount = getTableCount(this.mDatabase, TABLE_HISTORY, " where time_stamp < " + timeStart);
            if (totalCount >= 5000) {
                HwLog.i(LOG_TAG, "History records table too large , totalCount:" + totalCount + " delete count:" + this.mDatabase.delete(TABLE_HISTORY, "time_stamp < " + timeStart, null) + ",timeBeforeTobeRmoved=" + timeStart);
            }
        }
    }

    private static long getTableCount(SQLiteDatabase database, String table, String selection) {
        Cursor cursor = database.rawQuery("select count(*) from " + table + selection, null);
        if (cursor == null) {
            return -1;
        }
        long count = -1;
        if (cursor.moveToNext()) {
            count = (long) cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public Cursor queryWithLimit(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        Cursor cursor;
        synchronized (syncObj) {
            SQLiteDatabase db = getReadableDatabase();
            cursor = null;
            if (db != null) {
                cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
            }
        }
        return cursor;
    }

    int recordHistory(Bundle extras) {
        if (extras == null) {
            HwLog.e(LOG_TAG, "recordHistory arg is null!");
            return -1;
        }
        ContentValues values = (ContentValues) extras.getParcelable(KEY_CONTENT_VALUE);
        if (values == null) {
            HwLog.i(LOG_TAG, "recordHistory, ContentValues is null");
            return -1;
        }
        int updateCount;
        String pkgName = values.getAsString("packageName");
        Long dateStartTimeLong = values.getAsLong(COLUMN_DATE_START_TIME);
        long dateStartTime = dateStartTimeLong == null ? 0 : dateStartTimeLong.longValue();
        String permissionType = values.getAsString(COLUMN_PERMISSION_TYPE);
        Integer actionInteger = values.getAsInteger("action");
        int action = actionInteger == null ? 0 : actionInteger.intValue();
        String whereClause = INSERT_HISTORY_SQL_1;
        String[] whereArgs = new String[]{pkgName, String.valueOf(dateStartTime), permissionType, String.valueOf(action)};
        synchronized (syncObj) {
            openDatabase();
            SQLiteDatabase db = this.mDatabase;
            long historyCount = 0;
            Cursor cursor = db.query(TABLE_HISTORY, new String[]{"count"}, whereClause, whereArgs, null, null, null);
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    historyCount = cursor.getLong(0);
                }
                cursor.close();
            }
            if (historyCount > 0) {
                values.put("count", Long.valueOf(Math.min(MAX_COUNT, 1 + historyCount)));
                updateCount = db.update(TABLE_HISTORY, values, INSERT_HISTORY_SQL_1, whereArgs);
            } else {
                values.put("count", Integer.valueOf(1));
                updateCount = db.insert(TABLE_HISTORY, null, values) == -1 ? -1 : 1;
            }
            if (updateCount > 1) {
                HwLog.e(LOG_TAG, "recordHistory ,update count=" + updateCount + ", there must be something wrong!");
            }
            checkHistoryRecordLimit();
        }
        return updateCount;
    }

    protected boolean onRecoverStart(SQLiteDatabase db, int oldVersion) {
        HwLog.i(LOG_TAG, "onRecoverStart: Start, oldVersion = " + oldVersion);
        this.mRestoreVersion = oldVersion;
        switch (oldVersion) {
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
                return createTempTablesVersion9(db, oldVersion);
            default:
                HwLog.w(LOG_TAG, "onRecoverStart: Invalid oldVersion = " + oldVersion);
                return false;
        }
    }

    protected boolean onRecoverComplete(SQLiteDatabase db, int oldVersion) {
        boolean res;
        if (Log.HWINFO) {
            HwLog.i(LOG_TAG, "onRecoverComplete: Start, oldVersion = " + oldVersion);
        }
        switch (oldVersion) {
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
                res = recoverFromVersion9(db, oldVersion);
                break;
            default:
                HwLog.w(LOG_TAG, "onRecoverComplete: Invalid oldVersion = " + oldVersion);
                res = false;
                break;
        }
        DBAdapter.getInstance(this.mContext).syncHsmDataToSys("recover", this.restoredApps, oldVersion);
        clearRestoredAppsCache();
        this.mRestoreVersion = -1;
        return res;
    }

    private boolean createTempTablesVersion9(SQLiteDatabase db, int oldVersion) {
        try {
            createPermissionCfgTable(db, true);
            createCommonTable(db, true);
            return true;
        } catch (Exception e) {
            HwLog.e(LOG_TAG, "createTempTablesVersion9: Exception", e);
            return false;
        }
    }

    private boolean recoverFromVersion9(SQLiteDatabase db, int oldVersion) {
        boolean bSuccess = recoverCfgTableFromVersion9(db, oldVersion) & recoverCommonTableFromVersion9(db, oldVersion);
        clearRecoverTmpTablesAndMap(db);
        HwLog.i(LOG_TAG, "recoverFromVersion9: bSuccess = " + bSuccess);
        return bSuccess;
    }
}
