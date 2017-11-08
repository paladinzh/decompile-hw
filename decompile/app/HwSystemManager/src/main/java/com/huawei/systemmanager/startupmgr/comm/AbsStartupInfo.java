package com.huawei.systemmanager.startupmgr.comm;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.startupmgr.db.StartupProvider.StartupGFeatureProvider;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import huawei.android.pfw.HwPFWStartupSetting;
import java.io.Serializable;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;

public abstract class AbsStartupInfo {
    private static final String SPLIT_STR = ";";
    private static final String TAG = "AbsStartupInfo";
    protected Drawable mAppIcon = null;
    protected boolean mCompetitor = false;
    protected String mLabel = null;
    protected String mPkgName = null;
    protected boolean mStatus = false;
    protected boolean mUserChangedFlag = false;

    public static class Cmp implements Comparator<AbsStartupInfo>, Serializable {
        private static final long serialVersionUID = -1;

        public int compare(AbsStartupInfo lhs, AbsStartupInfo rhs) {
            if (lhs.mStatus == rhs.mStatus) {
                return Collator.getInstance().compare(lhs.mLabel, rhs.mLabel);
            }
            return lhs.mStatus ? -1 : 1;
        }
    }

    protected abstract List<ContentValues> getExtContentValuesList();

    protected abstract int getFwkStartupSettingType();

    protected abstract List<ContentValues> getModifyStatusContentValuesList();

    public abstract void persistFullDataOtherTable(Context context);

    public abstract boolean validInfo();

    public String getPackageName() {
        return this.mPkgName;
    }

    public String getLabel() {
        return this.mLabel;
    }

    public Drawable getIconDrawable() {
        return this.mAppIcon;
    }

    public boolean getStatus() {
        return this.mStatus;
    }

    public void setStatus(boolean status) {
        this.mStatus = status;
    }

    public void setUserHasChanged() {
        this.mUserChangedFlag = true;
    }

    public boolean isUserChanged() {
        return this.mUserChangedFlag;
    }

    public boolean isCompetitor() {
        return this.mCompetitor;
    }

    public void setPkgIsCompetitor() {
        this.mCompetitor = true;
    }

    public void loadLabelAndIcon() {
        this.mAppIcon = HsmPackageManager.getInstance().getIcon(this.mPkgName);
        this.mLabel = HsmPackageManager.getInstance().getLabel(this.mPkgName);
    }

    public void persistStatusData(Context ctx, boolean writeFwk) {
        HwLog.i(TAG, "persistStatusData called, writeFwk:" + writeFwk);
        callProviderBulkInsert(ctx, StartupGFeatureProvider.CONTENT_URI, getModifyStatusContentValuesList());
        if (writeFwk) {
            StartupBinderAccess.writeSingleDataSettingToFwk(getFwkStartupSettingType(), this.mPkgName, this.mStatus);
        }
    }

    public void persistFullData(Context ctx, boolean writeFwk) {
        List<ContentValues> cvs = getExtContentValuesList();
        cvs.addAll(getModifyStatusContentValuesList());
        callProviderBulkInsert(ctx, StartupGFeatureProvider.CONTENT_URI, cvs);
        if (writeFwk) {
            StartupBinderAccess.writeSingleDataSettingToFwk(getFwkStartupSettingType(), this.mPkgName, this.mStatus);
        }
        persistFullDataOtherTable(ctx);
    }

    public HwPFWStartupSetting getPFWStartupSetting() {
        return new HwPFWStartupSetting(this.mPkgName, getFwkStartupSettingType(), this.mStatus ? 1 : 0);
    }

    String listToDBString(List<String> strList) {
        StringBuilder result = new StringBuilder();
        result.append(";");
        for (String s : strList) {
            if (!TextUtils.isEmpty(s)) {
                result.append(s).append(";");
            }
        }
        return result.toString();
    }

    List<String> dbStringToList(String fullStr) {
        List<String> result = Lists.newArrayList();
        if (fullStr != null) {
            for (String str : fullStr.split(";")) {
                if (!(TextUtils.isEmpty(str) || result.contains(str))) {
                    result.add(str);
                }
            }
        }
        return result;
    }

    void callProviderInsert(Context ctx, Uri uri, ContentValues cv) {
        try {
            ctx.getContentResolver().insert(uri, cv);
        } catch (SQLiteException ex) {
            ex.printStackTrace();
            HwLog.e(TAG, "callProviderInsert catch SQLiteException: " + ex.getMessage());
        } catch (Exception ex2) {
            ex2.printStackTrace();
            HwLog.e(TAG, "callProviderInsert catch Exception: " + ex2.getMessage());
        }
    }

    void callProviderBulkInsert(Context ctx, Uri uri, List<ContentValues> cvs) {
        try {
            ctx.getContentResolver().bulkInsert(uri, (ContentValues[]) cvs.toArray(new ContentValues[cvs.size()]));
        } catch (SQLiteException ex) {
            ex.printStackTrace();
            HwLog.e(TAG, "callProviderBulkInsert catch SQLiteException: " + ex.getMessage());
        } catch (Exception ex2) {
            ex2.printStackTrace();
            HwLog.e(TAG, "callProviderBulkInsert catch Exception: " + ex2.getMessage());
        }
    }
}
