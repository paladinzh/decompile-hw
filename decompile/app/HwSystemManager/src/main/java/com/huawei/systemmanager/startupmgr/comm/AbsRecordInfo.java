package com.huawei.systemmanager.startupmgr.comm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.comm.misc.TimeUtil;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgUtils;
import java.text.Collator;
import java.util.Comparator;

public abstract class AbsRecordInfo {
    private static final String TAG = "AbsRecordInfo";
    protected Drawable mAppIcon = null;
    protected String mLabel = null;
    protected String mPkgName;
    protected boolean mResult;
    protected long mTimeOfDayStart;
    protected long mTimeOfLastExact;
    protected int mTotalCount = 0;
    protected String mType;
    protected int mUid = -1;

    public static class Cmp implements Comparator<AbsRecordInfo> {
        public int compare(AbsRecordInfo lhs, AbsRecordInfo rhs) {
            if (lhs.mTimeOfLastExact == rhs.mTimeOfLastExact) {
                return Collator.getInstance().compare(lhs.mLabel, rhs.mLabel);
            }
            return lhs.mTimeOfLastExact > rhs.mTimeOfLastExact ? -1 : 1;
        }
    }

    public abstract boolean extMemberValid();

    public abstract int getDescriptionString(boolean z);

    public abstract String getQueryCountColName();

    public abstract Uri getQueryUri();

    public abstract String[] getQueryWhereArgs();

    public abstract String getQueryWhereClause();

    public abstract String getTitleString(Context context);

    public abstract ContentValues recordToContentValues();

    public static AbsRecordInfo createRecordFromBundle(Context ctx, Bundle bundle) {
        if (validBasicBundle(bundle)) {
            String type = bundle.getString("B_RECORD_TYPE", "");
            if ("r".equals(type)) {
                return NormalRecordInfo.createNormalRecordFromBundle(bundle, type);
            }
            if ("p".equals(type) || "s".equals(type)) {
                HwLog.w(TAG, "createRecordFromBundle type provider or service");
                return AwakedRecordInfo.createAwakedRecordFromBundle(ctx, bundle, type);
            }
        }
        return null;
    }

    private static boolean validBasicBundle(Bundle bundle) {
        if (bundle != null && bundle.containsKey(StartupFwkConst.KEY_RECORD_PACKAGE) && bundle.containsKey(StartupFwkConst.KEY_RECORD_RESULT) && bundle.containsKey(StartupFwkConst.KEY_RECORD_TIME)) {
            return bundle.containsKey("B_RECORD_TYPE");
        }
        return false;
    }

    public Drawable getAppIcon() {
        return this.mAppIcon;
    }

    public String getPackageName() {
        return this.mPkgName;
    }

    public String getPackageLabel() {
        return this.mLabel;
    }

    public int getPackageUid() {
        return this.mUid;
    }

    public long getTimeOfLastExact() {
        return this.mTimeOfLastExact;
    }

    public boolean uiMemberValid() {
        return (TextUtils.isEmpty(this.mLabel) || this.mAppIcon == null) ? false : true;
    }

    public boolean validInfoForDB() {
        return validForWrite();
    }

    public boolean validForJumpToSingleApp() {
        if (this.mUid < 0 || TextUtils.isEmpty(this.mPkgName) || TextUtils.isEmpty(this.mLabel)) {
            return false;
        }
        return true;
    }

    public void loadExtUIMembers(Context ctx) {
        this.mAppIcon = HsmPackageManager.getInstance().getIcon(this.mPkgName);
        this.mLabel = HsmPackageManager.getInstance().getLabel(this.mPkgName);
        this.mUid = HsmPkgUtils.getPackageUid(this.mPkgName);
        loadSubExtUIMembersIfExist(ctx);
    }

    public String getDescriptionString(Context ctx) {
        return ctx.getResources().getQuantityString(getDescriptionString(this.mResult), this.mTotalCount, new Object[]{Integer.valueOf(this.mTotalCount), DateUtils.formatDateTime(ctx, this.mTimeOfLastExact, 17)});
    }

    public String getRecordInfo() {
        String pkg = TextUtils.isEmpty(this.mPkgName) ? "" : this.mPkgName;
        return String.format("PKG=%s:COUNT=%s,", new Object[]{pkg, String.valueOf(this.mTotalCount)});
    }

    void setBasicMember(Bundle bundle, String type) {
        this.mPkgName = bundle.getString(StartupFwkConst.KEY_RECORD_PACKAGE);
        this.mResult = bundle.getBoolean(StartupFwkConst.KEY_RECORD_RESULT);
        this.mTimeOfLastExact = bundle.getLong(StartupFwkConst.KEY_RECORD_TIME);
        this.mType = type;
        this.mTimeOfDayStart = TimeUtil.getDayStartTime(this.mTimeOfLastExact);
    }

    boolean validForWrite() {
        return basicMemberValid() ? extMemberValid() : false;
    }

    protected void loadSubExtUIMembersIfExist(Context ctx) {
    }

    private boolean basicMemberValid() {
        return 0 < this.mTimeOfDayStart && this.mTimeOfDayStart <= this.mTimeOfLastExact && !TextUtils.isEmpty(this.mPkgName);
    }

    public void insertOrUpdateRecordCountToDB(Context ctx) {
        int oldCount = queryRecordInfoOldCount(ctx);
        if (oldCount <= 0) {
            this.mTotalCount = 1;
            if (validForWrite()) {
                ctx.getContentResolver().insert(getQueryUri(), recordToContentValues());
                return;
            } else {
                HwLog.w(TAG, "insertOrUpdateRecordCountToDB not valid for insert " + toString());
                return;
            }
        }
        this.mTotalCount = oldCount + 1;
        if (validForWrite()) {
            ctx.getContentResolver().update(getQueryUri(), recordToContentValues(), getQueryWhereClause(), getQueryWhereArgs());
        } else {
            HwLog.w(TAG, "insertOrUpdateRecordCountToDB not valid for update " + toString());
        }
    }

    private int queryRecordInfoOldCount(Context ctx) {
        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(getQueryUri(), new String[]{getQueryCountColName()}, getQueryWhereClause(), getQueryWhereArgs(), null);
            if (cursor == null || cursor.getCount() <= 0) {
                CursorHelper.closeCursor(cursor);
                return -1;
            }
            cursor.moveToNext();
            int value = cursor.getInt(0);
            return value;
        } catch (SQLiteException ex) {
            HwLog.e(TAG, "queryRecordInfoOldCount catch SQLiteException: " + ex.getMessage());
            return -1;
        } catch (Exception ex2) {
            HwLog.e(TAG, "queryRecordInfoOldCount catch Exception: " + ex2.getMessage());
            return -1;
        } finally {
            CursorHelper.closeCursor(cursor);
        }
    }

    public String toString() {
        return "Basic recordInfo {" + this.mPkgName + SqlMarker.COMMA_SEPARATE + this.mResult + SqlMarker.COMMA_SEPARATE + this.mTimeOfDayStart + SqlMarker.COMMA_SEPARATE + this.mTimeOfLastExact + SqlMarker.COMMA_SEPARATE + this.mTotalCount + "}";
    }
}
