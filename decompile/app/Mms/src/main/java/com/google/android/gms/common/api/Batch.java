package com.google.android.gms.common.api;

import com.google.android.gms.common.api.PendingResult.zza;
import com.google.android.gms.common.api.internal.zzb;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public final class Batch extends zzb<BatchResult> {
    private int zzafZ;
    private boolean zzaga;
    private boolean zzagb;
    private final PendingResult<?>[] zzagc;
    private final Object zzpV;

    /* compiled from: Unknown */
    public static final class Builder {
        private GoogleApiClient zzaaj;
        private List<PendingResult<?>> zzage = new ArrayList();

        public Builder(GoogleApiClient googleApiClient) {
            this.zzaaj = googleApiClient;
        }

        public <R extends Result> BatchResultToken<R> add(PendingResult<R> pendingResult) {
            BatchResultToken<R> batchResultToken = new BatchResultToken(this.zzage.size());
            this.zzage.add(pendingResult);
            return batchResultToken;
        }

        public Batch build() {
            return new Batch(this.zzage, this.zzaaj);
        }
    }

    private Batch(List<PendingResult<?>> pendingResultList, GoogleApiClient apiClient) {
        super(apiClient);
        this.zzpV = new Object();
        this.zzafZ = pendingResultList.size();
        this.zzagc = new PendingResult[this.zzafZ];
        if (pendingResultList.isEmpty()) {
            zza(new BatchResult(Status.zzagC, this.zzagc));
            return;
        }
        for (int i = 0; i < pendingResultList.size(); i++) {
            PendingResult pendingResult = (PendingResult) pendingResultList.get(i);
            this.zzagc[i] = pendingResult;
            pendingResult.zza(new zza(this) {
                final /* synthetic */ Batch zzagd;

                {
                    this.zzagd = r1;
                }

                /* JADX WARNING: inconsistent code. */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void zzu(Status status) {
                    synchronized (this.zzagd.zzpV) {
                        if (this.zzagd.isCanceled()) {
                            return;
                        }
                        if (status.isCanceled()) {
                            this.zzagd.zzagb = true;
                        } else if (!status.isSuccess()) {
                            this.zzagd.zzaga = true;
                        }
                        this.zzagd.zzafZ = this.zzagd.zzafZ - 1;
                        if (this.zzagd.zzafZ == 0) {
                            if (this.zzagd.zzagb) {
                                super.cancel();
                            } else {
                                this.zzagd.zza(new BatchResult(!this.zzagd.zzaga ? Status.zzagC : new Status(13), this.zzagd.zzagc));
                            }
                        }
                    }
                }
            });
        }
    }

    public void cancel() {
        super.cancel();
        for (PendingResult cancel : this.zzagc) {
            cancel.cancel();
        }
    }

    public BatchResult createFailedResult(Status status) {
        return new BatchResult(status, this.zzagc);
    }

    public /* synthetic */ Result zzc(Status status) {
        return createFailedResult(status);
    }
}
