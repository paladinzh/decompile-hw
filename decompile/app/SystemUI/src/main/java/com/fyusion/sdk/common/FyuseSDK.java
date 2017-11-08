package com.fyusion.sdk.common;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.renderscript.RenderScript;
import com.fyusion.sdk.common.a.a.f;
import com.fyusion.sdk.common.a.b.a;
import fyusion.vislib.BuildConfig;

/* compiled from: Unknown */
public class FyuseSDK {
    private static Context a;
    private static Boolean c = Boolean.valueOf(false);
    private static Bundle d;
    private static FyuseSDK e;
    private RenderScript b;

    private FyuseSDK() {
    }

    private static String a() {
        try {
            Class cls = Class.forName("com.fyusion.sdk.common.ext.a");
            return " | Vislib: " + cls.getField("FYUSE_VISLIB_BRANCH").get(null) + " | " + cls.getField("FYUSE_VISLIB_COMMIT").get(null) + " | " + cls.getField("FYUSE_VISLIB_DEPENDENCY_VERSION").get(null);
        } catch (Exception e) {
            return BuildConfig.FLAVOR;
        }
    }

    private static void a(Context context) throws IllegalStateException {
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
            if ("onCreate".equals(stackTraceElement.getMethodName())) {
                try {
                    if (Application.class.isAssignableFrom(Class.forName(stackTraceElement.getClassName()))) {
                        return;
                    }
                } catch (ClassNotFoundException e) {
                    if (Application.class.isAssignableFrom(context.getClass())) {
                        return;
                    }
                }
            }
        }
        h.b("FyuseSDK", "Fyuse SDK version: " + getFullVersion());
        throw new IllegalStateException("FyuseSDK has not been initialized. Please call FyuseSDK.init() in your Application class.");
    }

    private static synchronized void a(Context context, String str, String str2, Bundle bundle, boolean z) {
        synchronized (FyuseSDK.class) {
            if (c.booleanValue()) {
            } else if (context == null) {
                throw new IllegalArgumentException("Context can not be null.");
            } else if (str == null) {
                throw new IllegalArgumentException("ClientId can not be null.");
            } else if (str2 == null) {
                throw new IllegalArgumentException("ClientSecret can not be null.");
            } else if (str2.length() == 32) {
                c = Boolean.valueOf(true);
                a = context;
                d = bundle;
                a(context);
                f.a().a(str);
                a.a(str);
                a.a().a(str, str2, z);
            } else {
                throw new IllegalArgumentException("Wrong credentials");
            }
        }
    }

    public static String getBuildDate() {
        return "2017-08-08T21:23:06-07:00";
    }

    public static Bundle getConfig() {
        if (c.booleanValue()) {
            return d;
        }
        throw new IllegalStateException("FyuseSDK has not been initialized. Please call FyuseSDK.init() in Application class.");
    }

    public static Context getContext() {
        if (a != null) {
            return a;
        }
        throw new IllegalStateException("FyuseSDK has not been initialized. Please call FyuseSDK.init() in Application class.");
    }

    public static String getFullVersion() {
        return "1.02.24-rel7 | " + getGitHash() + " | " + getBuildDate() + a();
    }

    public static String getGitHash() {
        return "'4dd9b23ea'";
    }

    public static synchronized FyuseSDK getInstance() {
        FyuseSDK fyuseSDK;
        synchronized (FyuseSDK.class) {
            if (e == null) {
                e = new FyuseSDK();
            }
            fyuseSDK = e;
        }
        return fyuseSDK;
    }

    public static int getSdkFlavor() {
        return 1;
    }

    public static String getVersion() {
        return "1.02.24-rel7";
    }

    public static void init(Context context, String str, String str2, Bundle bundle) {
        a(context, str, str2, bundle, false);
    }

    public RenderScript getRenderScript() {
        if (this.b == null) {
            this.b = RenderScript.create(getContext());
        }
        return this.b;
    }
}
