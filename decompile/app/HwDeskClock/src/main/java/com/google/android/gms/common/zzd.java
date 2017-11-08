package com.google.android.gms.common;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Base64;
import android.util.Log;

/* compiled from: Unknown */
public class zzd {
    private static final zzd zzYA = new zzd();

    private zzd() {
    }

    private boolean zza(PackageInfo packageInfo, boolean z) {
        if (packageInfo.signatures.length == 1) {
            zza zzb = new zzb(packageInfo.signatures[0].toByteArray());
            if ((!z ? zzc.zzmU() : zzc.zzmT()).contains(zzb)) {
                return true;
            }
            if (Log.isLoggable("GoogleSignatureVerifier", 2)) {
                Log.v("GoogleSignatureVerifier", "Signature not valid.  Found: \n" + Base64.encodeToString(zzb.getBytes(), 0));
            }
            return false;
        }
        Log.w("GoogleSignatureVerifier", "Package has more than one signature.");
        return false;
    }

    public static zzd zzmY() {
        return zzYA;
    }

    zza zza(PackageInfo packageInfo, zza... zzaArr) {
        if (packageInfo.signatures.length == 1) {
            zza zzb = new zzb(packageInfo.signatures[0].toByteArray());
            for (int i = 0; i < zzaArr.length; i++) {
                if (zzaArr[i].equals(zzb)) {
                    return zzaArr[i];
                }
            }
            if (Log.isLoggable("GoogleSignatureVerifier", 2)) {
                Log.v("GoogleSignatureVerifier", "Signature not valid.  Found: \n" + Base64.encodeToString(zzb.getBytes(), 0));
            }
            return null;
        }
        Log.w("GoogleSignatureVerifier", "Package has more than one signature.");
        return null;
    }

    public boolean zza(PackageManager packageManager, PackageInfo packageInfo) {
        if (packageInfo == null) {
            return false;
        }
        if (GooglePlayServicesUtil.zzc(packageManager)) {
            return zza(packageInfo, true);
        }
        boolean zza = zza(packageInfo, false);
        if (!zza && zza(packageInfo, true)) {
            Log.w("GoogleSignatureVerifier", "Test-keys aren't accepted on this build.");
        }
        return zza;
    }

    public boolean zzb(PackageManager packageManager, String str) {
        try {
            return zza(packageManager, packageManager.getPackageInfo(str, 64));
        } catch (NameNotFoundException e) {
            if (Log.isLoggable("GoogleSignatureVerifier", 3)) {
                Log.d("GoogleSignatureVerifier", "Package manager can't find package " + str + ", defaulting to false");
            }
            return false;
        }
    }
}
