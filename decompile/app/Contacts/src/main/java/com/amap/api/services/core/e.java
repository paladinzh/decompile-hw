package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.cloud.CloudImage;
import com.amap.api.services.cloud.CloudItem;
import com.amap.api.services.cloud.CloudItemDetail;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: CloudHandler */
public abstract class e<T, V> extends b<T, V> {
    public e(Context context, T t) {
        super(context, t);
    }

    protected CloudItemDetail a(JSONObject jSONObject) throws JSONException {
        CloudItemDetail cloudItemDetail = new CloudItemDetail(n.a(jSONObject, "_id"), n.b(jSONObject, "_location"), n.a(jSONObject, "_name"), n.a(jSONObject, "_address"));
        cloudItemDetail.setCreatetime(n.a(jSONObject, "_createtime"));
        cloudItemDetail.setUpdatetime(n.a(jSONObject, "_updatetime"));
        if (jSONObject.has("_distance")) {
            String optString = jSONObject.optString("_distance");
            if (!d(optString)) {
                cloudItemDetail.setDistance(Integer.parseInt(optString));
            }
        }
        List arrayList = new ArrayList();
        JSONArray optJSONArray = jSONObject.optJSONArray("_image");
        if (optJSONArray == null || optJSONArray.length() == 0) {
            cloudItemDetail.setmCloudImage(arrayList);
            return cloudItemDetail;
        }
        for (int i = 0; i < optJSONArray.length(); i++) {
            JSONObject jSONObject2 = optJSONArray.getJSONObject(i);
            arrayList.add(new CloudImage(n.a(jSONObject2, "_id"), n.a(jSONObject2, "_preurl"), n.a(jSONObject2, "_url")));
        }
        cloudItemDetail.setmCloudImage(arrayList);
        return cloudItemDetail;
    }

    public Map<String, String> c() {
        Map<String, String> hashMap = new HashMap();
        hashMap.put("Content-Type", "application/x-www-form-urlencoded");
        hashMap.put("Accept-Encoding", "gzip");
        hashMap.put("User-Agent", "AMAP SDK Android Search 3.2.1");
        hashMap.put("X-INFO", al.a(this.d, h.b(false), null, false));
        hashMap.put("platinfo", String.format("platform=Android&sdkversion=%s&product=%s", new Object[]{"3.2.1", "cloud"}));
        hashMap.put("logversion", "2.1");
        return hashMap;
    }

    protected void a(CloudItem cloudItem, JSONObject jSONObject) {
        Iterator keys = jSONObject.keys();
        HashMap hashMap = new HashMap();
        if (keys != null) {
            while (keys.hasNext()) {
                Object next = keys.next();
                if (!(next == null || next.toString().startsWith("_"))) {
                    hashMap.put(next.toString(), jSONObject.optString(next.toString()));
                }
            }
            cloudItem.setCustomfield(hashMap);
        }
    }

    protected boolean d(String str) {
        if (str == null || str.equals("") || str.equals("[]")) {
            return true;
        }
        return false;
    }
}
