package com.huawei.systemmanager.adblock.ui.connect.result;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.systemmanager.adblock.comm.AdBlock;
import com.huawei.systemmanager.adblock.comm.AdConst.CloudResult;
import com.huawei.systemmanager.adblock.comm.AdUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AdBlockResultHandle implements CloudResult {
    private static final String TAG = "AdBlockResultHandle";

    public void handleResult(Context context, JSONObject jsonResponse, List<AdBlock> adBlocks, int updateType) {
        if (jsonResponse == null) {
            HwLog.e(TAG, "handleResult jsonResponse is null");
            return;
        }
        try {
            int rtnCode = jsonResponse.getInt(CloudResult.AD_RESULT_RTNCODE);
            if (rtnCode != 0) {
                HwLog.w(TAG, "handleResult rtnCode is not ok=" + rtnCode);
                return;
            }
            if (jsonResponse.has(CloudResult.AD_RESULT_ADSTRATEGIES)) {
                handleAdUrl(adBlocks, jsonResponse);
            }
            if (jsonResponse.has(CloudResult.AD_RESULT_VIEWSTRATEGIES)) {
                handleAdView(adBlocks, jsonResponse);
            }
            if (jsonResponse.has(CloudResult.AD_RESULT_DLSTRATEGIES)) {
                handleAdApkDownload(adBlocks, jsonResponse);
            }
            commit(context, adBlocks, updateType);
        } catch (JSONException e) {
            HwLog.w(TAG, "handleResult JSONException", e);
        }
    }

    private void handleAdUrl(List<AdBlock> adBlocks, JSONObject jsonResponse) {
        try {
            JSONArray array = jsonResponse.getJSONArray(CloudResult.AD_RESULT_ADSTRATEGIES);
            if (array == null || array.length() == 0) {
                HwLog.i(TAG, "handleAdUrl array is empty");
                return;
            }
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String pkg = obj.getString("pkg");
                JSONArray urls = obj.getJSONArray("url");
                int isUseTencent = obj.getInt(CloudResult.AD_USE_TENCENT);
                if (TextUtils.isEmpty(pkg) || urls.length() <= 0) {
                    HwLog.w(TAG, "handleAdUrl pkg or url is empty");
                } else {
                    AdBlock adBlock = AdBlock.getByPkgName(adBlocks, pkg);
                    if (adBlock == null) {
                        HwLog.w(TAG, "handleAdUrl adBlock is null, pkg=" + pkg);
                        return;
                    }
                    if (urls.toString().equals("[\"null\"]") || urls.toString().equals("[\"\"]")) {
                        adBlock.setUrls("");
                    } else {
                        adBlock.setUrls(urls.toString());
                    }
                    adBlock.setUseTencent(isUseTencent);
                }
            }
        } catch (JSONException e) {
            HwLog.e(TAG, "handleAdUrl JSONException", e);
        } catch (RuntimeException e2) {
            HwLog.e(TAG, "handleAdUrl RuntimeException", e2);
        }
    }

    private void handleAdView(List<AdBlock> adBlocks, JSONObject jsonResponse) {
        try {
            JSONArray array = jsonResponse.getJSONArray(CloudResult.AD_RESULT_VIEWSTRATEGIES);
            if (array == null || array.length() == 0) {
                HwLog.i(TAG, "handleAdView array is empty");
                return;
            }
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String pkg = obj.getString("pkg");
                if (TextUtils.isEmpty(pkg)) {
                    HwLog.e(TAG, "handleAdView pkg is empty");
                } else {
                    AdBlock adBlock = AdBlock.getByPkgName(adBlocks, pkg);
                    if (adBlock == null) {
                        HwLog.e(TAG, "handleAdView adBlock is null, pkg=" + pkg);
                        return;
                    }
                    if (obj.has("views")) {
                        JSONArray views = obj.getJSONArray("views");
                        if (views.length() > 0) {
                            adBlock.setViews(views.toString());
                        }
                    }
                    if (obj.has(CloudResult.AD_RESULT_VIEW_ID)) {
                        JSONArray viewids = obj.getJSONArray(CloudResult.AD_RESULT_VIEW_ID);
                        if (viewids.length() > 0) {
                            adBlock.setViewIds(viewids.toString());
                        }
                    }
                }
            }
        } catch (JSONException e) {
            HwLog.e(TAG, "handleAdView JSONException", e);
        } catch (RuntimeException e2) {
            HwLog.e(TAG, "handleAdView RuntimeException", e2);
        }
    }

    private void handleAdApkDownload(List<AdBlock> adBlocks, JSONObject jsonResponse) {
        try {
            JSONArray array = jsonResponse.getJSONArray(CloudResult.AD_RESULT_DLSTRATEGIES);
            if (array == null || array.length() == 0) {
                HwLog.i(TAG, "handleAdApkDownload array is empty");
                return;
            }
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String pkg = obj.getString("pkg");
                int type = obj.getInt("type");
                if (TextUtils.isEmpty(pkg)) {
                    HwLog.e(TAG, "handleAdApkDownload pkg is empty");
                } else if (2 == type) {
                    AdBlock adBlock = AdBlock.getByPkgName(adBlocks, pkg);
                    if (adBlock != null) {
                        adBlock.setDlCheck(true);
                    } else {
                        HwLog.w(TAG, "handleAdApkDownload adBlock is null");
                    }
                } else {
                    continue;
                }
            }
        } catch (JSONException e) {
            HwLog.e(TAG, "handleAdApkDownload JSONException", e);
        } catch (RuntimeException e2) {
            HwLog.e(TAG, "handleAdApkDownload RuntimeException", e2);
        }
    }

    private void commit(Context context, List<AdBlock> adBlocks, int updateType) {
        List<String> dispatchList = new ArrayList();
        List<String> deleteList = new ArrayList();
        for (AdBlock adBlock : adBlocks) {
            try {
                if (adBlock.isValid() && adBlock.isPackageInstalled(context)) {
                    HwLog.i(TAG, "commit adBlock pkg:" + adBlock.getPkgName());
                    adBlock.saveOrUpdate(context, true);
                    dispatchList.add(adBlock.getPkgName());
                } else {
                    deleteList.add(adBlock.getPkgName());
                }
            } catch (RuntimeException e) {
                HwLog.w(TAG, "commit RuntimeException pkg:" + adBlock.getPkgName(), e);
            }
        }
        if (!deleteList.isEmpty()) {
            try {
                AdBlock.deleteByPackages(context, deleteList);
            } catch (RuntimeException e2) {
                HwLog.w(TAG, "commit deleteByPackages RuntimeException", e2);
            }
        }
        if (2 == updateType) {
            AdUtils.dispatchAll(context);
        } else {
            AdUtils.dispatchPart(context, dispatchList);
        }
    }
}
