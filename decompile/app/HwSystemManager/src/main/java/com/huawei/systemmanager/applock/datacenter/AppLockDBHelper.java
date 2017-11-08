package com.huawei.systemmanager.applock.datacenter;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Environment;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.applock.datacenter.tbl.AppLockAuthSuccessTable;
import com.huawei.systemmanager.applock.datacenter.tbl.AppLockPreferenceTable;
import com.huawei.systemmanager.applock.datacenter.tbl.AppLockStatusTable;
import com.huawei.systemmanager.applock.datacenter.tbl.AppLockTableViews;
import com.huawei.systemmanager.comm.database.DbOpWrapper;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.comm.database.ITableInfo;
import com.huawei.systemmanager.comm.database.gfeature.AbsFeatureView;
import com.huawei.systemmanager.comm.database.gfeature.GFeatureDBOpenHelper;
import com.huawei.systemmanager.util.HwLog;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

class AppLockDBHelper extends GFeatureDBOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DB_NAME = "applock.db";
    private static final String TAG = "AppLockDBHelper";
    private static AppLockDBHelper sInstance = null;

    static class AppLockDBContextWrapper extends ContextWrapper {
        private static final int BUFFER_SIZE = 1024;
        private static final String DB_PATH_41 = (Environment.getDataDirectory() + "/system/hsmmanager/databases/");
        private static final String DB_PATH_NEW = (Environment.getDataDirectory() + "/misc/hsm/databases/");

        static class AppLockDBFileNameFilter implements FilenameFilter {
            private String mFilterFileName;

            public AppLockDBFileNameFilter(String filename) {
                this.mFilterFileName = filename;
            }

            public boolean accept(File dir, String filename) {
                if (filename.startsWith(this.mFilterFileName)) {
                    return true;
                }
                return false;
            }
        }

        public AppLockDBContextWrapper(Context context) {
            super(context);
        }

        public File getDatabasePath(String name) {
            HwLog.d(AppLockDBHelper.TAG, "getDatabasePath: DB path = " + DB_PATH_NEW);
            File dbPath = new File(DB_PATH_NEW);
            if (!dbPath.exists()) {
                if (dbPath.mkdirs()) {
                    HwLog.e(AppLockDBHelper.TAG, "getDatabasePath: DB path is newly created");
                } else {
                    HwLog.e(AppLockDBHelper.TAG, "getDatabasePath: Fail to make dirs, name = " + DB_PATH_NEW + name);
                    return null;
                }
            }
            boolean bDBFileCreated = true;
            File dbFile = new File(DB_PATH_NEW + name);
            if (dbFile.exists()) {
                HwLog.i(AppLockDBHelper.TAG, "getDatabasePath: DB exists ,name = " + name);
            } else {
                try {
                    if (moveDBFiles(super.getDatabasePath(name).getPath(), DB_PATH_NEW + name)) {
                        HwLog.i(AppLockDBHelper.TAG, "getDatabasePath: DB is moved from old path ,name = " + name);
                        removeOldDBFiles(name);
                    } else if (moveDBFiles(DB_PATH_41 + name, DB_PATH_NEW + name)) {
                        HwLog.i(AppLockDBHelper.TAG, "getDatabasePath: DB is moved from old path 4.1 ,name = " + name);
                    } else {
                        bDBFileCreated = dbFile.createNewFile();
                        HwLog.i(AppLockDBHelper.TAG, "getDatabasePath: DB is newly created ,name = " + name);
                    }
                } catch (Exception e) {
                    bDBFileCreated = false;
                    HwLog.e(AppLockDBHelper.TAG, "getDatabasePath: Exception", e);
                }
            }
            if (bDBFileCreated) {
                return dbFile;
            }
            return null;
        }

        public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory, DatabaseErrorHandler errorHandler) {
            return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
        }

        private boolean moveDBFiles(String oldDBPath, String newDBPath) {
            IOException e;
            Object obj;
            Exception e2;
            Throwable th;
            Object outStream;
            HwLog.d(AppLockDBHelper.TAG, "moveDBFiles: oldDBPath = " + oldDBPath);
            File oldDBFile = new File(oldDBPath);
            if (oldDBFile.exists()) {
                File newDbFile = new File(newDBPath);
                Closeable closeable = null;
                Closeable closeable2 = null;
                try {
                    OutputStream outStream2;
                    InputStream inStream = new FileInputStream(oldDBFile);
                    try {
                        outStream2 = new FileOutputStream(newDbFile);
                    } catch (IOException e3) {
                        e = e3;
                        obj = inStream;
                        HwLog.e(AppLockDBHelper.TAG, "moveDBFiles: IOException", e);
                        closeStream(closeable);
                        closeStream(closeable2);
                        return false;
                    } catch (Exception e4) {
                        e2 = e4;
                        obj = inStream;
                        try {
                            HwLog.e(AppLockDBHelper.TAG, "moveDBFiles: Exception", e2);
                            closeStream(closeable);
                            closeStream(closeable2);
                            return false;
                        } catch (Throwable th2) {
                            th = th2;
                            closeStream(closeable);
                            closeStream(closeable2);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        obj = inStream;
                        closeStream(closeable);
                        closeStream(closeable2);
                        throw th;
                    }
                    try {
                        byte[] buffer = new byte[1024];
                        for (int nRead = inStream.read(buffer); nRead > 0; nRead = inStream.read(buffer)) {
                            outStream2.write(buffer, 0, nRead);
                        }
                        outStream2.flush();
                        HwLog.i(AppLockDBHelper.TAG, "moveDBFiles: Finish moving successfully");
                        closeStream(inStream);
                        closeStream(outStream2);
                        return true;
                    } catch (IOException e5) {
                        e = e5;
                        outStream = outStream2;
                        obj = inStream;
                        HwLog.e(AppLockDBHelper.TAG, "moveDBFiles: IOException", e);
                        closeStream(closeable);
                        closeStream(closeable2);
                        return false;
                    } catch (Exception e6) {
                        e2 = e6;
                        outStream = outStream2;
                        obj = inStream;
                        HwLog.e(AppLockDBHelper.TAG, "moveDBFiles: Exception", e2);
                        closeStream(closeable);
                        closeStream(closeable2);
                        return false;
                    } catch (Throwable th4) {
                        th = th4;
                        outStream = outStream2;
                        obj = inStream;
                        closeStream(closeable);
                        closeStream(closeable2);
                        throw th;
                    }
                } catch (IOException e7) {
                    e = e7;
                    HwLog.e(AppLockDBHelper.TAG, "moveDBFiles: IOException", e);
                    closeStream(closeable);
                    closeStream(closeable2);
                    return false;
                } catch (Exception e8) {
                    e2 = e8;
                    HwLog.e(AppLockDBHelper.TAG, "moveDBFiles: Exception", e2);
                    closeStream(closeable);
                    closeStream(closeable2);
                    return false;
                }
            }
            HwLog.w(AppLockDBHelper.TAG, "moveDBFiles: old DB file not exists ,skip oldDBPath=" + oldDBPath);
            return false;
        }

        private void closeStream(Closeable stream) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    HwLog.e(AppLockDBHelper.TAG, "moveDBFiles: IOException", e);
                } catch (Exception e2) {
                    HwLog.e(AppLockDBHelper.TAG, "closeStream: Exception", e2);
                }
            }
        }

        private void removeOldDBFiles(String name) {
            try {
                File oldDB = super.getDatabasePath(name).getParentFile();
                HwLog.d(AppLockDBHelper.TAG, "removeOldDBFiles: Dir Path = " + oldDB.getPath() + ",  name = " + name);
                File[] dbFiles = oldDB.listFiles(new AppLockDBFileNameFilter(name));
                if (dbFiles == null || dbFiles.length <= 0) {
                    HwLog.i(AppLockDBHelper.TAG, "removeOldDBFiles: No file needs to be removed");
                    return;
                }
                for (File file : dbFiles) {
                    if (file.delete()) {
                        HwLog.d(AppLockDBHelper.TAG, "removeOldDBFiles: Remove file " + file.getPath());
                    } else {
                        HwLog.w(AppLockDBHelper.TAG, "removeOldDBFiles: Fail to remove file " + file.getPath());
                    }
                }
            } catch (Exception e) {
                HwLog.e(AppLockDBHelper.TAG, "removeOldDBFiles: Exception", e);
            }
        }
    }

    public static synchronized AppLockDBHelper getInstance(Context context) {
        AppLockDBHelper appLockDBHelper;
        synchronized (AppLockDBHelper.class) {
            if (sInstance == null) {
                sInstance = new AppLockDBHelper(new AppLockDBContextWrapper(context.getApplicationContext()));
            }
            appLockDBHelper = sInstance;
        }
        return appLockDBHelper;
    }

    public static int getDatabaseVersion() {
        return 2;
    }

    protected List<AbsFeatureView> getFeatureViews() {
        return null;
    }

    protected void concreteOnUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        upgradeDatabaseFromLEV1ToV2(db, oldVersion, newVersion);
    }

    protected void createConcreteTables(SQLiteDatabase db) {
        List<ITableInfo> tbls = Lists.newArrayList();
        tbls.add(new AppLockStatusTable());
        tbls.add(new AppLockAuthSuccessTable());
        tbls.add(new AppLockPreferenceTable());
        for (ITableInfo tbl : tbls) {
            DbOpWrapper.createTable(db, tbl.getTableName(), tbl.getColumnDefines());
            DbOpWrapper.createIndex(db, tbl.getTableName(), tbl.getIndexCols());
        }
    }

    protected void dropConcreteTables(SQLiteDatabase db) {
        List<ITableInfo> tbls = Lists.newArrayList();
        tbls.add(new AppLockStatusTable());
        tbls.add(new AppLockAuthSuccessTable());
        for (ITableInfo tbl : tbls) {
            DbOpWrapper.dropTable(db, tbl.getTableName());
        }
    }

    protected void createConcreteViews(SQLiteDatabase db) {
        DbOpWrapper.runSqlSentencesAlone(db, AppLockTableViews.getTableViewSentences());
    }

    protected void dropConcreteViews(SQLiteDatabase db) {
        DbOpWrapper.runSqlSentencesAlone(db, AppLockTableViews.getDropViewSentences());
    }

    public Cursor queryAppLockData(String tableOrViewName, String[] projection, String selection, String[] selectionArgs) {
        return queryInner(tableOrViewName, projection, selection, selectionArgs);
    }

    public long insertDefaultLockStatus(String pkgName) {
        HwLog.d(TAG, "insertDefaultLockStatus: " + pkgName);
        ContentValues values = new ContentValues();
        values.put("packageName", pkgName);
        values.put(AppLockStatusTable.COL_LOCK_STATUS, Integer.valueOf(0));
        return insertInner(AppLockStatusTable.TABLE_NAME, values);
    }

    public long replaceLockStatus(String pkgName, int lockstatus) {
        HwLog.d(TAG, "replaceLockStatus:" + pkgName + SqlMarker.COMMA_SEPARATE + lockstatus);
        ContentValues values = new ContentValues();
        values.put("packageName", pkgName);
        values.put(AppLockStatusTable.COL_LOCK_STATUS, Integer.valueOf(lockstatus));
        return replaceInner(AppLockStatusTable.TABLE_NAME, values);
    }

    public long replaceLockStatus(ContentValues values) {
        HwLog.d(TAG, "replaceLockStatus:" + values);
        return replaceInner(AppLockStatusTable.TABLE_NAME, values);
    }

    public int batchReplaceDefaultLockStatus(Set<String> pkgSet) {
        List<ContentValues> rows = Lists.newArrayList();
        for (String pkg : pkgSet) {
            ContentValues row = new ContentValues();
            row.put("packageName", pkg);
            row.put(AppLockStatusTable.COL_LOCK_STATUS, Integer.valueOf(0));
            rows.add(row);
        }
        return bulkReplaceInner(AppLockStatusTable.TABLE_NAME, (ContentValues[]) rows.toArray(new ContentValues[rows.size()]));
    }

    public int batchReplaceLockStatus(ContentValues[] arrayValues) {
        return bulkReplaceInner(AppLockStatusTable.TABLE_NAME, arrayValues);
    }

    public int deleteLockData(Set<String> pkgSet) {
        int delCount = 0;
        for (String pkgName : pkgSet) {
            if (-1 != deleteLockData(pkgName)) {
                delCount++;
            }
        }
        return delCount == 0 ? -1 : delCount;
    }

    public int deleteLockData(String pkgName) {
        HwLog.d(TAG, "deleteLockData:" + pkgName);
        return deleteInner(AppLockStatusTable.TABLE_NAME, "packageName = ?", new String[]{pkgName});
    }

    public Cursor queryAuthSuccessPackage(String tableName, String[] projection, String selection, String[] selectionArgs) {
        return queryInner(tableName, projection, selection, selectionArgs);
    }

    public long addAuthSuccessPackage(ContentValues values) {
        HwLog.v(TAG, "addAuthSuccessPackage:" + values);
        return replaceInner(AppLockAuthSuccessTable.TABLE_NAME, values);
    }

    public int deleteAuthSuccessPackage(String whereClause, String[] whereArgs) {
        return deleteInner(AppLockAuthSuccessTable.TABLE_NAME, whereClause, whereArgs);
    }

    public int clearAuthSuccessPackage() {
        return deleteInner(AppLockAuthSuccessTable.TABLE_NAME, null, null);
    }

    public void resetLockStatusBeforeRecovery() {
        HwLog.v(TAG, "resetLockStatusBeforeRecovery");
        deleteInner(AppLockAuthSuccessTable.TABLE_NAME, null, null);
        ContentValues values = new ContentValues();
        values.put(AppLockStatusTable.COL_LOCK_STATUS, Integer.valueOf(0));
        updateInner(AppLockStatusTable.TABLE_NAME, values, null, null);
    }

    public Cursor queryAppLockPreferenceData(String tblOrViewName, String[] projection, String selection, String[] selectionArgs) {
        return queryInner(tblOrViewName, projection, selection, selectionArgs);
    }

    public long updateAppLockPreferenceData(ContentValues values) {
        if (!values.containsKey(AppLockPreferenceTable.COL_PREF_BACKUP)) {
            values.put(AppLockPreferenceTable.COL_PREF_BACKUP, String.valueOf(0));
        }
        return replaceInner(AppLockPreferenceTable.TABLE_NAME, values);
    }

    private AppLockDBHelper(Context context) {
        super(context, DB_NAME, null, 2);
    }

    private void upgradeDatabaseFromLEV1ToV2(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 1 && newVersion > 1) {
            ITableInfo tbl = new AppLockPreferenceTable();
            DbOpWrapper.createTable(db, tbl.getTableName(), tbl.getColumnDefines());
            DbOpWrapper.createIndex(db, tbl.getTableName(), tbl.getIndexCols());
            DbOpWrapper.runSqlSentencesBatch(db, AppLockTableViews.getTableViewSentences());
        }
    }
}
