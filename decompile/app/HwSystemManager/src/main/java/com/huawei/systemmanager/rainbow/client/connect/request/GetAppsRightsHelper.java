package com.huawei.systemmanager.rainbow.client.connect.request;

import android.content.Context;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys.CloudReqVerSpfKeys;
import com.huawei.systemmanager.rainbow.client.connect.result.AppPermissions;
import com.huawei.systemmanager.rainbow.client.connect.result.ClientServerSync;
import com.huawei.systemmanager.rainbow.client.helper.LocalSharedPrefrenceHelper;
import com.huawei.systemmanager.rainbow.client.helper.ServerRequestHelper;
import com.huawei.systemmanager.rainbow.comm.request.AbsServerRequest;
import com.huawei.systemmanager.rainbow.comm.request.ICommonRequest.RequestType;
import com.huawei.systemmanager.util.HwLog;
import org.json.JSONException;
import org.json.JSONObject;

public class GetAppsRightsHelper extends AbsServerRequest {
    private static final String TAG = "GetAppsRightsHelper";

    protected boolean shouldRun(Context ctx) {
        if (new LocalSharedPrefrenceHelper(ctx).getLong(CloudReqVerSpfKeys.RIGHT_LIST_VERSION_SPF, 0) < ClientServerSync.getVersion("right")) {
            return true;
        }
        HwLog.d(TAG, "No need update for getAppsPermissionRights!");
        return false;
    }

    protected String getRequestUrl(RequestType type) {
        return ClientServerSync.getUrl("right");
    }

    protected void addExtPostRequestParam(Context ctx, JSONObject param) {
        try {
            param.put("rlVer", Long.toString(new LocalSharedPrefrenceHelper(ctx).getLong(CloudReqVerSpfKeys.RIGHT_LIST_VERSION_SPF, 0)));
            param.put("IncSupport", "1");
        } catch (JSONException ex) {
            HwLog.e(TAG, "addcheckVersionForPostPara catch JSONException: " + ex.getMessage());
        }
    }

    protected int checkResponseCode(Context ctx, int resultCode) {
        return ServerRequestHelper.checkServerResponseCode(resultCode);
    }

    protected void parseResponseAndPost(Context ctx, JSONObject jsonResponse) throws JSONException {
        new AppPermissions().parseAndUpdate(ctx, jsonResponse);
    }
}
