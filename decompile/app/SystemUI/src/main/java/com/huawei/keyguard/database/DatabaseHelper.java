package com.huawei.keyguard.database;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.UserHandle;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.support.magazine.MagazineUtils;
import com.huawei.keyguard.util.HwLog;
import java.util.HashSet;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final HashSet<String> VALIDTABLES = new HashSet();
    private Context mContext;
    private SQLiteDatabase mDatabase;
    private Runnable mReLoader = new Runnable() {
        private int total = 1;

        public void run() {
            if (DatabaseHelper.this.mContext == null || this.total > 50 || (MagazineUtils.isDataInited(DatabaseHelper.this.mContext) && !DatabaseHelper.this.needUpdatePicWhenOTA(DatabaseHelper.this.mDatabase))) {
                HwLog.i("MagazineDBHelper", "copy image has stop, total=" + this.total);
                this.total = 1;
                DatabaseHelper.this.mReLoader = null;
                return;
            }
            DatabaseHelper.this.loadData();
            this.total++;
            GlobalContext.getBackgroundHandler().postDelayed(this, 60000);
        }
    };

    private void delRepeatChannelInfo(android.database.sqlite.SQLiteDatabase r15) {
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
        r14 = this;
        r12 = "channels.channelId";
        r9 = 0;
        r1 = "channels";	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r0 = 1;	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r2 = new java.lang.String[r0];	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r0 = "channelId";	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r3 = 0;	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r2[r3] = r0;	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r5 = "channels.channelId";	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r6 = "count(*) > 1";	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r3 = 0;	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r4 = 0;	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r7 = 0;	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r8 = 0;	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r0 = r15;	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r9 = r0.query(r1, r2, r3, r4, r5, r6, r7, r8);	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        if (r9 == 0) goto L_0x004f;	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
    L_0x0021:
        r0 = r9.getCount();	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        if (r0 <= 0) goto L_0x004f;	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
    L_0x0027:
        r13 = "title is null and icon is null and version is null";	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r0 = "channels";	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r1 = "title is null and icon is null and version is null";	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r2 = 0;	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r10 = r15.delete(r0, r1, r2);	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r0 = "MagazineDBHelper";	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r1 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r1.<init>();	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r2 = "triggerLoadData is finished, delete repeat channel counts = ";	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r1 = r1.append(r2);	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r1 = r1.append(r10);	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r1 = r1.toString();	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        com.huawei.keyguard.util.HwLog.w(r0, r1);	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
    L_0x004f:
        if (r9 == 0) goto L_0x0054;
    L_0x0051:
        r9.close();
    L_0x0054:
        return;
    L_0x0055:
        r11 = move-exception;
        r0 = "MagazineDBHelper";	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r1 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r1.<init>();	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r2 = "delRepeatChannelInfo e = ";	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r1 = r1.append(r2);	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r2 = r11.toString();	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r1 = r1.append(r2);	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        r1 = r1.toString();	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        com.huawei.keyguard.util.HwLog.w(r0, r1);	 Catch:{ SQLException -> 0x0055, all -> 0x007a }
        if (r9 == 0) goto L_0x0054;
    L_0x0076:
        r9.close();
        goto L_0x0054;
    L_0x007a:
        r0 = move-exception;
        if (r9 == 0) goto L_0x0080;
    L_0x007d:
        r9.close();
    L_0x0080:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.keyguard.database.DatabaseHelper.delRepeatChannelInfo(android.database.sqlite.SQLiteDatabase):void");
    }

    private boolean needUpdatePicWhenOTA(android.database.sqlite.SQLiteDatabase r10) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x009f in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r9 = this;
        r8 = 0;
        r5 = com.huawei.keyguard.support.magazine.HwFyuseUtils.isSupport3DFyuse();
        if (r5 != 0) goto L_0x0011;
    L_0x0007:
        r5 = "MagazineDBHelper";
        r6 = "fyuse is not supported";
        com.huawei.keyguard.util.HwLog.i(r5, r6);
        return r8;
    L_0x0011:
        r0 = 0;
        r3 = " select * from common where key = 'need_update_fyuse_pic_when_ota'";
        r5 = 0;
        r0 = r10.rawQuery(r3, r5);	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        if (r0 == 0) goto L_0x0022;	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
    L_0x001c:
        r5 = r0.getCount();	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        if (r5 != 0) goto L_0x0032;	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
    L_0x0022:
        r5 = "MagazineDBHelper";	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r6 = "no data in table common, means need update pic when OTA";	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        com.huawei.keyguard.util.HwLog.e(r5, r6);	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r5 = 1;
        if (r0 == 0) goto L_0x0031;
    L_0x002e:
        r0.close();
    L_0x0031:
        return r5;
    L_0x0032:
        r5 = r0.moveToNext();	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        if (r5 == 0) goto L_0x0050;	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
    L_0x0038:
        r5 = "value";	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r5 = r0.getColumnIndex(r5);	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r4 = r0.getString(r5);	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r5 = "true";	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r5 = r5.equalsIgnoreCase(r4);	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        if (r0 == 0) goto L_0x004f;
    L_0x004c:
        r0.close();
    L_0x004f:
        return r5;
    L_0x0050:
        if (r0 == 0) goto L_0x0055;
    L_0x0052:
        r0.close();
    L_0x0055:
        return r8;
    L_0x0056:
        r2 = move-exception;
        r5 = "MagazineDBHelper";	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r6 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r6.<init>();	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r7 = "needUpdatePicWhenOTA got OperationCanceledException ex = ";	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r6 = r6.append(r7);	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r7 = r2.toString();	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r6 = r6.append(r7);	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r6 = r6.toString();	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        com.huawei.keyguard.util.HwLog.e(r5, r6);	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        if (r0 == 0) goto L_0x007a;
    L_0x0077:
        r0.close();
    L_0x007a:
        return r8;
    L_0x007b:
        r1 = move-exception;
        r5 = "MagazineDBHelper";	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r6 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r6.<init>();	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r7 = "needUpdatePicWhenOTA got SQLiteException ex = ";	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r6 = r6.append(r7);	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r7 = r1.toString();	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r6 = r6.append(r7);	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        r6 = r6.toString();	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        com.huawei.keyguard.util.HwLog.e(r5, r6);	 Catch:{ SQLiteException -> 0x007b, OperationCanceledException -> 0x0056, all -> 0x00a0 }
        if (r0 == 0) goto L_0x009f;
    L_0x009c:
        r0.close();
    L_0x009f:
        return r8;
    L_0x00a0:
        r5 = move-exception;
        if (r0 == 0) goto L_0x00a6;
    L_0x00a3:
        r0.close();
    L_0x00a6:
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.keyguard.database.DatabaseHelper.needUpdatePicWhenOTA(android.database.sqlite.SQLiteDatabase):boolean");
    }

    static {
        VALIDTABLES.add("like");
        VALIDTABLES.add("type");
        VALIDTABLES.add("pictures");
        VALIDTABLES.add("channels");
        VALIDTABLES.add("theme_share_info");
        VALIDTABLES.add("common");
        VALIDTABLES.add("temp_updated_channels");
        VALIDTABLES.add("counter");
        VALIDTABLES.add("deletedhiads");
    }

    public DatabaseHelper(Context context) {
        super(context, "magazineunlock.db", null, 5);
        this.mContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        HwLog.i("MagazineDBHelper", "MagazineDatabase onCreate");
        createChannelTable(db);
        createTempUpdatedChannelTable(db);
        createPicturesTable(db);
        createDeleteTable(db);
        createCommonTable(db);
        initCounter(db);
    }

    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        checkBreakedOperations(db);
        this.mDatabase = db;
        triggerLoadData(db);
        HwLog.i("MagazineDBHelper", "MagazineDatabase onOpen: " + db.getPath());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        HwLog.i("MagazineDBHelper", "MagazineDatabase onUpgrade from version " + oldVersion + " to " + newVersion);
        switch (oldVersion) {
            case 1:
                if (newVersion > 1) {
                    initCounter(db);
                    break;
                }
                return;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
        }
    }

    public static boolean checkCollumnExists(SQLiteDatabase db, String table, String collumn) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + collumn + " FROM " + table + " LIMIT 0", null);
            if (cursor == null || cursor.getColumnIndex(collumn) == -1) {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            if (cursor != null) {
                cursor.close();
            }
            return true;
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void checkBreakedOperations(SQLiteDatabase db) {
        SharedPreferences sp = this.mContext.getSharedPreferences("magazine_preferences", 0);
        if (!sp.getBoolean("additianal_check_breakop", false)) {
            if (!checkCollumnExists(db, "pictures", "status")) {
                addValidStatusColumn(db);
                HwLog.w("MagazineDBHelper", "Do addValidStatusColumn for additional check");
            }
            if (!checkCollumnExists(db, "pictures", "hiadMaterialId")) {
                upgradeForHidInfo(db);
                HwLog.w("MagazineDBHelper", "Do upgradeForHidInfo for additional check");
            }
            if (!checkCollumnExists(db, "pictures", "picFormat")) {
                addPicFormatColumn(db);
                HwLog.w("MagazineDBHelper", "Do addPicFormatColumn for additional check");
            }
            sp.edit().putBoolean("additianal_check_breakop", true).apply();
        }
    }

    private void upgradeForHidInfo(SQLiteDatabase db) {
        try {
            db.beginTransaction();
            addHiAdColumn(db);
            createDeleteTable(db);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            HwLog.e("MagazineDBHelper", "UpgradeDBVerionFrom3to4 ex = " + e.toString());
        } catch (Exception e2) {
            HwLog.e("MagazineDBHelper", "UpgradeDBVerionFrom3to4 ex = " + e2.toString());
        } finally {
            db.endTransaction();
        }
    }

    private void createChannelTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE channels (_id INTEGER PRIMARY KEY AUTOINCREMENT,channelId TEXT,isSelected TEXT,downloadId TEXT,picNum TEXT,previewUrl TEXT,description TEXT,serviceId TEXT,version TEXT,title TEXT,cnTitle TEXT,coverUrl TEXT,downloadUrl TEXT,size TEXT,hashcode TEXT,icon BLOB);");
    }

    private void createTempUpdatedChannelTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE temp_updated_channels (_id INTEGER PRIMARY KEY AUTOINCREMENT,channelId TEXT,updated TEXT,picNum TEXT,previewUrl TEXT,description TEXT,serviceId TEXT,version TEXT,title TEXT,cnTitle TEXT,coverUrl TEXT,downloadUrl TEXT,size TEXT,hashcode TEXT);");
    }

    private void createPicturesTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pictures (_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,path TEXT,version TEXT,channelId TEXT,theme TEXT,isNew INTEGER,isCustom INTEGER,isPrivate INTEGER,isHidden INTEGER,width TEXT,height TEXT,date_modified TEXT,bucket_id INTEGER,title TEXT,content TEXT,SenderId TEXT,ReceiverId TEXT,isFavorite INTEGER,status INTEGER DEFAULT 0,picType INTEGER DEFAULT 1,hiadMaterialId TEXT,picFormat INTEGER DEFAULT -1);");
    }

    private void createCommonTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE common (_id INTEGER PRIMARY KEY AUTOINCREMENT,key TEXT,value TEXT,state TEXT);");
    }

    public static boolean isValidTable(String name) {
        return VALIDTABLES.contains(name);
    }

    private void triggerLoadData(SQLiteDatabase db) {
        if (this.mContext == null) {
            HwLog.w("MagazineDBHelper", "triggerLoadData, context is null");
        } else if (!MagazineUtils.isDataInited(this.mContext) || needUpdatePicWhenOTA(db)) {
            loadData();
            GlobalContext.getBackgroundHandler().postDelayed(this.mReLoader, 60000);
        } else {
            delRepeatChannelInfo(db);
            if (KeyguardCfg.isMagazineUpdateEnabled()) {
                updateAlarm();
            }
            HwLog.w("MagazineDBHelper", "send theme apply to self");
        }
    }

    private void loadData() {
        Intent intent = new Intent("com.huawei.android.thememanager.applytheme");
        intent.setPackage("com.android.keyguard");
        HwLog.d("MagazineDBHelper", "send applytheme broadcaset : ");
        this.mContext.sendBroadcastAsUser(intent, UserHandle.OWNER, "com.android.huawei.magazineunlock.permission.WRITE");
    }

    private void updateAlarm() {
        Intent intent = new Intent("com.android.keyguard.magazineunlock.UPDATE_ALARM");
        intent.setPackage("com.android.keyguard");
        HwLog.w("MagazineDBHelper", "send updateAlarm broadcaset : ");
        this.mContext.sendBroadcastAsUser(intent, UserHandle.OWNER, "com.android.huawei.magazineunlock.permission.WRITE");
    }

    private void initCounter(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE counter (_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT, type INTEGER, value INTEGER, value_2 INTEGER, comments TEXT);");
    }

    private void addValidStatusColumn(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE pictures ADD COLUMN status INTEGER DEFAULT 0;");
        } catch (SQLException ex) {
            HwLog.e("MagazineDBHelper", "UpgradeDBVesionFrom1to2 ex = " + ex.toString());
        }
    }

    private void createDeleteTable(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE deletedhiads (_id INTEGER PRIMARY KEY AUTOINCREMENT,hiadMaterialId TEXT, deletetime TEXT);");
        } catch (SQLException ex) {
            throw ex;
        }
    }

    private void addHiAdColumn(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE pictures ADD COLUMN picType INTEGER DEFAULT 1;");
            db.execSQL("ALTER TABLE pictures ADD COLUMN hiadMaterialId TEXT;");
        } catch (SQLException ex) {
            HwLog.e("MagazineDBHelper", "UpgradeDBVesionFrom3to4 ex = " + ex.toString());
        }
    }

    private void addPicFormatColumn(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE pictures ADD COLUMN picFormat INTEGER DEFAULT -1;");
        } catch (SQLException ex) {
            HwLog.e("MagazineDBHelper", "UpgradeDBVesionFrom4to5 ex = " + ex.toString());
        }
    }
}
