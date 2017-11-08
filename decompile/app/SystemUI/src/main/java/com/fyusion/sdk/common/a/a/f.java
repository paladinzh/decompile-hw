package com.fyusion.sdk.common.a.a;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import com.fyusion.sdk.common.FyuseSDK;
import com.fyusion.sdk.common.h;
import fyusion.vislib.BuildConfig;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* compiled from: Unknown */
public class f {
    private c a;
    private e b;
    private boolean c;
    private Map<String, d> d;
    private String e;
    private String f;

    /* compiled from: Unknown */
    public interface a<E extends d> {
        void a(E e);
    }

    /* compiled from: Unknown */
    private static class b {
        static final f a = new f();
    }

    private f() {
        this.c = false;
        this.a = new c();
        this.d = new ConcurrentHashMap();
    }

    public static f a() {
        return b.a;
    }

    public static String a(int i, int i2) {
        return i + "x" + i2;
    }

    private static String a(Context context) {
        String str = BuildConfig.VERSION_NAME;
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            h.b("Fyulytics", "No app version found");
            return str;
        }
    }

    public static String a(String str, String str2) {
        return str + "_" + str2;
    }

    public static long b() {
        return System.currentTimeMillis();
    }

    private static String b(Context context) {
        String str = BuildConfig.FLAVOR;
        if (VERSION.SDK_INT < 3) {
            return str;
        }
        try {
            str = context.getPackageName();
        } catch (Exception e) {
            h.b("Fyulytics", "Can't get Installer package");
        }
        return (str == null || str.length() == 0) ? BuildConfig.FLAVOR : str;
    }

    private void e() {
        if (this.b == null) {
            Context context = FyuseSDK.getContext();
            g gVar = new g(context);
            this.a.a(context);
            Bundle config = FyuseSDK.getConfig();
            if (config != null) {
                this.a.a(config.getBoolean("analytics.wifi-only"));
            }
            this.a.a(gVar);
            this.b = new e(gVar);
        }
    }

    private void f() {
        if (this.b.a() >= 20) {
            h.b("Fyulytics", "Fyuse SDK version: " + FyuseSDK.getFullVersion());
            if (com.fyusion.sdk.common.a.d()) {
                this.a.b(this.b.b());
            }
        }
    }

    public synchronized void a(d dVar) {
        e();
        if (dVar != null) {
            this.b.a(dVar);
            f();
        }
    }

    public synchronized void a(String str) {
        this.e = str;
        this.a.a("https://api.fyu.se/1.1/logs/event?");
    }

    public synchronized <E extends d> boolean a(String str, a<E> aVar) {
        d dVar = (d) this.d.remove(str);
        if (dVar == null) {
            return false;
        }
        dVar.h = b() - dVar.f;
        if (aVar != null) {
            aVar.a(dVar);
        }
        a(dVar);
        return true;
    }

    public synchronized boolean b(d dVar) {
        String a = dVar.a();
        if (this.d.containsKey(a)) {
            return false;
        }
        this.d.put(a, dVar);
        return true;
    }

    synchronized boolean c() {
        return this.c;
    }

    String d() {
        if (this.f == null) {
            try {
                Context context = FyuseSDK.getContext();
                this.f = "app=" + this.e + "&did=" + URLEncoder.encode(com.fyusion.sdk.common.a.f(), "UTF-8") + "&dm=" + URLEncoder.encode(Build.MODEL, "UTF-8") + "&os=1" + "&ov=" + URLEncoder.encode(VERSION.RELEASE, "UTF-8") + "&sv=" + URLEncoder.encode(FyuseSDK.getVersion(), "UTF-8") + "&fv=" + FyuseSDK.getSdkFlavor() + "&an=" + URLEncoder.encode(b(context), "UTF-8") + "&av=" + URLEncoder.encode(a(context), "UTF-8") + "&l=" + URLEncoder.encode(Locale.getDefault().getLanguage(), "UTF-8") + "&tz=" + URLEncoder.encode(Calendar.getInstance().getTimeZone().getID(), "UTF-8");
            } catch (Throwable e) {
                h.b("Fyulytics", "Unable to make url params", e);
            }
        }
        return this.f;
    }
}
