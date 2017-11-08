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

public class RoamingTrafficSetting extends ITableInfo {
    private static final String TAG = "RoamingTrafficSetting";
    public static final int TYPE_DISABLE_NETWORK = 2;
    public static final int TYPE_NOLY_NOTIFY = 1;
    private DBTable dbTable = new Tables();
    private String mImsi;
    private int mNotifyType = 2;
    private long mPackage = -1;

    public static class Tables extends DBTable {
        static final String COL_ID = "id";
        static final String COL_IMSI = "imsi";
        static final String COL_NOTIFY_TYPE = "notify_type";
        static final String COL_PACKAGE = "package";
        public static final String TABLE_NAME = "roamingtrafficsetting";

        public String getTableCreateCmd() {
            return "create table if not exists roamingtrafficsetting ( id integer primary key autoincrement, imsi text, package long, notify_type int);";
        }

        public String getTableDropCmd() {
            return "DROP TABLE IF EXISTS roamingtrafficsetting";
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

    public RoamingTrafficSetting(String imsi) {
        this.mImsi = imsi;
    }

    public boolean hasRoamingSeted() {
        return this.mPackage >= 0;
    }

    public RoamingTrafficSetting get() {
        String[] whereArg = new String[]{this.mImsi};
        Cursor cursor = GlobalContext.getContext().getContentResolver().query(this.dbTable.getUri(), null, "imsi = ?", whereArg, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            HwLog.i(TAG, "result is empty");
            return this;
        }
        if (cursor.moveToNext()) {
            int packageIndex = cursor.getColumnIndex("package");
            int typeIndex = cursor.getColumnIndex("notify_type");
            this.mPackage = cursor.getLong(packageIndex);
            this.mNotifyType = cursor.getInt(typeIndex);
        }
        cursor.close();
        return this;
    }

    public ITableInfo save(Object[] obj) {
        ContentResolver cr = GlobalContext.getContext().getContentResolver();
        String[] whereArg = new String[]{this.mImsi};
        ContentValues values = new ContentValues();
        values.put("package", Long.valueOf(this.mPackage));
        values.put("notify_type", Integer.valueOf(this.mNotifyType));
        if (cr.update(this.dbTable.getUri(), values, "imsi = ?", whereArg) <= 0) {
            values.put("imsi", this.mImsi);
            cr.insert(this.dbTable.getUri(), values);
        }
        return this;
    }

    public long getPackage() {
        return this.mPackage;
    }

    public void setPackage(long mPackage) {
        this.mPackage = mPackage;
    }

    public ITableInfo clear() {
        String[] whereArg = new String[]{this.mImsi};
        GlobalContext.getContext().getContentResolver().delete(this.dbTable.getUri(), "imsi = ?", whereArg);
        return this;
    }

    public int getNotifyType() {
        return this.mNotifyType;
    }

    public void setNotifyType(int notifyType) {
        this.mNotifyType = notifyType;
    }
}
