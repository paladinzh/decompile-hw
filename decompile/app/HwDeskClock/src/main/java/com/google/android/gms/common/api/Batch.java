package com.google.android.gms.common.api;

/* compiled from: Unknown */
public final class Batch extends zzb<BatchResult> {
    private final PendingResult<?>[] zzYS;

    /* compiled from: Unknown */
    public static final class Builder {
    }

    public void cancel() {
        super.cancel();
        for (PendingResult cancel : this.zzYS) {
            cancel.cancel();
        }
    }

    public BatchResult createFailedResult(Status status) {
        return new BatchResult(status, this.zzYS);
    }

    public /* synthetic */ Result zzb(Status status) {
        return createFailedResult(status);
    }
}
