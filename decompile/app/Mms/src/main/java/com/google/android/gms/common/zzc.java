package com.google.android.gms.common;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.google.android.gms.common.internal.zzn;

/* compiled from: Unknown */
public class zzc {
    public static final String GOOGLE_PLAY_SERVICES_PACKAGE = "com.google.android.gms";
    public static final int GOOGLE_PLAY_SERVICES_VERSION_CODE = zze.GOOGLE_PLAY_SERVICES_VERSION_CODE;
    private static final zzc zzafF = new zzc();

    zzc() {
    }

    private String zzj(@Nullable Context context, @Nullable String str) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("gcore_");
        stringBuilder.append(GOOGLE_PLAY_SERVICES_VERSION_CODE);
        stringBuilder.append("-");
        if (!TextUtils.isEmpty(str)) {
            stringBuilder.append(str);
        }
        stringBuilder.append("-");
        if (context != null) {
            stringBuilder.append(context.getPackageName());
        }
        stringBuilder.append("-");
        if (context != null) {
            try {
                stringBuilder.append(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode);
            } catch (NameNotFoundException e) {
            }
        }
        return stringBuilder.toString();
    }

    public static zzc zzoK() {
        return zzafF;
    }

    @Nullable
    public PendingIntent getErrorResolutionPendingIntent(Context context, int errorCode, int requestCode) {
        return zza(context, errorCode, requestCode, null);
    }

    public String getErrorString(int errorCode) {
        return zze.getErrorString(errorCode);
    }

    @Nullable
    public String getOpenSourceSoftwareLicenseInfo(Context context) {
        return zze.getOpenSourceSoftwareLicenseInfo(context);
    }

    public int isGooglePlayServicesAvailable(Context context) {
        int isGooglePlayServicesAvailable = zze.isGooglePlayServicesAvailable(context);
        return !zze.zzd(context, isGooglePlayServicesAvailable) ? isGooglePlayServicesAvailable : 18;
    }

    public boolean isUserResolvableError(int errorCode) {
        return zze.isUserRecoverableError(errorCode);
    }

    @Nullable
    public PendingIntent zza(Context context, int i, int i2, @Nullable String str) {
        Intent zza = zza(context, i, str);
        return zza != null ? PendingIntent.getActivity(context, i2, zza, 268435456) : null;
    }

    @Nullable
    public Intent zza(Context context, int i, @Nullable String str) {
        switch (i) {
            case 1:
            case 2:
                return zzn.zzx("com.google.android.gms", zzj(context, str));
            case 3:
                return zzn.zzcJ("com.google.android.gms");
            case 42:
                return zzn.zzqU();
            default:
                return null;
        }
    }

    public int zzaj(Context context) {
        return zze.zzaj(context);
    }

    public void zzak(Context context) throws GooglePlayServicesRepairableException, GooglePlayServicesNotAvailableException {
        zze.zzad(context);
    }

    public void zzal(Context context) {
        zze.zzal(context);
    }

    @Nullable
    @Deprecated
    public Intent zzbu(int i) {
        return zza(null, i, null);
    }

    public boolean zzd(Context context, int i) {
        return zze.zzd(context, i);
    }

    public boolean zzi(Context context, String str) {
        return zze.zzi(context, str);
    }
}
