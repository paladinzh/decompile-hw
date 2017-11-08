package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: ConfigManager */
public class bp {

    /* compiled from: ConfigManager */
    public static class a {
        public JSONObject a;
        public JSONObject b;
        public JSONObject c;
        public JSONObject d;
        @Deprecated
        public JSONObject e;
        public JSONObject f;
        public a g;
        public c h;
        public b i;

        /* compiled from: ConfigManager */
        public static class a {
            public boolean a;
            public boolean b;
        }

        /* compiled from: ConfigManager */
        public static class b {
            public String a;
            public String b;
        }

        /* compiled from: ConfigManager */
        public static class c {
            public String a;
            public String b;
            public String c;
        }
    }

    /* compiled from: ConfigManager */
    static class b extends dj {
        private Context a;
        private bv b;
        private String c = "";

        b(Context context, bv bvVar, String str) {
            this.a = context;
            this.b = bvVar;
            this.c = str;
        }

        public Map<String, String> c() {
            Map<String, String> hashMap = new HashMap();
            hashMap.put("User-Agent", this.b.c());
            hashMap.put("platinfo", String.format("platform=Android&sdkversion=%s&product=%s", new Object[]{this.b.b(), this.b.a()}));
            hashMap.put("logversion", "2.0");
            return hashMap;
        }

        public Map<String, String> b() {
            Object q = bq.q(this.a);
            if (!TextUtils.isEmpty(q)) {
                q = bs.b(new StringBuilder(q).reverse().toString());
            }
            Map hashMap = new HashMap();
            hashMap.put("key", bl.f(this.a));
            hashMap.put("opertype", this.c);
            hashMap.put("plattype", "android");
            hashMap.put("product", this.b.a());
            hashMap.put(NumberInfo.VERSION_KEY, this.b.b());
            hashMap.put("output", "json");
            hashMap.put("androidversion", VERSION.SDK_INT + "");
            hashMap.put("deviceId", q);
            hashMap.put("abitype", Build.CPU_ABI);
            hashMap.put("ext", this.b.d());
            String a = bn.a();
            String a2 = bn.a(this.a, a, bx.b(hashMap));
            hashMap.put("ts", a);
            hashMap.put("scode", a2);
            return hashMap;
        }

        public String a() {
            return "https://restapi.amap.com/v3/config/resource?";
        }
    }

    public static a a(byte[] bArr) {
        boolean z = false;
        a aVar = new a();
        if (bArr != null) {
            try {
                if (bArr.length != 0) {
                    JSONObject jSONObject = new JSONObject(bx.a(bArr));
                    if (!"1".equals(a(jSONObject, "status")) || !jSONObject.has("result")) {
                        return aVar;
                    }
                    jSONObject = jSONObject.getJSONObject("result");
                    if (jSONObject != null) {
                        boolean b;
                        JSONObject jSONObject2;
                        if (bx.a(jSONObject, "exception")) {
                            b = b(jSONObject.getJSONObject("exception"));
                        } else {
                            b = false;
                        }
                        if (bx.a(jSONObject, "common")) {
                            z = a(jSONObject.getJSONObject("common"));
                        }
                        a aVar2 = new a();
                        aVar2.a = b;
                        aVar2.b = z;
                        aVar.g = aVar2;
                        if (jSONObject.has("sdkupdate")) {
                            jSONObject2 = jSONObject.getJSONObject("sdkupdate");
                            c cVar = new c();
                            a(jSONObject2, cVar);
                            aVar.h = cVar;
                        }
                        if (bx.a(jSONObject, "sdkcoordinate")) {
                            jSONObject2 = jSONObject.getJSONObject("sdkcoordinate");
                            b bVar = new b();
                            a(jSONObject2, bVar);
                            aVar.i = bVar;
                        }
                        if (bx.a(jSONObject, "callamap")) {
                            aVar.e = jSONObject.getJSONObject("callamap");
                        }
                        if (bx.a(jSONObject, "ca")) {
                            aVar.f = jSONObject.getJSONObject("ca");
                        }
                        if (bx.a(jSONObject, "locate")) {
                            aVar.d = jSONObject.getJSONObject("locate");
                        }
                        if (bx.a(jSONObject, "callamappro")) {
                            aVar.c = jSONObject.getJSONObject("callamappro");
                        }
                        if (bx.a(jSONObject, "opflag")) {
                            aVar.b = jSONObject.getJSONObject("opflag");
                        }
                        if (bx.a(jSONObject, "amappushflag")) {
                            aVar.a = jSONObject.getJSONObject("amappushflag");
                        }
                    }
                    return aVar;
                }
            } catch (Throwable e) {
                cb.a(e, "ConfigManager", "loadConfig");
                return aVar;
            } catch (Throwable e2) {
                cb.a(e2, "ConfigManager", "loadConfig");
                return aVar;
            }
        }
        return aVar;
    }

    public static a a(Context context, bv bvVar, String str) {
        try {
            return a(new dd().a(new b(context, bvVar, str)));
        } catch (Throwable e) {
            cb.a(e, "ConfigManager", "loadConfig");
            return new a();
        } catch (Throwable e2) {
            cb.a(e2, "ConfigManager", "loadConfig");
            return new a();
        }
    }

    private static boolean a(String str) {
        if (str == null || !str.equals("1")) {
            return false;
        }
        return true;
    }

    public static String a(JSONObject jSONObject, String str) throws JSONException {
        if (jSONObject == null) {
            return "";
        }
        String str2 = "";
        if (jSONObject.has(str) && !jSONObject.getString(str).equals("[]")) {
            str2 = jSONObject.optString(str);
        }
        return str2;
    }

    private static void a(JSONObject jSONObject, b bVar) {
        if (jSONObject != null) {
            try {
                String a = a(jSONObject, "md5");
                String a2 = a(jSONObject, Constant.URLS);
                bVar.b = a;
                bVar.a = a2;
            } catch (Throwable e) {
                cb.a(e, "ConfigManager", "parseSDKCoordinate");
            } catch (Throwable e2) {
                cb.a(e2, "ConfigManager", "parseSDKCoordinate");
            }
        }
    }

    private static void a(JSONObject jSONObject, c cVar) {
        if (jSONObject != null) {
            try {
                Object a = a(jSONObject, "md5");
                Object a2 = a(jSONObject, Constant.URLS);
                Object a3 = a(jSONObject, "sdkversion");
                if (!TextUtils.isEmpty(a) && !TextUtils.isEmpty(a2) && !TextUtils.isEmpty(a3)) {
                    cVar.a = a2;
                    cVar.b = a;
                    cVar.c = a3;
                }
            } catch (Throwable e) {
                cb.a(e, "ConfigManager", "parseSDKUpdate");
            } catch (Throwable e2) {
                cb.a(e2, "ConfigManager", "parseSDKUpdate");
            }
        }
    }

    private static boolean a(JSONObject jSONObject) {
        if (jSONObject == null) {
            return false;
        }
        try {
            return a(a(jSONObject.getJSONObject("commoninfo"), "com_isupload"));
        } catch (Throwable e) {
            cb.a(e, "ConfigManager", "parseCommon");
            return false;
        } catch (Throwable e2) {
            cb.a(e2, "ConfigManager", "parseCommon");
            return false;
        }
    }

    private static boolean b(JSONObject jSONObject) {
        if (jSONObject == null) {
            return false;
        }
        try {
            return a(a(jSONObject.getJSONObject("exceptinfo"), "ex_isupload"));
        } catch (Throwable e) {
            cb.a(e, "ConfigManager", "parseException");
            return false;
        } catch (Throwable e2) {
            cb.a(e2, "ConfigManager", "parseException");
            return false;
        }
    }
}
