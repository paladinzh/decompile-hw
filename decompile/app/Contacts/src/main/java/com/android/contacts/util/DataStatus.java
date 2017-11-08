package com.android.contacts.util;

import android.database.Cursor;
import com.android.contacts.model.ValuesDelta;

public class DataStatus {
    private int mIconRes = -1;
    private int mLabelRes = -1;
    private int mPresence = -1;
    private String mResPackage = null;
    private String mStatus = null;
    private long mTimestamp = -1;

    public DataStatus(Cursor cursor) {
        fromCursor(cursor);
    }

    private void fromCursor(Cursor cursor) {
        this.mPresence = getInt(cursor, "mode", -1);
        this.mStatus = getString(cursor, "status");
        this.mTimestamp = getLong(cursor, "status_ts", -1);
        this.mResPackage = getString(cursor, "status_res_package");
        this.mIconRes = getInt(cursor, "status_icon", -1);
        this.mLabelRes = getInt(cursor, "status_label", -1);
    }

    public void fromValuesDelta(ValuesDelta aValuesDelta) {
        this.mPresence = aValuesDelta.getAsInteger("mode", Integer.valueOf(-1)).intValue();
        this.mStatus = aValuesDelta.getAsString("status");
        Long temp = aValuesDelta.getAsLong("status_ts");
        this.mTimestamp = temp != null ? temp.longValue() : 0;
        this.mResPackage = aValuesDelta.getAsString("status_res_package");
        this.mIconRes = aValuesDelta.getAsInteger("status_icon", Integer.valueOf(-1)).intValue();
        this.mLabelRes = aValuesDelta.getAsInteger("status_label", Integer.valueOf(-1)).intValue();
    }

    public int getPresence() {
        return this.mPresence;
    }

    private static String getString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    private static int getInt(Cursor cursor, String columnName, int missingValue) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.isNull(columnIndex) ? missingValue : cursor.getInt(columnIndex);
    }

    private static long getLong(Cursor cursor, String columnName, long missingValue) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.isNull(columnIndex) ? missingValue : cursor.getLong(columnIndex);
    }
}
