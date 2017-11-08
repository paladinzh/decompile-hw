package com.google.android.gms.dynamic;

import android.content.Context;
import android.os.IBinder;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.internal.zzx;

/* compiled from: Unknown */
public abstract class zzg<T> {
    private final String zzanc;
    private T zzand;

    /* compiled from: Unknown */
    public static class zza extends Exception {
        public zza(String str) {
            super(str);
        }

        public zza(String str, Throwable th) {
            super(str, th);
        }
    }

    protected zzg(String str) {
        this.zzanc = str;
    }

    protected final T zzar(Context context) throws zza {
        if (this.zzand == null) {
            zzx.zzv(context);
            Context remoteContext = GooglePlayServicesUtil.getRemoteContext(context);
            if (remoteContext != null) {
                try {
                    this.zzand = zzd((IBinder) remoteContext.getClassLoader().loadClass(this.zzanc).newInstance());
                } catch (Throwable e) {
                    throw new zza("Could not load creator class.", e);
                } catch (Throwable e2) {
                    throw new zza("Could not instantiate creator.", e2);
                } catch (Throwable e22) {
                    throw new zza("Could not access creator.", e22);
                }
            }
            throw new zza("Could not get remote context.");
        }
        return this.zzand;
    }

    protected abstract T zzd(IBinder iBinder);
}
