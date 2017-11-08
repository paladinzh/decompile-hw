package com.fyusion.sdk.common.internal.analytics;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import com.android.gallery3d.gadget.XmlUtils;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.FyuseSDK;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* compiled from: Unknown */
public class Fyulytics {
    public static final String TAG = "Fyulytics";
    private c a;
    private d b;
    private boolean c;
    private Map<String, Event> d;
    private String e;
    private String f;

    /* compiled from: Unknown */
    public interface a<E extends Event> {
        void a(E e);
    }

    /* compiled from: Unknown */
    private static class b {
        static final Fyulytics a = new Fyulytics();
    }

    private Fyulytics() {
        this.c = false;
        this.a = new c();
        this.d = new ConcurrentHashMap();
    }

    private static String a(Context context) {
        String str = "1.0";
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            DLog.i(TAG, "No app version found");
            return str;
        }
    }

    private static String b(Context context) {
        String str = "";
        if (VERSION.SDK_INT < 3) {
            return str;
        }
        try {
            str = context.getPackageName();
        } catch (Exception e) {
            DLog.i(TAG, "Can't get Installer package");
        }
        return (str == null || str.length() == 0) ? "" : str;
    }

    private void c() {
        if (this.b == null) {
            Context context = FyuseSDK.getContext();
            e eVar = new e(context);
            this.a.a(context);
            Bundle config = FyuseSDK.getConfig();
            if (config != null) {
                this.a.a(config.getBoolean("analytics.wifi-only"));
            }
            this.a.a(eVar);
            this.b = new d(eVar);
        }
    }

    public static long currentTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    public static long currentTimestampMs() {
        return System.currentTimeMillis();
    }

    private void d() {
        if (this.b.a() >= 20) {
            DLog.i(TAG, "Fyuse SDK version: " + FyuseSDK.getFullVersion());
            if (com.fyusion.sdk.common.a.e()) {
                this.a.b(this.b.b());
            }
        }
    }

    public static String makeSizeString(int i, int i2) {
        return i + "x" + i2;
    }

    public static String makeTimedEventKey(String str, String str2) {
        return str + "_" + str2;
    }

    public static Fyulytics sharedInstance() {
        return b.a;
    }

    synchronized boolean a() {
        return this.c;
    }

    String b() {
        if (this.f == null) {
            try {
                Context context = FyuseSDK.getContext();
                this.f = "app=" + this.e + "&did=" + URLEncoder.encode(com.fyusion.sdk.common.a.h(), XmlUtils.INPUT_ENCODING) + "&dm=" + URLEncoder.encode(Build.MODEL, XmlUtils.INPUT_ENCODING) + "&os=1" + "&ov=" + URLEncoder.encode(VERSION.RELEASE, XmlUtils.INPUT_ENCODING) + "&sv=" + URLEncoder.encode(FyuseSDK.getVersion(), XmlUtils.INPUT_ENCODING) + "&fv=" + FyuseSDK.getSdkFlavor() + "&an=" + URLEncoder.encode(b(context), XmlUtils.INPUT_ENCODING) + "&av=" + URLEncoder.encode(a(context), XmlUtils.INPUT_ENCODING) + "&l=" + URLEncoder.encode(Locale.getDefault().getLanguage(), XmlUtils.INPUT_ENCODING) + "&tz=" + URLEncoder.encode(Calendar.getInstance().getTimeZone().getID(), XmlUtils.INPUT_ENCODING);
            } catch (Throwable e) {
                DLog.w(TAG, "Unable to make url params", e);
            }
        }
        return this.f;
    }

    public synchronized <E extends Event> boolean endEvent(String str, a<E> aVar) {
        Event event = (Event) this.d.remove(str);
        if (event == null) {
            return false;
        }
        event.dur = currentTimestampMs() - event.timestamp;
        if (aVar != null) {
            aVar.a(event);
        }
        recordEvent(event);
        return true;
    }

    public void flushEvents() {
        if (this.b != null && this.b.a() > 0 && com.fyusion.sdk.common.a.e()) {
            this.a.b(this.b.b());
        }
    }

    public Event getTimedEvent(String str) {
        return (Event) this.d.get(str);
    }

    public synchronized void init(String str) {
        this.e = str;
        this.a.a("https://api.fyu.se/1.1/logs/event?");
    }

    public synchronized void recordEvent(Event event) {
        c();
        if (event != null) {
            this.b.a(event);
            d();
        }
    }

    public synchronized boolean startEvent(Event event) {
        String timedEventKey = event.getTimedEventKey();
        if (this.d.containsKey(timedEventKey)) {
            return false;
        }
        this.d.put(timedEventKey, event);
        return true;
    }
}
