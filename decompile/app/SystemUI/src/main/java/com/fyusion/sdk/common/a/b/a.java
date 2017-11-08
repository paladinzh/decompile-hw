package com.fyusion.sdk.common.a.b;

import android.os.Build;
import android.os.Build.VERSION;
import com.fyusion.sdk.common.FyuseSDK;
import com.fyusion.sdk.common.h;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Locale;

/* compiled from: Unknown */
public class a {
    private static String a;
    private static String b;

    public static String a() {
        return c("https://api.fyu.se/1.1/auth/sdk?%s");
    }

    private static String a(String str, String str2) {
        if (b != null) {
            if (a == null) {
                b();
            }
            return String.format(str, new Object[]{str2, a + "&key=" + com.fyusion.sdk.common.a.e()});
        }
        throw new IllegalStateException("UrlMaker.init() is not called yet");
    }

    public static void a(String str) {
        b = str;
    }

    public static String b(String str) {
        return a("https://api.fyu.se/1.1/data/details/%s?%s", str);
    }

    private static void b() {
        try {
            a = "app=" + b + "&os=1" + "&d=" + URLEncoder.encode(com.fyusion.sdk.common.a.f(), "UTF-8") + "&ov=" + URLEncoder.encode(VERSION.RELEASE, "UTF-8") + "&v=" + URLEncoder.encode(FyuseSDK.getVersion(), "UTF-8") + "&dt=" + URLEncoder.encode(Build.MODEL, "UTF-8") + "&l=" + URLEncoder.encode(Locale.getDefault().getLanguage(), "UTF-8") + "&tz=" + URLEncoder.encode(Calendar.getInstance().getTimeZone().getID(), "UTF-8") + "&p=" + FyuseSDK.getSdkFlavor();
        } catch (UnsupportedEncodingException e) {
            h.d("UrlMaker", e.getMessage());
        }
    }

    private static String c(String str) {
        if (b != null) {
            if (a == null) {
                b();
            }
            return String.format(str, new Object[]{a + "&key=" + com.fyusion.sdk.common.a.e()});
        }
        throw new IllegalStateException("UrlMaker.init() is not called yet");
    }
}
