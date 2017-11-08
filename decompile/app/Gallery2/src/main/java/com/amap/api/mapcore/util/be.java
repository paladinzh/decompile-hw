package com.amap.api.mapcore.util;

import android.content.Context;
import com.amap.api.mapcore.util.ez.a;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: OfflineUpdateCityHandler */
public class be extends by<String, List<OfflineMapProvince>> {
    private Context d;

    protected /* synthetic */ Object b(JSONObject jSONObject) throws AMapException {
        return a(jSONObject);
    }

    public be(Context context, String str) {
        super(context, str);
    }

    public void a(Context context) {
        this.d = context;
    }

    protected List<OfflineMapProvince> a(JSONObject jSONObject) throws AMapException {
        try {
            if (this.d != null) {
                bu.c(jSONObject.toString(), this.d);
            }
        } catch (Throwable th) {
            fo.b(th, "OfflineUpdateCityHandler", "loadData jsonInit");
            th.printStackTrace();
        }
        try {
            if (this.d == null) {
                return null;
            }
            return bu.a(jSONObject, this.d);
        } catch (Throwable th2) {
            fo.b(th2, "OfflineUpdateCityHandler", "loadData parseJson");
            th2.printStackTrace();
            return null;
        }
    }

    protected JSONObject a(a aVar) {
        JSONObject jSONObject = aVar.n;
        if (!jSONObject.has("result")) {
            JSONObject jSONObject2 = new JSONObject();
            try {
                jSONObject2.put("result", new JSONObject().put("offlinemap_with_province_vfour", jSONObject));
                return jSONObject2;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jSONObject;
    }

    protected String a() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("006");
        return stringBuilder.toString();
    }

    protected Map<String, String> b() {
        Map<String, String> hashMap = new HashMap();
        hashMap.put("mapver", this.a);
        return hashMap;
    }
}
