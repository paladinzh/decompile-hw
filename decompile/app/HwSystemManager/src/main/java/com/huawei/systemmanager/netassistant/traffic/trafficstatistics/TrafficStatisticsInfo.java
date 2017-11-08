package com.huawei.systemmanager.netassistant.traffic.trafficstatistics;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.netassistant.db.comm.DBTable;
import com.huawei.systemmanager.netassistant.db.comm.ITableInfo;
import com.huawei.systemmanager.netassistant.db.traffic.TrafficDBProvider;
import com.huawei.systemmanager.netassistant.traffic.trafficinfo.MonthTotalTrafficInfo;
import com.huawei.systemmanager.push.PushResponse;
import com.huawei.systemmanager.util.HwLog;

@TargetApi(19)
public class TrafficStatisticsInfo extends ITableInfo {
    public static final long NOT_SET = -1;
    private static final String TAG = TrafficStatisticsInfo.class.getSimpleName();
    private DBTable dbTable;
    private String mImsi;
    private int mMonth;
    private long mRecords = -1;
    private long recordTime;
    private int trafficType;

    public static class Tables extends DBTable {
        static final String COL_DATA = "data";
        static final String COL_ID = "id";
        static final String COL_IMSI = "imsi";
        static final String COL_MONTH = "month";
        static final String COL_TIME = "recordtime";
        static final String COL_TYPE = "type";
        public static final String TABLE_NAME = "trafficstatistics";

        public String getTableName() {
            return TABLE_NAME;
        }

        public String getTableCreateCmd() {
            return "create table if not exists trafficstatistics ( id integer primary key autoincrement, imsi text, type text, data long, recordtime long, month int);";
        }

        public String getAuthority() {
            return TrafficDBProvider.AUTHORITY;
        }

        public String getTableDropCmd() {
            return "DROP TABLE IF EXISTS trafficstatistics";
        }

        public String getPrimaryColumn() {
            return "id";
        }
    }

    public TrafficStatisticsInfo(String imsi, int month, int type) {
        this.mImsi = imsi;
        this.mMonth = month;
        this.trafficType = type;
        this.dbTable = new Tables();
    }

    public long getTraffic() {
        return this.mRecords;
    }

    public void setTraffic(long setBytes) {
        this.mRecords = setBytes;
    }

    public ITableInfo get() {
        String[] projection = new String[]{"imsi", "type", PushResponse.DATA_FIELD, "recordtime", "month"};
        String[] selectArg = new String[]{String.valueOf(this.mMonth), this.mImsi, String.valueOf(this.trafficType)};
        Cursor cursor = GlobalContext.getContext().getContentResolver().query(this.dbTable.getUri(), projection, "month = ? and imsi = ? and type = ?", selectArg, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            HwLog.e(TAG, "result is empty");
            this.recordTime = DateUtil.getMonthStartTimeMills(this.mImsi);
            return this;
        }
        if (cursor.moveToNext()) {
            int mDataIndex = cursor.getColumnIndex(PushResponse.DATA_FIELD);
            int mRecordTimeIndex = cursor.getColumnIndex("recordtime");
            do {
                this.mRecords = cursor.getLong(mDataIndex);
                this.recordTime = cursor.getLong(mRecordTimeIndex);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return this;
    }

    public ITableInfo save(Object[] obj) {
        MonthTotalTrafficInfo info;
        boolean netEnable = CustomizeManager.getInstance().isFeatureEnabled(30);
        ContentResolver cr = GlobalContext.getContext().getContentResolver();
        String[] args = new String[]{this.mImsi, String.valueOf(this.trafficType), String.valueOf(this.mMonth)};
        ContentValues values = new ContentValues();
        values.put(PushResponse.DATA_FIELD, Long.valueOf(this.mRecords));
        values.put("recordtime", Long.valueOf(this.recordTime));
        int result = cr.update(this.dbTable.getUri(), values, "imsi = ? and type = ? and month = ?", args);
        if (netEnable && this.trafficType == 301) {
            String secImsi = SimCardManager.getInstance().getOtherImsi(this.mImsi);
            if (TextUtils.isEmpty(secImsi)) {
                HwLog.d(TAG, "not sec card");
            } else {
                int secYearMonth = DateUtil.getYearMonth(secImsi);
                TrafficStatisticsInfo secInfo = new TrafficStatisticsInfo(secImsi, secYearMonth, 301);
                secInfo.get();
                if (secInfo.mRecords == -1) {
                    HwLog.i(TAG, "no traffic data on sec card, should reset monthly total");
                    info = MonthTotalTrafficInfo.create(secImsi);
                    if (info != null) {
                        info.save();
                    }
                    ContentValues secValue = new ContentValues();
                    secValue.put(PushResponse.DATA_FIELD, Integer.valueOf(0));
                    secValue.put("recordtime", Long.valueOf(this.recordTime));
                    secValue.put("type", Integer.valueOf(301));
                    secValue.put("imsi", secImsi);
                    secValue.put("month", Integer.valueOf(secYearMonth));
                    try {
                        cr.insert(this.dbTable.getUri(), secValue);
                    } catch (Exception e) {
                        HwLog.e(TAG, "insert fail , e = " + e.getMessage());
                    }
                }
            }
        }
        if (result <= 0) {
            if (netEnable && this.trafficType == 301) {
                info = MonthTotalTrafficInfo.create(this.mImsi);
                if (info != null) {
                    info.save();
                }
            }
            values.put("type", Integer.valueOf(this.trafficType));
            values.put("imsi", this.mImsi);
            values.put("month", Integer.valueOf(this.mMonth));
            try {
                cr.insert(this.dbTable.getUri(), values);
            } catch (Exception e2) {
                HwLog.e(TAG, "insert fail , e = " + e2.getMessage());
            }
        }
        return this;
    }

    public ITableInfo clear() {
        String[] args = new String[]{this.mImsi, String.valueOf(this.mMonth), String.valueOf(this.trafficType)};
        GlobalContext.getContext().getContentResolver().delete(this.dbTable.getUri(), "imsi = ? and month = ? and type = ?", args);
        return this;
    }

    public void setRecordTime(long recordTime) {
        this.recordTime = recordTime;
    }
}
