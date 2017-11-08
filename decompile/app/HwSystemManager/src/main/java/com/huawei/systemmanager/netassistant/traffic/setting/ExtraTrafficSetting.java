package com.huawei.systemmanager.netassistant.traffic.setting;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.netassistant.db.comm.DBTable;
import com.huawei.systemmanager.netassistant.db.comm.ITableInfo;
import com.huawei.systemmanager.netassistant.db.traffic.TrafficDBProvider;
import com.huawei.systemmanager.util.HwLog;

public class ExtraTrafficSetting extends ITableInfo {
    private static final String TAG = "ExtraTrafficSetting";
    private DBTable dbTable = new Tables();
    private String mImsi;
    private long mPackage = -1;

    public static class Tables extends DBTable {
        static final String COL_ID = "id";
        static final String COL_IMSI = "imsi";
        static final String COL_PACKAGE = "package";
        public static final String TABLE_NAME = "extrafficpkg";

        public String getTableCreateCmd() {
            return "create table if not exists extrafficpkg ( id integer primary key autoincrement, imsi text, package long);";
        }

        public String getTableDropCmd() {
            return "DROP TABLE IF EXISTS extrafficpkg";
        }

        public String getPrimaryColumn() {
            return "id";
        }

        public String getTableName() {
            return TABLE_NAME;
        }

        public String getAuthority() {
            return TrafficDBProvider.AUTHORITY;
        }
    }

    public ExtraTrafficSetting(String imsi) {
        this.mImsi = imsi;
    }

    public ExtraTrafficSetting get() {
        String[] projection = new String[]{"package"};
        String[] whereArg = new String[]{this.mImsi};
        Cursor cursor = GlobalContext.getContext().getContentResolver().query(this.dbTable.getUri(), projection, "imsi = ?", whereArg, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            HwLog.i(TAG, "result is empty");
            return this;
        }
        if (cursor.moveToNext()) {
            this.mPackage = cursor.getLong(cursor.getColumnIndex("package"));
        }
        cursor.close();
        return this;
    }

    public ITableInfo save(Object[] obj) {
        ContentResolver cr = GlobalContext.getContext().getContentResolver();
        String[] whereArg = new String[]{this.mImsi};
        ContentValues values = new ContentValues();
        values.put("package", Long.valueOf(this.mPackage));
        if (cr.update(this.dbTable.getUri(), values, "imsi = ?", whereArg) <= 0) {
            values.put("imsi", this.mImsi);
            cr.insert(this.dbTable.getUri(), values);
        }
        return this;
    }

    public ITableInfo clear() {
        String[] whereArg = new String[]{this.mImsi};
        GlobalContext.getContext().getContentResolver().delete(this.dbTable.getUri(), "imsi = ?", whereArg);
        return this;
    }

    public long getPackage() {
        return this.mPackage;
    }

    public void setPackage(long mPackage) {
        this.mPackage = mPackage;
    }
}
