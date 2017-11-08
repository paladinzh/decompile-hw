package com.huawei.systemmanager.startupmgr.comm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.database.gfeature.GFeatureCvt;
import com.huawei.systemmanager.startupmgr.comm.StartupDBConst.NormalViewKeys;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class NormalStartupInfo extends AbsStartupInfo {
    public static final String[] NORMAL_FULL_QUERY_PROJECTION = new String[]{"packageName", "status", "type", NormalViewKeys.ACTIONS_COL, "userchanged"};
    public static final String QUERY_BY_PKG_WHERE = "packageName = ? ";
    private static final String TAG = "NormalStartupInfo";
    List<String> mActionList;
    List<String> mReadableActionList;
    int mStartType;

    public static NormalStartupInfo fromCursor(Cursor cursor) {
        NormalStartupInfo result = new NormalStartupInfo(cursor.getString(0), "1".equals(cursor.getString(1)), cursor.getInt(2));
        result.setActionSet(cursor.getString(3));
        if ("1".equals(cursor.getString(4))) {
            result.setUserHasChanged();
        }
        return result;
    }

    public NormalStartupInfo(String pkgName) {
        this(pkgName, false, 0);
    }

    private NormalStartupInfo(String pkgName, boolean status, int startType) {
        this.mStartType = 0;
        this.mActionList = Lists.newArrayList();
        this.mReadableActionList = Lists.newArrayList();
        this.mPkgName = pkgName;
        this.mStatus = status;
        this.mStartType = startType;
    }

    public void appendStartType(int type) {
        this.mStartType |= type;
    }

    public void setHasAny() {
        appendStartType(4);
    }

    public int startupDescriptionResId() {
        if ((this.mStartType & 1) != 0 && (this.mStartType & 2) != 0) {
            return R.string.startupmgr_normal_type_item_description_3;
        }
        if ((this.mStartType & 1) != 0) {
            return R.string.startupmgr_normal_type_item_description_1;
        }
        return R.string.startupmgr_normal_type_item_description_2;
    }

    public void appendAction(String action) {
        if (TextUtils.isEmpty(action)) {
            HwLog.w("NormalStartupInfo", "appendAction empty action");
        } else if (!this.mActionList.contains(action)) {
            this.mActionList.add(action);
        }
    }

    public void setActionSet(String actions) {
        this.mActionList.addAll(dbStringToList(actions));
    }

    public void loadReadableActionList(Context ctx) {
        this.mReadableActionList.addAll(MonitorActionsAssist.convertReadableActions(ctx, this.mActionList));
        if (this.mReadableActionList.isEmpty()) {
            this.mReadableActionList.add(ctx.getString(R.string.startupmgr_action_description_self_defined));
        }
    }

    public int getActionCount() {
        return this.mActionList.size();
    }

    public int getReadableActionCount() {
        return this.mReadableActionList.size();
    }

    public String getCombinedActionString(Context ctx) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < this.mReadableActionList.size(); i++) {
            result.append("\n").append(ctx.getString(R.string.startupmgr_change_confirm_msg_item_n, new Object[]{Integer.valueOf(i + 1), this.mReadableActionList.get(i)}));
        }
        return result.toString();
    }

    protected List<ContentValues> getModifyStatusContentValuesList() {
        List<ContentValues> result = Lists.newArrayList();
        result.add(GFeatureCvt.cvtToStdContentValue(this.mPkgName, NormalViewKeys.STATUS_STORE, this.mStatus ? "1" : "0"));
        if (isUserChanged()) {
            result.add(GFeatureCvt.cvtToStdContentValue(this.mPkgName, NormalViewKeys.USER_CHANGED_STORE, "1"));
        }
        return result;
    }

    protected List<ContentValues> getExtContentValuesList() {
        List<ContentValues> result = Lists.newArrayList();
        result.add(GFeatureCvt.cvtToStdContentValue(this.mPkgName, NormalViewKeys.TYPE_STORE, String.valueOf(this.mStartType)));
        result.add(GFeatureCvt.cvtToStdContentValue(this.mPkgName, NormalViewKeys.ACTIONS_STORE, listToDBString(this.mActionList)));
        return result;
    }

    public boolean validInfo() {
        boolean z = true;
        if (this.mActionList.isEmpty()) {
            if ((this.mStartType & 4) == 0) {
                z = false;
            }
            return z;
        }
        if ((this.mStartType & 1) == 0 && (this.mStartType & 2) == 0) {
            z = false;
        }
        return z;
    }

    protected int getFwkStartupSettingType() {
        return 0;
    }

    public void persistFullDataOtherTable(Context ctx) {
    }

    public String toString() {
        return "NormalStartupInfo " + this.mPkgName + ", startType: " + this.mStartType + ", actionList: " + this.mActionList;
    }
}
