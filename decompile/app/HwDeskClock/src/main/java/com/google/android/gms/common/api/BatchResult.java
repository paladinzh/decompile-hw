package com.google.android.gms.common.api;

/* compiled from: Unknown */
public final class BatchResult implements Result {
    private final Status zzQA;
    private final PendingResult<?>[] zzYS;

    BatchResult(Status status, PendingResult<?>[] pendingResults) {
        this.zzQA = status;
        this.zzYS = pendingResults;
    }

    public Status getStatus() {
        return this.zzQA;
    }
}
