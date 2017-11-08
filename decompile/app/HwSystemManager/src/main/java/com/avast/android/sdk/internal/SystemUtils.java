package com.avast.android.sdk.internal;

/* compiled from: Unknown */
public class SystemUtils {
    static {
        try {
            System.loadLibrary("avast-utils");
        } catch (UnsatisfiedLinkError e) {
        }
    }

    public static String a() {
        String cPUArchitecture = getCPUArchitecture();
        return !"unknown".equals(cPUArchitecture) ? cPUArchitecture : "armeabi";
    }

    private static native String getCPUArchitecture();
}
