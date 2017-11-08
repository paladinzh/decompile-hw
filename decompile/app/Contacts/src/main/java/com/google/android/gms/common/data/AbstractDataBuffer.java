package com.google.android.gms.common.data;

import android.os.Bundle;
import java.util.Iterator;

/* compiled from: Unknown */
public abstract class AbstractDataBuffer<T> implements DataBuffer<T> {
    protected final DataHolder zzahi;

    protected AbstractDataBuffer(DataHolder dataHolder) {
        this.zzahi = dataHolder;
        if (this.zzahi != null) {
            this.zzahi.zzu(this);
        }
    }

    @Deprecated
    public final void close() {
        release();
    }

    public abstract T get(int i);

    public int getCount() {
        return this.zzahi != null ? this.zzahi.getCount() : 0;
    }

    @Deprecated
    public boolean isClosed() {
        return this.zzahi == null || this.zzahi.isClosed();
    }

    public Iterator<T> iterator() {
        return new zzb(this);
    }

    public void release() {
        if (this.zzahi != null) {
            this.zzahi.close();
        }
    }

    public Iterator<T> singleRefIterator() {
        return new zzg(this);
    }

    public Bundle zzpZ() {
        return this.zzahi.zzpZ();
    }
}
