package com.huawei.permissionmanager.ui;

import android.content.Context;
import com.huawei.permission.MPermissionUtil;
import com.huawei.permissionmanager.db.AppInfo;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.permissionmanager.model.PermissionApps;
import com.huawei.permissionmanager.model.PermissionApps.PermissionApp;
import com.huawei.permissionmanager.utils.RecommendBaseItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Map;

public class AppInfoWrapperForSinglePermission extends ListViewObject {
    private static final String LOG_TAG = "AppInfoWrapperForSinglePermission";
    String mLabel;
    int mPermissionStatus = 0;
    int mPermissionType;
    String mPkgName;
    boolean mRecommend = false;
    int mRecommendPercent;
    int mRecommendStatus;
    int mUid;

    public /* bridge */ /* synthetic */ String getTagText(Context context) {
        return super.getTagText(context);
    }

    public /* bridge */ /* synthetic */ boolean isTag() {
        return super.isTag();
    }

    public AppInfoWrapperForSinglePermission(int permissionType) {
        this.mPermissionType = permissionType;
    }

    public static boolean updatePureAppInfoWrapperList(Context ctx, Permission permissionObject, List<AppInfoWrapperForSinglePermission> permissonAppsList, Map<String, RecommendBaseItem> recommendMap, PermissionApps pas) {
        boolean recommendAvailable = false;
        if (permissonAppsList == null) {
            HwLog.e(LOG_TAG, "error: permission list can't be null!");
            return false;
        } else if (permissionObject == null) {
            HwLog.e(LOG_TAG, "error: permissionObject can't be null!");
            return false;
        } else {
            permissonAppsList.clear();
            List<AppInfo> appList = DBAdapter.getInstance(ctx).getAppListByPermission(permissionObject, "updatePureAppInfoWrapperList");
            if (appList == null) {
                HwLog.e(LOG_TAG, "error: appList can't be null!");
                return false;
            }
            HwLog.d(LOG_TAG, "appList size + " + appList.size());
            int permissionType = permissionObject.getPermissionCode();
            for (AppInfo appInfo : appList) {
                AppInfoWrapperForSinglePermission appInfoWrapper = new AppInfoWrapperForSinglePermission(permissionType);
                appInfoWrapper.mUid = appInfo.mAppUid;
                appInfoWrapper.mPkgName = appInfo.mPkgName;
                appInfoWrapper.mLabel = appInfo.mAppLabel;
                appInfoWrapper.mPermissionStatus = getPermissionStatus(appInfo, permissionType, pas);
                RecommendBaseItem recommendItem = (RecommendBaseItem) recommendMap.get(appInfo.mPkgName);
                if (recommendItem != null) {
                    recommendAvailable = true;
                    appInfoWrapper.mRecommend = true;
                    appInfoWrapper.mRecommendStatus = recommendItem.getCurrentPermissionRecommendStatus();
                    appInfoWrapper.mRecommendPercent = recommendItem.getRecommendPercent();
                } else {
                    appInfoWrapper.mRecommend = false;
                }
                permissonAppsList.add(appInfoWrapper);
            }
            return recommendAvailable;
        }
    }

    private static int getPermissionStatus(AppInfo appInfo, int permissionType, PermissionApps pas) {
        int i = 1;
        PermissionApp pa;
        if (pas != null && MPermissionUtil.isClassAType(permissionType)) {
            pa = pas.getApp(Integer.toString(appInfo.mAppUid));
            if (pa == null) {
                HwLog.w(LOG_TAG, "getPermissionStatus, pa null.");
                return 1;
            }
            String permName = (String) MPermissionUtil.typeToSinglePermission.get(permissionType);
            if (permName == null) {
                HwLog.w(LOG_TAG, "getPermissionStatus, permName null.");
                return 1;
            }
            if (!pa.areRuntimePermissionsGranted(permName)) {
                i = 2;
            }
            return i;
        } else if (pas == null || !MPermissionUtil.isClassBType(permissionType)) {
            return (!MPermissionUtil.isClassEType(permissionType) || (appInfo.mPermissionCode & permissionType) <= 0 || (appInfo.mPermissionCfg & permissionType) <= 0) ? 1 : 2;
        } else {
            pa = pas.getApp(Integer.toString(appInfo.mAppUid));
            if (pa == null) {
                HwLog.w(LOG_TAG, "getPermissionStatus, pa null.");
                return 1;
            }
            if (!pa.areRuntimePermissionsGranted()) {
                i = 2;
            }
            return i;
        }
    }
}
