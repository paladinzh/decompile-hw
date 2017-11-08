package com.huawei.systemmanager.rainbow.client.background.handle.serv;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import com.google.common.base.Objects;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.rainbow.client.background.handle.IIntentHandler;
import com.huawei.systemmanager.rainbow.client.base.ClientConstant.CloudActions;
import com.huawei.systemmanager.rainbow.client.connect.RequestMgr;
import com.huawei.systemmanager.rainbow.client.util.NetWorkHelper;
import com.huawei.systemmanager.util.HwLog;

public class RecommendSingleApk implements IIntentHandler {
    private static final long MIN_INTERVAL_TIME = 30000;
    public static final String PACKAGE_NAME_KEY = "packageName";
    public static final String REQUEST_ID_KEY = "request_id";
    public static final String RESULT_KEY = "result";
    private static final String TAG = "RecommendSingleApk";
    private String mLastRequestPkg;
    private long mLastRequestTime;

    public void handleIntent(Context ctx, Intent intent) {
        if (AbroadUtils.isAbroad()) {
            HwLog.e(TAG, "The cloud is not enabled");
        } else if (ctx == null || intent == null) {
            HwLog.e(TAG, "handleIntent param is null!");
        } else {
            String pkgName = intent.getStringExtra("packageName");
            boolean result = tryFetchFromServer(ctx, pkgName);
            HwLog.i(TAG, "handleIntent end, pkg:" + pkgName + ", result:" + result);
            long requestId = intent.getLongExtra(REQUEST_ID_KEY, -1);
            if (requestId > 0) {
                Intent replyIntent = new Intent(CloudActions.ACTION_REPLY_RECOMMEND_SINGLE_APK);
                replyIntent.putExtra("packageName", pkgName);
                replyIntent.putExtra(REQUEST_ID_KEY, requestId);
                replyIntent.putExtra("result", result);
                replyIntent.setPackage(ctx.getPackageName());
                ctx.sendBroadcast(replyIntent, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
            }
        }
    }

    private boolean tryFetchFromServer(Context ctx, String pkgName) {
        if (!NetWorkHelper.isAccessNetworkAllowAndNetAvailable(ctx)) {
            HwLog.e(TAG, "handleIntent network environment invalid!");
            return false;
        } else if (!Objects.equal(this.mLastRequestPkg, pkgName) || SystemClock.uptimeMillis() - this.mLastRequestTime >= MIN_INTERVAL_TIME) {
            boolean res = RequestMgr.generateSingleRecommendRequest(ctx, pkgName).processRequest(ctx);
            if (res) {
                this.mLastRequestPkg = pkgName;
                this.mLastRequestTime = SystemClock.uptimeMillis();
            }
            return res;
        } else {
            HwLog.w(TAG, "tryFetchFromServer too frequently the same pkg");
            return false;
        }
    }
}
