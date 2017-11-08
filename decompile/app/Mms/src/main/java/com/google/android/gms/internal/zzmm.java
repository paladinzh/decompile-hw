package com.google.android.gms.internal;

import android.support.v4.util.ArrayMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

/* compiled from: Unknown */
public class zzmm<E> extends AbstractSet<E> {
    private final ArrayMap<E, E> zzanZ;

    public zzmm() {
        this.zzanZ = new ArrayMap();
    }

    public zzmm(int i) {
        this.zzanZ = new ArrayMap(i);
    }

    public zzmm(Collection<E> collection) {
        this(collection.size());
        addAll(collection);
    }

    public boolean add(E object) {
        if (this.zzanZ.containsKey(object)) {
            return false;
        }
        this.zzanZ.put(object, object);
        return true;
    }

    public boolean addAll(Collection<? extends E> collection) {
        return !(collection instanceof zzmm) ? super.addAll(collection) : zza((zzmm) collection);
    }

    public void clear() {
        this.zzanZ.clear();
    }

    public boolean contains(Object object) {
        return this.zzanZ.containsKey(object);
    }

    public Iterator<E> iterator() {
        return this.zzanZ.keySet().iterator();
    }

    public boolean remove(Object object) {
        if (!this.zzanZ.containsKey(object)) {
            return false;
        }
        this.zzanZ.remove(object);
        return true;
    }

    public int size() {
        return this.zzanZ.size();
    }

    public boolean zza(zzmm<? extends E> zzmm) {
        int size = size();
        this.zzanZ.putAll(zzmm.zzanZ);
        return size() > size;
    }
}
