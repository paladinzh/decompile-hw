package com.huawei.systemmanager.rainbow.client.connect.request;

import android.content.Context;
import com.google.common.base.Strings;
import com.huawei.systemmanager.rainbow.client.base.GetAppListBasic;
import com.huawei.systemmanager.rainbow.client.connect.result.ClientServerSync;
import com.huawei.systemmanager.rainbow.client.connect.result.WhiteBlackListInfo;
import com.huawei.systemmanager.rainbow.client.helper.LocalSharedPrefrenceHelper;
import com.huawei.systemmanager.rainbow.client.helper.ServerRequestHelper;
import com.huawei.systemmanager.rainbow.comm.request.AbsServerRequest;
import com.huawei.systemmanager.rainbow.comm.request.ICommonRequest.RequestType;
import com.huawei.systemmanager.util.HwLog;
import org.json.JSONException;
import org.json.JSONObject;

public class GetAppListRequestHelper extends AbsServerRequest {
    private static final String TAG = "GetAppListRequestHelper";
    private String mFieldKey = null;
    private int mListType = 0;

    public GetAppListRequestHelper(String fieldKey, int listType) {
        this.mFieldKey = fieldKey;
        this.mListType = listType;
    }

    protected boolean shouldRun(Context ctx) {
        if (ClientServerSync.getVersion(this.mFieldKey) > new LocalSharedPrefrenceHelper(ctx).getLong(this.mFieldKey, 0)) {
            return true;
        }
        return false;
    }

    protected String getRequestUrl(RequestType type) {
        return ClientServerSync.getUrl(this.mFieldKey);
    }

    protected void addExtPostRequestParam(Context ctx, JSONObject param) {
        try {
            String currentVersion = getAppListVersionByType(ctx, this.mListType);
            String listType = Integer.toString(this.mListType);
            param.put("blVer", currentVersion);
            param.put("blType", formatListType(listType));
        } catch (JSONException ex) {
            HwLog.e(TAG, "addcheckVersionForPostPara catch JSONException: " + ex.getMessage());
        }
    }

    protected int checkResponseCode(Context ctx, int resultCode) {
        return ServerRequestHelper.checkServerResponseCode(resultCode);
    }

    protected void parseResponseAndPost(Context ctx, JSONObject jsonResponse) throws JSONException {
        new WhiteBlackListInfo(this.mListType).parseAndUpdate(ctx, jsonResponse);
    }

    private String formatListType(String listType) {
        return Strings.padStart(Strings.nullToEmpty(listType), 4, '0');
    }

    private String getAppListVersionByType(Context ctx, int listType) {
        return Long.toString(new LocalSharedPrefrenceHelper(ctx).getLong((String) GetAppListBasic.getBlackWhiteMaps().get(listType), 0));
    }
}
