package com.huawei.systemmanager.startupmgr.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import com.google.common.collect.Lists;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.permissionmanager.db.DBHelper;
import com.huawei.systemmanager.comm.database.DbOpWrapper;
import com.huawei.systemmanager.comm.database.ITableInfo;
import com.huawei.systemmanager.comm.database.gfeature.AbsFeatureView;
import com.huawei.systemmanager.comm.database.gfeature.GFeatureCvt;
import com.huawei.systemmanager.comm.database.gfeature.GFeatureDBOpenHelper;
import com.huawei.systemmanager.startupmgr.comm.StartupDBConst;
import com.huawei.systemmanager.startupmgr.comm.StartupDBConst.AwakedViewKeys;
import com.huawei.systemmanager.startupmgr.comm.StartupDBConst.NormalViewKeys;
import com.huawei.systemmanager.startupmgr.localize.LocalizePackageNameTable;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

class StartupDBOpenHelper extends GFeatureDBOpenHelper {
    private static final int DATABASE_VERSION = 3;
    private static final String DB_NAME = "startupmgr.db";
    private static final String TAG = "StartupDBOpenHelper";
    private static StartupDBOpenHelper sInstance = null;

    public void insertAwakedSwitchBackup(android.content.Context r9, android.content.ContentValues r10) {
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
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r8 = this;
        r6 = 0;
        r0 = r9.getContentResolver();	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        r1 = com.huawei.permissionmanager.db.DBAdapter.COMMON_TABLE_URI;	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        r3 = "key = 20160906";	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        r2 = 0;	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        r4 = 0;	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        r5 = 0;	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        if (r6 == 0) goto L_0x0026;	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
    L_0x0013:
        r0 = r6.getCount();	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        if (r0 <= 0) goto L_0x002c;	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
    L_0x0019:
        r0 = r9.getContentResolver();	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        r1 = com.huawei.permissionmanager.db.DBAdapter.COMMON_TABLE_URI;	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        r2 = "key = 20160906";	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        r3 = 0;	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        r0.update(r1, r10, r2, r3);	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
    L_0x0026:
        if (r6 == 0) goto L_0x002b;
    L_0x0028:
        r6.close();
    L_0x002b:
        return;
    L_0x002c:
        r0 = r9.getContentResolver();	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        r1 = com.huawei.permissionmanager.db.DBAdapter.COMMON_TABLE_URI;	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        r0.insert(r1, r10);	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        goto L_0x0026;
    L_0x0036:
        r7 = move-exception;
        r0 = "StartupDBOpenHelper";	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        r1 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        r1.<init>();	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        r2 = "Database exception!";	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        r1 = r1.append(r2);	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        r2 = r7.getMessage();	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        r1 = r1.append(r2);	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        r1 = r1.toString();	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        com.huawei.systemmanager.util.HwLog.e(r0, r1);	 Catch:{ RuntimeException -> 0x0036, all -> 0x005b }
        if (r6 == 0) goto L_0x002b;
    L_0x0057:
        r6.close();
        goto L_0x002b;
    L_0x005b:
        r0 = move-exception;
        if (r6 == 0) goto L_0x0061;
    L_0x005e:
        r6.close();
    L_0x0061:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.startupmgr.db.StartupDBOpenHelper.insertAwakedSwitchBackup(android.content.Context, android.content.ContentValues):void");
    }

    public static synchronized StartupDBOpenHelper getInstance(Context context) {
        StartupDBOpenHelper startupDBOpenHelper;
        synchronized (StartupDBOpenHelper.class) {
            if (sInstance == null) {
                sInstance = new StartupDBOpenHelper(context);
            }
            startupDBOpenHelper = sInstance;
        }
        return startupDBOpenHelper;
    }

    public static int getDatabaseVersion() {
        return 3;
    }

    private StartupDBOpenHelper(Context context) {
        super(context, DB_NAME, null, 3);
    }

    protected List<AbsFeatureView> getFeatureViews() {
        List<AbsFeatureView> result = Lists.newArrayList();
        result.add(new StartupNormalFeatureView());
        result.add(new StartupAwakedFeatureView());
        return result;
    }

    protected void concreteOnUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        upgradeFrom1To2(db, oldVersion, newVersion);
    }

    protected void createConcreteTables(SQLiteDatabase db) {
        createAllTablesAndIndexIfNotExist(db);
    }

    protected void dropConcreteTables(SQLiteDatabase db) {
        HwLog.i(TAG, "dropConcreteTables");
    }

    protected void createConcreteViews(SQLiteDatabase db) {
        HwLog.i(TAG, "createConcreteViews");
    }

    protected void dropConcreteViews(SQLiteDatabase db) {
        HwLog.i(TAG, "dropConcreteViews");
    }

    public void insertNormalBackup(ContentValues values) {
        int result = updateGFeatureTable(StartupDBConst.SHARED_REAL_TABLE, GFeatureCvt.cvtToStdContentValue(values.getAsString("packageName"), NormalViewKeys.STATUS_STORE, values.getAsString("status")));
        if (result > 0) {
            replaceFeatureRow(StartupDBConst.SHARED_REAL_TABLE, GFeatureCvt.cvtToStdContentValue(values.getAsString("packageName"), NormalViewKeys.USER_CHANGED_STORE, "1"));
        }
        HwLog.d(TAG, "insertNormalBackup result: " + result);
    }

    public void insertAwakedBackup(ContentValues values) {
        int result = updateGFeatureTable(StartupDBConst.SHARED_REAL_TABLE, GFeatureCvt.cvtToStdContentValue(values.getAsString("packageName"), AwakedViewKeys.STATUS_STORE, values.getAsString("status")));
        if (result > 0) {
            replaceFeatureRow(StartupDBConst.SHARED_REAL_TABLE, GFeatureCvt.cvtToStdContentValue(values.getAsString("packageName"), AwakedViewKeys.USER_CHANGED_STORE, "1"));
        }
        HwLog.d(TAG, "insertNormalBackup result: " + result);
    }

    public Cursor queryNormalBackup() {
        return queryComm(NormalViewKeys.NORMAL_STARTUP_INFO_VIEW, new String[]{"packageName", "status"}, null, null);
    }

    public Cursor queryAwakedBackup() {
        return queryComm(AwakedViewKeys.AWAKED_STARTUP_INFO_VIEW, new String[]{"packageName", "status"}, null, null);
    }

    public void checkRecordTable(String tableName) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (DBHelper.HISTORY_MAX_SIZE > DatabaseUtils.queryNumEntries(db, tableName)) {
                HwLog.d(TAG, "checkRecordTable " + tableName + "'s count not reach 2*" + 5000);
                return;
            }
            HwLog.i(TAG, "checkRecordTable delete records with future time newer than " + timeCurrent + ", delete count: " + db.delete(tableName, "timeOfLastExact > ? ", new String[]{String.valueOf(3600000 + System.currentTimeMillis())}));
            HwLog.i(TAG, "checkRecordTable delete records with older than " + timeBeforeToDel + ", delete count: " + db.delete(tableName, "timeOfLastExact < ? ", new String[]{String.valueOf(DatabaseUtils.longForQuery(db, "SELECT timeOfLastExact FROM " + tableName + " ORDER BY " + "timeOfLastExact" + " DESC LIMIT 1 OFFSET " + 5000, null))}));
        } catch (SQLiteException ex) {
            HwLog.w(TAG, "checkRecordTable catch SQLiteException: " + ex.getMessage());
        } catch (Exception ex2) {
            HwLog.w(TAG, "checkRecordTable catch Exception: " + ex2.getMessage());
        }
    }

    private List<ITableInfo> allConcreteTables() {
        List<ITableInfo> tbls = Lists.newArrayList();
        tbls.add(new AwakedCallerTable());
        tbls.add(new AwakedRecordTable());
        tbls.add(new NormalRecordTable());
        tbls.add(new LocalizePackageNameTable());
        return tbls;
    }

    private void createAllTablesAndIndexIfNotExist(SQLiteDatabase db) {
        for (ITableInfo tbl : allConcreteTables()) {
            DbOpWrapper.createTable(db, tbl.getTableName(), tbl.getColumnDefines());
            DbOpWrapper.createIndex(db, tbl.getTableName(), tbl.getIndexCols());
        }
    }

    private void upgradeFrom1To2(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 1 && newVersion > 1) {
            createAllTablesAndIndexIfNotExist(db);
        }
    }

    public Cursor queryAwakedSwitchBackup(Context context) {
        return context.getContentResolver().query(DBAdapter.COMMON_TABLE_URI, null, "key = 20160906", null, null);
    }
}
