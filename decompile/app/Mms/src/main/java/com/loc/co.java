package com.loc;

import android.content.Context;
import android.net.NetworkInfo;
import com.loc.v.a;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: LocNetManager */
public class co {
    private static co e = null;
    v a = null;
    String b = null;
    bo c = null;
    bp d = null;
    private long f = 0;
    private int g = e.j;
    private int h = e.j;

    private co(Context context) {
        try {
            this.a = new a("loc", "2.4.0", "AMAP_Location_SDK_Android 2.4.0").a(e.b()).a();
        } catch (Throwable e) {
            e.a(e, "LocNetManager", "LocNetManager");
        }
        this.b = o.a(context, this.a, new HashMap(), true);
        this.c = bo.a();
    }

    public static int a(NetworkInfo networkInfo) {
        return (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) ? networkInfo.getType() : -1;
    }

    public static synchronized co a(Context context) {
        co coVar;
        synchronized (co.class) {
            if (e == null) {
                e = new co(context);
            }
            coVar = e;
        }
        return coVar;
    }

    public String a(byte[] bArr, Context context, String str, boolean z) {
        String str2;
        if (a(cw.c(context)) == -1) {
            return null;
        }
        byte[] a;
        Map hashMap = new HashMap();
        bs cpVar = new cp();
        hashMap.clear();
        hashMap.put("Content-Type", "application/x-www-form-urlencoded");
        hashMap.put("Connection", "Keep-Alive");
        if (z) {
            hashMap.put("Accept-Encoding", "gzip");
            hashMap.put("User-Agent", "AMAP_Location_SDK_Android 2.4.0");
            hashMap.put("platinfo", String.format(Locale.US, "platform=Android&sdkversion=%s&product=%s", new Object[]{"2.4.0", "loc"}));
            hashMap.put("logversion", "2.1");
        }
        cpVar.a(hashMap);
        cpVar.a(str);
        cpVar.a(bArr);
        cpVar.a(t.a(context));
        cpVar.a(e.j);
        cpVar.b(e.j);
        if (z) {
            a = this.c.a(cpVar);
        } else {
            try {
                a = this.c.b(cpVar);
            } catch (Throwable e) {
                e.a(e, "LocNetManager", "post");
                str2 = null;
                return str2;
            } catch (Throwable e2) {
                e.a(e2, "LocNetManager", "post");
                str2 = null;
                return str2;
            }
        }
        str2 = new String(a, "utf-8");
        return str2;
    }

    public HttpURLConnection a(Context context, String str, HashMap<String, String> hashMap, byte[] bArr) throws Exception {
        boolean z = false;
        try {
            if (a(cw.c(context)) == -1) {
                return null;
            }
            bs cpVar = new cp();
            cpVar.a((Map) hashMap);
            cpVar.a(str);
            cpVar.a(bArr);
            cpVar.a(t.a(context));
            cpVar.a(e.j);
            cpVar.b(e.j);
            if (str.toLowerCase(Locale.US).startsWith("https")) {
                z = true;
            }
            return this.c.a(cpVar, z);
        } catch (Throwable th) {
            e.a(th, "LocNetManager", "doHttpPost");
            return null;
        }
    }

    public byte[] a(Context context, JSONObject jSONObject, cs csVar, String str) throws Exception {
        if (cw.a(jSONObject, "httptimeout")) {
            try {
                this.g = jSONObject.getInt("httptimeout");
            } catch (Throwable th) {
                e.a(th, "LocNetManager", "req");
            }
        }
        if (a(cw.c(context)) == -1) {
            return null;
        }
        Map hashMap = new HashMap();
        bs cpVar = new cp();
        hashMap.clear();
        hashMap.put("Content-Type", "application/octet-stream");
        hashMap.put("Accept-Encoding", "gzip");
        hashMap.put("gzipped", "1");
        hashMap.put("Connection", "Keep-Alive");
        hashMap.put("User-Agent", "AMAP_Location_SDK_Android 2.4.0");
        hashMap.put("X-INFO", this.b);
        hashMap.put("KEY", m.f(context));
        hashMap.put("enginever", "4.2");
        String a = o.a();
        String a2 = o.a(context, a, "key=" + m.f(context));
        hashMap.put("ts", a);
        hashMap.put("scode", a2);
        hashMap.put("platinfo", String.format(Locale.US, "platform=Android&sdkversion=%s&product=%s", new Object[]{"2.4.0", "loc"}));
        hashMap.put("logversion", "2.1");
        hashMap.put("encr", "1");
        cpVar.a(hashMap);
        cpVar.a(str);
        cpVar.a(cw.a(csVar.a()));
        cpVar.a(t.a(context));
        cpVar.a(this.g);
        cpVar.b(this.g);
        return this.c.b(cpVar);
    }
}
