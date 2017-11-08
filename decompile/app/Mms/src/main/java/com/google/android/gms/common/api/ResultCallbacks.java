package com.google.android.gms.common.api;

import android.support.annotation.NonNull;
import com.google.android.gms.common.api.internal.zzb;

/* compiled from: Unknown */
public abstract class ResultCallbacks<R extends Result> implements ResultCallback<R> {
    public abstract void onFailure(@NonNull Status status);

    public final void onResult(@NonNull R result) {
        Status status = result.getStatus();
        if (status.isSuccess()) {
            onSuccess(result);
            return;
        }
        onFailure(status);
        zzb.zzc((Result) result);
    }

    public abstract void onSuccess(@NonNull R r);
}
