package com.google.android.gms.common.data;

import java.util.NoSuchElementException;

/* compiled from: Unknown */
public class zzg<T> extends zzb<T> {
    private T zzajy;

    public zzg(DataBuffer<T> dataBuffer) {
        super(dataBuffer);
    }

    public T next() {
        if (hasNext()) {
            this.zzajc++;
            if (this.zzajc != 0) {
                ((zzc) this.zzajy).zzbF(this.zzajc);
            } else {
                this.zzajy = this.zzajb.get(0);
                if (!(this.zzajy instanceof zzc)) {
                    throw new IllegalStateException("DataBuffer reference of type " + this.zzajy.getClass() + " is not movable");
                }
            }
            return this.zzajy;
        }
        throw new NoSuchElementException("Cannot advance the iterator beyond " + this.zzajc);
    }
}
