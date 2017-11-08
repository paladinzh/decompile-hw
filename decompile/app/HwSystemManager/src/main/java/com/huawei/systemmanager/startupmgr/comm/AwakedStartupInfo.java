package com.huawei.systemmanager.startupmgr.comm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.database.gfeature.GFeatureCvt;
import com.huawei.systemmanager.startupmgr.comm.StartupDBConst.AwakedViewKeys;
import com.huawei.systemmanager.startupmgr.db.AwakedCallerTable;
import com.huawei.systemmanager.startupmgr.db.StartupProvider.AwakedCallerProvider;
import com.huawei.systemmanager.startupmgr.db.StartupProvider.StartupGFeatureProvider;
import com.huawei.systemmanager.startupmgr.localize.LocalizePackageWrapper;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class AwakedStartupInfo extends AbsStartupInfo {
    public static final String[] AWAKED_FULL_QUERY_PROJECTION = new String[]{"packageName", "status", AwakedViewKeys.LAST_CALLER_PKG_COL, AwakedViewKeys.CALLER_PKG_SET_COL, "userchanged"};
    public static final String QUERY_BY_PKG_WHERE = "packageName = ? ";
    private static final String TAG = "AwakedStartupInfo";
    List<String> mCallerLabelList = Lists.newArrayList();
    List<String> mCallerPkgsList = Lists.newArrayList();
    String mLastCallerPkg = null;

    public static AwakedStartupInfo fromCursor(Cursor cursor) {
        AwakedStartupInfo result = new AwakedStartupInfo(cursor.getString(0), "1".equals(cursor.getString(1)));
        result.setOrigCallerPkgList(cursor.getString(3));
        result.setLastCaller(cursor.getString(2));
        if ("1".equals(cursor.getString(4))) {
            result.setUserHasChanged();
        }
        return result;
    }

    public AwakedStartupInfo(String pkgName, boolean status) {
        this.mPkgName = pkgName;
        this.mStatus = status;
    }

    public void setOrigCallerPkgList(String pkgsStr) {
        this.mCallerPkgsList.clear();
        this.mCallerPkgsList.addAll(dbStringToList(pkgsStr));
    }

    public void loadCallerLabel(Context ctx) {
        this.mCallerLabelList.addAll(convertPackages(ctx, this.mCallerPkgsList));
    }

    public int getCallerCount() {
        return this.mCallerLabelList.size();
    }

    public String getCombinedLabelString(Context ctx) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < this.mCallerLabelList.size(); i++) {
            result.append("\n").append(ctx.getString(R.string.startupmgr_change_confirm_msg_item_n, new Object[]{Integer.valueOf(i + 1), this.mCallerLabelList.get(i)}));
        }
        return result.toString();
    }

    public void setLastCaller(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            HwLog.w("AwakedStartupInfo", "setLastCaller empty caller");
            return;
        }
        this.mLastCallerPkg = pkgName;
        if (!this.mCallerPkgsList.contains(this.mLastCallerPkg)) {
            this.mCallerPkgsList.add(this.mLastCallerPkg);
        }
    }

    public void updateCallerInfoToDB(Context ctx) {
        callProviderBulkInsert(ctx, StartupGFeatureProvider.CONTENT_URI, getExtContentValuesList());
        writeCallerToAwakedCallerTable(ctx);
    }

    protected List<ContentValues> getModifyStatusContentValuesList() {
        List<ContentValues> result = Lists.newArrayList();
        result.add(GFeatureCvt.cvtToStdContentValue(this.mPkgName, AwakedViewKeys.STATUS_STORE, this.mStatus ? "1" : "0"));
        if (isUserChanged()) {
            result.add(GFeatureCvt.cvtToStdContentValue(this.mPkgName, AwakedViewKeys.USER_CHANGED_STORE, "1"));
        }
        return result;
    }

    protected List<ContentValues> getExtContentValuesList() {
        List<ContentValues> result = Lists.newArrayList();
        result.add(GFeatureCvt.cvtToStdContentValue(this.mPkgName, AwakedViewKeys.CALLER_PKG_SET_STORE, listToDBString(this.mCallerPkgsList)));
        result.add(GFeatureCvt.cvtToStdContentValue(this.mPkgName, AwakedViewKeys.LAST_CALLER_PKG_STORE, this.mLastCallerPkg));
        return result;
    }

    public boolean validInfo() {
        if (TextUtils.isEmpty(this.mLastCallerPkg) || this.mCallerPkgsList.isEmpty()) {
            return false;
        }
        return true;
    }

    protected int getFwkStartupSettingType() {
        return 1;
    }

    public void persistFullDataOtherTable(Context ctx) {
        writeCallerToAwakedCallerTable(ctx);
    }

    public String toString() {
        return "AwakedStartupInfo " + this.mPkgName + ", lastCaller: " + this.mLastCallerPkg + " callerSet: " + this.mCallerPkgsList;
    }

    private List<String> convertPackages(Context ctx, List<String> pkgs) {
        List<String> result = Lists.newArrayList();
        for (String pkg : pkgs) {
            result.add(LocalizePackageWrapper.getSinglePackageLocalizeName(ctx, pkg));
        }
        return result;
    }

    private void writeCallerToAwakedCallerTable(Context ctx) {
        List<ContentValues> cvs = Lists.newArrayList();
        for (String caller : this.mCallerPkgsList) {
            ContentValues cv = new ContentValues();
            cv.put("packageName", this.mPkgName);
            cv.put(AwakedCallerTable.COL_CALLER_PKG, caller);
            cvs.add(cv);
        }
        callProviderBulkInsert(ctx, AwakedCallerProvider.CONTENT_URI, cvs);
    }
}
