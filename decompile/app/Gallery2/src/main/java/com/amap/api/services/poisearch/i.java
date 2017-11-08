package com.amap.api.services.poisearch;

import android.content.Context;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.ServiceSettings;
import com.amap.api.services.core.c;
import com.amap.api.services.core.d;
import com.amap.api.services.core.j;
import com.amap.api.services.core.w;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: PoiSearchIdHandler */
class i extends g<String, PoiItemDetail> {
    private String h = "zh-CN";

    public /* synthetic */ Object b(String str) throws AMapException {
        return e(str);
    }

    public i(Context context, String str, String str2) {
        super(context, str);
        if ("en".equals(str2)) {
            this.h = str2;
        }
    }

    public String b() {
        return c.a() + "/place/detail?";
    }

    public PoiItemDetail e(String str) throws AMapException {
        try {
            return a(new JSONObject(str));
        } catch (Throwable e) {
            d.a(e, "PoiSearchIdHandler", "paseJSONJSONException");
            return null;
        } catch (Throwable e2) {
            d.a(e2, "PoiSearchIdHandler", "paseJSONException");
            return null;
        }
    }

    private PoiItemDetail a(JSONObject jSONObject) throws JSONException {
        if (jSONObject == null) {
            return null;
        }
        JSONArray optJSONArray = jSONObject.optJSONArray("pois");
        if (optJSONArray == null || optJSONArray.length() <= 0) {
            return null;
        }
        JSONObject optJSONObject = optJSONArray.optJSONObject(0);
        if (optJSONObject == null) {
            return null;
        }
        PoiItemDetail d = j.d(optJSONObject);
        JSONObject optJSONObject2 = optJSONObject.optJSONObject("rich_content");
        if (optJSONObject2 != null) {
            j.a(d, optJSONObject2);
        }
        optJSONObject2 = optJSONObject.optJSONObject("deep_info");
        if (optJSONObject2 != null) {
            j.e(d, optJSONObject2, optJSONObject);
        }
        return d;
    }

    protected String a_() {
        return f();
    }

    private String f() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("id=").append((String) this.a);
        stringBuilder.append("&output=json");
        stringBuilder.append("&extensions=all");
        stringBuilder.append("&language=").append(ServiceSettings.getInstance().getLanguage());
        stringBuilder.append("&key=" + w.f(this.d));
        return stringBuilder.toString();
    }
}
