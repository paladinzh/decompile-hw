package com.google.android.gms.common.api.internal;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Releasable;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultTransform;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.TransformedResult;
import com.google.android.gms.common.internal.zzq;
import com.google.android.gms.common.internal.zzx;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/* compiled from: Unknown */
public abstract class zzb<R extends Result> extends PendingResult<R> {
    private boolean zzL;
    private final Object zzagI = new Object();
    protected final zza<R> zzagJ;
    private final WeakReference<GoogleApiClient> zzagK;
    private final ArrayList<com.google.android.gms.common.api.PendingResult.zza> zzagL = new ArrayList();
    private ResultCallback<? super R> zzagM;
    private volatile boolean zzagN;
    private boolean zzagO;
    private boolean zzagP;
    private zzq zzagQ;
    private Integer zzagR;
    private volatile zzx<R> zzagS;
    private volatile R zzagy;
    private final CountDownLatch zzpJ = new CountDownLatch(1);

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
                    ((zzb) msg.obj).zzx(Status.zzagF);
                    return;
                default:
                    Log.wtf("BasePendingResult", "Don't know how to handle message: " + msg.what, new Exception());
                    return;
            }
        }

        public void zza(ResultCallback<? super R> resultCallback, R r) {
            sendMessage(obtainMessage(1, new Pair(resultCallback, r)));
        }

        public void zza(zzb<R> zzb, long j) {
            sendMessageDelayed(obtainMessage(2, zzb), j);
        }

        protected void zzb(ResultCallback<? super R> resultCallback, R r) {
            try {
                resultCallback.onResult(r);
            } catch (RuntimeException e) {
                zzb.zzc((Result) r);
                throw e;
            }
        }

        public void zzph() {
            removeMessages(2);
        }
    }

    @Deprecated
    protected zzb(Looper looper) {
        this.zzagJ = new zza(looper);
        this.zzagK = new WeakReference(null);
    }

    protected zzb(GoogleApiClient googleApiClient) {
        this.zzagJ = new zza(googleApiClient == null ? Looper.getMainLooper() : googleApiClient.getLooper());
        this.zzagK = new WeakReference(googleApiClient);
    }

    private R get() {
        R r;
        boolean z = false;
        synchronized (this.zzagI) {
            if (!this.zzagN) {
                z = true;
            }
            zzx.zza(z, (Object) "Result has already been consumed.");
            zzx.zza(isReady(), (Object) "Result is not ready.");
            r = this.zzagy;
            this.zzagy = null;
            this.zzagM = null;
            this.zzagN = true;
        }
        zzpf();
        return r;
    }

    private void zzb(R r) {
        this.zzagy = r;
        this.zzagQ = null;
        this.zzpJ.countDown();
        Status status = this.zzagy.getStatus();
        if (this.zzagM != null) {
            this.zzagJ.zzph();
            if (!this.zzL) {
                this.zzagJ.zza(this.zzagM, get());
            }
        }
        Iterator it = this.zzagL.iterator();
        while (it.hasNext()) {
            ((com.google.android.gms.common.api.PendingResult.zza) it.next()).zzu(status);
        }
        this.zzagL.clear();
    }

    public static void zzc(Result result) {
        if (result instanceof Releasable) {
            try {
                ((Releasable) result).release();
            } catch (Throwable e) {
                Log.w("BasePendingResult", "Unable to release " + result, e);
            }
        }
    }

    public final R await() {
        boolean z = false;
        zzx.zza(Looper.myLooper() != Looper.getMainLooper(), (Object) "await must not be called on the UI thread");
        zzx.zza(!this.zzagN, (Object) "Result has already been consumed");
        if (this.zzagS == null) {
            z = true;
        }
        zzx.zza(z, (Object) "Cannot await if then() has been called.");
        try {
            this.zzpJ.await();
        } catch (InterruptedException e) {
            zzx(Status.zzagD);
        }
        zzx.zza(isReady(), (Object) "Result is not ready.");
        return get();
    }

    public final R await(long time, TimeUnit units) {
        boolean z = false;
        boolean z2 = ((time > 0 ? 1 : (time == 0 ? 0 : -1)) <= 0) || Looper.myLooper() != Looper.getMainLooper();
        zzx.zza(z2, (Object) "await must not be called on the UI thread when time is greater than zero.");
        zzx.zza(!this.zzagN, (Object) "Result has already been consumed.");
        if (this.zzagS == null) {
            z = true;
        }
        zzx.zza(z, (Object) "Cannot await if then() has been called.");
        try {
            if (!this.zzpJ.await(time, units)) {
                zzx(Status.zzagF);
            }
        } catch (InterruptedException e) {
            zzx(Status.zzagD);
        }
        zzx.zza(isReady(), (Object) "Result is not ready.");
        return get();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void cancel() {
        synchronized (this.zzagI) {
            if (!(this.zzL || this.zzagN)) {
                if (this.zzagQ != null) {
                    try {
                        this.zzagQ.cancel();
                    } catch (RemoteException e) {
                    }
                }
                zzc(this.zzagy);
                this.zzagM = null;
                this.zzL = true;
                zzb(zzc(Status.zzagG));
            }
        }
    }

    public boolean isCanceled() {
        boolean z;
        synchronized (this.zzagI) {
            z = this.zzL;
        }
        return z;
    }

    public final boolean isReady() {
        return this.zzpJ.getCount() == 0;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void setResultCallback(ResultCallback<? super R> callback) {
        boolean z = false;
        zzx.zza(!this.zzagN, (Object) "Result has already been consumed.");
        synchronized (this.zzagI) {
            if (this.zzagS == null) {
                z = true;
            }
            zzx.zza(z, (Object) "Cannot set callbacks if then() has been called.");
            if (isCanceled()) {
                return;
            }
            if (this.zzagP) {
                if (((GoogleApiClient) this.zzagK.get()) == null || !(callback instanceof zzx)) {
                    cancel();
                    return;
                }
            }
            if (isReady()) {
                this.zzagJ.zza((ResultCallback) callback, get());
            } else {
                this.zzagM = callback;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void setResultCallback(ResultCallback<? super R> callback, long time, TimeUnit units) {
        boolean z = false;
        zzx.zza(!this.zzagN, (Object) "Result has already been consumed.");
        synchronized (this.zzagI) {
            if (this.zzagS == null) {
                z = true;
            }
            zzx.zza(z, (Object) "Cannot set callbacks if then() has been called.");
            if (isCanceled()) {
                return;
            }
            if (this.zzagP) {
                if (((GoogleApiClient) this.zzagK.get()) == null || !(callback instanceof zzx)) {
                    cancel();
                    return;
                }
            }
            if (isReady()) {
                this.zzagJ.zza((ResultCallback) callback, get());
            } else {
                this.zzagM = callback;
                this.zzagJ.zza(this, units.toMillis(time));
            }
        }
    }

    public <S extends Result> TransformedResult<S> then(ResultTransform<? super R, ? extends S> transform) {
        TransformedResult<S> then;
        boolean z = false;
        zzx.zza(!this.zzagN, (Object) "Result has already been consumed.");
        synchronized (this.zzagI) {
            zzx.zza(this.zzagS == null, (Object) "Cannot call then() twice.");
            if (this.zzagM == null) {
                z = true;
            }
            zzx.zza(z, (Object) "Cannot call then() if callbacks are set.");
            this.zzagS = new zzx(this.zzagK);
            then = this.zzagS.then(transform);
            if (isReady()) {
                this.zzagJ.zza(this.zzagS, get());
            } else {
                this.zzagM = this.zzagS;
            }
        }
        return then;
    }

    public final void zza(com.google.android.gms.common.api.PendingResult.zza zza) {
        boolean z = false;
        zzx.zza(!this.zzagN, (Object) "Result has already been consumed.");
        if (zza != null) {
            z = true;
        }
        zzx.zzb(z, (Object) "Callback cannot be null.");
        synchronized (this.zzagI) {
            if (isReady()) {
                zza.zzu(this.zzagy.getStatus());
            } else {
                this.zzagL.add(zza);
            }
        }
    }

    public final void zza(R r) {
        boolean z = false;
        synchronized (this.zzagI) {
            if (this.zzagO || this.zzL) {
                zzc((Result) r);
                return;
            }
            zzx.zza(!isReady(), (Object) "Results have already been set");
            if (!this.zzagN) {
                z = true;
            }
            zzx.zza(z, (Object) "Result has already been consumed");
            zzb(r);
        }
    }

    protected final void zza(zzq zzq) {
        synchronized (this.zzagI) {
            this.zzagQ = zzq;
        }
    }

    protected abstract R zzc(Status status);

    public Integer zzpa() {
        return this.zzagR;
    }

    protected void zzpf() {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void zzpg() {
        synchronized (this.zzagI) {
            if (((GoogleApiClient) this.zzagK.get()) == null) {
                cancel();
            } else if (this.zzagM == null || (this.zzagM instanceof zzx)) {
                this.zzagP = true;
            } else {
                cancel();
            }
        }
    }

    public final void zzx(Status status) {
        synchronized (this.zzagI) {
            if (!isReady()) {
                zza(zzc(status));
                this.zzagO = true;
            }
        }
    }
}
