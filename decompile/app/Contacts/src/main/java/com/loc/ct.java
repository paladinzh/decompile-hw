package com.loc;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.SystemClock;
import android.text.TextUtils;
import com.loc.p.a.b;
import com.loc.p.a.c;
import org.json.JSONObject;

/* compiled from: AuthUtil */
public class ct {
    private static Context a;
    private static String b = "提示信息";
    private static String c = "确认";
    private static String d = "取消";
    private static String e = "";
    private static String f = "";
    private static String g = "";
    private static boolean h = false;
    private static long i = 0;
    private static long j = 0;
    private static boolean k = false;
    private static int l = 0;
    private static boolean m = false;
    private static int n = 0;
    private static boolean o = false;
    private static String p = CallInterceptDetails.BRANDED_STATE;
    private static String q = CallInterceptDetails.BRANDED_STATE;
    private static int r = -1;
    private static long s = 0;
    private static String t = "0";
    private static int u = -1;
    private static long v = 0;
    private static String w;
    private static String x;
    private static String y = "0";
    private static boolean z = false;

    /* compiled from: AuthUtil */
    static class a {
        String a = "0";
        String b = "0";
        String c = "0";

        a() {
        }
    }

    private static String a(JSONObject jSONObject, String str) {
        if (jSONObject == null) {
            return null;
        }
        String str2 = "";
        try {
            if (jSONObject.has(str) && !jSONObject.getString(str).equals("[]")) {
                str2 = jSONObject.optString(str);
            }
        } catch (Throwable th) {
            e.a(th, "AuthUtil", "parse2String");
        }
        return str2;
    }

    public static synchronized void a(String str) {
        synchronized (ct.class) {
            y = str;
        }
    }

    public static boolean a() {
        return h;
    }

    public static synchronized boolean a(Context context) {
        boolean z;
        synchronized (ct.class) {
            a = context;
            z = false;
            try {
                com.loc.p.a a = p.a(context, e.a("2.4.0"), "callamappro;fast;sdkupdate;sdkcoordinate;locate;opflag;exception;amappushflag");
                if (a != null) {
                    z = a(a);
                }
            } catch (Throwable th) {
                e.a(th, "AuthUtil", "getConfig");
            }
        }
        return z;
    }

    private static boolean a(com.loc.p.a aVar) {
        try {
            JSONObject jSONObject = aVar.b;
            if (jSONObject != null && jSONObject.has("opflag")) {
                p = jSONObject.getString("opflag");
            }
            jSONObject = aVar.c;
            if (jSONObject != null) {
                if (jSONObject.has("callamapflag")) {
                    q = jSONObject.getString("callamapflag");
                }
                if (jSONObject.has("count")) {
                    r = jSONObject.getInt("count");
                }
                if (jSONObject.has("nowtime")) {
                    s = jSONObject.getLong("nowtime");
                }
                if (!(r == -1 || s == 0)) {
                    if (!cw.a(s, cv.b(a, "pref", "nowtime", 0))) {
                        b(a);
                    }
                }
            }
            jSONObject = aVar.a;
            if (jSONObject != null) {
                if (jSONObject.has("amappushflag")) {
                    t = jSONObject.getString("amappushflag");
                }
                if (jSONObject.has("count")) {
                    u = jSONObject.getInt("count");
                }
                if (jSONObject.has("nowtime")) {
                    v = jSONObject.getLong("nowtime");
                }
                if (!(u == -1 || v == 0)) {
                    if (!cw.a(v, cv.b(a, "pref", "pushSerTime", 0))) {
                        c(a);
                    }
                }
            }
            JSONObject jSONObject2 = aVar.f;
            if (jSONObject2 != null) {
                if (jSONObject2.has("f")) {
                    y = a(jSONObject2, "f");
                    if (CallInterceptDetails.BRANDED_STATE.equals(y)) {
                        long b = cv.b(a, "abcd", "abc", 0);
                        long elapsedRealtime = SystemClock.elapsedRealtime();
                        if (!(elapsedRealtime - b <= 3600000)) {
                            cv.a(a, "abcd", "abc", elapsedRealtime);
                        }
                        if (!(elapsedRealtime <= b)) {
                            if (!(elapsedRealtime - b >= 3600000)) {
                                y = "0";
                            }
                        }
                        if (!(elapsedRealtime >= b)) {
                            y = "0";
                            cv.a(a, "abcd", "abc", elapsedRealtime);
                        }
                    } else {
                        y = "0";
                    }
                }
                if (jSONObject2.has("a")) {
                    b = a(jSONObject2, "a");
                }
                if (jSONObject2.has("o")) {
                    c = a(jSONObject2, "o");
                }
                if (jSONObject2.has("c")) {
                    d = a(jSONObject2, "c");
                }
                if (jSONObject2.has("i")) {
                    e = a(jSONObject2, "i");
                }
                if (jSONObject2.has("u")) {
                    f = a(jSONObject2, "u");
                }
                if (jSONObject2.has("t")) {
                    g = a(jSONObject2, "t");
                }
                if ("".equals(e) || e == null) {
                    if ("".equals(f) || f == null) {
                        y = "0";
                    }
                }
            }
            v a = e.a("2.4.0");
            c cVar = aVar.h;
            if (cVar == null) {
                new aw(a, null, a).a();
            } else {
                Object obj = cVar.b;
                Object obj2 = cVar.a;
                Object obj3 = cVar.c;
                if (TextUtils.isEmpty(obj) || TextUtils.isEmpty(obj2) || TextUtils.isEmpty(obj3)) {
                    new aw(a, null, a).a();
                } else {
                    new aw(a, new ax(obj2, obj, obj3), a).a();
                }
            }
            b bVar = aVar.i;
            if (bVar != null) {
                w = bVar.a;
                x = bVar.b;
                if (!(TextUtils.isEmpty(w) || TextUtils.isEmpty(x))) {
                    new u(a, "loc", w, x).a();
                }
            }
            com.loc.p.a.a aVar2 = aVar.g;
            if (aVar2 != null) {
                boolean z = aVar2.a;
                v a2 = e.a("2.4.0");
                a2.a(z);
                ab.a(a, a2);
            }
            jSONObject2 = aVar.d;
            if (jSONObject2 != null) {
                a b2 = b(jSONObject2, "fs");
                if (b2 != null) {
                    k = b2.c.equals(CallInterceptDetails.BRANDED_STATE);
                    try {
                        l = Integer.parseInt(b2.b);
                    } catch (Throwable th) {
                        e.a(th, "AuthUtil", "loadconfig part2");
                    }
                }
                b2 = b(jSONObject2, "us");
                if (b2 != null) {
                    m = b2.c.equals(CallInterceptDetails.BRANDED_STATE);
                    o = !b2.a.equals("0");
                    try {
                        n = Integer.parseInt(b2.b);
                    } catch (Throwable th2) {
                        e.a(th2, "AuthUtil", "loadconfig part1");
                    }
                }
                a b3 = b(jSONObject2, "rs");
                if (b3 != null) {
                    h = b3.c.equals(CallInterceptDetails.BRANDED_STATE);
                    if (h) {
                        j = cw.b();
                    }
                    i = (long) (Integer.parseInt(b3.b) * 1000);
                }
            }
        } catch (Throwable th22) {
            e.a(th22, "AuthUtil", "loadconfig");
            y = "0";
            return false;
        }
        return true;
    }

    public static long b() {
        return i;
    }

    private static a b(JSONObject jSONObject, String str) {
        Throwable th;
        a aVar = null;
        if (jSONObject != null) {
            a aVar2;
            try {
                if (jSONObject.has(str)) {
                    JSONObject jSONObject2 = jSONObject.getJSONObject(str);
                    if (jSONObject2 != null) {
                        aVar2 = new a();
                        try {
                            if (jSONObject2.has("b")) {
                                aVar2.a = a(jSONObject2, "b");
                            }
                            if (jSONObject2.has("t")) {
                                aVar2.b = a(jSONObject2, "t");
                            }
                            if (jSONObject2.has("st")) {
                                aVar2.c = a(jSONObject2, "st");
                            }
                            aVar = aVar2;
                        } catch (Throwable th2) {
                            th = th2;
                            e.a(th, "AuthUtil", "getLocateObj");
                            return aVar2;
                        }
                    }
                }
            } catch (Throwable th3) {
                Throwable th4 = th3;
                aVar2 = null;
                th = th4;
                e.a(th, "AuthUtil", "getLocateObj");
                return aVar2;
            }
        }
        return aVar;
    }

    private static void b(Context context) {
        Editor edit;
        try {
            edit = context.getSharedPreferences("pref", 0).edit();
            if (s == 0) {
                edit.remove("nowtime");
            } else {
                edit.putLong("nowtime", s);
            }
        } catch (Throwable th) {
            e.a(th, "AuthUtil", "resetPrefsBind");
            return;
        }
        if (r != -1) {
            edit.putInt("count", 0);
        } else {
            edit.remove("count");
        }
        cv.a(edit);
    }

    public static long c() {
        return j;
    }

    private static void c(Context context) {
        Editor edit;
        try {
            edit = context.getSharedPreferences("pref", 0).edit();
            if (v == 0) {
                edit.remove("pushSerTime");
            } else {
                edit.putLong("pushSerTime", v);
            }
        } catch (Throwable th) {
            e.a(th, "AuthUtil", "resetPrefsBind");
            return;
        }
        if (u != -1) {
            edit.putInt("pushCount", 0);
        } else {
            edit.remove("pushCount");
        }
        cv.a(edit);
    }

    public static boolean d() {
        return k;
    }

    public static int e() {
        return l;
    }

    public static boolean f() {
        return m;
    }

    public static int g() {
        return n;
    }

    public static boolean h() {
        return o;
    }

    public static boolean i() {
        boolean equals = CallInterceptDetails.BRANDED_STATE.equals(p);
        by.a = equals;
        return equals;
    }

    public static String j() {
        return b;
    }

    public static String k() {
        return c;
    }

    public static String l() {
        return d;
    }

    public static String m() {
        return e;
    }

    public static String n() {
        return f;
    }

    public static String o() {
        return g;
    }

    public static boolean p() {
        if (!CallInterceptDetails.BRANDED_STATE.equals(q)) {
            return false;
        }
        if (r == -1 || s == 0) {
            return true;
        }
        if (cw.a(s, cv.b(a, "pref", "nowtime", 0))) {
            int b = cv.b(a, "pref", "count", 0);
            if (b >= r) {
                return false;
            }
            cv.a(a, "pref", "count", b + 1);
            return true;
        }
        b(a);
        cv.a(a, "pref", "count", 1);
        return true;
    }

    public static boolean q() {
        if (!CallInterceptDetails.BRANDED_STATE.equals(t)) {
            return false;
        }
        if (u == -1 || v == 0) {
            return true;
        }
        if (cw.a(v, cv.b(a, "pref", "pushSerTime", 0))) {
            int b = cv.b(a, "pref", "pushCount", 0);
            if (b >= u) {
                return false;
            }
            cv.a(a, "pref", "pushCount", b + 1);
            return true;
        }
        c(a);
        cv.a(a, "pref", "pushCount", 1);
        return true;
    }

    public static synchronized boolean r() {
        boolean equals;
        synchronized (ct.class) {
            equals = CallInterceptDetails.BRANDED_STATE.equals(y);
        }
        return equals;
    }
}
