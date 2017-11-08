package com.huawei.permissionmanager.db;

import android.content.ContentValues;
import com.huawei.systemmanager.comm.misc.TimeUtil;

public class HistoryRecord {
    private int mAction;
    private long mCount;
    private long mDateStartTime;
    private int mPermissionType;
    private String mPkgName;
    private long mTimestamp;

    public HistoryRecord(String pkgName, int permissionType, int action, long count, long dateStartTime, long timestamp) {
        this.mPkgName = pkgName;
        this.mPermissionType = permissionType;
        this.mAction = action;
        this.mCount = count;
        this.mDateStartTime = dateStartTime;
        this.mTimestamp = timestamp;
    }

    public String getPkgName() {
        return this.mPkgName;
    }

    public int getPermissionType() {
        return this.mPermissionType;
    }

    public long getCount() {
        return this.mCount;
    }

    public long getTimestamp() {
        return this.mTimestamp;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put("packageName", this.mPkgName);
        values.put(DBHelper.COLUMN_PERMISSION_TYPE, Integer.valueOf(this.mPermissionType));
        values.put("action", Integer.valueOf(this.mAction));
        values.put(DBHelper.COLUMN_DATE_START_TIME, Long.valueOf(this.mDateStartTime));
        values.put(DBHelper.COLUMN_TIME_STAMP, Long.valueOf(this.mTimestamp));
        return values;
    }

    public void resetDateStartTime() {
        if (this.mDateStartTime <= 0) {
            this.mDateStartTime = TimeUtil.getDayStartTime(this.mTimestamp);
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HistoryRecord[packageName:").append(this.mPkgName).append(",permissionType:").append(this.mPermissionType).append(",action:").append(this.mAction).append(",dateStartTime:").append(this.mDateStartTime).append(",timestamp:").append(this.mTimestamp).append("]");
        return builder.toString();
    }
}
