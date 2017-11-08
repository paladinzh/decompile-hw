package com.huawei.systemmanager.rainbow.client.connect.request;

import android.content.Context;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys.CloudReqVerSpfKeys;
import com.huawei.systemmanager.rainbow.client.connect.result.AllCheckVersionConfigs;
import com.huawei.systemmanager.rainbow.client.helper.LocalSharedPrefrenceHelper;
import com.huawei.systemmanager.rainbow.client.helper.ServerRequestHelper;
import com.huawei.systemmanager.rainbow.comm.request.AbsServerRequest;
import com.huawei.systemmanager.rainbow.comm.request.ICommonRequest.RequestType;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic;
import com.huawei.systemmanager.util.HwLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CheckVersionRequest extends AbsServerRequest {
    private static final String TAG = "CheckVersionRequest";

    protected String getRequestUrl(RequestType type) {
        return RainbowRequestBasic.getUrlForCommon("checkVersion.do");
    }

    protected void addExtPostRequestParam(Context ctx, JSONObject param) {
        addcheckVersionForPostPara(ctx, param);
    }

    protected int checkResponseCode(Context ctx, int resultCode) {
        return ServerRequestHelper.checkServerResponseCode(resultCode);
    }

    protected void parseResponseAndPost(Context ctx, JSONObject jsonResponse) throws JSONException {
        new AllCheckVersionConfigs().parseAndUpdate(ctx, jsonResponse);
    }

    private void addcheckVersionForPostPara(Context ctx, JSONObject param) {
        try {
            LocalSharedPrefrenceHelper sharedService = new LocalSharedPrefrenceHelper(ctx);
            JSONArray jsonArray = new JSONArray();
            checkVersionJSONObj(jsonArray, "right", Long.toString(sharedService.getLong(CloudReqVerSpfKeys.RIGHT_LIST_VERSION_SPF, 0)));
            checkVersionJSONObj(jsonArray, "wbList_0009", Long.toString(sharedService.getLong(CloudReqVerSpfKeys.CONTROL_BLACK_LIST_VERSION_SPF, 0)));
            checkVersionJSONObj(jsonArray, "wbList_0010", Long.toString(sharedService.getLong(CloudReqVerSpfKeys.CONTROL_WHITE_LIST_VERSION_SPF, 0)));
            checkVersionJSONObj(jsonArray, "v2_0005", Long.toString(sharedService.getLong(CloudReqVerSpfKeys.BACKGROUND_LIST_VERSION_SPF, 0)));
            checkVersionJSONObj(jsonArray, "wbList_0011", Long.toString(sharedService.getLong(CloudReqVerSpfKeys.PUSH_LIST_VERSION_SPF, 0)));
            checkVersionJSONObj(jsonArray, "wbList_0030", Long.toString(sharedService.getLong(CloudReqVerSpfKeys.PHONE_LIST_VERSION_SPF, 0)));
            checkVersionJSONObj(jsonArray, "recRight", Long.toString(sharedService.getLong(CloudReqVerSpfKeys.RECOMMEND_RIGHTS_SPF, 0)));
            checkVersionJSONObj(jsonArray, "dozeVer", Long.toString(sharedService.getLong(CloudReqVerSpfKeys.UNIFIED_POWER_APPS_SPF, 0)));
            checkVersionJSONObj(jsonArray, "startupVer", Long.toString(sharedService.getLong(CloudReqVerSpfKeys.STARTUP_SPF, 0)));
            checkVersionJSONObj(jsonArray, "notificationVer", Long.toString(sharedService.getLong(CloudReqVerSpfKeys.NOTIFICATION_SPF, 0)));
            checkVersionJSONObj(jsonArray, "wbList_0034", Long.toString(sharedService.getLong(CloudReqVerSpfKeys.COMPETITOR_SPF, 0)));
            checkVersionJSONObj(jsonArray, "messageSafe", Long.toString(sharedService.getLong(CloudReqVerSpfKeys.MESSAGE_SAFE_SPF, 0)));
            if (jsonArray.length() > 0) {
                param.put("components", jsonArray.toString());
            }
        } catch (JSONException ex) {
            HwLog.e(TAG, "addcheckVersionForPostPara catch JSONException: " + ex.getMessage());
        }
    }

    private void checkVersionJSONObj(JSONArray jsonArray, String itemName, String itemValue) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", itemName);
        jsonObject.put("version", itemValue);
        jsonArray.put(jsonObject);
    }
}
