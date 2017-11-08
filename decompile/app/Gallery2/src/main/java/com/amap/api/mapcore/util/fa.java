package com.amap.api.mapcore.util;

import android.content.Context;
import android.util.Log;
import com.android.gallery3d.gadget.XmlUtils;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

@Deprecated
/* compiled from: AuthManager */
public class fa {
    public static int a = -1;
    public static String b = "";
    private static fh c;
    private static String d = "http://apiinit.amap.com/v3/log/init";
    private static String e = null;

    private static boolean a(Context context, fh fhVar, boolean z) {
        c = fhVar;
        try {
            String a = a();
            Map hashMap = new HashMap();
            hashMap.put("Content-Type", "application/x-www-form-urlencoded");
            hashMap.put("Accept-Encoding", "gzip");
            hashMap.put("Connection", "Keep-Alive");
            hashMap.put("User-Agent", c.d());
            hashMap.put("X-INFO", fb.a(context, c, null, z));
            hashMap.put("logversion", "2.1");
            hashMap.put("platinfo", String.format("platform=Android&sdkversion=%s&product=%s", new Object[]{c.b(), c.a()}));
            gx a2 = gx.a();
            hd fjVar = new fj();
            fjVar.a(ff.a(context));
            fjVar.a(hashMap);
            fjVar.b(a(context));
            fjVar.a(a);
            return a(a2.b(fjVar));
        } catch (Throwable th) {
            fl.a(th, "Auth", "getAuth");
            return true;
        }
    }

    @Deprecated
    public static synchronized boolean a(Context context, fh fhVar) {
        boolean a;
        synchronized (fa.class) {
            a = a(context, fhVar, false);
        }
        return a;
    }

    public static void a(String str) {
        ey.a(str);
    }

    private static String a() {
        return d;
    }

    private static boolean a(byte[] bArr) {
        if (bArr == null) {
            return true;
        }
        try {
            JSONObject jSONObject = new JSONObject(fi.a(bArr));
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
            fl.a(e, "Auth", "lData");
            return false;
        } catch (Throwable e2) {
            fl.a(e2, "Auth", "lData");
            return false;
        }
    }

    private static Map<String, String> a(Context context) {
        Map<String, String> hashMap = new HashMap();
        try {
            hashMap.put("resType", "json");
            hashMap.put("encode", XmlUtils.INPUT_ENCODING);
            String a = fb.a();
            hashMap.put("ts", a);
            hashMap.put("key", ey.f(context));
            hashMap.put("scode", fb.a(context, a, fi.d("resType=json&encode=UTF-8&key=" + ey.f(context))));
        } catch (Throwable th) {
            fl.a(th, "Auth", "gParams");
        }
        return hashMap;
    }
}
