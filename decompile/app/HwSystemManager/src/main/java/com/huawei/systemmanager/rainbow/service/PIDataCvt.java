package com.huawei.systemmanager.rainbow.service;

import android.content.Context;
import android.util.Log;
import com.google.common.base.Joiner;
import com.huawei.permissionmanager.db.RecommendDBHelper;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderConst;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderConst.RecommendCallMethod;
import com.huawei.systemmanager.rainbow.comm.meta.CloudMetaMgr;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendItem;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendParamException;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendQueryInput;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendQueryOutput;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

class PIDataCvt {
    private static final String TAG = PIDataCvt.class.getSimpleName();

    PIDataCvt() {
    }

    static PermissionRecommendInfo cvtToRecommendInfo(Context ctx, String pkgName) {
        PermissionRecommendInfo recInfo = new PermissionRecommendInfo();
        cvtRecommendPermission(ctx, pkgName, recInfo);
        cvtRecommendStartup(ctx, pkgName, recInfo);
        recInfo.mInitRecommendStatus = RecommendDBHelper.getInstance(ctx).getRecommendPackageInstallStatusForServiceProcess();
        if (Log.HWINFO) {
            HwLog.i(TAG, "cvtToRecommendInfo result for pkg[" + pkgName + "] are: " + recInfo);
        }
        return recInfo;
    }

    private static void cvtRecommendPermission(Context ctx, String pkgName, PermissionRecommendInfo recInfo) {
        cvtFromRecommendProvider(ctx, pkgName, 6, recInfo);
    }

    private static void cvtRecommendStartup(Context ctx, String pkgName, PermissionRecommendInfo recInfo) {
        cvtFromRecommendProvider(ctx, pkgName, 4, recInfo);
    }

    private static void cvtFromRecommendProvider(Context ctx, String pkgName, int businessId, PermissionRecommendInfo recInfo) {
        try {
            List<RecommendItem> items = (List) RecommendQueryOutput.fromBundle(ctx.getContentResolver().call(CloudProviderConst.CLOUD_AUTHORITY_URI, RecommendCallMethod.CALL_METHOD_QUERY_RECOMMEND, null, RecommendQueryInput.generateOnePkgMultiItemInput(businessId, pkgName, null))).get(pkgName);
            if (items != null && !items.isEmpty()) {
                for (RecommendItem item : items) {
                    recInfo.mRecommendInfoList.add(Joiner.on(":").join(CloudMetaMgr.getPIKey(item.getConfigItemId()), Integer.valueOf(CloudMetaMgr.getPIType(item.getConfigType())), Integer.valueOf(item.getPercentage())));
                }
                HwLog.d(TAG, "cvtFromRecommendProvider: " + recInfo.mRecommendInfoList);
            }
        } catch (RecommendParamException ex) {
            HwLog.e(TAG, "cvtFromRecommendProvider catch RecommendParamException: " + ex.getMessage() + ", forBusiness: " + businessId);
        } catch (Exception ex2) {
            HwLog.e(TAG, "cvtFromRecommendProvider catch RecommendParamException: " + ex2.getMessage() + ", forBusiness: " + businessId);
        }
    }
}
