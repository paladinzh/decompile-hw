package com.google.android.gms.security;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.common.zzc;
import com.google.android.gms.common.zze;
import java.lang.reflect.Method;

/* compiled from: Unknown */
public class ProviderInstaller {
    public static final String PROVIDER_NAME = "GmsCore_OpenSSL";
    private static final zzc zzbgP = zzc.zzoK();
    private static Method zzbgQ = null;
    private static final Object zzqy = new Object();

    /* compiled from: Unknown */
    public interface ProviderInstallListener {
        void onProviderInstallFailed(int i, Intent intent);

        void onProviderInstalled();
    }

    public static void installIfNeeded(Context context) throws GooglePlayServicesRepairableException, GooglePlayServicesNotAvailableException {
        zzx.zzb((Object) context, (Object) "Context must not be null");
        zzbgP.zzak(context);
        Context remoteContext = zze.getRemoteContext(context);
        if (remoteContext != null) {
            synchronized (zzqy) {
                try {
                    if (zzbgQ == null) {
                        zzaV(remoteContext);
                    }
                    zzbgQ.invoke(null, new Object[]{remoteContext});
                } catch (Exception e) {
                    Log.e("ProviderInstaller", "Failed to install provider: " + e.getMessage());
                    throw new GooglePlayServicesNotAvailableException(8);
                }
            }
            return;
        }
        Log.e("ProviderInstaller", "Failed to get remote context");
        throw new GooglePlayServicesNotAvailableException(8);
    }

    public static void installIfNeededAsync(final Context context, final ProviderInstallListener listener) {
        zzx.zzb((Object) context, (Object) "Context must not be null");
        zzx.zzb((Object) listener, (Object) "Listener must not be null");
        zzx.zzcD("Must be called on the UI thread");
        new AsyncTask<Void, Void, Integer>() {
            protected /* synthetic */ Object doInBackground(Object[] objArr) {
                return zzc((Void[]) objArr);
            }

            protected /* synthetic */ void onPostExecute(Object obj) {
                zze((Integer) obj);
            }

            protected Integer zzc(Void... voidArr) {
                try {
                    ProviderInstaller.installIfNeeded(context);
                    return Integer.valueOf(0);
                } catch (GooglePlayServicesRepairableException e) {
                    return Integer.valueOf(e.getConnectionStatusCode());
                } catch (GooglePlayServicesNotAvailableException e2) {
                    return Integer.valueOf(e2.errorCode);
                }
            }

            protected void zze(Integer num) {
                if (num.intValue() != 0) {
                    listener.onProviderInstallFailed(num.intValue(), ProviderInstaller.zzbgP.zza(context, num.intValue(), "pi"));
                    return;
                }
                listener.onProviderInstalled();
            }
        }.execute(new Void[0]);
    }

    private static void zzaV(Context context) throws ClassNotFoundException, NoSuchMethodException {
        zzbgQ = context.getClassLoader().loadClass("com.google.android.gms.common.security.ProviderInstallerImpl").getMethod("insertProvider", new Class[]{Context.class});
    }
}
