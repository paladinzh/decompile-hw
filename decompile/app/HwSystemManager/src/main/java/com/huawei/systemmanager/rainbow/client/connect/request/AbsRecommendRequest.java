package com.huawei.systemmanager.rainbow.client.connect.request;

import android.content.ContentValues;
import android.content.Context;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.rainbow.client.connect.result.ClientServerSync;
import com.huawei.systemmanager.rainbow.client.helper.ServerRequestHelper;
import com.huawei.systemmanager.rainbow.comm.request.AbsServerRequest;
import com.huawei.systemmanager.rainbow.comm.request.ICommonRequest.RequestType;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendCvtUtils;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbsRecommendRequest extends AbsServerRequest {
    private static final String TAG = "AbsRecommendRequest";

    protected abstract Map<String, String> getRequestPkgVerMap(Context context);

    protected String getRequestUrl(RequestType type) {
        return ClientServerSync.getUrl("recRight");
    }

    protected void addExtPostRequestParam(Context ctx, JSONObject param) {
        try {
            JSONArray jarArray = new JSONArray();
            for (Entry<String, String> entry : getRequestPkgVerMap(ctx).entrySet()) {
                JSONObject item = new JSONObject();
                item.put("aPN", entry.getKey());
                item.put("arV", entry.getValue());
                jarArray.put(item);
            }
            param.put("apL", jarArray);
        } catch (JSONException ex) {
            HwLog.e(TAG, "addExtPostRequestParam catch JSONException: " + ex.getMessage());
        }
    }

    protected int checkResponseCode(Context ctx, int resultCode) {
        return ServerRequestHelper.checkServerResponseCode(resultCode);
    }

    protected void parseResponseAndPost(Context ctx, JSONObject jsonResponse) {
        HwLog.d(TAG, "parseResponseAndPost");
        try {
            JSONArray array = jsonResponse.getJSONArray("arList");
            if (array != null && array.length() > 0) {
                for (int i = 0; i < array.length(); i++) {
                    parseOnePackage(ctx, array.getJSONObject(i));
                }
            }
        } catch (JSONException ex) {
            HwLog.e(TAG, "parseResponseAndPost catch JSONException: " + ex.getMessage());
        }
    }

    private void parseOnePackage(Context ctx, JSONObject obj) throws JSONException {
        String pkgName = obj.getString("aPN");
        JSONArray rightList = obj.getJSONArray("rL");
        List<String> rList = Lists.newArrayList();
        if (rightList != null) {
            for (int i = 0; i < rightList.length(); i++) {
                rList.add(rightList.getString(i));
            }
        }
        List<ContentValues> data = RecommendCvtUtils.cvtCloudDataToContentValues(pkgName, rList, obj.getString("arV"));
        if (data.isEmpty()) {
            HwLog.w(TAG, "parseOnePackage empty result, don't insert database! " + obj.toString());
        } else {
            RecommendHelper.bulkInsertRecommendData(ctx, data);
        }
    }
}
