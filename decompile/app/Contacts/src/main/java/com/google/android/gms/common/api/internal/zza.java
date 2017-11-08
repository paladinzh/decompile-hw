package com.google.android.gms.common.api.internal;

import android.os.DeadObjectException;
import android.os.RemoteException;
import com.google.android.gms.common.api.Api.zzc;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.internal.zzx;
import java.util.concurrent.atomic.AtomicReference;

/* compiled from: Unknown */
public class zza {

    /* compiled from: Unknown */
    public interface zzb<R> {
        void zzs(R r);

        void zzw(Status status);
    }

    /* compiled from: Unknown */
    public static abstract class zza<R extends Result, A extends com.google.android.gms.common.api.Api.zzb> extends zzb<R> implements zzb<R>, zze<A> {
        private final zzc<A> zzaeE;
        private AtomicReference<zzd> zzagH = new AtomicReference();

        protected zza(zzc<A> zzc, GoogleApiClient googleApiClient) {
            super((GoogleApiClient) zzx.zzb((Object) googleApiClient, (Object) "GoogleApiClient must not be null"));
            this.zzaeE = (zzc) zzx.zzz(zzc);
        }

        private void zza(RemoteException remoteException) {
            zzw(new Status(8, remoteException.getLocalizedMessage(), null));
        }

        protected abstract void zza(A a) throws RemoteException;

        public void zza(zzd zzd) {
            this.zzagH.set(zzd);
        }

        public final void zzb(A a) throws DeadObjectException {
            try {
                zza((com.google.android.gms.common.api.Api.zzb) a);
            } catch (RemoteException e) {
                zza(e);
                throw e;
            } catch (RemoteException e2) {
                zza(e2);
            }
        }

        public final zzc<A> zzoR() {
            return this.zzaeE;
        }

        public void zzpe() {
            setResultCallback(null);
        }

        protected void zzpf() {
            zzd zzd = (zzd) this.zzagH.getAndSet(null);
            if (zzd != null) {
                zzd.zzc(this);
            }
        }

        public /* synthetic */ void zzs(Object obj) {
            super.zza((Result) obj);
        }

        public final void zzw(Status status) {
            boolean z = false;
            if (!status.isSuccess()) {
                z = true;
            }
            zzx.zzb(z, (Object) "Failed result must not be success");
            zza(zzc(status));
        }
    }
}
