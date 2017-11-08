package com.huawei.systemmanager.comm.database;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.util.HwLog;
import java.util.Arrays;

public final class DbOpWrapper {
    private static final String IDX_SUFFIX = "_idx";
    private static final String TAG = DbOpWrapper.class.getSimpleName();

    private static class ColToStrFunction implements Function<String[], String> {
        private ColToStrFunction() {
        }

        public String apply(String[] input) {
            if (input == null) {
                return "";
            }
            return Joiner.on(" ").join((Object[]) input);
        }
    }

    public static void createTable(SQLiteDatabase db, String tableName, String[][] colsInfo) {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(SqlMarker.LEFT_PARENTHESES);
        strBuf.append(Joiner.on(SqlMarker.COMMA_SEPARATE).join(Lists.transform(Arrays.asList(colsInfo), new ColToStrFunction())));
        strBuf.append(SqlMarker.RIGHT_PARENTHESES).append(SqlMarker.SQL_END);
        HwLog.i(TAG, "createTable sql:" + strBuf.toString());
        runSingleSqlSentence(db, strBuf.toString());
    }

    public static void createIndex(SQLiteDatabase db, String tableName, String[] idxCols) {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("CREATE UNIQUE INDEX IF NOT EXISTS ").append(tableName).append(IDX_SUFFIX);
        strBuf.append(" ON ").append(tableName).append(SqlMarker.LEFT_PARENTHESES);
        strBuf.append(Joiner.on(SqlMarker.COMMA_SEPARATE).join((Object[]) idxCols));
        strBuf.append(SqlMarker.RIGHT_PARENTHESES).append(SqlMarker.SQL_END);
        HwLog.i(TAG, "createIndex sql:" + strBuf.toString());
        runSingleSqlSentence(db, strBuf.toString());
    }

    public static void dropTable(SQLiteDatabase db, String tableName) {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("DROP TABLE IF EXISTS ").append(tableName);
        HwLog.i(TAG, "dropTable sql:" + strBuf.toString());
        runSingleSqlSentence(db, strBuf.toString());
    }

    public static void dropView(SQLiteDatabase db, String viewName) {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("DROP VIEW IF EXISTS ").append(viewName);
        HwLog.i(TAG, "dropTable sql:" + strBuf.toString());
        runSingleSqlSentence(db, strBuf.toString());
    }

    public static void runSingleSqlSentence(SQLiteDatabase db, String sql) {
        try {
            db.execSQL(sql);
        } catch (SQLException ex) {
            ex.printStackTrace();
            HwLog.e(TAG, "runSingleSqlSentence " + sql + " catch SQLiteException");
        } catch (Exception ex2) {
            ex2.printStackTrace();
            HwLog.e(TAG, "runSingleSqlSentence " + sql + " catch Exception");
        }
    }

    public static void runSqlSentencesBatch(SQLiteDatabase db, String[] sqls) {
        boolean z;
        if (sqls == null || sqls.length <= 0) {
            z = false;
        } else {
            z = true;
        }
        Preconditions.checkArgument(z, "invalid SQL Sentences");
        if (1 == sqls.length) {
            runSingleSqlSentence(db, sqls[0]);
            return;
        }
        try {
            db.beginTransaction();
            for (String execSQL : sqls) {
                db.execSQL(execSQL);
            }
            db.setTransactionSuccessful();
        } catch (SQLException ex) {
            ex.printStackTrace();
            HwLog.e(TAG, "runSqlSentences catch SQLiteException");
        } catch (Exception ex2) {
            ex2.printStackTrace();
            HwLog.e(TAG, "runSqlSentences catch Exception");
        } finally {
            db.endTransaction();
        }
    }

    public static void runSqlSentencesAlone(SQLiteDatabase db, String[] sqls) {
        for (String runSingleSqlSentence : sqls) {
            runSingleSqlSentence(db, runSingleSqlSentence);
        }
    }
}
