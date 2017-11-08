package com.amap.api.services.core;

import android.content.Context;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.json.JSONObject;
import tmsdk.common.TMSDKContext;

/* compiled from: ManifestManager */
class n extends bt {
    private Context a;
    private String b;

    public n(Context context) {
        this.a = context;
        this.b = w.f(context);
    }

    public o a() {
        String str = "feachManifest";
        try {
            bs a = bs.a(false);
            a(ac.a(this.a));
            return a(a.a((bt) this));
        } catch (Throwable e) {
            d.a(e, "ManifestManager", str);
            return null;
        }
    }

    private JSONObject a(JSONObject jSONObject, String str) {
        if (jSONObject != null) {
            return jSONObject.optJSONObject(str);
        }
        return null;
    }

    private String b(JSONObject jSONObject, String str) {
        if (jSONObject != null) {
            return jSONObject.optString(str);
        }
        return null;
    }

    private boolean a(String str) {
        if (str == null || !str.equals("1")) {
            return false;
        }
        return true;
    }

    private o a(byte[] bArr) {
        String str = "loadData";
        if (bArr == null) {
            return null;
        }
        try {
            JSONObject jSONObject = new JSONObject(new String(bArr));
            String optString = jSONObject.optString("status");
            if (!"0".equals(optString) && "1".equals(optString)) {
                JSONObject a = a(jSONObject, "result");
                return new o(a(b(a(a(a, "common"), "commoninfo"), "com_isupload")), a(b(a(a(a, "exception"), "exceptinfo"), "ex_isupload")));
            }
        } catch (Throwable e) {
            d.a(e, "ManifestManager", str);
        } catch (Throwable e2) {
            d.a(e2, "ManifestManager", str);
        }
        return null;
    }

    public Map<String, String> d_() {
        Map<String, String> hashMap = new HashMap();
        hashMap.put("User-Agent", "AMAP SDK Android Search 2.5.0");
        return hashMap;
    }

    public Map<String, String> c_() {
        Map<String, String> hashMap = new HashMap();
        hashMap.put("key", this.b);
        hashMap.put("opertype", "common;exception");
        hashMap.put("plattype", "android");
        hashMap.put(TMSDKContext.CON_PRODUCT, "sea");
        hashMap.put("version", "2.5.0");
        hashMap.put("ext", "standard");
        hashMap.put("output", "json");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("key=").append(this.b);
        stringBuffer.append("&opertype=common;exception");
        stringBuffer.append("&plattype=android");
        stringBuffer.append("&product=").append("sea");
        stringBuffer.append("&version=").append("2.5.0");
        stringBuffer.append("&ext=standard");
        stringBuffer.append("&output=json");
        String a = ae.a(stringBuffer.toString());
        String a2 = y.a();
        hashMap.put("ts", a2);
        hashMap.put("scode", y.a(this.a, a2, a));
        return hashMap;
    }

    public String b() {
        return c.a() + "/config/resource";
    }

    public HttpEntity e() {
        return null;
    }
}
