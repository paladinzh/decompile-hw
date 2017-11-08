package com.google.android.gms.common.data;

import java.util.NoSuchElementException;

/* compiled from: Unknown */
public class zzg<T> extends zzb<T> {
    private T zzabB;

    public T next() {
        if (hasNext()) {
            this.zzabf++;
            if (this.zzabf != 0) {
                ((zzc) this.zzabB).zzbm(this.zzabf);
            } else {
                this.zzabB = this.zzabe.get(0);
                if (!(this.zzabB instanceof zzc)) {
                    throw new IllegalStateException("DataBuffer reference of type " + this.zzabB.getClass() + " is not movable");
                }
            }
            return this.zzabB;
        }
        throw new NoSuchElementException("Cannot advance the iterator beyond " + this.zzabf);
    }
}
