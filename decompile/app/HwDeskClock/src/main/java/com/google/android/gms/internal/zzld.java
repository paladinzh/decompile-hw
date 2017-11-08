package com.google.android.gms.internal;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/* compiled from: Unknown */
public class zzld<K, V> extends zzlh<K, V> implements Map<K, V> {
    zzlg<K, V> zzaex;

    private zzlg<K, V> zzoX() {
        if (this.zzaex == null) {
            this.zzaex = new zzlg<K, V>(this) {
                final /* synthetic */ zzld zzaey;

                {
                    this.zzaey = r1;
                }

                protected void colClear() {
                    this.zzaey.clear();
                }

                protected Object colGetEntry(int index, int offset) {
                    return this.zzaey.mArray[(index << 1) + offset];
                }

                protected Map<K, V> colGetMap() {
                    return this.zzaey;
                }

                protected int colGetSize() {
                    return this.zzaey.mSize;
                }

                protected int colIndexOfKey(Object key) {
                    return key != null ? this.zzaey.indexOf(key, key.hashCode()) : this.zzaey.indexOfNull();
                }

                protected int colIndexOfValue(Object value) {
                    return this.zzaey.indexOfValue(value);
                }

                protected void colPut(K key, V value) {
                    this.zzaey.put(key, value);
                }

                protected void colRemoveAt(int index) {
                    this.zzaey.removeAt(index);
                }

                protected V colSetValue(int index, V value) {
                    return this.zzaey.setValueAt(index, value);
                }
            };
        }
        return this.zzaex;
    }

    public Set<Entry<K, V>> entrySet() {
        return zzoX().getEntrySet();
    }

    public Set<K> keySet() {
        return zzoX().getKeySet();
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        ensureCapacity(this.mSize + map.size());
        for (Entry entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public Collection<V> values() {
        return zzoX().getValues();
    }
}
