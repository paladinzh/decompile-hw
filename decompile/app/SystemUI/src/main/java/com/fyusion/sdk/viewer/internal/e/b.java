package com.fyusion.sdk.viewer.internal.e;

import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
public class b extends a<JSONObject> {
    /* synthetic */ Object a(String str) {
        return b(str);
    }

    JSONObject b(String str) {
        try {
            return new JSONObject(str);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
