package com.huawei.systemmanager.netassistant.db.traffic;

import com.huawei.systemmanager.netassistant.db.comm.DBTable;
import com.huawei.systemmanager.netassistant.db.comm.IDBProvider;
import com.huawei.systemmanager.netassistant.db.comm.IDataBaseHelper;
import com.huawei.systemmanager.netassistant.traffic.backgroundtraffic.BackgroundTrafficInfo;
import com.huawei.systemmanager.netassistant.traffic.leisuretraffic.LeisureTrafficSetting;
import com.huawei.systemmanager.netassistant.traffic.notrafficapp.NoTrafficAppDbInfo;
import com.huawei.systemmanager.netassistant.traffic.roamingtraffic.RoamingAppInfo;
import com.huawei.systemmanager.netassistant.traffic.setting.ExtraTrafficSetting;
import com.huawei.systemmanager.netassistant.traffic.setting.RoamingTrafficSetting;
import com.huawei.systemmanager.netassistant.traffic.trafficinfo.TrafficPackageSettings;
import com.huawei.systemmanager.netassistant.traffic.trafficstatistics.TrafficStatisticsInfo.Tables;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class TrafficDBProvider extends IDBProvider {
    public static final String AUTHORITY = "com.huawei.systemmanager.netassistant.db.traffic.TrafficDBProvider";
    private static final String DBNAME = "traffic.db";
    public static final int DBVERSION = 2;
    private static final DBTable[] TABLES = new DBTable[]{new Tables(), new NoTrafficAppDbInfo.Tables(), new LeisureTrafficSetting.Tables(), new ExtraTrafficSetting.Tables(), new RoamingAppInfo.Tables(), new RoamingTrafficSetting.Tables(), new TrafficPackageSettings.Tables()};
    private static final String TAG = TrafficDBProvider.class.getSimpleName();

    public IDataBaseHelper initDatabase() {
        HwLog.d(TAG, "init database");
        return TrafficDBHelper.getInstance(getContext(), DBNAME, 2, TABLES);
    }

    public String getAuthority() {
        return AUTHORITY;
    }

    protected int getDBVersion() {
        return 2;
    }

    protected ArrayList<String> getBackupSupportedUriList() {
        ArrayList<String> list = new ArrayList();
        for (DBTable table : TABLES) {
            list.add(table.getUri().toString());
        }
        list.add(BackgroundTrafficInfo.BACKGROUND_TRAFFIC_URI.toString());
        return list;
    }

    protected boolean canRecoverDB(int nRecoverVersion) {
        return true;
    }

    protected boolean onRecoverStart(int nRecoverVersion) {
        HwLog.d(TAG, "onRecoverStart: nRecoverVersion = " + nRecoverVersion);
        for (DBTable table : TABLES) {
            this.mHelper.delete(table.getTableName(), null, null);
        }
        return true;
    }

    protected boolean onRecoverComplete(int nRecoverVersion) {
        HwLog.i(TAG, "onRecoverComplete: Success = " + getRecoverSucceedCount() + ", Failure = " + getRecoverFailedCount());
        return true;
    }
}
