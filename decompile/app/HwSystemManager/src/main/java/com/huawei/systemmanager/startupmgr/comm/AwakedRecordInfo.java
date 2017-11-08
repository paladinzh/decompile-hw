package com.huawei.systemmanager.startupmgr.comm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.TimeUtil;
import com.huawei.systemmanager.startupmgr.db.AwakedRecordTable;
import com.huawei.systemmanager.startupmgr.db.StartupProvider.StartupRecordProvider;
import com.huawei.systemmanager.startupmgr.localize.LocalizePackageWrapper;
import com.huawei.systemmanager.util.HwLog;

public class AwakedRecordInfo extends AbsRecordInfo {
    public static final String[] AWAKED_RECORD_FULL_QUERY_PROJECTION = new String[]{"packageName", AwakedRecordTable.COL_CALLER_PACKAGE_NAME, "startupResult", "timeOfDay", "timeOfLastExact", "totalCount"};
    private static final String TAG = "AwakedRecordInfo";
    private String mCallerLabel;
    private String mCallerPkg;

    static AwakedRecordInfo createAwakedRecordFromBundle(Context ctx, Bundle bundle, String type) {
        AwakedRecordInfo info = new AwakedRecordInfo();
        info.setBasicMember(bundle, type);
        info.setCallerPkg(SysCallUtils.getPackageByPidUid(ctx, bundle.getInt(StartupFwkConst.KEY_RECORD_CALLER_PID), bundle.getInt(StartupFwkConst.KEY_RECORD_CALLER_UID)));
        return info;
    }

    public static AwakedRecordInfo createWhenFirstConfirm(String pkgName, String callerPkg, String type, boolean bAllow) {
        AwakedRecordInfo result = new AwakedRecordInfo();
        result.mPkgName = pkgName;
        result.mCallerPkg = callerPkg;
        result.mType = type;
        result.mResult = bAllow;
        result.mTimeOfLastExact = System.currentTimeMillis();
        result.mTimeOfDayStart = TimeUtil.getDayStartTime(result.mTimeOfLastExact);
        return result;
    }

    public static AwakedRecordInfo fromCursor(Cursor cursor) {
        boolean z = true;
        AwakedRecordInfo result = new AwakedRecordInfo();
        result.mPkgName = cursor.getString(0);
        result.mCallerPkg = cursor.getString(1);
        if (1 != cursor.getInt(2)) {
            z = false;
        }
        result.mResult = z;
        result.mTimeOfDayStart = cursor.getLong(3);
        result.mTimeOfLastExact = cursor.getLong(4);
        result.mTotalCount = cursor.getInt(5);
        return result;
    }

    public String getTitleString(Context ctx) {
        return ctx.getString(R.string.startupmgr_awaked_record_item_title, new Object[]{this.mLabel, this.mCallerLabel});
    }

    public int getDescriptionString(boolean allow) {
        if (allow) {
            return R.plurals.startupmgr_awaked_record_item_allow_description;
        }
        return R.plurals.startupmgr_awaked_record_item_forbid_description;
    }

    protected void loadSubExtUIMembersIfExist(Context ctx) {
        this.mCallerLabel = LocalizePackageWrapper.getSinglePackageLocalizeName(ctx, this.mCallerPkg);
    }

    public boolean extMemberValid() {
        return !TextUtils.isEmpty(this.mCallerPkg);
    }

    public String getQueryWhereClause() {
        return "packageName = ? and callerPackageName = ? and startupResult = ? and timeOfDay = ? ";
    }

    public String[] getQueryWhereArgs() {
        int i = 1;
        String[] strArr = new String[4];
        strArr[0] = this.mPkgName;
        strArr[1] = this.mCallerPkg;
        if (!this.mResult) {
            i = 0;
        }
        strArr[2] = String.valueOf(i);
        strArr[3] = String.valueOf(this.mTimeOfDayStart);
        return strArr;
    }

    public Uri getQueryUri() {
        return Uri.withAppendedPath(StartupRecordProvider.CONTENT_URI_BASE, AwakedRecordTable.TABLE_NAME);
    }

    public String getQueryCountColName() {
        return "totalCount";
    }

    public ContentValues recordToContentValues() {
        ContentValues cv = new ContentValues();
        cv.put("packageName", this.mPkgName);
        cv.put(AwakedRecordTable.COL_CALLER_PACKAGE_NAME, this.mCallerPkg);
        cv.put("startupResult", Boolean.valueOf(this.mResult));
        cv.put("timeOfDay", Long.valueOf(this.mTimeOfDayStart));
        cv.put("timeOfLastExact", Long.valueOf(this.mTimeOfLastExact));
        cv.put("totalCount", Integer.valueOf(this.mTotalCount));
        return cv;
    }

    public boolean uiMemberValid() {
        return super.uiMemberValid() && !TextUtils.isEmpty(this.mCallerLabel);
    }

    public String toString() {
        return "Awaked RecordInfo {" + super.toString() + " caller " + this.mCallerPkg + "}";
    }

    private void setCallerPkg(String callerPkg) {
        HwLog.d(TAG, "setCallerPkg: " + callerPkg);
        this.mCallerPkg = callerPkg;
    }
}
