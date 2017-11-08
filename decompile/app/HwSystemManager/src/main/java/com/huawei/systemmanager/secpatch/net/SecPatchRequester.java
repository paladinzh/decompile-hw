package com.huawei.systemmanager.secpatch.net;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.secpatch.common.ConstValues;
import com.huawei.systemmanager.secpatch.common.SecPatchCheckResult;
import com.huawei.systemmanager.secpatch.common.SecPatchItem;
import com.huawei.systemmanager.secpatch.common.SecPatchQueryResult;
import com.huawei.systemmanager.secpatch.db.DBAdapter;
import com.huawei.systemmanager.secpatch.util.SecPatchHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SecPatchRequester {
    public static final String TAG = "SecPatchHelper";
    private static AtomicBoolean mUpdateHistorySuccess = new AtomicBoolean(false);
    private static AtomicBoolean mUpdateRepairSuccess = new AtomicBoolean(false);

    public static void initUpdateStatus(boolean updateHistoryStatus, boolean updateRepairStatus) {
        mUpdateHistorySuccess.set(updateHistoryStatus);
        mUpdateRepairSuccess.set(updateRepairStatus);
    }

    public static boolean getCurrentUpdateHostoryStatus() {
        return mUpdateHistorySuccess.get();
    }

    public static boolean getCurrentUpdateRepairStatus() {
        return mUpdateRepairSuccess.get();
    }

    public static boolean queryAllPatch(Context context, long newAllVersion) {
        boolean updateHistoryStatus = false;
        if (context == null || newAllVersion <= SecPatchHelper.getLocalVersionByKeyName(context, ConstValues.SPF_ALL_VERSION_KEY) || mUpdateHistorySuccess.get()) {
            return false;
        }
        SecPatchQueryRequest getAllPatchRequest = new SecPatchQueryRequest("all", SecPatchHelper.getSystemVersionName(), "0");
        if (getAllPatchRequest.processRequest(context)) {
            SecPatchQueryResult result = getAllPatchRequest.getRequestResult();
            if (result.isPatchAll()) {
                SecPatchHelper.setLocalVersionWhenUpdate(context, ConstValues.SPF_ALL_VERSION_KEY, newAllVersion);
                mUpdateHistorySuccess.set(true);
                List<SecPatchItem> allSecPatchList = result.getSecPatchList();
                if (Utility.isNullOrEmptyList(allSecPatchList)) {
                    HwLog.e("SecPatchHelper", "The allSecPatchList is invalid!");
                } else {
                    DBAdapter.deleteFixedVersionListByGiven(context, allSecPatchList);
                    DBAdapter.deleteSecPatchByStatus(context, "true");
                    DBAdapter.addSecPatch(context, allSecPatchList, "true");
                    updateHistoryStatus = true;
                }
                return updateHistoryStatus;
            }
            HwLog.e("SecPatchHelper", "queryAllPatch result.mSrvCode: Failed");
            return false;
        }
        HwLog.e("SecPatchHelper", "queryAllPatch processRequest: Failed");
        return false;
    }

    public static boolean queryUpdatePatch(Context context, long newAvaVersion) {
        boolean updateRepairStatus = false;
        if (context == null || newAvaVersion <= SecPatchHelper.getLocalVersionByKeyName(context, ConstValues.SPF_AVA_VERSION_KEY) || mUpdateRepairSuccess.get()) {
            return false;
        }
        SecPatchQueryRequest getUpdatePatchRequest = new SecPatchQueryRequest(ConstValues.REQUEST_OPT_AVA, SecPatchHelper.getSystemVersionName(), "0");
        if (getUpdatePatchRequest.processRequest(context)) {
            SecPatchQueryResult result = getUpdatePatchRequest.getRequestResult();
            if (result.isPatchToBeFixed()) {
                SecPatchHelper.setLocalVersionWhenUpdate(context, ConstValues.SPF_AVA_VERSION_KEY, newAvaVersion);
                mUpdateRepairSuccess.set(true);
                List<SecPatchItem> updateSecPatchList = result.getSecPatchList();
                if (updateSecPatchList == null) {
                    HwLog.e("SecPatchHelper", "The updateSecPatchList is invalid!");
                } else if (updateSecPatchList.isEmpty()) {
                    DBAdapter.deleteSecPatchByStatus(context, "false");
                    DBAdapter.deleteAllVersionList(context);
                    updateRepairStatus = true;
                } else {
                    for (SecPatchItem patchItem : updateSecPatchList) {
                        patchItem.updatePverToFixversion();
                    }
                    DBAdapter.deleteSecPatchByStatus(context, "false");
                    DBAdapter.addSecPatch(context, updateSecPatchList, "false");
                    List<String> versionNameList = new ArrayList();
                    for (SecPatchItem patchItem2 : updateSecPatchList) {
                        String fix_version = patchItem2.mFixed_version;
                        if (!(TextUtils.isEmpty(fix_version) || versionNameList.contains(fix_version))) {
                            versionNameList.add(fix_version);
                        }
                    }
                    if (versionNameList.size() > 0) {
                        DBAdapter.deleteAllVersionList(context);
                        DBAdapter.addMoreTobeUpdateVersion(context, versionNameList);
                    }
                    updateRepairStatus = true;
                }
                return updateRepairStatus;
            }
            HwLog.e("SecPatchHelper", "queryUpdatePatch result.mSrvCode: Failed");
            return false;
        }
        HwLog.e("SecPatchHelper", "queryUpdatePatch processRequest: Failed");
        return false;
    }

    public static SecPatchCheckResult queryCheckVersion(Context context) {
        SecPatchCheckResult result = new SecPatchCheckResult();
        if (context == null) {
            return result;
        }
        SecPatchCheckRequest getCheckVersionRequest = new SecPatchCheckRequest(SecPatchHelper.getSystemVersionName());
        if (getCheckVersionRequest.processRequest(context)) {
            return getCheckVersionRequest.getRequestResult();
        }
        HwLog.e("SecPatchHelper", "queryCheckVersion processRequest: Failed");
        return result;
    }
}
