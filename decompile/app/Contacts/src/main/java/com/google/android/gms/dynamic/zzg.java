package com.google.android.gms.dynamic;

import android.content.Context;
import android.os.IBinder;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.common.zze;

/* compiled from: Unknown */
public abstract class zzg<T> {
    private final String zzavI;
    private T zzavJ;

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
        this.zzavI = str;
    }

    protected final T zzaB(Context context) throws zza {
        if (this.zzavJ == null) {
            zzx.zzz(context);
            Context remoteContext = zze.getRemoteContext(context);
            if (remoteContext != null) {
                try {
                    this.zzavJ = zzd((IBinder) remoteContext.getClassLoader().loadClass(this.zzavI).newInstance());
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
        return this.zzavJ;
    }

    protected abstract T zzd(IBinder iBinder);
}
