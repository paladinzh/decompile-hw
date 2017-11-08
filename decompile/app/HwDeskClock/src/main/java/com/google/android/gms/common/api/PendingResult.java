package com.google.android.gms.common.api;

/* compiled from: Unknown */
public interface PendingResult<R extends Result> {

    /* compiled from: Unknown */
    public interface BatchCallback {
        void onComplete(Status status);
    }

    R await();

    void cancel();
}
