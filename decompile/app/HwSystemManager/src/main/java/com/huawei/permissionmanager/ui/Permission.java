package com.huawei.permissionmanager.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import com.huawei.permissionmanager.db.AppInfo;
import java.util.ArrayList;

public abstract class Permission extends ListViewObject {
    private ArrayList<String> mAndroidPermissionSet;
    private Context mContext;
    private CheckPackagePermissionInterface mICheckPackagePermission;
    private int mPermissionCategoryId;
    private int mPermissionCount = 0;
    private int mPermissionDescriptionsCode;
    private int mPermissionNameCode;
    private int mPermissionNoneAppTipsCode;
    private int mPermissionPopupInfoCode;
    private int mPermissionType;

    public abstract int getHistoryStringId();

    public abstract String getName(Context context);

    public /* bridge */ /* synthetic */ String getTagText(Context context) {
        return super.getTagText(context);
    }

    public /* bridge */ /* synthetic */ boolean isTag() {
        return super.isTag();
    }

    public Permission(Context context, int permName, int permDescriptions, int permissionType, int permissionPopupInfoCode, int permissionCategoryId, int permissionNoneAppTipsCode, CheckPackagePermissionInterface interfaceCheckPackagePermission) {
        super(false, 0);
        this.mContext = context;
        this.mPermissionDescriptionsCode = permDescriptions;
        this.mPermissionNameCode = permName;
        this.mPermissionPopupInfoCode = permissionPopupInfoCode;
        this.mPermissionType = permissionType;
        this.mPermissionCategoryId = permissionCategoryId;
        this.mPermissionNoneAppTipsCode = permissionNoneAppTipsCode;
        this.mAndroidPermissionSet = new ArrayList();
        this.mICheckPackagePermission = interfaceCheckPackagePermission;
    }

    public String getmPermissionForbitTips(Context mContext, String pkgName) {
        if (mContext != null) {
            return "";
        }
        throw new IllegalArgumentException("mContext can not be null.");
    }

    public String getmPermissionNames() {
        return this.mContext.getString(this.mPermissionNameCode);
    }

    public boolean donotAskAgain() {
        return true;
    }

    public String getmPermissionDescriptions(int count) {
        return this.mContext.getResources().getQuantityString(this.mPermissionDescriptionsCode, count, new Object[]{Integer.valueOf(count)});
    }

    public String getPermissionPopupInfo() {
        return this.mContext.getString(this.mPermissionPopupInfoCode);
    }

    public int getPermissionCode() {
        return this.mPermissionType;
    }

    public String getPermissionCategoryName() {
        return this.mContext.getString(this.mPermissionCategoryId);
    }

    public String getPermissionNoneAppTrips() {
        return this.mContext.getString(this.mPermissionNoneAppTipsCode);
    }

    public ArrayList<String> getAndroidPermissionSet() {
        return this.mAndroidPermissionSet;
    }

    public void addAndroidPermission(String permissionString) {
        this.mAndroidPermissionSet.add(permissionString);
    }

    public boolean isPermissionRequestedByPackage(String packageName, PackageManager pakcageManager) {
        return this.mICheckPackagePermission.isAppRequestPermission(packageName, pakcageManager, this);
    }

    public boolean isPermissionRequested(int permissionCode) {
        return (this.mPermissionType & permissionCode) != 0;
    }

    public boolean isPermissionBlocked(AppInfo appInfo) {
        if ((this.mPermissionType & appInfo.mPermissionCfg) != 0) {
            return true;
        }
        return false;
    }

    public int getPermissionType(AppInfo appInfo) {
        if ((this.mPermissionType & appInfo.mPermissionCode) == 0) {
            return 0;
        }
        if ((this.mPermissionType & appInfo.mPermissionCfg) != 0) {
            return 2;
        }
        return 1;
    }

    public int getPermissionCount() {
        return this.mPermissionCount;
    }

    public void setPermissionCount(int count) {
        this.mPermissionCount = count;
    }

    public boolean showBillingWarning() {
        return false;
    }

    public boolean showNotAskAgain() {
        return false;
    }
}
