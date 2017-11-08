package com.huawei.systemmanager.comm.database.gfeature;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.comm.database.DbOpWrapper;
import com.huawei.systemmanager.comm.database.ITableInfo;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Set;

public abstract class GFeatureDBOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = GFeatureDBOpenHelper.class.getSimpleName();

    protected abstract void concreteOnUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2);

    protected abstract void createConcreteTables(SQLiteDatabase sQLiteDatabase);

    protected abstract void createConcreteViews(SQLiteDatabase sQLiteDatabase);

    protected abstract void dropConcreteTables(SQLiteDatabase sQLiteDatabase);

    protected abstract void dropConcreteViews(SQLiteDatabase sQLiteDatabase);

    protected abstract List<AbsFeatureView> getFeatureViews();

    public GFeatureDBOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public void onCreate(SQLiteDatabase db) {
        createTableAndViews(db);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        concreteOnUpgrade(db, oldVersion, newVersion);
        List<AbsFeatureView> featureViews = getFeatureViews();
        if (featureViews != null && !featureViews.isEmpty()) {
            createGFeatureTable(db, featureViews);
            dropGFeatureViews(db, featureViews);
            createGFeatureViews(db, featureViews);
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTableAndViews(db);
        createTableAndViews(db);
    }

    public long replaceFeatureRow(String realTblPrefix, ContentValues cv) {
        if (GFeatureCvt.isStdContentValue(cv)) {
            return replaceInner(getGFeatureTableName(realTblPrefix), cv);
        }
        return -1;
    }

    public int replaceFeatureRows(String realTblPrefix, ContentValues[] cvs) {
        int i = 1;
        if (cvs.length == 0) {
            HwLog.d(TAG, "replaceFeatureRows empty input!");
            return -1;
        } else if (1 == cvs.length) {
            if (-1 == replaceFeatureRow(realTblPrefix, cvs[0])) {
                i = 0;
            }
            return i;
        } else if (GFeatureCvt.isStdContentValue(cvs)) {
            return bulkReplaceInner(getGFeatureTableName(realTblPrefix), cvs);
        } else {
            return -1;
        }
    }

    public int deleteFeatureRows(String realTblPrefix, String pkgName) {
        HwLog.v(TAG, "deleteFeatureRows " + pkgName);
        return deleteInner(getGFeatureTableName(realTblPrefix), "packageName = ?", new String[]{pkgName});
    }

    public int deleteFeatureRows(String realTblPrefix, List<String> pkgNames) {
        int count = 0;
        SQLiteDatabase db = getWritableDatabase();
        try {
            String tableName = getGFeatureTableName(realTblPrefix);
            HwLog.v(TAG, "deleteFeatureRows delete table is: " + tableName + " with pkgList is: " + pkgNames.toString());
            db.beginTransaction();
            while (pkgNames.iterator().hasNext()) {
                count += db.delete(tableName, "packageName = ?", new String[]{(String) pkgNames.iterator().next()});
            }
            db.setTransactionSuccessful();
        } catch (SQLException ex) {
            ex.printStackTrace();
            HwLog.e(TAG, "deleteFeatureRows list catch SQLException:" + ex.getMessage());
        } catch (Exception ex2) {
            ex2.printStackTrace();
            HwLog.e(TAG, "deleteFeatureRows list catch exception " + ex2.getMessage());
        } finally {
            db.endTransaction();
        }
        HwLog.v(TAG, "deleteFeatureRows delete count is: " + count);
        return count;
    }

    public void dropFeatureTable(String realTblPrefix) {
        DbOpWrapper.dropTable(getWritableDatabase(), getGFeatureTableName(realTblPrefix));
    }

    public Cursor queryGFeatureTable(String realTblPrefix, String[] projection, String selection, String[] selectionArgs) {
        return queryInner(getGFeatureTableName(realTblPrefix), projection, selection, selectionArgs);
    }

    public int updateGFeatureTable(String realTblPrefix, ContentValues values) {
        if (!GFeatureCvt.isStdContentValue(values)) {
            return 0;
        }
        return updateInner(getGFeatureTableName(realTblPrefix), values, "packageName = ? and featureName = ? ", new String[]{values.getAsString("packageName"), values.getAsString(GFeatureTable.COL_FEATURE_NAME)});
    }

    public int deleteComm(String tableName, String whereClause, String[] whereArgs) {
        return deleteInner(tableName, whereClause, whereArgs);
    }

    public long insertComm(String tableName, ContentValues values) {
        return insertInner(tableName, values);
    }

    public Cursor queryComm(String tableOrViewName, String[] projection, String selection, String[] selectionArgs) {
        return queryInner(tableOrViewName, projection, selection, selectionArgs);
    }

    public int updateCommon(String tableName, ContentValues values, String whereClause, String[] whereArgs) {
        return updateInner(tableName, values, whereClause, whereArgs);
    }

    public int bulkInsertComm(String tableName, ContentValues[] values) {
        return bulkInsertInner(tableName, values);
    }

    public int bulkReplaceComm(String tableName, ContentValues[] values) {
        return bulkReplaceInner(tableName, values);
    }

    protected Cursor queryInner(String tableOrViewName, String[] projection, String selection, String[] selectionArgs) {
        try {
            return getWritableDatabase().query(tableOrViewName, projection, selection, selectionArgs, null, null, null);
        } catch (SQLException ex) {
            ex.printStackTrace();
            HwLog.e(TAG, "queryInner catch SQLException:" + ex.getMessage());
            return null;
        } catch (Exception ex2) {
            ex2.printStackTrace();
            HwLog.e(TAG, "queryInner catch exception " + ex2.getMessage());
            return null;
        }
    }

    protected int deleteInner(String tableName, String whereClause, String[] whereArgs) {
        try {
            return getWritableDatabase().delete(tableName, whereClause, whereArgs);
        } catch (SQLException ex) {
            ex.printStackTrace();
            HwLog.e(TAG, "deleteInner catch SQLException:" + ex.getMessage());
            return -1;
        } catch (Exception ex2) {
            ex2.printStackTrace();
            HwLog.e(TAG, "deleteInner catch exception " + ex2.getMessage());
            return -1;
        }
    }

    protected int updateInner(String tableName, ContentValues values, String whereClause, String[] whereArgs) {
        try {
            return getWritableDatabase().update(tableName, values, whereClause, whereArgs);
        } catch (SQLException ex) {
            ex.printStackTrace();
            HwLog.e(TAG, "deleteInner catch SQLException:" + ex.getMessage());
            return -1;
        } catch (Exception ex2) {
            ex2.printStackTrace();
            HwLog.e(TAG, "deleteInner catch exception " + ex2.getMessage());
            return -1;
        }
    }

    protected long insertInner(String tableName, ContentValues values) {
        try {
            return getWritableDatabase().insert(tableName, null, values);
        } catch (SQLException ex) {
            ex.printStackTrace();
            HwLog.e(TAG, "insertInner catch SQLException:" + ex.getMessage());
            return -1;
        } catch (Exception ex2) {
            ex2.printStackTrace();
            HwLog.e(TAG, "insertInner catch exception " + ex2.getMessage());
            return -1;
        }
    }

    protected long replaceInner(String tableName, ContentValues values) {
        try {
            return getWritableDatabase().replace(tableName, null, values);
        } catch (SQLException ex) {
            ex.printStackTrace();
            HwLog.e(TAG, "replaceInner catch SQLException:" + ex.getMessage());
            return -1;
        } catch (Exception ex2) {
            ex2.printStackTrace();
            HwLog.e(TAG, "replaceInner catch exception " + ex2.getMessage());
            return -1;
        }
    }

    protected int bulkInsertInner(String tableName, ContentValues[] values) {
        int insertCount = 0;
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            for (ContentValues insert : values) {
                if (-1 != db.insert(tableName, null, insert)) {
                    insertCount++;
                }
            }
            db.setTransactionSuccessful();
        } catch (SQLException ex) {
            ex.printStackTrace();
            HwLog.e(TAG, "bulkInsertInner catch SQLException:" + ex.getMessage());
        } catch (Exception ex2) {
            ex2.printStackTrace();
            HwLog.e(TAG, "bulkInsertInner catch exception");
        } finally {
            db.endTransaction();
        }
        return insertCount;
    }

    protected int bulkReplaceInner(String tableName, ContentValues[] values) {
        int replaceCount = 0;
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            for (ContentValues replace : values) {
                if (-1 != db.replace(tableName, null, replace)) {
                    replaceCount++;
                }
            }
            db.setTransactionSuccessful();
        } catch (SQLException ex) {
            ex.printStackTrace();
            HwLog.e(TAG, "bulkReplaceInner catch SQLException:" + ex.getMessage());
        } catch (Exception ex2) {
            ex2.printStackTrace();
            HwLog.e(TAG, "bulkReplaceInner catch exception");
        } finally {
            db.endTransaction();
        }
        return replaceCount;
    }

    private void createTableAndViews(SQLiteDatabase db) {
        List<AbsFeatureView> featureViews = getFeatureViews();
        if (!(featureViews == null || featureViews.isEmpty())) {
            createGFeatureTable(db, featureViews);
            createGFeatureViews(db, featureViews);
        }
        createConcreteTables(db);
        createConcreteViews(db);
    }

    private void dropTableAndViews(SQLiteDatabase db) {
        List<AbsFeatureView> featureViews = getFeatureViews();
        if (!(featureViews == null || featureViews.isEmpty())) {
            dropGFeatureTable(db, featureViews);
            dropGFeatureViews(db, featureViews);
        }
        dropConcreteTables(db);
        dropConcreteViews(db);
    }

    private void createGFeatureTable(SQLiteDatabase db, List<AbsFeatureView> featureViews) {
        ITableInfo tableInfo = new GFeatureTable();
        for (String prefix : getRealTablePrefixSet(featureViews)) {
            DbOpWrapper.createTable(db, getGFeatureTableName(prefix), tableInfo.getColumnDefines());
            DbOpWrapper.createIndex(db, getGFeatureTableName(prefix), tableInfo.getIndexCols());
        }
    }

    private void dropGFeatureTable(SQLiteDatabase db, List<AbsFeatureView> featureViews) {
        for (String prefix : getRealTablePrefixSet(featureViews)) {
            DbOpWrapper.dropTable(db, getGFeatureTableName(prefix));
        }
    }

    private void createGFeatureViews(SQLiteDatabase db, List<AbsFeatureView> featureViews) {
        for (AbsFeatureView view : featureViews) {
            List<String> createSqls = view.generateCreateViewSqls(getGFeatureTableName(view.getLinkedRealTablePrefix()));
            HwLog.d(TAG, "createGFeatureViews " + createSqls);
            DbOpWrapper.runSqlSentencesAlone(db, (String[]) createSqls.toArray(new String[createSqls.size()]));
        }
    }

    private void dropGFeatureViews(SQLiteDatabase db, List<AbsFeatureView> featureViews) {
        for (AbsFeatureView view : featureViews) {
            List<String> dropSqls = view.generateDropViewSqls();
            HwLog.d(TAG, "dropGFeatureViews " + dropSqls);
            DbOpWrapper.runSqlSentencesAlone(db, (String[]) dropSqls.toArray(new String[dropSqls.size()]));
        }
    }

    private Set<String> getRealTablePrefixSet(List<AbsFeatureView> featureViews) {
        Set<String> prefixs = Sets.newHashSet();
        for (AbsFeatureView view : featureViews) {
            prefixs.add(view.getLinkedRealTablePrefix());
        }
        return prefixs;
    }

    private String getGFeatureTableName(String tblPrefix) {
        return tblPrefix + "_GFEATURE_TABLE";
    }
}
