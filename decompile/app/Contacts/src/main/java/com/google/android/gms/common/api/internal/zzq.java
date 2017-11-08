package com.google.android.gms.common.api.internal;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.google.android.gms.common.internal.zzx;

/* compiled from: Unknown */
public final class zzq<L> {
    private volatile L mListener;
    private final zza zzaiw;

    /* compiled from: Unknown */
    public interface zzb<L> {
        void zzpr();

        void zzt(L l);
    }

    /* compiled from: Unknown */
    private final class zza extends Handler {
        final /* synthetic */ zzq zzaix;

        public zza(zzq zzq, Looper looper) {
            this.zzaix = zzq;
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            if (msg.what != 1) {
                z = false;
            }
            zzx.zzac(z);
            this.zzaix.zzb((zzb) msg.obj);
        }
    }

    zzq(Looper looper, L l) {
        this.zzaiw = new zza(this, looper);
        this.mListener = zzx.zzb((Object) l, (Object) "Listener must not be null");
    }

    public void clear() {
        this.mListener = null;
    }

    public void zza(zzb<? super L> zzb) {
        zzx.zzb((Object) zzb, (Object) "Notifier must not be null");
        this.zzaiw.sendMessage(this.zzaiw.obtainMessage(1, zzb));
    }

    void zzb(zzb<? super L> zzb) {
        Object obj = this.mListener;
        if (obj != null) {
            try {
                zzb.zzt(obj);
                return;
            } catch (RuntimeException e) {
                zzb.zzpr();
                throw e;
            }
        }
        zzb.zzpr();
    }
}
