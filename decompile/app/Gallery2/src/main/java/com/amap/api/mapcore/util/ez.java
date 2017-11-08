package com.amap.api.mapcore.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.security.Key;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONException;
import org.json.JSONObject;
import tmsdk.common.TMSDKContext;

/* compiled from: AuthConfigManager */
public class ez {
    public static int a = -1;
    public static String b = "";

    /* compiled from: AuthConfigManager */
    public static class a {
        public String a;
        public int b = -1;
        public JSONObject c;
        public JSONObject d;
        public JSONObject e;
        public JSONObject f;
        public JSONObject g;
        public JSONObject h;
        public JSONObject i;
        public JSONObject j;
        public JSONObject k;
        public JSONObject l;
        public JSONObject m;
        public JSONObject n;
        public JSONObject o;
        public a p;
        public d q;
        public c r;
        public b s;
        public b t;
        public b u;
        public b v;

        /* compiled from: AuthConfigManager */
        public static class a {
            public boolean a;
            public boolean b;
        }

        /* compiled from: AuthConfigManager */
        public static class b {
            public boolean a;
            public String b;
            public String c;
            public String d;
        }

        /* compiled from: AuthConfigManager */
        public static class c {
            public String a;
            public String b;
        }

        /* compiled from: AuthConfigManager */
        public static class d {
            public String a;
            public String b;
            public String c;
        }
    }

    /* compiled from: AuthConfigManager */
    static class b extends gy {
        private String c;
        private Map<String, String> d;

        b(Context context, fh fhVar, String str, Map<String, String> map) {
            super(context, fhVar);
            this.c = str;
            this.d = map;
        }

        public Map<String, String> a() {
            return null;
        }

        public String c() {
            return "https://restapi.amap.com/v3/iasdkauth";
        }

        public byte[] d() {
            return null;
        }

        public byte[] e() {
            return fi.a(fi.a(m()));
        }

        protected String f() {
            return "3.0";
        }

        private Map<String, String> m() {
            Object q = fc.q(this.a);
            if (!TextUtils.isEmpty(q)) {
                q = fe.b(new StringBuilder(q).reverse().toString());
            }
            Map<String, String> hashMap = new HashMap();
            hashMap.put("authkey", this.c);
            hashMap.put("plattype", "android");
            hashMap.put(TMSDKContext.CON_PRODUCT, this.b.a());
            hashMap.put("version", this.b.b());
            hashMap.put("output", "json");
            hashMap.put("androidversion", VERSION.SDK_INT + "");
            hashMap.put("deviceId", q);
            if (!(this.d == null || this.d.isEmpty())) {
                hashMap.putAll(this.d);
            }
            if (VERSION.SDK_INT < 21) {
                q = null;
            } else {
                try {
                    ApplicationInfo applicationInfo = this.a.getApplicationInfo();
                    Field declaredField = Class.forName(ApplicationInfo.class.getName()).getDeclaredField("primaryCpuAbi");
                    declaredField.setAccessible(true);
                    String str = (String) declaredField.get(applicationInfo);
                } catch (Throwable th) {
                    fl.a(th, "ConfigManager", "getcpu");
                    q = null;
                }
            }
            if (TextUtils.isEmpty(q)) {
                q = Build.CPU_ABI;
            }
            hashMap.put("abitype", q);
            hashMap.put("ext", this.b.e());
            return hashMap;
        }
    }

    public static boolean a(String str, boolean z) {
        boolean z2 = true;
        try {
            String[] split = URLDecoder.decode(str).split("/");
            if (split[split.length - 1].charAt(4) % 2 != 1) {
                z2 = false;
            }
            return z2;
        } catch (Throwable th) {
            return z;
        }
    }

    public static a a(Context context, fh fhVar, String str, Map<String, String> map) {
        hf a;
        Object obj;
        ex e;
        hf hfVar;
        Object obj2;
        Object obj3;
        byte[] bArr;
        String a2;
        JSONObject jSONObject;
        int i;
        String str2;
        String str3;
        List list;
        String str4;
        a aVar;
        JSONObject jSONObject2;
        Throwable th;
        JSONObject jSONObject3;
        d dVar;
        c cVar;
        b bVar;
        b bVar2;
        hf hfVar2 = null;
        a aVar2 = new a();
        Object obj4;
        try {
            a = new gx().a(new b(context, fhVar, str, map), true);
            if (a == null) {
                obj = hfVar2;
            } else {
                try {
                    obj = a.a;
                } catch (ex e2) {
                    e = e2;
                    hfVar = hfVar2;
                    aVar2.a = e.a();
                    obj4 = hfVar2;
                    obj2 = obj;
                    hfVar = a;
                    obj3 = obj2;
                    if (bArr == null) {
                        return aVar2;
                    }
                    if (TextUtils.isEmpty(a2)) {
                        a2 = fi.a(bArr);
                    }
                    try {
                        jSONObject = new JSONObject(a2);
                        if (jSONObject.has("status")) {
                            i = jSONObject.getInt("status");
                            if (i == 1) {
                                a = 1;
                            } else if (i == 0) {
                                a2 = "authcsid";
                                str2 = "authgsid";
                                if (hfVar != null) {
                                    str3 = hfVar.c;
                                    if (hfVar.b != null) {
                                        list = (List) hfVar.b.get("gsid");
                                        if (list != null) {
                                            a2 = (String) list.get(0);
                                            str2 = str3;
                                        }
                                        a2 = str2;
                                        str2 = str3;
                                    } else {
                                        a2 = str2;
                                        str2 = str3;
                                    }
                                } else {
                                    str4 = str2;
                                    str2 = a2;
                                    a2 = str4;
                                }
                                fi.a(context, str2, a2, jSONObject.toString());
                                a = 0;
                                if (jSONObject.has("info")) {
                                    b = jSONObject.getString("info");
                                }
                                if (a == 0) {
                                    aVar2.a = b;
                                    return aVar2;
                                }
                            }
                            if (jSONObject.has("ver")) {
                                aVar2.b = jSONObject.getInt("ver");
                            }
                            if (fi.a(jSONObject, "result")) {
                                aVar = new a();
                                aVar.a = false;
                                aVar.b = false;
                                aVar2.p = aVar;
                                jSONObject2 = jSONObject.getJSONObject("result");
                                if (fi.a(jSONObject2, "11K")) {
                                    try {
                                        aVar.a = a(jSONObject2.getJSONObject("11K").getString("able"), false);
                                    } catch (Throwable th2) {
                                        fl.a(th2, "AuthConfigManager", "loadException");
                                    }
                                }
                                if (fi.a(jSONObject2, "11B")) {
                                    aVar2.d = jSONObject2.getJSONObject("11B");
                                }
                                if (fi.a(jSONObject2, "11C")) {
                                    aVar2.g = jSONObject2.getJSONObject("11C");
                                }
                                if (fi.a(jSONObject2, "11I")) {
                                    aVar2.h = jSONObject2.getJSONObject("11I");
                                }
                                if (fi.a(jSONObject2, "11H")) {
                                    aVar2.i = jSONObject2.getJSONObject("11H");
                                }
                                if (fi.a(jSONObject2, "11E")) {
                                    aVar2.j = jSONObject2.getJSONObject("11E");
                                }
                                if (fi.a(jSONObject2, "11F")) {
                                    aVar2.k = jSONObject2.getJSONObject("11F");
                                }
                                if (fi.a(jSONObject2, "13A")) {
                                    aVar2.m = jSONObject2.getJSONObject("13A");
                                }
                                if (fi.a(jSONObject2, "13J")) {
                                    aVar2.e = jSONObject2.getJSONObject("13J");
                                }
                                if (fi.a(jSONObject2, "11G")) {
                                    aVar2.l = jSONObject2.getJSONObject("11G");
                                }
                                if (fi.a(jSONObject2, "001")) {
                                    jSONObject3 = jSONObject2.getJSONObject("001");
                                    dVar = new d();
                                    a(jSONObject3, dVar);
                                    aVar2.q = dVar;
                                }
                                if (fi.a(jSONObject2, "002")) {
                                    jSONObject3 = jSONObject2.getJSONObject("002");
                                    cVar = new c();
                                    a(jSONObject3, cVar);
                                    aVar2.r = cVar;
                                }
                                if (fi.a(jSONObject2, "006")) {
                                    aVar2.n = jSONObject2.getJSONObject("006");
                                }
                                if (fi.a(jSONObject2, "010")) {
                                    aVar2.o = jSONObject2.getJSONObject("010");
                                }
                                if (fi.a(jSONObject2, "11Z")) {
                                    jSONObject3 = jSONObject2.getJSONObject("11Z");
                                    bVar = new b();
                                    a(jSONObject3, bVar);
                                    aVar2.s = bVar;
                                }
                                if (fi.a(jSONObject2, "135")) {
                                    aVar2.f = jSONObject2.getJSONObject("135");
                                }
                                if (fi.a(jSONObject2, "13S")) {
                                    aVar2.c = jSONObject2.getJSONObject("13S");
                                }
                                if (fi.a(jSONObject2, "121")) {
                                    jSONObject3 = jSONObject2.getJSONObject("121");
                                    bVar = new b();
                                    a(jSONObject3, bVar);
                                    aVar2.t = bVar;
                                }
                                if (fi.a(jSONObject2, "122")) {
                                    jSONObject3 = jSONObject2.getJSONObject("122");
                                    bVar = new b();
                                    a(jSONObject3, bVar);
                                    aVar2.u = bVar;
                                }
                                if (fi.a(jSONObject2, "123")) {
                                    jSONObject3 = jSONObject2.getJSONObject("123");
                                    bVar2 = new b();
                                    a(jSONObject3, bVar2);
                                    aVar2.v = bVar2;
                                }
                            }
                            return aVar2;
                        }
                    } catch (Throwable th22) {
                        fl.a(th22, "AuthConfigManager", "loadConfig");
                    }
                    return aVar2;
                } catch (IllegalBlockSizeException e3) {
                    hfVar = hfVar2;
                    obj4 = hfVar2;
                    obj2 = obj;
                    hfVar = a;
                    obj3 = obj2;
                    if (bArr == null) {
                        return aVar2;
                    }
                    if (TextUtils.isEmpty(a2)) {
                        a2 = fi.a(bArr);
                    }
                    jSONObject = new JSONObject(a2);
                    if (jSONObject.has("status")) {
                        i = jSONObject.getInt("status");
                        if (i == 1) {
                            a = 1;
                        } else if (i == 0) {
                            a2 = "authcsid";
                            str2 = "authgsid";
                            if (hfVar != null) {
                                str4 = str2;
                                str2 = a2;
                                a2 = str4;
                            } else {
                                str3 = hfVar.c;
                                if (hfVar.b != null) {
                                    a2 = str2;
                                    str2 = str3;
                                } else {
                                    list = (List) hfVar.b.get("gsid");
                                    if (list != null) {
                                        a2 = (String) list.get(0);
                                        str2 = str3;
                                    }
                                    a2 = str2;
                                    str2 = str3;
                                }
                            }
                            fi.a(context, str2, a2, jSONObject.toString());
                            a = 0;
                            if (jSONObject.has("info")) {
                                b = jSONObject.getString("info");
                            }
                            if (a == 0) {
                                aVar2.a = b;
                                return aVar2;
                            }
                        }
                        if (jSONObject.has("ver")) {
                            aVar2.b = jSONObject.getInt("ver");
                        }
                        if (fi.a(jSONObject, "result")) {
                            aVar = new a();
                            aVar.a = false;
                            aVar.b = false;
                            aVar2.p = aVar;
                            jSONObject2 = jSONObject.getJSONObject("result");
                            if (fi.a(jSONObject2, "11K")) {
                                aVar.a = a(jSONObject2.getJSONObject("11K").getString("able"), false);
                            }
                            if (fi.a(jSONObject2, "11B")) {
                                aVar2.d = jSONObject2.getJSONObject("11B");
                            }
                            if (fi.a(jSONObject2, "11C")) {
                                aVar2.g = jSONObject2.getJSONObject("11C");
                            }
                            if (fi.a(jSONObject2, "11I")) {
                                aVar2.h = jSONObject2.getJSONObject("11I");
                            }
                            if (fi.a(jSONObject2, "11H")) {
                                aVar2.i = jSONObject2.getJSONObject("11H");
                            }
                            if (fi.a(jSONObject2, "11E")) {
                                aVar2.j = jSONObject2.getJSONObject("11E");
                            }
                            if (fi.a(jSONObject2, "11F")) {
                                aVar2.k = jSONObject2.getJSONObject("11F");
                            }
                            if (fi.a(jSONObject2, "13A")) {
                                aVar2.m = jSONObject2.getJSONObject("13A");
                            }
                            if (fi.a(jSONObject2, "13J")) {
                                aVar2.e = jSONObject2.getJSONObject("13J");
                            }
                            if (fi.a(jSONObject2, "11G")) {
                                aVar2.l = jSONObject2.getJSONObject("11G");
                            }
                            if (fi.a(jSONObject2, "001")) {
                                jSONObject3 = jSONObject2.getJSONObject("001");
                                dVar = new d();
                                a(jSONObject3, dVar);
                                aVar2.q = dVar;
                            }
                            if (fi.a(jSONObject2, "002")) {
                                jSONObject3 = jSONObject2.getJSONObject("002");
                                cVar = new c();
                                a(jSONObject3, cVar);
                                aVar2.r = cVar;
                            }
                            if (fi.a(jSONObject2, "006")) {
                                aVar2.n = jSONObject2.getJSONObject("006");
                            }
                            if (fi.a(jSONObject2, "010")) {
                                aVar2.o = jSONObject2.getJSONObject("010");
                            }
                            if (fi.a(jSONObject2, "11Z")) {
                                jSONObject3 = jSONObject2.getJSONObject("11Z");
                                bVar = new b();
                                a(jSONObject3, bVar);
                                aVar2.s = bVar;
                            }
                            if (fi.a(jSONObject2, "135")) {
                                aVar2.f = jSONObject2.getJSONObject("135");
                            }
                            if (fi.a(jSONObject2, "13S")) {
                                aVar2.c = jSONObject2.getJSONObject("13S");
                            }
                            if (fi.a(jSONObject2, "121")) {
                                jSONObject3 = jSONObject2.getJSONObject("121");
                                bVar = new b();
                                a(jSONObject3, bVar);
                                aVar2.t = bVar;
                            }
                            if (fi.a(jSONObject2, "122")) {
                                jSONObject3 = jSONObject2.getJSONObject("122");
                                bVar = new b();
                                a(jSONObject3, bVar);
                                aVar2.u = bVar;
                            }
                            if (fi.a(jSONObject2, "123")) {
                                jSONObject3 = jSONObject2.getJSONObject("123");
                                bVar2 = new b();
                                a(jSONObject3, bVar2);
                                aVar2.v = bVar2;
                            }
                        }
                        return aVar2;
                    }
                    return aVar2;
                } catch (Throwable th3) {
                    th22 = th3;
                    hfVar = hfVar2;
                    fl.a(th22, "ConfigManager", "loadConfig");
                    obj4 = hfVar2;
                    obj2 = obj;
                    hfVar = a;
                    obj3 = obj2;
                    if (bArr == null) {
                        return aVar2;
                    }
                    if (TextUtils.isEmpty(a2)) {
                        a2 = fi.a(bArr);
                    }
                    jSONObject = new JSONObject(a2);
                    if (jSONObject.has("status")) {
                        i = jSONObject.getInt("status");
                        if (i == 1) {
                            a = 1;
                        } else if (i == 0) {
                            a2 = "authcsid";
                            str2 = "authgsid";
                            if (hfVar != null) {
                                str3 = hfVar.c;
                                if (hfVar.b != null) {
                                    list = (List) hfVar.b.get("gsid");
                                    if (list != null) {
                                        a2 = (String) list.get(0);
                                        str2 = str3;
                                    }
                                    a2 = str2;
                                    str2 = str3;
                                } else {
                                    a2 = str2;
                                    str2 = str3;
                                }
                            } else {
                                str4 = str2;
                                str2 = a2;
                                a2 = str4;
                            }
                            fi.a(context, str2, a2, jSONObject.toString());
                            a = 0;
                            if (jSONObject.has("info")) {
                                b = jSONObject.getString("info");
                            }
                            if (a == 0) {
                                aVar2.a = b;
                                return aVar2;
                            }
                        }
                        if (jSONObject.has("ver")) {
                            aVar2.b = jSONObject.getInt("ver");
                        }
                        if (fi.a(jSONObject, "result")) {
                            aVar = new a();
                            aVar.a = false;
                            aVar.b = false;
                            aVar2.p = aVar;
                            jSONObject2 = jSONObject.getJSONObject("result");
                            if (fi.a(jSONObject2, "11K")) {
                                aVar.a = a(jSONObject2.getJSONObject("11K").getString("able"), false);
                            }
                            if (fi.a(jSONObject2, "11B")) {
                                aVar2.d = jSONObject2.getJSONObject("11B");
                            }
                            if (fi.a(jSONObject2, "11C")) {
                                aVar2.g = jSONObject2.getJSONObject("11C");
                            }
                            if (fi.a(jSONObject2, "11I")) {
                                aVar2.h = jSONObject2.getJSONObject("11I");
                            }
                            if (fi.a(jSONObject2, "11H")) {
                                aVar2.i = jSONObject2.getJSONObject("11H");
                            }
                            if (fi.a(jSONObject2, "11E")) {
                                aVar2.j = jSONObject2.getJSONObject("11E");
                            }
                            if (fi.a(jSONObject2, "11F")) {
                                aVar2.k = jSONObject2.getJSONObject("11F");
                            }
                            if (fi.a(jSONObject2, "13A")) {
                                aVar2.m = jSONObject2.getJSONObject("13A");
                            }
                            if (fi.a(jSONObject2, "13J")) {
                                aVar2.e = jSONObject2.getJSONObject("13J");
                            }
                            if (fi.a(jSONObject2, "11G")) {
                                aVar2.l = jSONObject2.getJSONObject("11G");
                            }
                            if (fi.a(jSONObject2, "001")) {
                                jSONObject3 = jSONObject2.getJSONObject("001");
                                dVar = new d();
                                a(jSONObject3, dVar);
                                aVar2.q = dVar;
                            }
                            if (fi.a(jSONObject2, "002")) {
                                jSONObject3 = jSONObject2.getJSONObject("002");
                                cVar = new c();
                                a(jSONObject3, cVar);
                                aVar2.r = cVar;
                            }
                            if (fi.a(jSONObject2, "006")) {
                                aVar2.n = jSONObject2.getJSONObject("006");
                            }
                            if (fi.a(jSONObject2, "010")) {
                                aVar2.o = jSONObject2.getJSONObject("010");
                            }
                            if (fi.a(jSONObject2, "11Z")) {
                                jSONObject3 = jSONObject2.getJSONObject("11Z");
                                bVar = new b();
                                a(jSONObject3, bVar);
                                aVar2.s = bVar;
                            }
                            if (fi.a(jSONObject2, "135")) {
                                aVar2.f = jSONObject2.getJSONObject("135");
                            }
                            if (fi.a(jSONObject2, "13S")) {
                                aVar2.c = jSONObject2.getJSONObject("13S");
                            }
                            if (fi.a(jSONObject2, "121")) {
                                jSONObject3 = jSONObject2.getJSONObject("121");
                                bVar = new b();
                                a(jSONObject3, bVar);
                                aVar2.t = bVar;
                            }
                            if (fi.a(jSONObject2, "122")) {
                                jSONObject3 = jSONObject2.getJSONObject("122");
                                bVar = new b();
                                a(jSONObject3, bVar);
                                aVar2.u = bVar;
                            }
                            if (fi.a(jSONObject2, "123")) {
                                jSONObject3 = jSONObject2.getJSONObject("123");
                                bVar2 = new b();
                                a(jSONObject3, bVar2);
                                aVar2.v = bVar2;
                            }
                        }
                        return aVar2;
                    }
                    return aVar2;
                }
            }
            try {
                obj4 = new byte[16];
                Object obj5 = new byte[(obj.length - 16)];
                System.arraycopy(obj, 0, obj4, 0, 16);
                System.arraycopy(obj, 16, obj5, 0, obj.length - 16);
                Key secretKeySpec = new SecretKeySpec(obj4, "AES");
                Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
                instance.init(2, secretKeySpec, new IvParameterSpec(fi.a()));
                a2 = fi.a(instance.doFinal(obj5));
                obj2 = obj;
                hfVar = a;
                bArr = obj2;
            } catch (ex e4) {
                e = e4;
                aVar2.a = e.a();
                obj4 = hfVar2;
                obj2 = obj;
                hfVar = a;
                obj3 = obj2;
                if (bArr == null) {
                    return aVar2;
                }
                if (TextUtils.isEmpty(a2)) {
                    a2 = fi.a(bArr);
                }
                jSONObject = new JSONObject(a2);
                if (jSONObject.has("status")) {
                    i = jSONObject.getInt("status");
                    if (i == 1) {
                        a = 1;
                    } else if (i == 0) {
                        a2 = "authcsid";
                        str2 = "authgsid";
                        if (hfVar != null) {
                            str3 = hfVar.c;
                            if (hfVar.b != null) {
                                list = (List) hfVar.b.get("gsid");
                                if (list != null) {
                                    a2 = (String) list.get(0);
                                    str2 = str3;
                                }
                                a2 = str2;
                                str2 = str3;
                            } else {
                                a2 = str2;
                                str2 = str3;
                            }
                        } else {
                            str4 = str2;
                            str2 = a2;
                            a2 = str4;
                        }
                        fi.a(context, str2, a2, jSONObject.toString());
                        a = 0;
                        if (jSONObject.has("info")) {
                            b = jSONObject.getString("info");
                        }
                        if (a == 0) {
                            aVar2.a = b;
                            return aVar2;
                        }
                    }
                    if (jSONObject.has("ver")) {
                        aVar2.b = jSONObject.getInt("ver");
                    }
                    if (fi.a(jSONObject, "result")) {
                        aVar = new a();
                        aVar.a = false;
                        aVar.b = false;
                        aVar2.p = aVar;
                        jSONObject2 = jSONObject.getJSONObject("result");
                        if (fi.a(jSONObject2, "11K")) {
                            aVar.a = a(jSONObject2.getJSONObject("11K").getString("able"), false);
                        }
                        if (fi.a(jSONObject2, "11B")) {
                            aVar2.d = jSONObject2.getJSONObject("11B");
                        }
                        if (fi.a(jSONObject2, "11C")) {
                            aVar2.g = jSONObject2.getJSONObject("11C");
                        }
                        if (fi.a(jSONObject2, "11I")) {
                            aVar2.h = jSONObject2.getJSONObject("11I");
                        }
                        if (fi.a(jSONObject2, "11H")) {
                            aVar2.i = jSONObject2.getJSONObject("11H");
                        }
                        if (fi.a(jSONObject2, "11E")) {
                            aVar2.j = jSONObject2.getJSONObject("11E");
                        }
                        if (fi.a(jSONObject2, "11F")) {
                            aVar2.k = jSONObject2.getJSONObject("11F");
                        }
                        if (fi.a(jSONObject2, "13A")) {
                            aVar2.m = jSONObject2.getJSONObject("13A");
                        }
                        if (fi.a(jSONObject2, "13J")) {
                            aVar2.e = jSONObject2.getJSONObject("13J");
                        }
                        if (fi.a(jSONObject2, "11G")) {
                            aVar2.l = jSONObject2.getJSONObject("11G");
                        }
                        if (fi.a(jSONObject2, "001")) {
                            jSONObject3 = jSONObject2.getJSONObject("001");
                            dVar = new d();
                            a(jSONObject3, dVar);
                            aVar2.q = dVar;
                        }
                        if (fi.a(jSONObject2, "002")) {
                            jSONObject3 = jSONObject2.getJSONObject("002");
                            cVar = new c();
                            a(jSONObject3, cVar);
                            aVar2.r = cVar;
                        }
                        if (fi.a(jSONObject2, "006")) {
                            aVar2.n = jSONObject2.getJSONObject("006");
                        }
                        if (fi.a(jSONObject2, "010")) {
                            aVar2.o = jSONObject2.getJSONObject("010");
                        }
                        if (fi.a(jSONObject2, "11Z")) {
                            jSONObject3 = jSONObject2.getJSONObject("11Z");
                            bVar = new b();
                            a(jSONObject3, bVar);
                            aVar2.s = bVar;
                        }
                        if (fi.a(jSONObject2, "135")) {
                            aVar2.f = jSONObject2.getJSONObject("135");
                        }
                        if (fi.a(jSONObject2, "13S")) {
                            aVar2.c = jSONObject2.getJSONObject("13S");
                        }
                        if (fi.a(jSONObject2, "121")) {
                            jSONObject3 = jSONObject2.getJSONObject("121");
                            bVar = new b();
                            a(jSONObject3, bVar);
                            aVar2.t = bVar;
                        }
                        if (fi.a(jSONObject2, "122")) {
                            jSONObject3 = jSONObject2.getJSONObject("122");
                            bVar = new b();
                            a(jSONObject3, bVar);
                            aVar2.u = bVar;
                        }
                        if (fi.a(jSONObject2, "123")) {
                            jSONObject3 = jSONObject2.getJSONObject("123");
                            bVar2 = new b();
                            a(jSONObject3, bVar2);
                            aVar2.v = bVar2;
                        }
                    }
                    return aVar2;
                }
                return aVar2;
            } catch (IllegalBlockSizeException e5) {
                obj4 = hfVar2;
                obj2 = obj;
                hfVar = a;
                obj3 = obj2;
                if (bArr == null) {
                    return aVar2;
                }
                if (TextUtils.isEmpty(a2)) {
                    a2 = fi.a(bArr);
                }
                jSONObject = new JSONObject(a2);
                if (jSONObject.has("status")) {
                    i = jSONObject.getInt("status");
                    if (i == 1) {
                        a = 1;
                    } else if (i == 0) {
                        a2 = "authcsid";
                        str2 = "authgsid";
                        if (hfVar != null) {
                            str4 = str2;
                            str2 = a2;
                            a2 = str4;
                        } else {
                            str3 = hfVar.c;
                            if (hfVar.b != null) {
                                a2 = str2;
                                str2 = str3;
                            } else {
                                list = (List) hfVar.b.get("gsid");
                                if (list != null) {
                                    a2 = (String) list.get(0);
                                    str2 = str3;
                                }
                                a2 = str2;
                                str2 = str3;
                            }
                        }
                        fi.a(context, str2, a2, jSONObject.toString());
                        a = 0;
                        if (jSONObject.has("info")) {
                            b = jSONObject.getString("info");
                        }
                        if (a == 0) {
                            aVar2.a = b;
                            return aVar2;
                        }
                    }
                    if (jSONObject.has("ver")) {
                        aVar2.b = jSONObject.getInt("ver");
                    }
                    if (fi.a(jSONObject, "result")) {
                        aVar = new a();
                        aVar.a = false;
                        aVar.b = false;
                        aVar2.p = aVar;
                        jSONObject2 = jSONObject.getJSONObject("result");
                        if (fi.a(jSONObject2, "11K")) {
                            aVar.a = a(jSONObject2.getJSONObject("11K").getString("able"), false);
                        }
                        if (fi.a(jSONObject2, "11B")) {
                            aVar2.d = jSONObject2.getJSONObject("11B");
                        }
                        if (fi.a(jSONObject2, "11C")) {
                            aVar2.g = jSONObject2.getJSONObject("11C");
                        }
                        if (fi.a(jSONObject2, "11I")) {
                            aVar2.h = jSONObject2.getJSONObject("11I");
                        }
                        if (fi.a(jSONObject2, "11H")) {
                            aVar2.i = jSONObject2.getJSONObject("11H");
                        }
                        if (fi.a(jSONObject2, "11E")) {
                            aVar2.j = jSONObject2.getJSONObject("11E");
                        }
                        if (fi.a(jSONObject2, "11F")) {
                            aVar2.k = jSONObject2.getJSONObject("11F");
                        }
                        if (fi.a(jSONObject2, "13A")) {
                            aVar2.m = jSONObject2.getJSONObject("13A");
                        }
                        if (fi.a(jSONObject2, "13J")) {
                            aVar2.e = jSONObject2.getJSONObject("13J");
                        }
                        if (fi.a(jSONObject2, "11G")) {
                            aVar2.l = jSONObject2.getJSONObject("11G");
                        }
                        if (fi.a(jSONObject2, "001")) {
                            jSONObject3 = jSONObject2.getJSONObject("001");
                            dVar = new d();
                            a(jSONObject3, dVar);
                            aVar2.q = dVar;
                        }
                        if (fi.a(jSONObject2, "002")) {
                            jSONObject3 = jSONObject2.getJSONObject("002");
                            cVar = new c();
                            a(jSONObject3, cVar);
                            aVar2.r = cVar;
                        }
                        if (fi.a(jSONObject2, "006")) {
                            aVar2.n = jSONObject2.getJSONObject("006");
                        }
                        if (fi.a(jSONObject2, "010")) {
                            aVar2.o = jSONObject2.getJSONObject("010");
                        }
                        if (fi.a(jSONObject2, "11Z")) {
                            jSONObject3 = jSONObject2.getJSONObject("11Z");
                            bVar = new b();
                            a(jSONObject3, bVar);
                            aVar2.s = bVar;
                        }
                        if (fi.a(jSONObject2, "135")) {
                            aVar2.f = jSONObject2.getJSONObject("135");
                        }
                        if (fi.a(jSONObject2, "13S")) {
                            aVar2.c = jSONObject2.getJSONObject("13S");
                        }
                        if (fi.a(jSONObject2, "121")) {
                            jSONObject3 = jSONObject2.getJSONObject("121");
                            bVar = new b();
                            a(jSONObject3, bVar);
                            aVar2.t = bVar;
                        }
                        if (fi.a(jSONObject2, "122")) {
                            jSONObject3 = jSONObject2.getJSONObject("122");
                            bVar = new b();
                            a(jSONObject3, bVar);
                            aVar2.u = bVar;
                        }
                        if (fi.a(jSONObject2, "123")) {
                            jSONObject3 = jSONObject2.getJSONObject("123");
                            bVar2 = new b();
                            a(jSONObject3, bVar2);
                            aVar2.v = bVar2;
                        }
                    }
                    return aVar2;
                }
                return aVar2;
            } catch (Throwable th4) {
                th22 = th4;
                fl.a(th22, "ConfigManager", "loadConfig");
                obj4 = hfVar2;
                obj2 = obj;
                hfVar = a;
                obj3 = obj2;
                if (bArr == null) {
                    return aVar2;
                }
                if (TextUtils.isEmpty(a2)) {
                    a2 = fi.a(bArr);
                }
                jSONObject = new JSONObject(a2);
                if (jSONObject.has("status")) {
                    i = jSONObject.getInt("status");
                    if (i == 1) {
                        a = 1;
                    } else if (i == 0) {
                        a2 = "authcsid";
                        str2 = "authgsid";
                        if (hfVar != null) {
                            str3 = hfVar.c;
                            if (hfVar.b != null) {
                                list = (List) hfVar.b.get("gsid");
                                if (list != null) {
                                    a2 = (String) list.get(0);
                                    str2 = str3;
                                }
                                a2 = str2;
                                str2 = str3;
                            } else {
                                a2 = str2;
                                str2 = str3;
                            }
                        } else {
                            str4 = str2;
                            str2 = a2;
                            a2 = str4;
                        }
                        fi.a(context, str2, a2, jSONObject.toString());
                        a = 0;
                        if (jSONObject.has("info")) {
                            b = jSONObject.getString("info");
                        }
                        if (a == 0) {
                            aVar2.a = b;
                            return aVar2;
                        }
                    }
                    if (jSONObject.has("ver")) {
                        aVar2.b = jSONObject.getInt("ver");
                    }
                    if (fi.a(jSONObject, "result")) {
                        aVar = new a();
                        aVar.a = false;
                        aVar.b = false;
                        aVar2.p = aVar;
                        jSONObject2 = jSONObject.getJSONObject("result");
                        if (fi.a(jSONObject2, "11K")) {
                            aVar.a = a(jSONObject2.getJSONObject("11K").getString("able"), false);
                        }
                        if (fi.a(jSONObject2, "11B")) {
                            aVar2.d = jSONObject2.getJSONObject("11B");
                        }
                        if (fi.a(jSONObject2, "11C")) {
                            aVar2.g = jSONObject2.getJSONObject("11C");
                        }
                        if (fi.a(jSONObject2, "11I")) {
                            aVar2.h = jSONObject2.getJSONObject("11I");
                        }
                        if (fi.a(jSONObject2, "11H")) {
                            aVar2.i = jSONObject2.getJSONObject("11H");
                        }
                        if (fi.a(jSONObject2, "11E")) {
                            aVar2.j = jSONObject2.getJSONObject("11E");
                        }
                        if (fi.a(jSONObject2, "11F")) {
                            aVar2.k = jSONObject2.getJSONObject("11F");
                        }
                        if (fi.a(jSONObject2, "13A")) {
                            aVar2.m = jSONObject2.getJSONObject("13A");
                        }
                        if (fi.a(jSONObject2, "13J")) {
                            aVar2.e = jSONObject2.getJSONObject("13J");
                        }
                        if (fi.a(jSONObject2, "11G")) {
                            aVar2.l = jSONObject2.getJSONObject("11G");
                        }
                        if (fi.a(jSONObject2, "001")) {
                            jSONObject3 = jSONObject2.getJSONObject("001");
                            dVar = new d();
                            a(jSONObject3, dVar);
                            aVar2.q = dVar;
                        }
                        if (fi.a(jSONObject2, "002")) {
                            jSONObject3 = jSONObject2.getJSONObject("002");
                            cVar = new c();
                            a(jSONObject3, cVar);
                            aVar2.r = cVar;
                        }
                        if (fi.a(jSONObject2, "006")) {
                            aVar2.n = jSONObject2.getJSONObject("006");
                        }
                        if (fi.a(jSONObject2, "010")) {
                            aVar2.o = jSONObject2.getJSONObject("010");
                        }
                        if (fi.a(jSONObject2, "11Z")) {
                            jSONObject3 = jSONObject2.getJSONObject("11Z");
                            bVar = new b();
                            a(jSONObject3, bVar);
                            aVar2.s = bVar;
                        }
                        if (fi.a(jSONObject2, "135")) {
                            aVar2.f = jSONObject2.getJSONObject("135");
                        }
                        if (fi.a(jSONObject2, "13S")) {
                            aVar2.c = jSONObject2.getJSONObject("13S");
                        }
                        if (fi.a(jSONObject2, "121")) {
                            jSONObject3 = jSONObject2.getJSONObject("121");
                            bVar = new b();
                            a(jSONObject3, bVar);
                            aVar2.t = bVar;
                        }
                        if (fi.a(jSONObject2, "122")) {
                            jSONObject3 = jSONObject2.getJSONObject("122");
                            bVar = new b();
                            a(jSONObject3, bVar);
                            aVar2.u = bVar;
                        }
                        if (fi.a(jSONObject2, "123")) {
                            jSONObject3 = jSONObject2.getJSONObject("123");
                            bVar2 = new b();
                            a(jSONObject3, bVar2);
                            aVar2.v = bVar2;
                        }
                    }
                    return aVar2;
                }
                return aVar2;
            }
        } catch (ex e6) {
            throw e6;
        } catch (IllegalBlockSizeException e7) {
            a = hfVar2;
            hfVar = hfVar2;
            obj4 = hfVar2;
            obj2 = obj;
            hfVar = a;
            obj3 = obj2;
            if (bArr == null) {
                return aVar2;
            }
            if (TextUtils.isEmpty(a2)) {
                a2 = fi.a(bArr);
            }
            jSONObject = new JSONObject(a2);
            if (jSONObject.has("status")) {
                i = jSONObject.getInt("status");
                if (i == 1) {
                    a = 1;
                } else if (i == 0) {
                    a2 = "authcsid";
                    str2 = "authgsid";
                    if (hfVar != null) {
                        str3 = hfVar.c;
                        if (hfVar.b != null) {
                            list = (List) hfVar.b.get("gsid");
                            if (list != null) {
                                a2 = (String) list.get(0);
                                str2 = str3;
                            }
                            a2 = str2;
                            str2 = str3;
                        } else {
                            a2 = str2;
                            str2 = str3;
                        }
                    } else {
                        str4 = str2;
                        str2 = a2;
                        a2 = str4;
                    }
                    fi.a(context, str2, a2, jSONObject.toString());
                    a = 0;
                    if (jSONObject.has("info")) {
                        b = jSONObject.getString("info");
                    }
                    if (a == 0) {
                        aVar2.a = b;
                        return aVar2;
                    }
                }
                if (jSONObject.has("ver")) {
                    aVar2.b = jSONObject.getInt("ver");
                }
                if (fi.a(jSONObject, "result")) {
                    aVar = new a();
                    aVar.a = false;
                    aVar.b = false;
                    aVar2.p = aVar;
                    jSONObject2 = jSONObject.getJSONObject("result");
                    if (fi.a(jSONObject2, "11K")) {
                        aVar.a = a(jSONObject2.getJSONObject("11K").getString("able"), false);
                    }
                    if (fi.a(jSONObject2, "11B")) {
                        aVar2.d = jSONObject2.getJSONObject("11B");
                    }
                    if (fi.a(jSONObject2, "11C")) {
                        aVar2.g = jSONObject2.getJSONObject("11C");
                    }
                    if (fi.a(jSONObject2, "11I")) {
                        aVar2.h = jSONObject2.getJSONObject("11I");
                    }
                    if (fi.a(jSONObject2, "11H")) {
                        aVar2.i = jSONObject2.getJSONObject("11H");
                    }
                    if (fi.a(jSONObject2, "11E")) {
                        aVar2.j = jSONObject2.getJSONObject("11E");
                    }
                    if (fi.a(jSONObject2, "11F")) {
                        aVar2.k = jSONObject2.getJSONObject("11F");
                    }
                    if (fi.a(jSONObject2, "13A")) {
                        aVar2.m = jSONObject2.getJSONObject("13A");
                    }
                    if (fi.a(jSONObject2, "13J")) {
                        aVar2.e = jSONObject2.getJSONObject("13J");
                    }
                    if (fi.a(jSONObject2, "11G")) {
                        aVar2.l = jSONObject2.getJSONObject("11G");
                    }
                    if (fi.a(jSONObject2, "001")) {
                        jSONObject3 = jSONObject2.getJSONObject("001");
                        dVar = new d();
                        a(jSONObject3, dVar);
                        aVar2.q = dVar;
                    }
                    if (fi.a(jSONObject2, "002")) {
                        jSONObject3 = jSONObject2.getJSONObject("002");
                        cVar = new c();
                        a(jSONObject3, cVar);
                        aVar2.r = cVar;
                    }
                    if (fi.a(jSONObject2, "006")) {
                        aVar2.n = jSONObject2.getJSONObject("006");
                    }
                    if (fi.a(jSONObject2, "010")) {
                        aVar2.o = jSONObject2.getJSONObject("010");
                    }
                    if (fi.a(jSONObject2, "11Z")) {
                        jSONObject3 = jSONObject2.getJSONObject("11Z");
                        bVar = new b();
                        a(jSONObject3, bVar);
                        aVar2.s = bVar;
                    }
                    if (fi.a(jSONObject2, "135")) {
                        aVar2.f = jSONObject2.getJSONObject("135");
                    }
                    if (fi.a(jSONObject2, "13S")) {
                        aVar2.c = jSONObject2.getJSONObject("13S");
                    }
                    if (fi.a(jSONObject2, "121")) {
                        jSONObject3 = jSONObject2.getJSONObject("121");
                        bVar = new b();
                        a(jSONObject3, bVar);
                        aVar2.t = bVar;
                    }
                    if (fi.a(jSONObject2, "122")) {
                        jSONObject3 = jSONObject2.getJSONObject("122");
                        bVar = new b();
                        a(jSONObject3, bVar);
                        aVar2.u = bVar;
                    }
                    if (fi.a(jSONObject2, "123")) {
                        jSONObject3 = jSONObject2.getJSONObject("123");
                        bVar2 = new b();
                        a(jSONObject3, bVar2);
                        aVar2.v = bVar2;
                    }
                }
                return aVar2;
            }
            return aVar2;
        } catch (ex e8) {
            e6 = e8;
            a = hfVar2;
            obj = hfVar2;
        } catch (Throwable th5) {
            th22 = th5;
            a = hfVar2;
            obj = hfVar2;
            fl.a(th22, "ConfigManager", "loadConfig");
            obj4 = hfVar2;
            obj2 = obj;
            hfVar = a;
            obj3 = obj2;
            if (bArr == null) {
                return aVar2;
            }
            if (TextUtils.isEmpty(a2)) {
                a2 = fi.a(bArr);
            }
            jSONObject = new JSONObject(a2);
            if (jSONObject.has("status")) {
                i = jSONObject.getInt("status");
                if (i == 1) {
                    a = 1;
                } else if (i == 0) {
                    a2 = "authcsid";
                    str2 = "authgsid";
                    if (hfVar != null) {
                        str4 = str2;
                        str2 = a2;
                        a2 = str4;
                    } else {
                        str3 = hfVar.c;
                        if (hfVar.b != null) {
                            a2 = str2;
                            str2 = str3;
                        } else {
                            list = (List) hfVar.b.get("gsid");
                            if (list != null) {
                                a2 = (String) list.get(0);
                                str2 = str3;
                            }
                            a2 = str2;
                            str2 = str3;
                        }
                    }
                    fi.a(context, str2, a2, jSONObject.toString());
                    a = 0;
                    if (jSONObject.has("info")) {
                        b = jSONObject.getString("info");
                    }
                    if (a == 0) {
                        aVar2.a = b;
                        return aVar2;
                    }
                }
                if (jSONObject.has("ver")) {
                    aVar2.b = jSONObject.getInt("ver");
                }
                if (fi.a(jSONObject, "result")) {
                    aVar = new a();
                    aVar.a = false;
                    aVar.b = false;
                    aVar2.p = aVar;
                    jSONObject2 = jSONObject.getJSONObject("result");
                    if (fi.a(jSONObject2, "11K")) {
                        aVar.a = a(jSONObject2.getJSONObject("11K").getString("able"), false);
                    }
                    if (fi.a(jSONObject2, "11B")) {
                        aVar2.d = jSONObject2.getJSONObject("11B");
                    }
                    if (fi.a(jSONObject2, "11C")) {
                        aVar2.g = jSONObject2.getJSONObject("11C");
                    }
                    if (fi.a(jSONObject2, "11I")) {
                        aVar2.h = jSONObject2.getJSONObject("11I");
                    }
                    if (fi.a(jSONObject2, "11H")) {
                        aVar2.i = jSONObject2.getJSONObject("11H");
                    }
                    if (fi.a(jSONObject2, "11E")) {
                        aVar2.j = jSONObject2.getJSONObject("11E");
                    }
                    if (fi.a(jSONObject2, "11F")) {
                        aVar2.k = jSONObject2.getJSONObject("11F");
                    }
                    if (fi.a(jSONObject2, "13A")) {
                        aVar2.m = jSONObject2.getJSONObject("13A");
                    }
                    if (fi.a(jSONObject2, "13J")) {
                        aVar2.e = jSONObject2.getJSONObject("13J");
                    }
                    if (fi.a(jSONObject2, "11G")) {
                        aVar2.l = jSONObject2.getJSONObject("11G");
                    }
                    if (fi.a(jSONObject2, "001")) {
                        jSONObject3 = jSONObject2.getJSONObject("001");
                        dVar = new d();
                        a(jSONObject3, dVar);
                        aVar2.q = dVar;
                    }
                    if (fi.a(jSONObject2, "002")) {
                        jSONObject3 = jSONObject2.getJSONObject("002");
                        cVar = new c();
                        a(jSONObject3, cVar);
                        aVar2.r = cVar;
                    }
                    if (fi.a(jSONObject2, "006")) {
                        aVar2.n = jSONObject2.getJSONObject("006");
                    }
                    if (fi.a(jSONObject2, "010")) {
                        aVar2.o = jSONObject2.getJSONObject("010");
                    }
                    if (fi.a(jSONObject2, "11Z")) {
                        jSONObject3 = jSONObject2.getJSONObject("11Z");
                        bVar = new b();
                        a(jSONObject3, bVar);
                        aVar2.s = bVar;
                    }
                    if (fi.a(jSONObject2, "135")) {
                        aVar2.f = jSONObject2.getJSONObject("135");
                    }
                    if (fi.a(jSONObject2, "13S")) {
                        aVar2.c = jSONObject2.getJSONObject("13S");
                    }
                    if (fi.a(jSONObject2, "121")) {
                        jSONObject3 = jSONObject2.getJSONObject("121");
                        bVar = new b();
                        a(jSONObject3, bVar);
                        aVar2.t = bVar;
                    }
                    if (fi.a(jSONObject2, "122")) {
                        jSONObject3 = jSONObject2.getJSONObject("122");
                        bVar = new b();
                        a(jSONObject3, bVar);
                        aVar2.u = bVar;
                    }
                    if (fi.a(jSONObject2, "123")) {
                        jSONObject3 = jSONObject2.getJSONObject("123");
                        bVar2 = new b();
                        a(jSONObject3, bVar2);
                        aVar2.v = bVar2;
                    }
                }
                return aVar2;
            }
            return aVar2;
        }
        if (bArr == null) {
            return aVar2;
        }
        if (TextUtils.isEmpty(a2)) {
            a2 = fi.a(bArr);
        }
        jSONObject = new JSONObject(a2);
        if (jSONObject.has("status")) {
            i = jSONObject.getInt("status");
            if (i == 1) {
                a = 1;
            } else if (i == 0) {
                a2 = "authcsid";
                str2 = "authgsid";
                if (hfVar != null) {
                    str4 = str2;
                    str2 = a2;
                    a2 = str4;
                } else {
                    str3 = hfVar.c;
                    if (hfVar.b != null) {
                        a2 = str2;
                        str2 = str3;
                    } else {
                        list = (List) hfVar.b.get("gsid");
                        if (list != null && list.size() > 0) {
                            a2 = (String) list.get(0);
                            str2 = str3;
                        } else {
                            a2 = str2;
                            str2 = str3;
                        }
                    }
                }
                fi.a(context, str2, a2, jSONObject.toString());
                a = 0;
                if (jSONObject.has("info")) {
                    b = jSONObject.getString("info");
                }
                if (a == 0) {
                    aVar2.a = b;
                    return aVar2;
                }
            }
            if (jSONObject.has("ver")) {
                aVar2.b = jSONObject.getInt("ver");
            }
            if (fi.a(jSONObject, "result")) {
                aVar = new a();
                aVar.a = false;
                aVar.b = false;
                aVar2.p = aVar;
                jSONObject2 = jSONObject.getJSONObject("result");
                if (fi.a(jSONObject2, "11K")) {
                    aVar.a = a(jSONObject2.getJSONObject("11K").getString("able"), false);
                }
                if (fi.a(jSONObject2, "11B")) {
                    aVar2.d = jSONObject2.getJSONObject("11B");
                }
                if (fi.a(jSONObject2, "11C")) {
                    aVar2.g = jSONObject2.getJSONObject("11C");
                }
                if (fi.a(jSONObject2, "11I")) {
                    aVar2.h = jSONObject2.getJSONObject("11I");
                }
                if (fi.a(jSONObject2, "11H")) {
                    aVar2.i = jSONObject2.getJSONObject("11H");
                }
                if (fi.a(jSONObject2, "11E")) {
                    aVar2.j = jSONObject2.getJSONObject("11E");
                }
                if (fi.a(jSONObject2, "11F")) {
                    aVar2.k = jSONObject2.getJSONObject("11F");
                }
                if (fi.a(jSONObject2, "13A")) {
                    aVar2.m = jSONObject2.getJSONObject("13A");
                }
                if (fi.a(jSONObject2, "13J")) {
                    aVar2.e = jSONObject2.getJSONObject("13J");
                }
                if (fi.a(jSONObject2, "11G")) {
                    aVar2.l = jSONObject2.getJSONObject("11G");
                }
                if (fi.a(jSONObject2, "001")) {
                    jSONObject3 = jSONObject2.getJSONObject("001");
                    dVar = new d();
                    a(jSONObject3, dVar);
                    aVar2.q = dVar;
                }
                if (fi.a(jSONObject2, "002")) {
                    jSONObject3 = jSONObject2.getJSONObject("002");
                    cVar = new c();
                    a(jSONObject3, cVar);
                    aVar2.r = cVar;
                }
                if (fi.a(jSONObject2, "006")) {
                    aVar2.n = jSONObject2.getJSONObject("006");
                }
                if (fi.a(jSONObject2, "010")) {
                    aVar2.o = jSONObject2.getJSONObject("010");
                }
                if (fi.a(jSONObject2, "11Z")) {
                    jSONObject3 = jSONObject2.getJSONObject("11Z");
                    bVar = new b();
                    a(jSONObject3, bVar);
                    aVar2.s = bVar;
                }
                if (fi.a(jSONObject2, "135")) {
                    aVar2.f = jSONObject2.getJSONObject("135");
                }
                if (fi.a(jSONObject2, "13S")) {
                    aVar2.c = jSONObject2.getJSONObject("13S");
                }
                if (fi.a(jSONObject2, "121")) {
                    jSONObject3 = jSONObject2.getJSONObject("121");
                    bVar = new b();
                    a(jSONObject3, bVar);
                    aVar2.t = bVar;
                }
                if (fi.a(jSONObject2, "122")) {
                    jSONObject3 = jSONObject2.getJSONObject("122");
                    bVar = new b();
                    a(jSONObject3, bVar);
                    aVar2.u = bVar;
                }
                if (fi.a(jSONObject2, "123")) {
                    jSONObject3 = jSONObject2.getJSONObject("123");
                    bVar2 = new b();
                    a(jSONObject3, bVar2);
                    aVar2.v = bVar2;
                }
            }
            return aVar2;
        }
        return aVar2;
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
        if (bVar != null) {
            try {
                String a = a(jSONObject, "m");
                String a2 = a(jSONObject, "u");
                String a3 = a(jSONObject, "v");
                String a4 = a(jSONObject, "able");
                bVar.c = a;
                bVar.b = a2;
                bVar.d = a3;
                bVar.a = a(a4, false);
            } catch (Throwable th) {
                fl.a(th, "ConfigManager", "parsePluginEntity");
            }
        }
    }

    private static void a(JSONObject jSONObject, c cVar) {
        if (jSONObject != null) {
            try {
                String a = a(jSONObject, "md5");
                String a2 = a(jSONObject, "url");
                cVar.b = a;
                cVar.a = a2;
            } catch (Throwable th) {
                fl.a(th, "ConfigManager", "parseSDKCoordinate");
            }
        }
    }

    private static void a(JSONObject jSONObject, d dVar) {
        if (jSONObject != null) {
            try {
                Object a = a(jSONObject, "md5");
                Object a2 = a(jSONObject, "url");
                Object a3 = a(jSONObject, "sdkversion");
                if (!TextUtils.isEmpty(a) && !TextUtils.isEmpty(a2) && !TextUtils.isEmpty(a3)) {
                    dVar.a = a2;
                    dVar.b = a;
                    dVar.c = a3;
                }
            } catch (Throwable th) {
                fl.a(th, "ConfigManager", "parseSDKUpdate");
            }
        }
    }
}
