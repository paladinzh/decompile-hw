package com.google.android.gms.maps.internal;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.dynamic.zze;
import com.google.android.gms.maps.internal.zzc.zza;
import com.google.android.gms.maps.model.RuntimeRemoteException;

/* compiled from: Unknown */
public class zzad {
    private static Context zzaSU;
    private static zzc zzaSV;

    private static Context getRemoteContext(Context context) {
        if (zzaSU == null) {
            zzaSU = !zzAg() ? GooglePlayServicesUtil.getRemoteContext(context) : context.getApplicationContext();
        }
        return zzaSU;
    }

    public static boolean zzAg() {
        return false;
    }

    private static Class<?> zzAh() {
        try {
            return Class.forName("com.google.android.gms.maps.internal.CreatorImpl");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T zza(ClassLoader classLoader, String str) {
        try {
            return zzd(((ClassLoader) zzx.zzz(classLoader)).loadClass(str));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to find dynamic class " + str);
        }
    }

    public static zzc zzaO(Context context) throws GooglePlayServicesNotAvailableException {
        zzx.zzz(context);
        if (zzaSV != null) {
            return zzaSV;
        }
        zzaP(context);
        zzaSV = zzaQ(context);
        try {
            zzaSV.zzd(zze.zzC(getRemoteContext(context).getResources()), GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_VERSION_CODE);
            return zzaSV;
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    private static void zzaP(Context context) throws GooglePlayServicesNotAvailableException {
        int isGooglePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        switch (isGooglePlayServicesAvailable) {
            case 0:
                return;
            default:
                throw new GooglePlayServicesNotAvailableException(isGooglePlayServicesAvailable);
        }
    }

    private static zzc zzaQ(Context context) {
        if (zzAg()) {
            Log.i(zzad.class.getSimpleName(), "Making Creator statically");
            return (zzc) zzd(zzAh());
        }
        Log.i(zzad.class.getSimpleName(), "Making Creator dynamically");
        return zza.zzcu((IBinder) zza(getRemoteContext(context).getClassLoader(), "com.google.android.gms.maps.internal.CreatorImpl"));
    }

    private static <T> T zzd(Class<?> cls) {
        try {
            return cls.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("Unable to instantiate the dynamic class " + cls.getName());
        } catch (IllegalAccessException e2) {
            throw new IllegalStateException("Unable to call the default constructor of " + cls.getName());
        }
    }
}
