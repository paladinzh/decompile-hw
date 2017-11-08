package com.huawei.systemmanager.rainbow.client.connect.request;

import android.content.Context;
import com.huawei.systemmanager.rainbow.client.connect.result.MessageSafeInfo;
import org.json.JSONException;
import org.json.JSONObject;

public class GetMessageSafeRequestHelper extends GetAppListRequestHelper {
    private static final String TAG = "GetMessageSafeRequestHelper";
    private int mListType = 0;

    public GetMessageSafeRequestHelper(String fieldKey, int listType) {
        super(fieldKey, listType);
        this.mListType = listType;
    }

    protected void parseResponseAndPost(Context ctx, JSONObject jsonResponse) throws JSONException {
        new MessageSafeInfo(this.mListType).parseAndUpdate(ctx, jsonResponse);
    }
}
