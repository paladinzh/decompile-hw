package com.huawei.systemmanager.netassistant.db.traffic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.huawei.systemmanager.netassistant.db.comm.DBTable;
import com.huawei.systemmanager.netassistant.db.comm.IDataBaseHelper;
import com.huawei.systemmanager.netassistant.traffic.trafficinfo.TrafficPackageSettings.Tables;
import com.huawei.systemmanager.util.HwLog;

public class TrafficDBHelper extends IDataBaseHelper {
    private static TrafficDBHelper single;

    public TrafficDBHelper(Context context, String dbName, int version, DBTable[] tables) {
        super(context, dbName, version, tables);
    }

    public static synchronized TrafficDBHelper getInstance(Context context, String dbName, int version, DBTable[] tables) {
        TrafficDBHelper trafficDBHelper;
        synchronized (TrafficDBHelper.class) {
            if (single == null) {
                single = new TrafficDBHelper(context, dbName, version, tables);
            }
            trafficDBHelper = single;
        }
        return trafficDBHelper;
    }

    public void onUpgrade(SQLiteDatabase db, int fromVersion, int toVersion) {
        if (toVersion != 2) {
            HwLog.i(TAG, "/updateDatabase: Illegal update request. Got " + toVersion + ", expected " + 2);
            throw new IllegalArgumentException();
        } else if (fromVersion > toVersion) {
            HwLog.i(TAG, "/updateDatabase: Illegal update request: can't downgrade from " + fromVersion + " to " + toVersion + ". Did you forget to wipe data?");
            throw new IllegalArgumentException();
        } else {
            upgradeDB(fromVersion, toVersion, db);
        }
    }

    private void upgradeDB(int fromVersion, int toVersion, SQLiteDatabase db) {
        if (fromVersion < toVersion && toVersion <= 2) {
            switch (fromVersion) {
                case 1:
                    upgradeFrom1To2(db);
                    upgradeDB(fromVersion + 1, toVersion, db);
                    return;
                default:
                    HwLog.i(TAG, "onUpgrade: Invalid oldVersion: " + fromVersion);
                    return;
            }
        }
    }

    private void upgradeFrom1To2(SQLiteDatabase db) {
        db.execSQL(new Tables().getTableCreateCmd());
        HwLog.i(TAG, "onUpgrade: upgradeFrom1To2 complete");
    }
}
