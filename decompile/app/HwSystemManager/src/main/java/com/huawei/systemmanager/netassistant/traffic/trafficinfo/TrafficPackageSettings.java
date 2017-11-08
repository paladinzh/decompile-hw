package com.huawei.systemmanager.netassistant.traffic.trafficinfo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.netassistant.db.comm.DBTable;
import com.huawei.systemmanager.netassistant.db.comm.ITableInfo;
import com.huawei.systemmanager.netassistant.db.traffic.TrafficDBProvider;
import com.huawei.systemmanager.util.HwLog;

public class TrafficPackageSettings extends ITableInfo {
    private static final String TAG = "TrafficPackageSettings";
    DBTable dbTable = new Tables();
    long initTimeMills = DateUtil.getCurrentTimeMills();
    String mImsi;

    public static class Tables extends DBTable {
        static final String COL_ID = "id";
        static final String COL_IMSI = "imsi";
        static final String COL_INIT_TIME = "init_time";
        public static final String TABLE_NAME = "packagesetting";

        public String getTableCreateCmd() {
            return "create table if not exists packagesetting ( id integer primary key autoincrement, imsi text, init_time long);";
        }

        public String getTableDropCmd() {
            return "DROP TABLE IF EXISTS packagesetting";
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

    public TrafficPackageSettings(String imsi) {
        this.mImsi = imsi;
    }

    public ITableInfo get() {
        String[] projection = new String[]{"imsi", "init_time"};
        String[] selectArg = new String[]{String.valueOf(this.mImsi)};
        Cursor cursor = GlobalContext.getContext().getContentResolver().query(this.dbTable.getUri(), projection, "imsi = ?", selectArg, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            HwLog.d(TAG, "result is empty");
            return this;
        }
        if (cursor.moveToNext()) {
            int initTimeIndex = cursor.getColumnIndex("init_time");
            do {
                this.initTimeMills = cursor.getLong(initTimeIndex);
            } while (cursor.moveToNext());
        }
        HwLog.i(TAG, "get traffic setting, init time = " + DateUtil.millisec2String(this.initTimeMills));
        cursor.close();
        return this;
    }

    public ITableInfo save(Object[] t) {
        ContentResolver cr = GlobalContext.getContext().getContentResolver();
        String[] args = new String[]{this.mImsi};
        ContentValues values = new ContentValues();
        values.put("imsi", this.mImsi);
        values.put("init_time", Long.valueOf(this.initTimeMills));
        if (cr.update(this.dbTable.getUri(), values, "imsi = ?", args) <= 0) {
            cr.insert(this.dbTable.getUri(), values);
        }
        HwLog.i(TAG, "update traffic setting, init time = " + DateUtil.millisec2String(this.initTimeMills));
        return this;
    }

    public ITableInfo clear() {
        String[] args = new String[]{this.mImsi};
        GlobalContext.getContext().getContentResolver().delete(this.dbTable.getUri(), "imsi = ?", args);
        return this;
    }
}
