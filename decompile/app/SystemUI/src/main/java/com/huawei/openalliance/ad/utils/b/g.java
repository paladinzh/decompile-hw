package com.huawei.openalliance.ad.utils.b;

import android.annotation.SuppressLint;
import android.os.Process;
import android.util.Log;
import fyusion.vislib.BuildConfig;
import java.text.SimpleDateFormat;

@SuppressLint({"SimpleDateFormat"})
/* compiled from: Unknown */
public class g {
    String a = null;
    String b = "HiAdSDK";
    f c = null;
    long d = 0;
    long e = 0;
    String f = null;
    String g;
    int h;
    int i;
    int j = 0;
    StringBuilder k = null;
    private SimpleDateFormat l = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /* compiled from: Unknown */
    public static class a {
        g a;

        public a(String str, f fVar) {
            this.a = new g(str, fVar);
        }

        public a a(int i) {
            this.a.j = i;
            return this;
        }

        public a a(String str) {
            this.a.a = str;
            return this;
        }

        public g a() {
            return this.a.a();
        }
    }

    protected g() {
    }

    protected g(String str, f fVar) {
        if (str != null) {
            this.b = str;
        }
        this.c = fVar;
    }

    private k a(k kVar) {
        kVar.a(this.l.format(Long.valueOf(this.d)));
        kVar.a(Character.valueOf('[')).a(Integer.valueOf(this.h)).a(Character.valueOf(']'));
        if (this.a != null) {
            kVar.a(Character.valueOf('[')).a(this.a).a(Character.valueOf(']'));
        }
        kVar.a(Character.valueOf('[')).a(this.b).a(Character.valueOf(']'));
        kVar.a(Character.valueOf('[')).a(this.c).a(Character.valueOf(']'));
        return kVar;
    }

    public static String a(Throwable th) {
        return BuildConfig.FLAVOR;
    }

    public static boolean a(g gVar) {
        return gVar == null || gVar.b();
    }

    private <T> g b(T t) {
        this.k.append(t);
        return this;
    }

    private k b(k kVar) {
        kVar.a("[");
        kVar.a(this.f).a(Character.valueOf('{')).a(Long.valueOf(this.e)).a(Character.valueOf('}'));
        kVar.a("]");
        kVar.a(Character.valueOf(' ')).a(this.k.toString());
        if (this.c.a() < f.OUT.a()) {
            kVar.a(Character.valueOf(' ')).a(Character.valueOf('('));
            kVar.a(this.g).a(Character.valueOf(':')).a(Integer.valueOf(this.i));
            kVar.a(Character.valueOf(')'));
        }
        return kVar;
    }

    protected g a() {
        this.d = System.currentTimeMillis();
        Thread currentThread = Thread.currentThread();
        this.e = currentThread.getId();
        this.f = currentThread.getName();
        this.h = Process.myPid();
        try {
            StackTraceElement stackTraceElement = currentThread.getStackTrace()[this.j + 7];
            this.g = stackTraceElement.getFileName();
            this.i = stackTraceElement.getLineNumber();
        } catch (Exception e) {
            Log.e("HiAdSDK", "create log error");
        }
        this.k = new StringBuilder(32);
        return this;
    }

    public <T> g a(T t) {
        b((Object) t);
        return this;
    }

    public void a(h hVar) {
        if (this.k != null) {
            hVar.a(this);
        }
    }

    public g b(Throwable th) {
        b(Character.valueOf('\n')).b(a(th));
        return this;
    }

    public boolean b() {
        return this.k == null;
    }

    public String d() {
        k a = k.a();
        a(a);
        return a.c();
    }

    public String e() {
        k a = k.a();
        b(a);
        return a.c();
    }

    public String toString() {
        k a = k.a();
        a(a);
        b(a);
        return a.c();
    }
}
