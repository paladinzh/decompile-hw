package com.huawei.systemmanager.rainbow.client.connect;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.power.comm.ActionConst;
import com.huawei.systemmanager.rainbow.client.connect.request.CheckVersionRequest;
import com.huawei.systemmanager.rainbow.client.connect.request.DownloadConfigFileRequest;
import com.huawei.systemmanager.rainbow.client.connect.request.GetAppListRequestHelper;
import com.huawei.systemmanager.rainbow.client.connect.request.GetAppsRightsHelper;
import com.huawei.systemmanager.rainbow.client.connect.request.GetFileVersionRequest;
import com.huawei.systemmanager.rainbow.client.connect.request.GetMessageSafeRequestHelper;
import com.huawei.systemmanager.rainbow.client.connect.request.PostDeviceTokenRequest;
import com.huawei.systemmanager.rainbow.client.connect.request.RecommendMultiPkgRequest;
import com.huawei.systemmanager.rainbow.client.connect.request.RecommendSinglePkgRequest;
import com.huawei.systemmanager.rainbow.client.helper.LocalSharedPrefrenceHelper;
import com.huawei.systemmanager.rainbow.client.util.OperationLocal;
import com.huawei.systemmanager.rainbow.comm.request.AbsRequest;
import com.huawei.systemmanager.rainbow.comm.request.AbsRequestGroup;
import com.huawei.systemmanager.rainbow.comm.request.GroupRequestPolicy.FailRequestPolicy;

public class RequestMgr {
    public static AbsRequest generateCloudRequest(Context ctx) {
        AbsRequestGroup rootGroup = new AbsRequestGroup(FailRequestPolicy.RETURN_WHEN_FAILED);
        rootGroup.addRequest(generateCheckVersion(ctx));
        rootGroup.addRequest(generateBusinessGroup(ctx));
        return rootGroup;
    }

    public static AbsRequest generateMultiRecommendRequest(Context ctx) {
        return RecommendMultiPkgRequest.generateRequestGroup(ctx);
    }

    public static AbsRequest generateSingleRecommendRequest(Context ctx, String pkgName) {
        AbsRequestGroup rootGroup = new AbsRequestGroup(FailRequestPolicy.RETURN_WHEN_FAILED);
        rootGroup.addRequest(generateCheckVersion(ctx));
        rootGroup.addRequest(new RecommendSinglePkgRequest(pkgName));
        return rootGroup;
    }

    public static boolean generateCloudRequestForFile(Context context) {
        String fileID = OperationLocal.getFileID();
        LocalSharedPrefrenceHelper sharedService = new LocalSharedPrefrenceHelper(context);
        GetFileVersionRequest request = new GetFileVersionRequest(fileID, sharedService.getString(fileID, "0"));
        request.processRequest(context);
        if (!request.canDownload()) {
            return false;
        }
        DownloadConfigFileRequest downloadRequest = new DownloadConfigFileRequest(request.getDownloadUrl(), request.getServerVer(), request.getSignature());
        downloadRequest.processRequest(context);
        if (!downloadRequest.isDownloadSuccess()) {
            return false;
        }
        sendBroadcastToTarget(context);
        sharedService.putString(fileID, request.getServerVer());
        return true;
    }

    private static void sendBroadcastToTarget(Context context) {
        context.sendBroadcast(new Intent(ActionConst.INTENT_UPDATE_FILES), "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
    }

    private static AbsRequest generateCheckVersion(Context ctx) {
        return new CheckVersionRequest();
    }

    private static AbsRequest generateBusinessGroup(Context ctx) {
        AbsRequestGroup businessGroup = new AbsRequestGroup(FailRequestPolicy.CONTINUE_WHEN_FAILED);
        businessGroup.addRequest(generatePermissionRight(ctx));
        businessGroup.addRequest(generateAppListGroup(ctx));
        return businessGroup;
    }

    private static AbsRequest generatePermissionRight(Context ctx) {
        return new GetAppsRightsHelper();
    }

    private static AbsRequest generateAppListGroup(Context ctx) {
        AbsRequestGroup appListGroup = new AbsRequestGroup(FailRequestPolicy.CONTINUE_WHEN_FAILED);
        appListGroup.addRequest(new GetAppListRequestHelper("wbList_0009", 9));
        appListGroup.addRequest(new GetAppListRequestHelper("wbList_0010", 10));
        appListGroup.addRequest(new GetAppListRequestHelper("v2_0005", 35));
        appListGroup.addRequest(new GetAppListRequestHelper("wbList_0011", 11));
        appListGroup.addRequest(new GetAppListRequestHelper("wbList_0030", 30));
        appListGroup.addRequest(new GetAppListRequestHelper("dozeVer", 31));
        appListGroup.addRequest(new GetAppListRequestHelper("startupVer", 32));
        appListGroup.addRequest(new GetAppListRequestHelper("notificationVer", 33));
        appListGroup.addRequest(new GetAppListRequestHelper("wbList_0034", 34));
        appListGroup.addRequest(new GetMessageSafeRequestHelper("messageSafe", 36));
        return appListGroup;
    }

    public static AbsRequest generateDeviceTokenRequest(Context ctx, String token) {
        AbsRequestGroup rootGroup = new AbsRequestGroup(FailRequestPolicy.RETURN_WHEN_FAILED);
        rootGroup.addRequest(new PostDeviceTokenRequest(token));
        return rootGroup;
    }
}
