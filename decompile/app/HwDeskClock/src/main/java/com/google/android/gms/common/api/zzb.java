package com.google.android.gms.common.api;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;
import com.google.android.gms.common.api.PendingResult.BatchCallback;
import com.google.android.gms.common.internal.zzq;
import com.google.android.gms.common.internal.zzx;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

/* compiled from: Unknown */
public abstract class zzb<R extends Result> implements PendingResult<R> {
    private boolean zzL;
    private final Object zzYD = new Object();
    protected final zza<R> zzYE;
    private final ArrayList<BatchCallback> zzYF = new ArrayList();
    private ResultCallback<R> zzYG;
    private volatile R zzYH;
    private volatile boolean zzYI;
    private boolean zzYJ;
    private zzq zzYK;
    private final CountDownLatch zzoR = new CountDownLatch(1);

    /* compiled from: Unknown */
    public static class zza<R extends Result> extends Handler {
        public zza() {
            this(Looper.getMainLooper());
        }

        public zza(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Pair pair = (Pair) msg.obj;
                    zzb((ResultCallback) pair.first, (Result) pair.second);
                    return;
                case 2:
                    ((zzb) msg.obj).zzw(Status.zzaaH);
                    return;
                default:
                    Log.wtf("AbstractPendingResult", "Don't know how to handle this message.");
                    return;
            }
        }

        public void zza(ResultCallback<R> resultCallback, R r) {
            sendMessage(obtainMessage(1, new Pair(resultCallback, r)));
        }

        protected void zzb(ResultCallback<R> resultCallback, R r) {
            try {
                resultCallback.onResult(r);
            } catch (RuntimeException e) {
                zzb.zzc(r);
                throw e;
            }
        }

        public void zzna() {
            removeMessages(2);
        }
    }

    protected zzb(Looper looper) {
        this.zzYE = new zza(looper);
    }

    private R get() {
        R r;
        boolean z = false;
        synchronized (this.zzYD) {
            if (!this.zzYI) {
                z = true;
            }
            zzx.zza(z, "Result has already been consumed.");
            zzx.zza(isReady(), "Result is not ready.");
            r = this.zzYH;
            this.zzYH = null;
            this.zzYG = null;
            this.zzYI = true;
        }
        zzmZ();
        return r;
    }

    private void zzb(R r) {
        this.zzYH = r;
        this.zzYK = null;
        this.zzoR.countDown();
        Status status = this.zzYH.getStatus();
        if (this.zzYG != null) {
            this.zzYE.zzna();
            if (!this.zzL) {
                this.zzYE.zza(this.zzYG, get());
            }
        }
        Iterator it = this.zzYF.iterator();
        while (it.hasNext()) {
            ((BatchCallback) it.next()).onComplete(status);
        }
        this.zzYF.clear();
    }

    static void zzc(Result result) {
        if (result instanceof Releasable) {
            try {
                ((Releasable) result).release();
            } catch (Throwable e) {
                Log.w("AbstractPendingResult", "Unable to release " + result, e);
            }
        }
    }

    public final R await() {
        boolean z = false;
        zzx.zza(Looper.myLooper() != Looper.getMainLooper(), "await must not be called on the UI thread");
        if (!this.zzYI) {
            z = true;
        }
        zzx.zza(z, "Result has already been consumed");
        try {
            this.zzoR.await();
        } catch (InterruptedException e) {
            zzw(Status.zzaaF);
        }
        zzx.zza(isReady(), "Result is not ready.");
        return get();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void cancel() {
        synchronized (this.zzYD) {
            if (!(this.zzL || this.zzYI)) {
                if (this.zzYK != null) {
                    try {
                        this.zzYK.cancel();
                    } catch (RemoteException e) {
                    }
                }
                zzc(this.zzYH);
                this.zzYG = null;
                this.zzL = true;
                zzb(zzb(Status.zzaaI));
            }
        }
    }

    public final boolean isReady() {
        return this.zzoR.getCount() == 0;
    }

    public final void zza(R r) {
        boolean z = false;
        synchronized (this.zzYD) {
            if (this.zzYJ || this.zzL) {
                zzc(r);
                return;
            }
            zzx.zza(!isReady(), "Results have already been set");
            if (!this.zzYI) {
                z = true;
            }
            zzx.zza(z, "Result has already been consumed");
            zzb((Result) r);
        }
    }

    protected abstract R zzb(Status status);

    protected void zzmZ() {
    }

    public final void zzw(Status status) {
        synchronized (this.zzYD) {
            if (!isReady()) {
                zza(zzb(status));
                this.zzYJ = true;
            }
        }
    }
}
