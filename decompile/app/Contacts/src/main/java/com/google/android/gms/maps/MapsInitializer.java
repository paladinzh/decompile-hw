package com.google.android.gms.maps;

import android.content.Context;
import android.os.RemoteException;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.maps.internal.zzad;
import com.google.android.gms.maps.internal.zzc;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.RuntimeRemoteException;

/* compiled from: Unknown */
public final class MapsInitializer {
    private static boolean zznY = false;

    private MapsInitializer() {
    }

    public static synchronized int initialize(Context context) {
        synchronized (MapsInitializer.class) {
            zzx.zzb((Object) context, (Object) "Context is null");
            if (zznY) {
                return 0;
            }
            try {
                zza(zzad.zzaO(context));
                zznY = true;
                return 0;
            } catch (GooglePlayServicesNotAvailableException e) {
                return e.errorCode;
            }
        }
    }

    public static void zza(zzc zzc) {
        try {
            CameraUpdateFactory.zza(zzc.zzAe());
            BitmapDescriptorFactory.zza(zzc.zzAf());
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }
}
