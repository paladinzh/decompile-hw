package com.google.android.gms.common;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Base64;
import android.util.Log;

/* compiled from: Unknown */
public class zzf {
    private static final zzf zzafS = new zzf();

    private zzf() {
    }

    public static zzf zzoO() {
        return zzafS;
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

    public boolean zza(PackageInfo packageInfo, boolean z) {
        if (!(packageInfo == null || packageInfo.signatures == null)) {
            zza zza;
            if (z) {
                zza = zza(packageInfo, zzd.zzafK);
            } else {
                zza = zza(packageInfo, zzd.zzafK[0]);
            }
            if (zza != null) {
                return true;
            }
        }
        return false;
    }

    public boolean zza(PackageManager packageManager, PackageInfo packageInfo) {
        if (packageInfo == null) {
            return false;
        }
        if (zze.zzc(packageManager)) {
            return zza(packageInfo, true);
        }
        boolean zza = zza(packageInfo, false);
        if (!zza && zza(packageInfo, true)) {
            Log.w("GoogleSignatureVerifier", "Test-keys aren't accepted on this build.");
        }
        return zza;
    }
}
