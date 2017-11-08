package com.huawei.systemmanager.startupmgr.comm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.startupmgr.db.NormalRecordTable;
import com.huawei.systemmanager.startupmgr.db.StartupProvider.StartupRecordProvider;

public class NormalRecordInfo extends AbsRecordInfo {
    public static final String[] NORMAL_RECORD_FULL_QUERY_PROJECTION = new String[]{"packageName", "startupResult", "timeOfDay", "timeOfLastExact", "totalCount"};

    static NormalRecordInfo createNormalRecordFromBundle(Bundle bundle, String type) {
        NormalRecordInfo info = new NormalRecordInfo();
        info.setBasicMember(bundle, type);
        return info;
    }

    public static NormalRecordInfo fromCursor(Cursor cursor) {
        boolean z = true;
        NormalRecordInfo result = new NormalRecordInfo();
        result.mPkgName = cursor.getString(0);
        if (1 != cursor.getInt(1)) {
            z = false;
        }
        result.mResult = z;
        result.mTimeOfDayStart = cursor.getLong(2);
        result.mTimeOfLastExact = cursor.getLong(3);
        result.mTotalCount = cursor.getInt(4);
        return result;
    }

    public String getTitleString(Context ctx) {
        return this.mLabel;
    }

    public int getDescriptionString(boolean allow) {
        if (allow) {
            return R.plurals.startupmgr_normal_record_item_allow_description;
        }
        return R.plurals.startupmgr_normal_record_item_forbid_description;
    }

    public boolean extMemberValid() {
        return true;
    }

    public String getQueryWhereClause() {
        return "packageName = ? and startupResult = ? and timeOfDay = ? ";
    }

    public String[] getQueryWhereArgs() {
        int i = 0;
        String[] strArr = new String[3];
        strArr[0] = this.mPkgName;
        if (this.mResult) {
            i = 1;
        }
        strArr[1] = String.valueOf(i);
        strArr[2] = String.valueOf(this.mTimeOfDayStart);
        return strArr;
    }

    public Uri getQueryUri() {
        return Uri.withAppendedPath(StartupRecordProvider.CONTENT_URI_BASE, NormalRecordTable.TABLE_NAME);
    }

    public String getQueryCountColName() {
        return "totalCount";
    }

    public ContentValues recordToContentValues() {
        ContentValues cv = new ContentValues();
        cv.put("packageName", this.mPkgName);
        cv.put("startupResult", Boolean.valueOf(this.mResult));
        cv.put("timeOfDay", Long.valueOf(this.mTimeOfDayStart));
        cv.put("timeOfLastExact", Long.valueOf(this.mTimeOfLastExact));
        cv.put("totalCount", Integer.valueOf(this.mTotalCount));
        return cv;
    }

    public String toString() {
        return "Normal RecordInfo {" + super.toString() + "}";
    }
}
