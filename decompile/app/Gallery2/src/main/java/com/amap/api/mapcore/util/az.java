package com.amap.api.mapcore.util;

import android.content.Context;
import com.amap.api.mapcore.util.ez.a;
import com.amap.api.maps.AMapException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: OfflineInitHandler */
public class az extends by<String, ay> {
    private final String d = "update";
    private final String e = "1";
    private final String f = "0";
    private final String g = "version";

    protected /* synthetic */ Object b(JSONObject jSONObject) throws AMapException {
        return a(jSONObject);
    }

    public az(Context context, String str) {
        super(context, str);
    }

    protected JSONObject a(a aVar) {
        return aVar.o;
    }

    protected String a() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("010");
        return stringBuilder.toString();
    }

    protected Map<String, String> b() {
        Map<String, String> hashMap = new HashMap();
        hashMap.put("mapver", this.a);
        return hashMap;
    }

    protected ay a(JSONObject jSONObject) throws AMapException {
        ay ayVar = new ay();
        if (jSONObject == null) {
            return null;
        }
        try {
            String optString = jSONObject.optString("update", "");
            if (optString.equals("0")) {
                ayVar.a(false);
            } else if (optString.equals("1")) {
                ayVar.a(true);
            }
            ayVar.a(jSONObject.optString("version", ""));
        } catch (Throwable th) {
            fo.b(th, "OfflineInitHandler", "loadData parseJson");
        }
        return ayVar;
    }
}
