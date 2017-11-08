package com.google.android.gms.auth;

import android.content.ComponentName;
import android.os.Build.VERSION;

/* compiled from: Unknown */
public final class GoogleAuthUtil {
    public static final String KEY_ANDROID_PACKAGE_NAME = (VERSION.SDK_INT < 14 ? "androidPackageName" : "androidPackageName");
    public static final String KEY_CALLER_UID = (VERSION.SDK_INT < 11 ? "callerUid" : "callerUid");
    private static final ComponentName zzQI = new ComponentName("com.google.android.gms", "com.google.android.gms.auth.GetToken");
    private static final ComponentName zzQJ = new ComponentName("com.google.android.gms", "com.google.android.gms.recovery.RecoveryService");

    private GoogleAuthUtil() {
    }
}
