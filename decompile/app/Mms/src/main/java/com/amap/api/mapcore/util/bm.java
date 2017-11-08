package com.amap.api.mapcore.util;

import android.content.Context;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: AuthManager */
public class bm {
    public static int a = -1;
    public static String b = "";
    private static bv c;
    private static String d = "http://apiinit.amap.com/v3/log/init";
    private static String e = null;

    private static boolean a(Context context, bv bvVar, boolean z) {
        c = bvVar;
        try {
            String a = a();
            Map hashMap = new HashMap();
            hashMap.put("Content-Type", "application/x-www-form-urlencoded");
            hashMap.put("Accept-Encoding", "gzip");
            hashMap.put("Connection", "Keep-Alive");
            hashMap.put("User-Agent", c.b);
            hashMap.put("X-INFO", bn.a(context, c, null, z));
            hashMap.put("logversion", "2.1");
            hashMap.put("platinfo", String.format("platform=Android&sdkversion=%s&product=%s", new Object[]{c.a, c.c}));
            dd a2 = dd.a();
            dj byVar = new by();
            byVar.a(bt.a(context));
            byVar.a(hashMap);
            byVar.b(a(context));
            byVar.a(a);
            return a(a2.b(byVar));
        } catch (Throwable th) {
            cb.a(th, "Auth", "getAuth");
            return true;
        }
    }

    public static synchronized boolean a(Context context, bv bvVar) {
        boolean a;
        synchronized (bm.class) {
            a = a(context, bvVar, true);
        }
        return a;
    }

    public static synchronized boolean b(Context context, bv bvVar) {
        boolean a;
        synchronized (bm.class) {
            a = a(context, bvVar, false);
        }
        return a;
    }

    public static void a(String str) {
        bl.a(str);
    }

    private static String a() {
        return d;
    }

    private static boolean a(byte[] bArr) {
        if (bArr == null) {
            return true;
        }
        try {
            JSONObject jSONObject = new JSONObject(bx.a(bArr));
            if (jSONObject.has("status")) {
                int i = jSONObject.getInt("status");
                if (i == 1) {
                    a = 1;
                } else if (i == 0) {
                    a = 0;
                }
            }
            if (jSONObject.has("info")) {
                b = jSONObject.getString("info");
            }
            if (a == 0) {
                Log.i("AuthFailure", b);
            }
            return a == 1;
        } catch (Throwable e) {
            cb.a(e, "Auth", "lData");
            return false;
        } catch (Throwable e2) {
            cb.a(e2, "Auth", "lData");
            return false;
        }
    }

    private static Map<String, String> a(Context context) {
        Map<String, String> hashMap = new HashMap();
        try {
            hashMap.put("resType", "json");
            hashMap.put("encode", "UTF-8");
            String a = bn.a();
            hashMap.put("ts", a);
            hashMap.put("key", bl.f(context));
            hashMap.put("scode", bn.a(context, a, bx.d("resType=json&encode=UTF-8&key=" + bl.f(context))));
        } catch (Throwable th) {
            cb.a(th, "Auth", "gParams");
        }
        return hashMap;
    }
}
