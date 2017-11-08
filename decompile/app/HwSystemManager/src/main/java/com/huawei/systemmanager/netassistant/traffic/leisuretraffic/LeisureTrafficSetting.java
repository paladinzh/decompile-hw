package com.huawei.systemmanager.netassistant.traffic.leisuretraffic;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.netassistant.db.comm.DBTable;
import com.huawei.systemmanager.netassistant.db.comm.ITableInfo;
import com.huawei.systemmanager.netassistant.db.traffic.TrafficDBProvider;
import com.huawei.systemmanager.util.HwLog;

public class LeisureTrafficSetting extends ITableInfo {
    public static final int SWITCH_OFF = 0;
    public static final int SWITCH_ON = 1;
    private static final String TAG = LeisureTrafficSetting.class.getSimpleName();
    DBTable dbTable = new Tables();
    HourMinute endHM = new HourMinute(7, 0);
    String mImsi;
    boolean mNotify = true;
    long mPackageSize = -1;
    boolean mSwitch = false;
    HourMinute startHM = new HourMinute(0, 0);

    public static class HourMinute {
        int hour;
        int minute;

        public HourMinute(int hour, int minute) {
            this.hour = hour;
            this.minute = minute;
        }

        public void set(int hour, int minute) {
            this.hour = hour;
            this.minute = minute;
        }
    }

    public static class Tables extends DBTable {
        static final String COL_END_HOUR = "end_hour";
        static final String COL_END_MINUTE = "end_minute";
        static final String COL_ID = "id";
        static final String COL_IMSI = "imsi";
        static final String COL_LEISURE_PACKAGE = "size";
        static final String COL_NOTIFY = "notify";
        static final String COL_START_HOUR = "start_hour";
        static final String COL_START_MINUTE = "start_minute";
        static final String COL_SWITCH = "switch";
        public static final String TABLE_NAME = "leisuretraffic";

        public String getTableCreateCmd() {
            return "create table if not exists leisuretraffic ( id integer primary key autoincrement, imsi text, switch int, size text, start_hour int, start_minute int, end_hour int, end_minute int, notify int);";
        }

        public String getTableDropCmd() {
            return "DROP TABLE IF EXISTS leisuretraffic";
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

    public void setmSwitch(boolean mSwitch) {
        this.mSwitch = mSwitch;
    }

    public void setmPackageSize(long mPackageSize) {
        this.mPackageSize = mPackageSize;
    }

    public void setStartHM(int hour, int minute) {
        this.startHM.set(hour, minute);
    }

    public void setEndHM(int hour, int minute) {
        this.endHM.set(hour, minute);
    }

    public void setmNotify(boolean mNotify) {
        this.mNotify = mNotify;
    }

    public int getStartHour() {
        return this.startHM.hour;
    }

    public int getStartMinute() {
        return this.startHM.minute;
    }

    public int getEndHour() {
        return this.endHM.hour;
    }

    public int getEndMinute() {
        return this.endHM.minute;
    }

    public boolean ismSwitch() {
        return this.mSwitch;
    }

    public long getmPackageSize() {
        return this.mPackageSize;
    }

    public String getStartHM() {
        return DateUtil.formatHourMinute(this.startHM.hour, this.startHM.minute);
    }

    public String getEndHM() {
        return DateUtil.formatHourMinute(this.endHM.hour, this.endHM.minute);
    }

    public boolean ismNotify() {
        return this.mNotify;
    }

    public LeisureTrafficSetting(String imsi) {
        this.mImsi = imsi;
    }

    public ITableInfo get() {
        Cursor cursor = GlobalContext.getContext().getContentResolver().query(this.dbTable.getUri(), new String[]{"imsi", "switch", "size", "start_hour", "start_minute", "end_hour", "end_minute", "notify"}, "imsi = ?", new String[]{String.valueOf(this.mImsi)}, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            HwLog.e(TAG, "result is empty");
            return this;
        }
        if (cursor.moveToNext()) {
            int switchIndex = cursor.getColumnIndex("switch");
            int packageIndex = cursor.getColumnIndex("size");
            int startHourIndex = cursor.getColumnIndex("start_hour");
            int startMinuteIndex = cursor.getColumnIndex("start_minute");
            int endHourIndex = cursor.getColumnIndex("end_hour");
            int endMinuteIndex = cursor.getColumnIndex("end_minute");
            do {
                this.mSwitch = cursor.getInt(switchIndex) == 1;
                this.mPackageSize = cursor.getLong(packageIndex);
                this.startHM.set(cursor.getInt(startHourIndex), cursor.getInt(startMinuteIndex));
                this.endHM.set(cursor.getInt(endHourIndex), cursor.getInt(endMinuteIndex));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return this;
    }

    public ITableInfo save(Object[] obj) {
        int i = 1;
        ContentResolver cr = GlobalContext.getContext().getContentResolver();
        String where = "imsi = ?";
        String[] args = new String[]{this.mImsi};
        ContentValues values = new ContentValues();
        values.put("imsi", this.mImsi);
        String str = "switch";
        if (!this.mSwitch) {
            i = 0;
        }
        values.put(str, Integer.valueOf(i));
        values.put("size", Long.valueOf(this.mPackageSize));
        values.put("start_hour", Integer.valueOf(this.startHM.hour));
        values.put("start_minute", Integer.valueOf(this.startHM.minute));
        values.put("end_hour", Integer.valueOf(this.endHM.hour));
        values.put("end_minute", Integer.valueOf(this.endHM.minute));
        if (cr.update(this.dbTable.getUri(), values, where, args) <= 0) {
            cr.insert(this.dbTable.getUri(), values);
        }
        return this;
    }

    public ITableInfo clear() {
        String[] args = new String[]{this.mImsi};
        GlobalContext.getContext().getContentResolver().delete(this.dbTable.getUri(), "imsi = ?", args);
        return this;
    }

    public boolean inLeisureTime() {
        return DateUtil.isBetweenCurrentTime(this.startHM.hour, this.startHM.minute, this.endHM.hour, this.endHM.minute) ? ismSwitch() : false;
    }

    public String getDesString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(CommonMethodUtil.formatBytes(GlobalContext.getContext(), this.mPackageSize));
        buffer.append(ConstValues.SEPARATOR_KEYWORDS_EN);
        buffer.append(getStartHM());
        buffer.append("-");
        buffer.append(getEndHM());
        return buffer.toString();
    }
}
