package com.google.android.gms.common.data;

import java.util.Iterator;

/* compiled from: Unknown */
public abstract class AbstractDataBuffer<T> implements DataBuffer<T> {
    protected final DataHolder zzYX;

    protected AbstractDataBuffer(DataHolder dataHolder) {
        this.zzYX = dataHolder;
        if (this.zzYX != null) {
            this.zzYX.zzq(this);
        }
    }

    public abstract T get(int i);

    public int getCount() {
        return this.zzYX != null ? this.zzYX.getCount() : 0;
    }

    public Iterator<T> iterator() {
        return new zzb(this);
    }

    public void release() {
        if (this.zzYX != null) {
            this.zzYX.close();
        }
    }
}
