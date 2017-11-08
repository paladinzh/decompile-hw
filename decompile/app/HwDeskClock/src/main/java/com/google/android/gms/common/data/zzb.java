package com.google.android.gms.common.data;

import com.google.android.gms.common.internal.zzx;
import java.util.Iterator;
import java.util.NoSuchElementException;

/* compiled from: Unknown */
public class zzb<T> implements Iterator<T> {
    protected final DataBuffer<T> zzabe;
    protected int zzabf = -1;

    public zzb(DataBuffer<T> dataBuffer) {
        this.zzabe = (DataBuffer) zzx.zzv(dataBuffer);
    }

    public boolean hasNext() {
        return this.zzabf < this.zzabe.getCount() + -1;
    }

    public T next() {
        if (hasNext()) {
            DataBuffer dataBuffer = this.zzabe;
            int i = this.zzabf + 1;
            this.zzabf = i;
            return dataBuffer.get(i);
        }
        throw new NoSuchElementException("Cannot advance the iterator beyond " + this.zzabf);
    }

    public void remove() {
        throw new UnsupportedOperationException("Cannot remove elements from a DataBufferIterator");
    }
}
