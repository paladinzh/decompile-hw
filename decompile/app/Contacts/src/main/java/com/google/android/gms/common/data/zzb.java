package com.google.android.gms.common.data;

import com.google.android.gms.common.internal.zzx;
import java.util.Iterator;
import java.util.NoSuchElementException;

/* compiled from: Unknown */
public class zzb<T> implements Iterator<T> {
    protected final DataBuffer<T> zzajb;
    protected int zzajc = -1;

    public zzb(DataBuffer<T> dataBuffer) {
        this.zzajb = (DataBuffer) zzx.zzz(dataBuffer);
    }

    public boolean hasNext() {
        return this.zzajc < this.zzajb.getCount() + -1;
    }

    public T next() {
        if (hasNext()) {
            DataBuffer dataBuffer = this.zzajb;
            int i = this.zzajc + 1;
            this.zzajc = i;
            return dataBuffer.get(i);
        }
        throw new NoSuchElementException("Cannot advance the iterator beyond " + this.zzajc);
    }

    public void remove() {
        throw new UnsupportedOperationException("Cannot remove elements from a DataBufferIterator");
    }
}
