package com.google.android.gms.common.api;

/* compiled from: Unknown */
public abstract class OptionalPendingResult<R extends Result> extends PendingResult<R> {
    public abstract R get();

    public abstract boolean isDone();
}
