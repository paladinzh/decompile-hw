package com.fyusion.sdk.common;

import android.os.Handler;
import android.os.HandlerThread;
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
    private static String b;
    private static List<f> c;
    private static boolean d = false;
    private static String e;
    private static String f;
    private static int g;
    private static int h = 9;
    private static String i = null;

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
                str2 = "";
                return str2;
            }
            str2 = "";
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
        h = i;
    }

    static f d(String str) {
        u();
        for (f fVar : c) {
            if (fVar.b().equals(str)) {
                return fVar;
            }
        }
        return null;
    }

    public static boolean e() {
        return d || b.h();
    }

    public static String g() {
        return b.a(f);
    }

    public static synchronized String h() {
        String j;
        synchronized (a.class) {
            j = b.j();
        }
        return j;
    }

    public static synchronized String i() {
        String k;
        synchronized (a.class) {
            k = b.k();
        }
        return k;
    }

    public static synchronized String j() {
        synchronized (a.class) {
            String str;
            String[] split = b.k().split("~");
            if (split != null) {
                if (split.length > 0) {
                    str = split[0];
                    return str;
                }
            }
            str = "";
            return str;
        }
    }

    @Deprecated
    public static String k() {
        return b.g();
    }

    private void k(String str) {
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (jSONObject.has("error") && jSONObject.getInt("error") > 0) {
                c(9);
            } else if (!jSONObject.has("success") || jSONObject.getInt("success") == 0) {
                c(9);
            } else if (jSONObject.has("access_token")) {
                c(11);
                i = jSONObject.getString("access_token");
                b.b(i);
                if (jSONObject.has("expires_in")) {
                    b.a(Long.valueOf(System.currentTimeMillis() + (((long) jSONObject.getInt("expires_in")) * 1000)));
                } else {
                    b.a(Long.valueOf(System.currentTimeMillis() + b.a.longValue()));
                }
                if (((JSONObject) jSONObject.get("conf")).has("upload_endpoint")) {
                    b.f(jSONObject.getString("upload_endpoint"));
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
            DLog.e("Auth", "Exception on parseAuthResponse", e);
            c(10);
        }
    }

    private static final boolean l(String str) {
        for (String equals : b.a()) {
            if (equals.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean q() {
        return !((b.f().longValue() > System.currentTimeMillis() ? 1 : (b.f().longValue() == System.currentTimeMillis() ? 0 : -1)) <= 0);
    }

    private String r() {
        return b.c();
    }

    private boolean s() {
        if (h != 3 && h != 2 && h != 5) {
            return false;
        }
        c(6);
        t();
        return true;
    }

    private void t() {
        if (h == 6 || h == 8) {
            c(7);
            if (g <= 2) {
                g++;
                String a = com.fyusion.sdk.common.internal.a.a.a();
                k kVar = new k();
                kVar.a(new l(this) {
                    final /* synthetic */ a a;

                    {
                        this.a = r1;
                    }

                    public void a() {
                        a.c(8);
                        this.a.t();
                    }

                    public void a(String str) {
                        this.a.k(str);
                    }

                    public void b(String str) {
                        a.c(8);
                        this.a.t();
                    }
                });
                try {
                    kVar.execute(new String[]{a});
                } catch (RejectedExecutionException e) {
                    c(8);
                    t();
                }
                return;
            }
            c(10);
        }
    }

    private static void u() {
        if (c == null) {
            c = new ArrayList();
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
                fVar.a(b.d(str2));
                List<String> list = (List) anonymousClass3.get(str2);
                if (list != null) {
                    for (String str3 : list) {
                        fVar.a(str3, Boolean.valueOf(b.d(str2 + "-" + str3)));
                    }
                }
                c.add(fVar);
            }
        }
    }

    public synchronized int a(String str, String str2) {
        if (str != null) {
            if (l(str)) {
                return 1;
            }
        }
        l();
        String b = b(str2);
        if (b != null) {
            if (b.equals(b.b())) {
                return 1;
            }
        }
        return !j().equals(c(str2)) ? 3 : 2;
    }

    synchronized boolean a(final String str, final String str2, boolean z) {
        boolean z2 = false;
        synchronized (this) {
            if (!z) {
                z2 = true;
            }
            d = z2;
            h = 1;
            HandlerThread handlerThread = new HandlerThread(str);
            handlerThread.start();
            new Handler(handlerThread.getLooper()).post(new Runnable(this) {
                final /* synthetic */ a c;

                public void run() {
                    DLog.i("Auth", "Fyuse SDK version: " + FyuseSDK.getFullVersion());
                    if (a.d) {
                        b.i();
                    }
                    a.g = 0;
                    a.i = b.c();
                    a.e = str;
                    a.f = str2;
                    if (a.i == null || a.i.isEmpty()) {
                        a.c(3);
                    } else if (this.c.q()) {
                        a.c(11);
                        return;
                    } else {
                        a.c(2);
                    }
                    a.d = b.h();
                    if (a.d) {
                        a.c(5);
                        this.c.s();
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
        if (!d) {
            return 1;
        }
        switch (h) {
            case 9:
            case 11:
                return 0;
            default:
                if (h == 10) {
                    h = 5;
                    s();
                }
                return 2;
        }
    }

    public boolean b() {
        return h == 9;
    }

    public boolean c() {
        return h == 10;
    }

    public boolean c(String str, String str2) {
        return b(str, str2) == 0;
    }

    public synchronized void d() {
        try {
            if (!d) {
                d = true;
                b.i();
            }
        } catch (Throwable e) {
            DLog.e("Auth", "Exception on writeCanUseInternetToPreferences()", e);
        }
        try {
            if (h != 4) {
                if (h != 10) {
                }
            }
            c(5);
            s();
        } catch (Throwable e2) {
            DLog.e("Auth", "Exception on authenticateClientOnline()", e2);
        }
    }

    public int e(String str) {
        if (b()) {
            return 0;
        }
        f d = d(str);
        if (d == null) {
            return 0;
        }
        if (d.a()) {
            return 3;
        }
        if (!d) {
            return 1;
        }
        switch (h) {
            case 9:
            case 11:
                return 0;
            default:
                if (h == 10) {
                    h = 5;
                    s();
                }
                return 2;
        }
    }

    public boolean f() {
        g = 0;
        return s();
    }

    public boolean f(String str) {
        return e(str) == 0;
    }

    public void g(String str) {
        b = str;
        b.c(str);
    }

    public String l() {
        return e;
    }

    public String m() {
        if (b != null) {
            return b;
        }
        b = b.e();
        if (b != null) {
            return b;
        }
        if (!a().q()) {
            return null;
        }
        b = a().r();
        return b;
    }

    public void n() {
        b = null;
        b.d();
    }
}
