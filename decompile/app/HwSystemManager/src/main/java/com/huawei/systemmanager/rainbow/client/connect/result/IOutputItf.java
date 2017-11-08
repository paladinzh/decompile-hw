package com.huawei.systemmanager.rainbow.client.connect.result;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public interface IOutputItf<T> {
    void parseAndUpdate(Context context, JSONObject jSONObject) throws JSONException;
}
