package com.amap.api.services.poisearch;

import android.content.Context;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.ServiceSettings;
import com.amap.api.services.core.aj;
import com.amap.api.services.core.h;
import com.amap.api.services.core.i;
import com.amap.api.services.core.n;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: PoiSearchIdHandler */
class c extends b<String, PoiItem> {
    public /* synthetic */ Object a(String str) throws AMapException {
        return e(str);
    }

    public c(Context context, String str) {
        super(context, str);
    }

    public String g() {
        return h.a() + "/place/detail?";
    }

    public PoiItem e(String str) throws AMapException {
        try {
            return a(new JSONObject(str));
        } catch (Throwable e) {
            i.a(e, "PoiSearchIdHandler", "paseJSONJSONException");
            return null;
        } catch (Throwable e2) {
            i.a(e2, "PoiSearchIdHandler", "paseJSONException");
            return null;
        }
    }

    private PoiItem a(JSONObject jSONObject) throws JSONException {
        if (jSONObject == null) {
            return null;
        }
        JSONArray optJSONArray = jSONObject.optJSONArray("pois");
        if (optJSONArray == null || optJSONArray.length() <= 0) {
            return null;
        }
        JSONObject optJSONObject = optJSONArray.optJSONObject(0);
        if (optJSONObject != null) {
            return n.d(optJSONObject);
        }
        return null;
    }

    protected String e() {
        return h();
    }

    private String h() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("id=").append((String) this.a);
        stringBuilder.append("&output=json");
        stringBuilder.append("&extensions=all");
        stringBuilder.append("&children=1");
        stringBuilder.append("&language=").append(ServiceSettings.getInstance().getLanguage());
        stringBuilder.append("&key=" + aj.f(this.d));
        return stringBuilder.toString();
    }
}
