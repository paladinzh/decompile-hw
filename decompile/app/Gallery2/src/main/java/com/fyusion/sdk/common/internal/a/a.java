package com.fyusion.sdk.common.internal.a;

import android.os.Build;
import android.os.Build.VERSION;
import com.android.gallery3d.gadget.XmlUtils;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.FyuseSDK;
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
            return String.format(str, new Object[]{str2, a + "&key=" + com.fyusion.sdk.common.a.g()});
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
            a = "app=" + b + "&os=1" + "&d=" + URLEncoder.encode(com.fyusion.sdk.common.a.h(), XmlUtils.INPUT_ENCODING) + "&ov=" + URLEncoder.encode(VERSION.RELEASE, XmlUtils.INPUT_ENCODING) + "&v=" + URLEncoder.encode(FyuseSDK.getVersion(), XmlUtils.INPUT_ENCODING) + "&dt=" + URLEncoder.encode(Build.MODEL, XmlUtils.INPUT_ENCODING) + "&l=" + URLEncoder.encode(Locale.getDefault().getLanguage(), XmlUtils.INPUT_ENCODING) + "&tz=" + URLEncoder.encode(Calendar.getInstance().getTimeZone().getID(), XmlUtils.INPUT_ENCODING) + "&p=" + FyuseSDK.getSdkFlavor();
        } catch (UnsupportedEncodingException e) {
            DLog.e("UrlMaker", e.getMessage());
        }
    }

    private static String c(String str) {
        if (b != null) {
            if (a == null) {
                b();
            }
            return String.format(str, new Object[]{a + "&key=" + com.fyusion.sdk.common.a.g()});
        }
        throw new IllegalStateException("UrlMaker.init() is not called yet");
    }
}
