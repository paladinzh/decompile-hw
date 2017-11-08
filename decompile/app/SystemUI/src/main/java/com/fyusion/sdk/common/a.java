package com.fyusion.sdk.common;

import android.os.Handler;
import android.os.HandlerThread;
import fyusion.vislib.BuildConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public class a {
    private static a a;
    private static List<f> b;
    private static boolean c = false;
    private static String d;
    private static String e;
    private static int f;
    private static int g = 9;
    private static String h = null;

    private a() {
    }

    public static a a() {
        if (a == null) {
            synchronized (a.class) {
                if (a == null) {
                    a = new a();
                }
            }
        }
        return a;
    }

    private f a(String str, HashMap<String, Boolean> hashMap, boolean z) {
        f d = d(str);
        if (d != null) {
            d.a((HashMap) hashMap);
            d.a(z);
            b.a(str, z);
        }
        return d;
    }

    public static synchronized String a(String str) {
        String a;
        synchronized (a.class) {
            a = a(str, 2);
        }
        return a;
    }

    private static synchronized String a(String str, int i) {
        synchronized (a.class) {
            String str2;
            if (str != null) {
                String[] split = str.split("~");
                if (split != null) {
                    if (split.length > i) {
                        str2 = split[i];
                        return str2;
                    }
                }
                str2 = BuildConfig.FLAVOR;
                return str2;
            }
            str2 = BuildConfig.FLAVOR;
            return str2;
        }
    }

    public static synchronized String b(String str) {
        String a;
        synchronized (a.class) {
            a = a(str, 1);
        }
        return a;
    }

    public static synchronized String c(String str) {
        String a;
        synchronized (a.class) {
            a = a(str, 0);
        }
        return a;
    }

    private static void c(int i) {
        g = i;
    }

    static f d(String str) {
        o();
        for (f fVar : b) {
            if (fVar.b().equals(str)) {
                return fVar;
            }
        }
        return null;
    }

    public static boolean d() {
        return c || b.e();
    }

    public static String e() {
        return b.a(e);
    }

    public static synchronized String f() {
        String g;
        synchronized (a.class) {
            g = b.g();
        }
        return g;
    }

    public static synchronized String g() {
        String h;
        synchronized (a.class) {
            h = b.h();
        }
        return h;
    }

    public static synchronized String h() {
        synchronized (a.class) {
            String str;
            String[] split = b.h().split("~");
            if (split != null) {
                if (split.length > 0) {
                    str = split[0];
                    return str;
                }
            }
            str = BuildConfig.FLAVOR;
            return str;
        }
    }

    private void h(String str) {
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (jSONObject.has("error") && jSONObject.getInt("error") > 0) {
                c(9);
            } else if (!jSONObject.has("success") || jSONObject.getInt("success") == 0) {
                c(9);
            } else if (jSONObject.has("access_token")) {
                c(11);
                h = jSONObject.getString("access_token");
                b.b(h);
                if (jSONObject.has("expires_in")) {
                    b.a(Long.valueOf(System.currentTimeMillis() + (((long) jSONObject.getInt("expires_in")) * 1000)));
                } else {
                    b.a(Long.valueOf(System.currentTimeMillis() + b.a.longValue()));
                }
                if (((JSONObject) jSONObject.get("conf")).has("upload_endpoint")) {
                    b.e(jSONObject.getString("upload_endpoint"));
                }
                if (jSONObject.has("components")) {
                    JSONObject jSONObject2 = jSONObject.getJSONObject("components");
                    JSONArray names = jSONObject2.names();
                    for (int i = 0; i < names.length(); i++) {
                        String str2 = (String) names.get(i);
                        if (jSONObject2.has(str2)) {
                            JSONObject jSONObject3 = jSONObject2.getJSONObject(str2);
                            boolean z = jSONObject3.has("on") && jSONObject3.getInt("on") == 1;
                            Iterator keys = jSONObject3.keys();
                            HashMap hashMap = new HashMap();
                            while (keys.hasNext()) {
                                String str3 = (String) keys.next();
                                if (!"on".equals(str3) && (jSONObject3.get(str3) instanceof Integer)) {
                                    hashMap.put(str3, Boolean.valueOf(((Integer) jSONObject3.get(str3)).intValue() == 1));
                                }
                            }
                            a(str2, hashMap, z);
                        }
                    }
                }
            } else {
                c(9);
            }
        } catch (Throwable e) {
            h.c("Auth", "Exception on parseAuthResponse", e);
            c(10);
        }
    }

    private static final boolean i(String str) {
        for (String equals : b.a()) {
            if (equals.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean l() {
        return !((b.d().longValue() > System.currentTimeMillis() ? 1 : (b.d().longValue() == System.currentTimeMillis() ? 0 : -1)) <= 0);
    }

    private boolean m() {
        if (g != 3 && g != 2 && g != 5) {
            return false;
        }
        c(6);
        n();
        return true;
    }

    private void n() {
        if (g == 6 || g == 8) {
            c(7);
            if (f <= 2) {
                f++;
                String a = com.fyusion.sdk.common.a.b.a.a();
                m mVar = new m();
                mVar.a(new n(this) {
                    final /* synthetic */ a a;

                    {
                        this.a = r1;
                    }

                    public void a() {
                        a.c(8);
                        this.a.n();
                    }

                    public void a(String str) {
                        this.a.h(str);
                    }

                    public void b(String str) {
                        a.c(8);
                        this.a.n();
                    }
                });
                try {
                    mVar.execute(new String[]{a});
                } catch (RejectedExecutionException e) {
                    c(8);
                    n();
                }
                return;
            }
            c(10);
        }
    }

    private static void o() {
        if (b == null) {
            b = new ArrayList();
            String str = "-";
            List asList = Arrays.asList(new String[]{"camera", "viewer", "share", "edit"});
            HashMap anonymousClass3 = new HashMap<String, List<String>>() {
                {
                    put("camera", Arrays.asList(new String[]{"4k"}));
                    put("viewer", Arrays.asList(new String[]{"local", "remote"}));
                    put("share", Arrays.asList(new String[]{"fullres"}));
                }
            };
            for (int i = 0; i < asList.size(); i++) {
                String str2 = (String) asList.get(i);
                f fVar = new f(str2);
                fVar.a(b.c(str2));
                List<String> list = (List) anonymousClass3.get(str2);
                if (list != null) {
                    for (String str3 : list) {
                        fVar.a(str3, Boolean.valueOf(b.c(str2 + "-" + str3)));
                    }
                }
                b.add(fVar);
            }
        }
    }

    public synchronized int a(String str, String str2) {
        if (str != null) {
            if (i(str)) {
                return 1;
            }
        }
        i();
        String b = b(str2);
        if (b != null) {
            if (b.equals(b.b())) {
                return 1;
            }
        }
        return !h().equals(c(str2)) ? 3 : 2;
    }

    synchronized boolean a(final String str, final String str2, boolean z) {
        boolean z2 = false;
        synchronized (this) {
            if (!z) {
                z2 = true;
            }
            c = z2;
            g = 1;
            HandlerThread handlerThread = new HandlerThread(str);
            handlerThread.start();
            new Handler(handlerThread.getLooper()).post(new Runnable(this) {
                final /* synthetic */ a c;

                public void run() {
                    h.b("Auth", "Fyuse SDK version: " + FyuseSDK.getFullVersion());
                    if (a.c) {
                        b.f();
                    }
                    a.f = 0;
                    a.h = b.c();
                    a.d = str;
                    a.e = str2;
                    if (a.h == null || a.h.isEmpty()) {
                        a.c(3);
                    } else if (this.c.l()) {
                        a.c(11);
                        return;
                    } else {
                        a.c(2);
                    }
                    a.c = b.e();
                    if (a.c) {
                        a.c(5);
                        this.c.m();
                    } else {
                        a.c(4);
                    }
                }
            });
        }
        return true;
    }

    public int b(String str, String str2) {
        if (b()) {
            return 0;
        }
        f d = d(str);
        if (d == null) {
            return 0;
        }
        if (d.a() && d.a(str2)) {
            return 3;
        }
        if (!c) {
            return 1;
        }
        switch (g) {
            case 9:
            case 11:
                return 0;
            default:
                if (g == 10) {
                    g = 5;
                    m();
                }
                return 2;
        }
    }

    public boolean b() {
        return g == 9;
    }

    public boolean c(String str, String str2) {
        return b(str, str2) == 0;
    }

    public String i() {
        return d;
    }
}
