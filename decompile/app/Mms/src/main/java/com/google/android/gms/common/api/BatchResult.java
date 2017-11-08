package com.google.android.gms.common.api;

import com.google.android.gms.common.internal.zzx;
import java.util.concurrent.TimeUnit;

/* compiled from: Unknown */
public final class BatchResult implements Result {
    private final Status zzUX;
    private final PendingResult<?>[] zzagc;

    BatchResult(Status status, PendingResult<?>[] pendingResults) {
        this.zzUX = status;
        this.zzagc = pendingResults;
    }

    public Status getStatus() {
        return this.zzUX;
    }

    public <R extends Result> R take(BatchResultToken<R> resultToken) {
        zzx.zzb(resultToken.mId < this.zzagc.length, (Object) "The result token does not belong to this batch");
        return this.zzagc[resultToken.mId].await(0, TimeUnit.MILLISECONDS);
    }
}
