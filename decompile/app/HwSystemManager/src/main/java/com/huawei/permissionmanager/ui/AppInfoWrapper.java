package com.huawei.permissionmanager.ui;

import android.content.Context;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.huawei.permissionmanager.db.AppInfo;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.permissionmanager.model.HwAppPermissions;
import com.huawei.permissionmanager.utils.CommonFunctionUtil;
import com.huawei.permissionmanager.utils.HwPermissionInfo;
import com.huawei.permissionmanager.utils.PermissionMap;
import com.huawei.permissionmanager.utils.RecommendBaseItem;
import com.huawei.permissionmanager.utils.RecommendCfg;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderConst;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderConst.RecommendCallMethod;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendItem;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendParamException;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendQueryInput;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendQueryOutput;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* compiled from: TagViewHolder */
class AppInfoWrapper extends ListViewObject {
    private static final String LOG_TAG = "AppInfoWrapper";
    public static final int TAG_TYPE_ALL_APP_MONITOR = 2;
    public static final int TAG_TYPE_ALL_APP_TRUST = 3;
    private static final int TRUST_TAG_POSITION = 0;
    AppInfo mAppInfo;
    boolean mHasRecommendItem = false;
    private boolean mIsTag;
    private int mNumber;
    int mPermissionCount;
    SparseArray<RecommendBaseItem> mRecommendMap;
    int mRecommentItemCount = 0;
    private int mTagType;

    public AppInfoWrapper(int tagType, int appNum) {
        this.mTagType = tagType;
        this.mNumber = appNum;
        this.mIsTag = true;
    }

    public static List<AppInfoWrapper> updatePureAppInfoWrapperList(Context ctx, String reason, boolean needCount) {
        return getAllAppListIncludeTag(ctx, DBAdapter.getInstance(ctx).getShareAppList(reason), needCount);
    }

    public static List<AppInfoWrapper> updatePureAppInfoWrapperList(Context ctx, String reason) {
        return updatePureAppInfoWrapperList(ctx, reason, true);
    }

    private static List<AppInfoWrapper> getAllAppListIncludeTag(Context context, List<AppInfo> appList, boolean needCount) {
        List<AppInfoWrapper> permissonAppsList = new ArrayList();
        if (appList == null || appList.size() == 0) {
            HwLog.e(LOG_TAG, "error: appList can't be null or size is 0!");
            return permissonAppsList;
        }
        HwLog.d(LOG_TAG, "appList size + " + appList.size());
        int trustCount = 0;
        Map<String, List<RecommendItem>> result = null;
        try {
            result = RecommendQueryOutput.fromBundle(context.getContentResolver().call(CloudProviderConst.CLOUD_AUTHORITY_URI, RecommendCallMethod.CALL_METHOD_QUERY_RECOMMEND, null, RecommendQueryInput.generateInputForBusiness(6)));
        } catch (RecommendParamException e) {
            HwLog.e(LOG_TAG, "error generateInputForBusiness RecommendParamException");
        } catch (Exception e2) {
            HwLog.e(LOG_TAG, "error generateInputForBusiness Exception");
        }
        for (AppInfo appInfo : appList) {
            AppInfoWrapper appInfoWrapper = new AppInfoWrapper();
            appInfoWrapper.mAppInfo = new AppInfo(appInfo);
            appInfoWrapper.mIsTag = false;
            appInfoWrapper.mNumber = 0;
            if (needCount) {
                appInfoWrapper.mPermissionCount = HwAppPermissions.getGroupCount(context, appInfo.mPkgName);
            }
            if (1 == appInfoWrapper.mAppInfo.mTrust) {
                HwAppPermissions aps = HwAppPermissions.create(context, appInfo.mPkgName);
                if (CommonFunctionUtil.checkAppTrustStatus(context, appInfo.mPkgName, appInfo.mAppUid, aps, true)) {
                    trustCount++;
                } else {
                    appInfoWrapper.mAppInfo.mTrust = 0;
                    HwLog.i(LOG_TAG, "count trust apps, not trust by permissions are forbidden.");
                }
            }
            getRecommendInfo(appInfoWrapper, result);
            permissonAppsList.add(appInfoWrapper);
        }
        permissonAppsList.add(0, new AppInfoWrapper(3, trustCount));
        trustCount++;
        permissonAppsList.add(trustCount, new AppInfoWrapper(2, permissonAppsList.size() - trustCount));
        HwLog.v(LOG_TAG, "The permissonAppsList size is: " + permissonAppsList.size());
        return permissonAppsList;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void getRecommendInfo(AppInfoWrapper appInfoWrapper, Map<String, List<RecommendItem>> result) {
        boolean z = true;
        if (result != null && !result.isEmpty() && appInfoWrapper != null && appInfoWrapper.mAppInfo != null) {
            String pkgName = appInfoWrapper.mAppInfo.mPkgName;
            List<RecommendItem> recommendList = (List) result.get(pkgName);
            if (result.get(pkgName) != null && !recommendList.isEmpty()) {
                SparseIntArray permissionIdMap = PermissionMap.getPermissionIdMap();
                if (permissionIdMap.size() != 0) {
                    SparseArray<RecommendBaseItem> recommendMap = new SparseArray();
                    int recommendItemCount = 0;
                    for (RecommendItem recommendItem : recommendList) {
                        int permissionType = permissionIdMap.get(recommendItem.getConfigItemId());
                        int currentValue = getCurrentPermissionTypeStatus(permissionType, appInfoWrapper.mAppInfo.mPermissionCode, appInfoWrapper.mAppInfo.mPermissionCfg);
                        int recommendValue = RecommendCfg.getCfgFromRecommendVaule(recommendItem.getConfigType());
                        if (recommendValue != 0 && isCurrentPermissionTypeValid(permissionType, appInfoWrapper.mAppInfo.mRequestPermissions)) {
                            recommendMap.put(permissionType, new RecommendBaseItem(true, recommendValue, recommendItem.getPercentage()));
                            if (currentValue != recommendValue) {
                                recommendItemCount++;
                            }
                        }
                    }
                    appInfoWrapper.mRecommendMap = recommendMap;
                    if (recommendMap.size() <= 0) {
                        z = false;
                    }
                    appInfoWrapper.mHasRecommendItem = z;
                    appInfoWrapper.mRecommentItemCount = recommendItemCount;
                }
            }
        }
    }

    private static boolean isCurrentPermissionTypeValid(int permissionType, List<HwPermissionInfo> hwPermissions) {
        boolean valid = false;
        if (hwPermissions == null || hwPermissions.isEmpty()) {
            return false;
        }
        for (HwPermissionInfo info : hwPermissions) {
            if (permissionType == info.mPermissionCode) {
                valid = true;
                break;
            }
        }
        return valid;
    }

    private static int getCurrentPermissionTypeStatus(int permissionType, int permissionCode, int permissionCfg) {
        if ((permissionType & permissionCode) == 0) {
            return 0;
        }
        if ((permissionType & permissionCfg) != 0) {
            return 2;
        }
        return 1;
    }

    public boolean isTag() {
        return this.mIsTag;
    }

    public int getAppCount() {
        return this.mNumber;
    }

    public String getTagText(Context context) {
        switch (this.mTagType) {
            case 2:
                return context.getString(R.string.ListViewFirstLine_Permissionmanager_Tips02, new Object[]{Integer.valueOf(this.mNumber)});
            case 3:
                return context.getString(R.string.ListViewFirstLine_Permissionmanager_Tips01, new Object[]{Integer.valueOf(this.mNumber)});
            default:
                return "";
        }
    }

    public String getTagDescription(Context context) {
        switch (this.mTagType) {
            case 2:
                return context.getString(R.string.all_apps_monitor_none);
            case 3:
                return context.getString(R.string.all_apps_trust_none);
            default:
                return "";
        }
    }
}
