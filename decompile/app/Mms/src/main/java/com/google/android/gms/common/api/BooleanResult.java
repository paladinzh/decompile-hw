package com.google.android.gms.common.api;

import com.google.android.gms.common.internal.zzx;

/* compiled from: Unknown */
public class BooleanResult implements Result {
    private final Status zzUX;
    private final boolean zzagf;

    public BooleanResult(Status status, boolean value) {
        this.zzUX = (Status) zzx.zzb((Object) status, (Object) "Status must not be null");
        this.zzagf = value;
    }

    public final boolean equals(Object obj) {
        boolean z = true;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BooleanResult)) {
            return false;
        }
        BooleanResult booleanResult = (BooleanResult) obj;
        if (this.zzUX.equals(booleanResult.zzUX)) {
            if (this.zzagf != booleanResult.zzagf) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public Status getStatus() {
        return this.zzUX;
    }

    public boolean getValue() {
        return this.zzagf;
    }

    public final int hashCode() {
        int i = 0;
        int hashCode = (this.zzUX.hashCode() + 527) * 31;
        if (this.zzagf) {
            i = 1;
        }
        return i + hashCode;
    }
}
