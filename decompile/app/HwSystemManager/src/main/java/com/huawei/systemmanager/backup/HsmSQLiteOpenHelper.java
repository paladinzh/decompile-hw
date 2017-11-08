package com.huawei.systemmanager.backup;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class HsmSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "HsmSQLiteOpenHelper";
    public static final String TMP_TABLE_SUFFIX = "_tmpbak";
    protected Map<String, String> mRecoverTableMap = null;

    protected abstract boolean onRecoverComplete(SQLiteDatabase sQLiteDatabase, int i);

    protected abstract boolean onRecoverStart(SQLiteDatabase sQLiteDatabase, int i);

    public HsmSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public void putRecoverTmpTableMap(String strTableName, String strBakTableName) {
        if (this.mRecoverTableMap == null) {
            this.mRecoverTableMap = new HashMap();
        }
        this.mRecoverTableMap.put(strTableName, strBakTableName);
    }

    public String getRecoverTmpTableMap(String strTableName) {
        if (this.mRecoverTableMap == null) {
            return null;
        }
        return (String) this.mRecoverTableMap.get(strTableName);
    }

    protected void clearRecoverTmpTablesAndMap(SQLiteDatabase db) {
        if (this.mRecoverTableMap != null && this.mRecoverTableMap.size() > 0) {
            HwLog.i(TAG, "clearRecoverMapsAndTables: size = " + this.mRecoverTableMap.size());
            for (Entry<String, String> entry : this.mRecoverTableMap.entrySet()) {
                String table = (String) entry.getKey();
                String tableBak = (String) entry.getValue();
                if (TextUtils.isEmpty(tableBak)) {
                    HwLog.w(TAG, "clearRecoverMapsAndTables: Get invalid table map for " + table);
                } else {
                    db.execSQL("DROP TABLE IF EXISTS " + tableBak);
                }
            }
            this.mRecoverTableMap.clear();
        }
    }
}
