package com.google.android.gms.common.api;

/* compiled from: Unknown */
public class BooleanResult implements Result {
    private final Status zzQA;
    private final boolean zzYW;

    public final boolean equals(Object obj) {
        boolean z = true;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BooleanResult)) {
            return false;
        }
        BooleanResult booleanResult = (BooleanResult) obj;
        if (this.zzQA.equals(booleanResult.zzQA)) {
            if (this.zzYW != booleanResult.zzYW) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public Status getStatus() {
        return this.zzQA;
    }

    public final int hashCode() {
        int i = 0;
        int hashCode = (this.zzQA.hashCode() + 527) * 31;
        if (this.zzYW) {
            i = 1;
        }
        return i + hashCode;
    }
}
