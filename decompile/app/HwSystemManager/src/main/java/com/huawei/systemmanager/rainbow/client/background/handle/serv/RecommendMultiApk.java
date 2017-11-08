package com.huawei.systemmanager.rainbow.client.background.handle.serv;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.rainbow.client.background.handle.IIntentHandler;
import com.huawei.systemmanager.rainbow.client.base.ClientConstant.CloudActions;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys;
import com.huawei.systemmanager.rainbow.client.connect.RequestMgr;
import com.huawei.systemmanager.rainbow.client.helper.LocalSharedPrefrenceHelper;
import com.huawei.systemmanager.rainbow.client.helper.ServerRequestHelper;
import com.huawei.systemmanager.rainbow.comm.request.AbsRequest;
import com.huawei.systemmanager.util.HwLog;

public class RecommendMultiApk implements IIntentHandler {
    private static final String TAG = "RecommendMultiApk";

    public void handleIntent(Context ctx, Intent intent) {
        if (AbroadUtils.isAbroad()) {
            HwLog.e(TAG, "The cloud is not enabled");
        } else if (intent != null && CloudActions.INTENT_CLOUD_RECOMMEND_MULTI_APK.equals(intent.getAction())) {
            handleMultiApkRequest(ctx, intent);
        }
    }

    private void handleMultiApkRequest(Context ctx, Intent intent) {
        if (ServerRequestHelper.shouldDoRequest(ctx)) {
            if (System.currentTimeMillis() - new LocalSharedPrefrenceHelper(ctx).getLong(CloudSpfKeys.LAST_RECOMMEND_MULTI_APK_SUCCESS_TIME, 0) < 3600000) {
                HwLog.i(TAG, "handleMultiApkRequest: Should update later, skip");
                return;
            }
            HwLog.i(TAG, "handleMultiApkRequest: Start");
            try {
                dealPostRequest(ctx);
            } catch (Exception e) {
                HwLog.e(TAG, "handleMultiApkRequest: Exception " + e.toString());
            }
        }
    }

    private void dealPostRequest(Context ctx) {
        AbsRequest request = RequestMgr.generateMultiRecommendRequest(ctx);
        if (request != null) {
            boolean result = request.processRequest(ctx);
            HwLog.d(TAG, "dealPostRequest: result : " + result);
            changeUpdateFlags(ctx, result);
        }
    }

    private void changeUpdateFlags(Context ctx, boolean requestResult) {
        if (requestResult) {
            new LocalSharedPrefrenceHelper(ctx).putLong(CloudSpfKeys.LAST_RECOMMEND_MULTI_APK_SUCCESS_TIME, System.currentTimeMillis());
        }
    }
}
