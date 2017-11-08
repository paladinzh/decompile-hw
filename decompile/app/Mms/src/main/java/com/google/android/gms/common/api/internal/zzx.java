package com.google.android.gms.common.api.internal;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Releasable;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.ResultTransform;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.TransformedResult;
import java.lang.ref.WeakReference;

/* compiled from: Unknown */
public class zzx<R extends Result> extends TransformedResult<R> implements ResultCallback<R> {
    private final Object zzagI = new Object();
    private final WeakReference<GoogleApiClient> zzagK;
    private ResultTransform<? super R, ? extends Result> zzaiN = null;
    private zzx<? extends Result> zzaiO = null;
    private ResultCallbacks<? super R> zzaiP = null;
    private PendingResult<R> zzaiQ = null;
    private Status zzaiR = null;
    private final zza zzaiS;

    /* compiled from: Unknown */
    private final class zza extends Handler {
        final /* synthetic */ zzx zzaiU;

        public zza(zzx zzx, Looper looper) {
            this.zzaiU = zzx;
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    PendingResult pendingResult = (PendingResult) msg.obj;
                    synchronized (this.zzaiU.zzagI) {
                        if (pendingResult == null) {
                            this.zzaiU.zzaiO.zzy(new Status(13, "Transform returned null"));
                        } else if (pendingResult instanceof zzt) {
                            this.zzaiU.zzaiO.zzy(((zzt) pendingResult).getStatus());
                        } else {
                            this.zzaiU.zzaiO.zza(pendingResult);
                        }
                    }
                    return;
                case 1:
                    RuntimeException runtimeException = (RuntimeException) msg.obj;
                    Log.e("TransformedResultImpl", "Runtime exception on the transformation worker thread: " + runtimeException.getMessage());
                    throw runtimeException;
                default:
                    Log.e("TransformedResultImpl", "TransformationResultHandler received unknown message type: " + msg.what);
                    return;
            }
        }
    }

    public zzx(WeakReference<GoogleApiClient> weakReference) {
        com.google.android.gms.common.internal.zzx.zzb((Object) weakReference, (Object) "GoogleApiClient reference must not be null");
        this.zzagK = weakReference;
        GoogleApiClient googleApiClient = (GoogleApiClient) this.zzagK.get();
        this.zzaiS = new zza(this, googleApiClient == null ? Looper.getMainLooper() : googleApiClient.getLooper());
    }

    private void zzc(Result result) {
        if (result instanceof Releasable) {
            try {
                ((Releasable) result).release();
            } catch (Throwable e) {
                Log.w("TransformedResultImpl", "Unable to release " + result, e);
            }
        }
    }

    private void zzpT() {
        if (this.zzaiN != null || this.zzaiP != null) {
            GoogleApiClient googleApiClient = (GoogleApiClient) this.zzagK.get();
            if (!(this.zzaiN == null || googleApiClient == null)) {
                googleApiClient.zza(this);
            }
            if (this.zzaiR != null) {
                zzz(this.zzaiR);
            } else if (this.zzaiQ != null) {
                this.zzaiQ.setResultCallback(this);
            }
        }
    }

    private boolean zzpV() {
        return (this.zzaiP == null || ((GoogleApiClient) this.zzagK.get()) == null) ? false : true;
    }

    private void zzy(Status status) {
        synchronized (this.zzagI) {
            this.zzaiR = status;
            zzz(this.zzaiR);
        }
    }

    private void zzz(Status status) {
        synchronized (this.zzagI) {
            if (this.zzaiN != null) {
                Object onFailure = this.zzaiN.onFailure(status);
                com.google.android.gms.common.internal.zzx.zzb(onFailure, (Object) "onFailure must not return null");
                this.zzaiO.zzy(onFailure);
            } else if (zzpV()) {
                this.zzaiP.onFailure(status);
            }
        }
    }

    public void andFinally(@NonNull ResultCallbacks<? super R> callbacks) {
        boolean z = false;
        synchronized (this.zzagI) {
            com.google.android.gms.common.internal.zzx.zza(this.zzaiP == null, (Object) "Cannot call andFinally() twice.");
            if (this.zzaiN == null) {
                z = true;
            }
            com.google.android.gms.common.internal.zzx.zza(z, (Object) "Cannot call then() and andFinally() on the same TransformedResult.");
            this.zzaiP = callbacks;
            zzpT();
        }
    }

    public void onResult(final R result) {
        synchronized (this.zzagI) {
            if (!result.getStatus().isSuccess()) {
                zzy(result.getStatus());
                zzc((Result) result);
            } else if (this.zzaiN != null) {
                zzs.zzpN().submit(new Runnable(this) {
                    final /* synthetic */ zzx zzaiU;

                    @WorkerThread
                    public void run() {
                        GoogleApiClient googleApiClient;
                        try {
                            this.zzaiU.zzaiS.sendMessage(this.zzaiU.zzaiS.obtainMessage(0, this.zzaiU.zzaiN.onSuccess(result)));
                            this.zzaiU.zzc(result);
                            googleApiClient = (GoogleApiClient) this.zzaiU.zzagK.get();
                            if (googleApiClient != null) {
                                googleApiClient.zzb(this.zzaiU);
                            }
                        } catch (RuntimeException e) {
                            this.zzaiU.zzaiS.sendMessage(this.zzaiU.zzaiS.obtainMessage(1, e));
                            this.zzaiU.zzc(result);
                            googleApiClient = (GoogleApiClient) this.zzaiU.zzagK.get();
                            if (googleApiClient != null) {
                                googleApiClient.zzb(this.zzaiU);
                            }
                        } catch (Throwable th) {
                            Throwable th2 = th;
                            this.zzaiU.zzc(result);
                            googleApiClient = (GoogleApiClient) this.zzaiU.zzagK.get();
                            if (googleApiClient != null) {
                                googleApiClient.zzb(this.zzaiU);
                            }
                        }
                    }
                });
            } else if (zzpV()) {
                this.zzaiP.onSuccess(result);
            }
        }
    }

    @NonNull
    public <S extends Result> TransformedResult<S> then(@NonNull ResultTransform<? super R, ? extends S> transform) {
        TransformedResult zzx;
        boolean z = false;
        synchronized (this.zzagI) {
            com.google.android.gms.common.internal.zzx.zza(this.zzaiN == null, (Object) "Cannot call then() twice.");
            if (this.zzaiP == null) {
                z = true;
            }
            com.google.android.gms.common.internal.zzx.zza(z, (Object) "Cannot call then() and andFinally() on the same TransformedResult.");
            this.zzaiN = transform;
            zzx = new zzx(this.zzagK);
            this.zzaiO = zzx;
            zzpT();
        }
        return zzx;
    }

    public void zza(PendingResult<?> pendingResult) {
        synchronized (this.zzagI) {
            this.zzaiQ = pendingResult;
            zzpT();
        }
    }

    void zzpU() {
        synchronized (this.zzagI) {
            this.zzaiP = null;
        }
    }
}
