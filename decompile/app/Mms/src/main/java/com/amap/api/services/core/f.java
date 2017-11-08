package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.cloud.CloudItem;
import com.amap.api.services.cloud.CloudItemDetail;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: CloudSearchIdHandler */
public class f extends e<z, CloudItemDetail> {
    public /* synthetic */ Object a(String str) throws AMapException {
        return e(str);
    }

    public f(Context context, z zVar) {
        super(context, zVar);
    }

    public String g() {
        return h.b() + "/datasearch/id?";
    }

    public CloudItemDetail e(String str) throws AMapException {
        if (str == null || str.equals("")) {
            return null;
        }
        CloudItemDetail b;
        try {
            b = b(new JSONObject(str));
        } catch (JSONException e) {
            e.printStackTrace();
            b = null;
        } catch (Exception e2) {
            e2.printStackTrace();
            b = null;
        }
        return b;
    }

    private CloudItemDetail b(JSONObject jSONObject) throws JSONException {
        if (jSONObject == null || !jSONObject.has("datas")) {
            return null;
        }
        JSONArray optJSONArray = jSONObject.optJSONArray("datas");
        if (optJSONArray.length() <= 0) {
            return null;
        }
        JSONObject jSONObject2 = optJSONArray.getJSONObject(0);
        CloudItem a = a(jSONObject2);
        a(a, jSONObject2);
        return a;
    }

    protected String e() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("key=" + aj.f(this.d));
        stringBuilder.append("&tableid=" + ((z) this.a).a);
        stringBuilder.append("&output=json");
        stringBuilder.append("&_id=" + ((z) this.a).b);
        return stringBuilder.toString();
    }
}
