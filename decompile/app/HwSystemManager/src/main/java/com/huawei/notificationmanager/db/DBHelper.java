package com.huawei.notificationmanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.huawei.notificationmanager.common.CommonObjects.NotificationCfgInfo;
import com.huawei.notificationmanager.common.ConstValues;
import com.huawei.notificationmanager.common.NotificationBackend;
import com.huawei.notificationmanager.util.Helper;
import com.huawei.notificationmanager.util.NmCenterDefValueXmlHelper;
import com.huawei.systemmanager.backup.HsmSQLiteOpenHelper;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import com.huawei.systemmanager.util.app.HsmPkgUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBHelper extends HsmSQLiteOpenHelper {
    private static final String DB_NAME = "notificationmgr.db";
    private static final int DB_VERSION = 13;
    private static final String TAG = "NotificationDBHelper";
    private Context mDBContext = null;

    private boolean recoverFromVersion10(android.database.sqlite.SQLiteDatabase r27, int r28) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:57:?
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.modifyBlocksTree(BlockProcessor.java:248)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:52)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r26 = this;
        r2 = "NotificationDBHelper";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "recoverFromVersion10: Start, oldVersion = ";
        r5 = r5.append(r6);
        r0 = r28;
        r5 = r5.append(r0);
        r5 = r5.toString();
        com.huawei.systemmanager.util.HwLog.i(r2, r5);
        r11 = "tbNotificationMgrCfg";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r2 = r2.append(r11);
        r5 = "_tmpbak";
        r2 = r2.append(r5);
        r3 = r2.toString();
        r4 = 0;
        switch(r28) {
            case 9: goto L_0x0039;
            case 10: goto L_0x0066;
            case 11: goto L_0x0066;
            case 12: goto L_0x0088;
            case 13: goto L_0x00b6;
            default: goto L_0x0037;
        };
    L_0x0037:
        r2 = 0;
        return r2;
    L_0x0039:
        r2 = 2;
        r4 = new java.lang.String[r2];
        r2 = "packageName";
        r5 = 0;
        r4[r5] = r2;
        r2 = "notificationcfg";
        r5 = 1;
        r4[r5] = r2;
    L_0x0048:
        r10 = 1;
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r9 = 0;
        r2 = r27;
        r12 = r2.query(r3, r4, r5, r6, r7, r8, r9);
        r2 = 1;
        r2 = com.huawei.systemmanager.comm.misc.Utility.isNullOrEmptyCursor(r12, r2);
        if (r2 == 0) goto L_0x00ec;
    L_0x005b:
        r2 = "NotificationDBHelper";
        r5 = "recoverFromVersion10: Empty backup data";
        com.huawei.systemmanager.util.HwLog.i(r2, r5);
        r2 = 1;
        return r2;
    L_0x0066:
        r2 = 5;
        r4 = new java.lang.String[r2];
        r2 = "packageName";
        r5 = 0;
        r4[r5] = r2;
        r2 = "notificationcfg";
        r5 = 1;
        r4[r5] = r2;
        r2 = "lockscreencfg";
        r5 = 2;
        r4[r5] = r2;
        r2 = "statusbarcfg";
        r5 = 3;
        r4[r5] = r2;
        r2 = "headsubcfg";
        r5 = 4;
        r4[r5] = r2;
        goto L_0x0048;
    L_0x0088:
        r2 = 7;
        r4 = new java.lang.String[r2];
        r2 = "packageName";
        r5 = 0;
        r4[r5] = r2;
        r2 = "notificationcfg";
        r5 = 1;
        r4[r5] = r2;
        r2 = "lockscreencfg";
        r5 = 2;
        r4[r5] = r2;
        r2 = "statusbarcfg";
        r5 = 3;
        r4[r5] = r2;
        r2 = "headsubcfg";
        r5 = 4;
        r4[r5] = r2;
        r2 = "hide_content";
        r5 = 5;
        r4[r5] = r2;
        r2 = "sound_vibrate";
        r5 = 6;
        r4[r5] = r2;
        goto L_0x0048;
    L_0x00b6:
        r2 = 8;
        r4 = new java.lang.String[r2];
        r2 = "packageName";
        r5 = 0;
        r4[r5] = r2;
        r2 = "notificationcfg";
        r5 = 1;
        r4[r5] = r2;
        r2 = "lockscreencfg";
        r5 = 2;
        r4[r5] = r2;
        r2 = "statusbarcfg";
        r5 = 3;
        r4[r5] = r2;
        r2 = "headsubcfg";
        r5 = 4;
        r4[r5] = r2;
        r2 = "hide_content";
        r5 = 5;
        r4[r5] = r2;
        r2 = "sound_vibrate";
        r5 = 6;
        r4[r5] = r2;
        r2 = "firststartcfg";
        r5 = 7;
        r4[r5] = r2;
        goto L_0x0048;
    L_0x00ec:
        r2 = "packageName";
        r21 = r12.getColumnIndex(r2);
        r2 = "notificationcfg";
        r17 = r12.getColumnIndex(r2);
        r2 = 9;
        r0 = r28;
        if (r2 != r0) goto L_0x01d7;
    L_0x0100:
        r27.beginTransaction();	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
    L_0x0103:
        r2 = r12.moveToNext();	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        if (r2 == 0) goto L_0x01ca;	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
    L_0x0109:
        r0 = r21;	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        r24 = r12.getString(r0);	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        r25 = new android.content.ContentValues;	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        r25.<init>();	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        r2 = "notificationcfg";	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        r0 = r17;	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        r5 = r12.getString(r0);	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        r0 = r25;	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        r0.put(r2, r5);	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        r2 = "packageName=?";	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        r5 = 1;	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        r5 = new java.lang.String[r5];	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        r6 = 0;	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        r5[r6] = r24;	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        r0 = r27;	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        r1 = r25;	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        r0.update(r11, r1, r2, r5);	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        goto L_0x0103;
    L_0x0133:
        r13 = move-exception;
        r2 = "NotificationDBHelper";	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        r5 = "recoverFromVersion10: Exception";	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        com.huawei.systemmanager.util.HwLog.e(r2, r5, r13);	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        r10 = 0;
        r27.endTransaction();
    L_0x0141:
        r12.close();
        r2 = "NotificationDBHelper";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "recoverFromVersion10: Recover cfg complete, count = ";
        r5 = r5.append(r6);
        r6 = r12.getCount();
        r5 = r5.append(r6);
        r6 = ", bSuccess = ";
        r5 = r5.append(r6);
        r5 = r5.append(r10);
        r5 = r5.toString();
        com.huawei.systemmanager.util.HwLog.d(r2, r5);
        r14 = "tbNotificationMgrLog";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r2 = r2.append(r14);
        r5 = "_tmpbak";
        r2 = r2.append(r5);
        r15 = r2.toString();
        r2 = 0;
        r5 = 0;
        r0 = r27;	 Catch:{ Exception -> 0x02b2 }
        r0.delete(r14, r2, r5);	 Catch:{ Exception -> 0x02b2 }
        r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x02b2 }
        r2.<init>();	 Catch:{ Exception -> 0x02b2 }
        r5 = "INSERT INTO ";	 Catch:{ Exception -> 0x02b2 }
        r2 = r2.append(r5);	 Catch:{ Exception -> 0x02b2 }
        r2 = r2.append(r14);	 Catch:{ Exception -> 0x02b2 }
        r5 = " SELECT * FROM ";	 Catch:{ Exception -> 0x02b2 }
        r2 = r2.append(r5);	 Catch:{ Exception -> 0x02b2 }
        r2 = r2.append(r15);	 Catch:{ Exception -> 0x02b2 }
        r2 = r2.toString();	 Catch:{ Exception -> 0x02b2 }
        r0 = r27;	 Catch:{ Exception -> 0x02b2 }
        r0.execSQL(r2);	 Catch:{ Exception -> 0x02b2 }
    L_0x01af:
        r2 = "NotificationDBHelper";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "recoverFromVersion10: End. bSuccess = ";
        r5 = r5.append(r6);
        r5 = r5.append(r10);
        r5 = r5.toString();
        com.huawei.systemmanager.util.HwLog.d(r2, r5);
        return r10;
    L_0x01ca:
        r27.setTransactionSuccessful();	 Catch:{ Exception -> 0x0133, all -> 0x01d2 }
        r27.endTransaction();
        goto L_0x0141;
    L_0x01d2:
        r2 = move-exception;
        r27.endTransaction();
        throw r2;
    L_0x01d7:
        r2 = "lockscreencfg";	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r20 = r12.getColumnIndex(r2);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r2 = "statusbarcfg";	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r23 = r12.getColumnIndex(r2);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r2 = "headsubcfg";	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r18 = r12.getColumnIndex(r2);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r2 = "hide_content";	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r19 = r12.getColumnIndex(r2);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r2 = "sound_vibrate";	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r22 = r12.getColumnIndex(r2);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r2 = "firststartcfg";	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r16 = r12.getColumnIndex(r2);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r27.beginTransaction();	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
    L_0x0204:
        r2 = r12.moveToNext();	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        if (r2 == 0) goto L_0x02a5;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
    L_0x020a:
        r0 = r21;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r24 = r12.getString(r0);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r25 = new android.content.ContentValues;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r25.<init>();	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r2 = "notificationcfg";	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0 = r17;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r5 = r12.getString(r0);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0 = r25;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0.put(r2, r5);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r2 = "lockscreencfg";	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0 = r20;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r5 = r12.getString(r0);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0 = r25;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0.put(r2, r5);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r2 = "statusbarcfg";	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0 = r23;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r5 = r12.getString(r0);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0 = r25;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0.put(r2, r5);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r2 = "headsubcfg";	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0 = r18;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r5 = r12.getString(r0);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0 = r25;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0.put(r2, r5);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r2 = 12;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0 = r28;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        if (r0 < r2) goto L_0x026f;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
    L_0x0253:
        r2 = "hide_content";	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0 = r19;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r5 = r12.getString(r0);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0 = r25;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0.put(r2, r5);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r2 = "sound_vibrate";	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0 = r22;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r5 = r12.getString(r0);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0 = r25;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0.put(r2, r5);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
    L_0x026f:
        r2 = 13;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0 = r28;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        if (r0 < r2) goto L_0x0283;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
    L_0x0275:
        r2 = "firststartcfg";	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0 = r16;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r5 = r12.getString(r0);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0 = r25;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0.put(r2, r5);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
    L_0x0283:
        r2 = "packageName=?";	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r5 = 1;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r5 = new java.lang.String[r5];	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r6 = 0;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r5[r6] = r24;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0 = r27;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r1 = r25;	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r0.update(r11, r1, r2, r5);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        goto L_0x0204;
    L_0x0295:
        r13 = move-exception;
        r2 = "NotificationDBHelper";	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r5 = "recoverFromVersion10: Exception";	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        com.huawei.systemmanager.util.HwLog.e(r2, r5, r13);	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r10 = 0;
        r27.endTransaction();
        goto L_0x0141;
    L_0x02a5:
        r27.setTransactionSuccessful();	 Catch:{ Exception -> 0x0295, all -> 0x02ad }
        r27.endTransaction();
        goto L_0x0141;
    L_0x02ad:
        r2 = move-exception;
        r27.endTransaction();
        throw r2;
    L_0x02b2:
        r13 = move-exception;
        r2 = "NotificationDBHelper";
        r5 = "recoverFromVersion10: Exception";
        com.huawei.systemmanager.util.HwLog.e(r2, r5, r13);
        r10 = 0;
        goto L_0x01af;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.notificationmanager.db.DBHelper.recoverFromVersion10(android.database.sqlite.SQLiteDatabase, int):boolean");
    }

    protected DBHelper(Context context) {
        super(context, DB_NAME, null, 13);
        this.mDBContext = context;
    }

    public static int getDBVersion() {
        return 13;
    }

    public void onCreate(SQLiteDatabase db) {
        if (db == null) {
            HwLog.e(TAG, "onCreate: Invalid db ,Fail to init DB tables and data");
            return;
        }
        createDBTables(db);
        createKeyIndex(db);
        initNotificationCfgs(db);
    }

    private void createDBTables(SQLiteDatabase db) {
        createNotificationCfgTable(db, ConstValues.TB_NOTIFICATIONMGR_CFG);
        createNotificationLogTable(db, ConstValues.TB_NOTIFICATIONMGR_LOG);
    }

    private void dropTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS tbNotificationMgrCfg");
        db.execSQL("DROP TABLE IF EXISTS tbNotificationMgrLog");
    }

    private void createKeyIndex(SQLiteDatabase db) {
        db.execSQL("create unique index if not exists packageName_idx ON tbNotificationMgrCfg(packageName)");
    }

    private void createNotificationCfgTable(SQLiteDatabase db, String tableName) {
        db.execSQL("create table if not exists " + tableName + " ( " + "_id" + " integer primary key autoincrement, " + "packageName" + " text, " + ConstValues.NOTIFICATION_CFG + " int " + " DEFAULT (0), " + ConstValues.NOTIFICATION_LOCKSCREEN_CFG + " int " + " DEFAULT (0), " + ConstValues.NOTIFICATION_STATUSBAR_CFG + " int " + " DEFAULT (1), " + ConstValues.NOTIFICATION_HEADSUP_CFG + " int " + " DEFAULT (0)," + ConstValues.NOTIFICATION_CANFORBID + " int " + " DEFAULT (1), " + ConstValues.NOTIFICATION_INDEX + " int " + " DEFAULT (10000), " + ConstValues.NOTIFICATION_HIDE_CONTENT + " int " + " DEFAULT (0)," + ConstValues.NOTIFICATION_SOUND_VIBRATE + " int " + " DEFAULT (3), " + ConstValues.NOTIFICATION_FIRSTSTART_CFG + " int " + " DEFAULT (0) " + ");");
    }

    private void createNotificationLogTable(SQLiteDatabase db, String tableName) {
        db.execSQL("create table if not exists " + tableName + " ( " + "_id" + " integer primary key autoincrement, " + "packageName" + " text, " + "logDatetime" + " long, " + "logTitle" + " text, " + "logText" + " text);");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        HwLog.i(TAG, "onUpgrade: oldVersion = " + oldVersion + ", newVersion =" + newVersion);
        if (oldVersion <= 9 && onUpgradeTo10(db)) {
            initNotificationCfgs(db, true);
        }
        if (oldVersion < 11) {
            upgradeFrom10To11(db, oldVersion, newVersion);
        }
        if (oldVersion < 12) {
            upgradeFrom11To12(db, oldVersion, newVersion);
        }
        if (oldVersion < 13) {
            upgradeFrom12To13(db, oldVersion, newVersion);
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        HwLog.i(TAG, "onDowngrade: oldVersion = " + oldVersion + ", newVersion =" + newVersion);
        dropTables(db);
        createDBTables(db);
        createKeyIndex(db);
        initNotificationCfgs(db);
    }

    private boolean onUpgradeTo10(SQLiteDatabase db) {
        return upgradeCfgTableTo10(db, ConstValues.TB_NOTIFICATIONMGR_CFG);
    }

    private void upgradeFrom10To11(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion > 10 || newVersion < 11) {
            HwLog.i(TAG, "upgradeFrom10To11 ,  not the 10 to 11 ,return");
            return;
        }
        HwLog.i(TAG, "upgradeFrom10To11 start");
        Cursor cursor = null;
        List<NotificationCfgInfo> list = new ArrayList();
        try {
            cursor = db.query(ConstValues.TB_NOTIFICATIONMGR_CFG, null, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    NotificationCfgInfo appInfo = new NotificationCfgInfo();
                    appInfo.parseCfgsFrom(cursor);
                    list.add(appInfo);
                }
                if (cursor != null) {
                    cursor.close();
                }
                NotificationBackend backend = new NotificationBackend();
                HwLog.i(TAG, "upgradeFrom10To11 list size is:  " + list.size());
                for (NotificationCfgInfo cfgInfo : list) {
                    backend.setPeekable(cfgInfo.mPkgName, cfgInfo.mUid, cfgInfo.isHeadsupNotificationEnabled());
                    backend.setSensitive(cfgInfo.mPkgName, cfgInfo.mUid, cfgInfo.isLockscreenNotificationEnabled(), cfgInfo.isHideContent());
                }
                HwLog.i(TAG, "upgradeFrom10To11 end");
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void upgradeFrom11To12(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion > 11 || newVersion < 12) {
            HwLog.i(TAG, "upgradeFrom11To12 version not match, return");
            return;
        }
        db.execSQL("alter table tbNotificationMgrCfg add column hide_content integer default (0);");
        db.execSQL("alter table tbNotificationMgrCfg add column sound_vibrate integer default (3);");
    }

    private void upgradeFrom12To13(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion > 12 || newVersion < 13) {
            HwLog.i(TAG, "upgradeFrom12To13 ,  not the 12 to 13 ,return");
            return;
        }
        db.execSQL("ALTER TABLE tbNotificationMgrCfg ADD firststartcfg int  DEFAULT (0);");
        db.execSQL("UPDATE tbNotificationMgrCfg set headsubcfg = 1 WHERE packageName = 'com.android.incallui'");
        HwLog.i(TAG, "When update From db 12 to 13, make sure com.android.incallui perfects the same with M edition in headsupconfig");
    }

    private boolean upgradeCfgTableTo10(SQLiteDatabase db, String tableName) {
        boolean bResult = false;
        String tableNew = tableName;
        try {
            String tableBak = tableName + "_bak";
            db.beginTransaction();
            db.execSQL("ALTER TABLE " + tableName + " RENAME TO " + tableBak);
            createNotificationCfgTable(db, tableName);
            db.execSQL("INSERT INTO " + tableName + "(packageName, notificationcfg)" + " SELECT " + "packageName, notificationCfg" + " FROM " + tableBak);
            db.execSQL("DROP TABLE " + tableBak);
            db.setTransactionSuccessful();
            bResult = true;
            HwLog.i(TAG, "upgradeTableTo10: Update db to 10 successfully. tableName = " + tableName);
        } catch (Exception e) {
            HwLog.e(TAG, "upgradeTableTo10: Fail to update db. tableName = " + tableName, e);
        } finally {
            db.endTransaction();
        }
        return bResult;
    }

    private Map<String, Integer> getCurrentCfgs(SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            cursor = db.query(ConstValues.TB_NOTIFICATIONMGR_CFG, null, null, null, null, null, null);
            if (Utility.isNullOrEmptyCursor(cursor, true)) {
                HwLog.e(TAG, "getCurrentCfgs: No configs");
                return null;
            } else if (cursor.moveToNext()) {
                int nColIndexPkgName = cursor.getColumnIndex("packageName");
                int nColIndexCfg = cursor.getColumnIndex(ConstValues.NOTIFICATION_CFG);
                Map<String, Integer> result = new HashMap();
                do {
                    result.put(cursor.getString(nColIndexPkgName), Integer.valueOf(cursor.getInt(nColIndexCfg)));
                } while (cursor.moveToNext());
                cursor.close();
                return result;
            } else {
                HwLog.e(TAG, "getCurrentCfgs: Fail to read configs");
                cursor.close();
                return null;
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
            HwLog.e(TAG, "getCurrentCfgs: Exception", e);
            return null;
        }
    }

    private void initNotificationCfgs(SQLiteDatabase db) {
        initNotificationCfgs(db, false);
    }

    private void initSystemManagerNotificationCfgs() {
        NotificationBackend backend = new NotificationBackend();
        HsmPackageManager hsmPm = HsmPackageManager.getInstance();
        String hsmPkgName = this.mDBContext.getPackageName();
        HsmPkgInfo pkgInfo = hsmPm.getPkgInfo(hsmPkgName);
        if (pkgInfo != null) {
            backend.setSensitive(hsmPkgName, pkgInfo.mUid, false, false);
        }
    }

    private void initNotificationCfgs(SQLiteDatabase db, boolean onDBUpgrade) {
        HwLog.d(TAG, "initNotificationCfgs starts, onDBUpgrade = " + onDBUpgrade);
        initSystemManagerNotificationCfgs();
        List<HsmPkgInfo> appList = Helper.getMonitoredAppList(this.mDBContext);
        if (Utility.isNullOrEmptyList(appList)) {
            HwLog.w(TAG, "initNotificationCfgs: Fail to get monitored app list");
            return;
        }
        Map oldCfgs = null;
        if (onDBUpgrade) {
            oldCfgs = getCurrentCfgs(db);
        }
        List<ContentValues> valueList = new ArrayList();
        NmCenterDefValueXmlHelper defHelper = new NmCenterDefValueXmlHelper();
        NotificationBackend backend = new NotificationBackend();
        for (HsmPkgInfo pkgInfo : appList) {
            Integer num = null;
            if (oldCfgs != null) {
                num = (Integer) oldCfgs.get(pkgInfo.mPkgName);
            }
            valueList.add(getDftCfgForPkg(defHelper, pkgInfo.mPkgName, num, backend));
        }
        try {
            HwLog.d(TAG, "initNotificationCfgs start to update db");
            db.beginTransaction();
            for (ContentValues value : valueList) {
                String packageName = value.getAsString("packageName");
                if (0 == db.replace(ConstValues.TB_NOTIFICATIONMGR_CFG, null, value)) {
                    HwLog.w(TAG, "initNotificationCfgs: fail to init package cfg, " + packageName);
                }
            }
            db.setTransactionSuccessful();
            this.mDBContext.getContentResolver().notifyChange(DBProvider.URI_NOTIFICATION_CFG, null);
        } catch (Exception e) {
            HwLog.e(TAG, "initNotificationCfgs: Exception, ", e);
        } finally {
            db.endTransaction();
            HwLog.i(TAG, "initNotificationCfgs ends");
        }
    }

    public void initLockScreenCfgDefaultValue(ContentValues cfg) {
        if (AbroadUtils.isAbroad()) {
            cfg.put(ConstValues.NOTIFICATION_LOCKSCREEN_CFG, Integer.valueOf(1));
            HwLog.d("DBHelper", "lockscreen=SWITCH_MODE_OPEN");
        }
    }

    private ContentValues getDftCfgForPkg(NmCenterDefValueXmlHelper parser, String pkgname, Integer oldCfg, NotificationBackend backend) {
        ContentValues cfg = parser.getCloudPreferedDefaultConfig(this.mDBContext, pkgname);
        HwLog.d(TAG, "getDftCfgForPkg: pkgname = " + pkgname + ", cfg = " + cfg + ", oldCfg = " + oldCfg);
        if (cfg == null) {
            cfg = new ContentValues();
            if (oldCfg != null) {
                cfg.put(ConstValues.NOTIFICATION_CFG, oldCfg);
                HwLog.i(TAG, "getDftCfgForPkg: Reuse old cfg. pkgname = " + pkgname + ", oldCfg = " + oldCfg);
            }
            cfg.put("packageName", pkgname);
            initLockScreenCfgDefaultValue(cfg);
            updateForMVersion(backend, cfg, pkgname);
            return cfg;
        }
        cfg.put("packageName", pkgname);
        initLockScreenCfgDefaultValue(cfg);
        if (oldCfg != null) {
            cfg.put(ConstValues.NOTIFICATION_CFG, oldCfg);
            HwLog.i(TAG, "getDftCfgForPkg: Override with old cfg. pkgname = " + pkgname + ", oldCfg = " + oldCfg);
            updateForMVersion(backend, cfg, pkgname);
            return cfg;
        }
        int uid = HsmPkgUtils.getPackageUid(pkgname);
        if (-1 == uid) {
            cfg.put(ConstValues.NOTIFICATION_CFG, Integer.valueOf(1));
            HwLog.w(TAG, "getDftCfgForPkg: Fail to get uid , skip applying default cfg. pkgname = " + pkgname);
            return cfg;
        }
        int nCfg = cfg.getAsInteger(ConstValues.NOTIFICATION_CFG).intValue();
        switch (nCfg) {
            case 0:
                cfg.put(ConstValues.NOTIFICATION_CFG, Integer.valueOf(1));
                Helper.setNotificationsEnabledForPackage(pkgname, uid, Boolean.valueOf(false));
                break;
            case 1:
                cfg.put(ConstValues.NOTIFICATION_CFG, Integer.valueOf(1));
                Helper.setNotificationsEnabledForPackage(pkgname, uid, Boolean.valueOf(true));
                break;
            case 2:
                cfg.put(ConstValues.NOTIFICATION_CFG, Integer.valueOf(0));
                Helper.setNotificationsEnabledForPackage(pkgname, uid, Boolean.valueOf(true));
                break;
            default:
                HwLog.w(TAG, "getDftCfgForPkg: Invalid cfg = " + nCfg + ", pkgname = " + pkgname);
                cfg.put(ConstValues.NOTIFICATION_CFG, Integer.valueOf(0));
                break;
        }
        updateForMVersion(backend, cfg, pkgname);
        return cfg;
    }

    private void updateForMVersion(NotificationBackend backend, ContentValues cfg, String pkgName) {
        if (backend != null && cfg != null && !TextUtils.isEmpty(pkgName)) {
            NotificationCfgInfo info = new NotificationCfgInfo(pkgName);
            info.copyCfgsFrom(cfg);
            HsmPkgInfo pkgInfo = HsmPackageManager.getInstance().getPkgInfo(info.mPkgName);
            if (pkgInfo != null) {
                info.mUid = pkgInfo.mUid;
            }
            backend.setPeekable(info.mPkgName, info.mUid, info.isHeadsupNotificationEnabled());
            backend.setSensitive(info.mPkgName, info.mUid, info.isLockscreenNotificationEnabled(), info.isHideContent());
        }
    }

    protected boolean onRecoverStart(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "onRecoverStart: Start, oldVersion = " + oldVersion);
        boolean bSuccess = false;
        switch (oldVersion) {
            case 9:
                bSuccess = createTempTablesVersion9(db, oldVersion);
                break;
            case 10:
            case 11:
            case 12:
            case 13:
                bSuccess = createTempTablesVersion10(db, oldVersion);
                break;
            default:
                HwLog.w(TAG, "onRecoverStart: Invalid recover version = " + oldVersion);
                break;
        }
        HwLog.i(TAG, "onRecoverStart: Result = " + bSuccess);
        return bSuccess;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean onRecoverComplete(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "onRecoverComplete: Start, oldVersion = " + oldVersion);
        boolean bSuccess = false;
        switch (oldVersion) {
            case 9:
                break;
            case 10:
            case 11:
            case 12:
            case 13:
                if (recoverFromVersion10(db, oldVersion)) {
                    bSuccess = true;
                    break;
                }
                break;
            default:
                HwLog.w(TAG, "onRecoverComplete: Invalid recover version = " + oldVersion);
                break;
        }
        clearRecoverTmpTablesAndMap(db);
        HwLog.i(TAG, "onRecoverComplete: End, bSuccess = " + bSuccess);
        return bSuccess;
    }

    private boolean createTempTablesVersion9(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "createTempTablesVersion9: Start, oldVersion = " + oldVersion);
        boolean bSuccess = false;
        try {
            String strCfgTable = ConstValues.TB_NOTIFICATIONMGR_CFG;
            String strCfgTableBak = strCfgTable + "_tmpbak";
            db.execSQL("DROP TABLE IF EXISTS " + strCfgTableBak);
            db.execSQL("create table " + strCfgTableBak + " ( " + "_id" + " integer primary key autoincrement, " + "packageName" + " text, " + "notificationCfg" + " int " + " DEFAULT (0)" + ");");
            String strLogTable = ConstValues.TB_NOTIFICATIONMGR_LOG;
            String strLogTableBak = strLogTable + "_tmpbak";
            db.execSQL("DROP TABLE IF EXISTS " + strLogTableBak);
            db.execSQL("create table " + strLogTableBak + " ( " + "_id" + " integer primary key autoincrement, " + "packageName" + " text, " + "logDatetime" + " long, " + "logTitle" + " text, " + "logText" + " text);");
            putRecoverTmpTableMap(strCfgTable, strCfgTableBak);
            putRecoverTmpTableMap(strLogTable, strLogTableBak);
            bSuccess = true;
        } catch (Exception e) {
            HwLog.e(TAG, "createTempTablesVersion9: Exception", e);
        }
        HwLog.i(TAG, "createTempTablesVersion9: End, bSuccess = " + bSuccess);
        return bSuccess;
    }

    private boolean createTempTablesVersion10(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "createTempTablesVersion10: Start, oldVersion = " + oldVersion);
        boolean bSuccess = false;
        try {
            String strCfgTable = ConstValues.TB_NOTIFICATIONMGR_CFG;
            String strCfgTableBak = strCfgTable + "_tmpbak";
            db.execSQL("DROP TABLE IF EXISTS " + strCfgTableBak);
            createNotificationCfgTable(db, strCfgTableBak);
            HwLog.d(TAG, "createTempTablesVersion10: Create table " + strCfgTableBak);
            String strLogTable = ConstValues.TB_NOTIFICATIONMGR_LOG;
            String strLogTableBak = strLogTable + "_tmpbak";
            db.execSQL("DROP TABLE IF EXISTS " + strLogTableBak);
            createNotificationLogTable(db, strLogTableBak);
            HwLog.d(TAG, "createTempTablesVersion10: Create table " + strLogTableBak);
            putRecoverTmpTableMap(strCfgTable, strCfgTableBak);
            putRecoverTmpTableMap(strLogTable, strLogTableBak);
            bSuccess = true;
        } catch (Exception e) {
            HwLog.e(TAG, "createTempTablesVersion10: Exception", e);
        }
        HwLog.i(TAG, "createTempTablesVersion10: End, bSuccess = " + bSuccess);
        return bSuccess;
    }

    private boolean recoverFromVersion9(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "recoverFromVersion9: Start, oldVersion = " + oldVersion);
        return upgradeCfgTableTo10(db, "tbNotificationMgrCfg_tmpbak");
    }
}
