package com.huawei.systemmanager.rainbow.client.connect.request;

import android.content.Context;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.rainbow.client.helper.ServerRequestHelper;
import com.huawei.systemmanager.rainbow.comm.request.AbsServerRequest;
import com.huawei.systemmanager.rainbow.comm.request.ICommonRequest.RequestType;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic;
import com.huawei.systemmanager.util.HwLog;
import org.json.JSONException;
import org.json.JSONObject;

public class PostDeviceTokenRequest extends AbsServerRequest {
    private static final String TAG = "PostDeviceTokenRequest";
    private String mDeviceToken = null;

    public PostDeviceTokenRequest(String deviceToken) {
        this.mDeviceToken = deviceToken;
    }

    protected String getRequestUrl(RequestType type) {
        return RainbowRequestBasic.getUrlForCommon("v2/registerDT.do");
    }

    protected void addExtPostRequestParam(Context ctx, JSONObject param) {
        try {
            if (this.mDeviceToken != null) {
                HwLog.d(TAG, "mDeviceToken length " + this.mDeviceToken.length());
                param.put("deviceToken", this.mDeviceToken);
                return;
            }
            HwLog.e(TAG, "mDeviceToken is null");
        } catch (JSONException ex) {
            HwLog.e(TAG, "addExtPostRequestParam catch JSONException: " + ex.getMessage());
        }
    }

    protected int checkResponseCode(Context ctx, int resultCode) {
        if (resultCode == 0) {
            Utility.setTokenRegistered(ctx, true);
        }
        return ServerRequestHelper.checkServerResponseCode(resultCode);
    }

    protected void parseResponseAndPost(Context ctx, JSONObject jsonResponse) {
        HwLog.d(TAG, "parseResponseAndPost");
    }
}
