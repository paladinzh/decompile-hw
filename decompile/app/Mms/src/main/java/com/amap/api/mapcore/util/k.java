package com.amap.api.mapcore.util;

import android.content.Context;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.amap.api.maps.AMapException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: OfflineInitHandler */
public class k extends aj<String, j> {
    protected /* synthetic */ Object b(String str) throws AMapException {
        return a(str);
    }

    public k(Context context, String str) {
        super(context, str);
        getClass();
        a(5000);
        getClass();
        b(50000);
    }

    public String a() {
        return "http://restapi.amap.com/v3/config/version";
    }

    protected j a(String str) throws AMapException {
        j jVar = new j();
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (jSONObject.has("offlinemap")) {
                jSONObject = jSONObject.getJSONObject("offlinemap");
                String optString = jSONObject.optString("update", "");
                if (optString.equals("0")) {
                    jVar.a(false);
                } else if (optString.equals("1")) {
                    jVar.a(true);
                }
                jVar.a(jSONObject.optString(NumberInfo.VERSION_KEY, ""));
            }
        } catch (Throwable th) {
            ce.a(th, "OfflineInitHandler", "loadData parseJson");
        }
        return jVar;
    }

    public Map<String, String> b() {
        Map<String, String> hashMap = new HashMap();
        hashMap.put("mapver", this.a);
        hashMap.put("output", "json");
        hashMap.put("key", bl.f(this.d));
        hashMap.put("opertype", "offlinemap_with_province_vfour");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("mapver=").append((String) this.a);
        stringBuffer.append("&output=json");
        stringBuffer.append("&key=").append(bl.f(this.d));
        stringBuffer.append("&opertype=offlinemap_with_province_vfour");
        String d = bx.d(stringBuffer.toString());
        String a = bn.a();
        hashMap.put("ts", a);
        hashMap.put("scode", bn.a(this.d, a, d));
        return hashMap;
    }
}
