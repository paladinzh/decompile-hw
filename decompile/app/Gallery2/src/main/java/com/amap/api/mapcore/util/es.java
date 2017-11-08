package com.amap.api.mapcore.util;

import android.content.Context;
import java.util.HashMap;
import java.util.Map;

/* compiled from: BasicLBSRestHandler */
public abstract class es<T, V> extends er<T, V> {
    protected abstract String f();

    public es(Context context, T t) {
        super(context, t);
    }

    public byte[] g() {
        try {
            return f().getBytes("utf-8");
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    public Map<String, String> b() {
        return null;
    }

    public Map<String, String> a() {
        Map<String, String> hashMap = new HashMap();
        hashMap.put("Content-Type", " application/json");
        hashMap.put("Accept-Encoding", "gzip");
        hashMap.put("User-Agent", "AMAP SDK Android Trace 4.1.2");
        hashMap.put("X-INFO", fb.b(this.d));
        hashMap.put("platinfo", String.format("platform=Android&sdkversion=%s&product=%s", new Object[]{"4.1.2", "trace"}));
        hashMap.put("logversion", "2.1");
        return hashMap;
    }

    protected V e() {
        return null;
    }

    public String c() {
        String str = "key=" + ey.f(this.d);
        String a = fb.a();
        String str2 = "&ts=" + a;
        return "http://restapi.amap.com/v3/grasproad?" + str + str2 + ("&scode=" + fb.a(this.d, a, str));
    }
}
