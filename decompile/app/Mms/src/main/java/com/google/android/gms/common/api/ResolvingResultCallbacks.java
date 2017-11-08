package com.google.android.gms.common.api;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.common.internal.zzx;

/* compiled from: Unknown */
public abstract class ResolvingResultCallbacks<R extends Result> extends ResultCallbacks<R> {
    private final Activity mActivity;
    private final int zzagz;

    protected ResolvingResultCallbacks(@NonNull Activity activity, int requestCode) {
        this.mActivity = (Activity) zzx.zzb((Object) activity, (Object) "Activity must not be null");
        this.zzagz = requestCode;
    }

    public final void onFailure(@NonNull Status result) {
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(this.mActivity, this.zzagz);
                return;
            } catch (Throwable e) {
                Log.e("ResolvingResultCallback", "Failed to start resolution", e);
                onUnresolvableFailure(new Status(8));
                return;
            }
        }
        onUnresolvableFailure(result);
    }

    public abstract void onSuccess(@NonNull R r);

    public abstract void onUnresolvableFailure(@NonNull Status status);
}
