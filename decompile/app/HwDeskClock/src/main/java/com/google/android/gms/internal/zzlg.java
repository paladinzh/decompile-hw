package com.google.android.gms.internal;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/* compiled from: Unknown */
abstract class zzlg<K, V> {
    zzb zzaeG;
    zzc zzaeH;
    zze zzaeI;

    /* compiled from: Unknown */
    final class zza<T> implements Iterator<T> {
        boolean mCanRemove = false;
        int mIndex;
        final int mOffset;
        int mSize;
        final /* synthetic */ zzlg zzaeJ;

        zza(zzlg zzlg, int i) {
            this.zzaeJ = zzlg;
            this.mOffset = i;
            this.mSize = zzlg.colGetSize();
        }

        public boolean hasNext() {
            return this.mIndex < this.mSize;
        }

        public T next() {
            T colGetEntry = this.zzaeJ.colGetEntry(this.mIndex, this.mOffset);
            this.mIndex++;
            this.mCanRemove = true;
            return colGetEntry;
        }

        public void remove() {
            if (this.mCanRemove) {
                this.mIndex--;
                this.mSize--;
                this.mCanRemove = false;
                this.zzaeJ.colRemoveAt(this.mIndex);
                return;
            }
            throw new IllegalStateException();
        }
    }

    /* compiled from: Unknown */
    final class zzb implements Set<Entry<K, V>> {
        final /* synthetic */ zzlg zzaeJ;

        zzb(zzlg zzlg) {
            this.zzaeJ = zzlg;
        }

        public boolean add(Entry<K, V> entry) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends Entry<K, V>> collection) {
            int colGetSize = this.zzaeJ.colGetSize();
            for (Entry entry : collection) {
                this.zzaeJ.colPut(entry.getKey(), entry.getValue());
            }
            return colGetSize != this.zzaeJ.colGetSize();
        }

        public void clear() {
            this.zzaeJ.colClear();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry entry = (Entry) o;
            int colIndexOfKey = this.zzaeJ.colIndexOfKey(entry.getKey());
            return colIndexOfKey >= 0 ? zzle.equal(this.zzaeJ.colGetEntry(colIndexOfKey, 1), entry.getValue()) : false;
        }

        public boolean containsAll(Collection<?> collection) {
            for (Object contains : collection) {
                if (!contains(contains)) {
                    return false;
                }
            }
            return true;
        }

        public boolean equals(Object object) {
            return zzlg.equalsSetHelper(this, object);
        }

        public int hashCode() {
            int colGetSize = this.zzaeJ.colGetSize() - 1;
            int i = 0;
            while (colGetSize >= 0) {
                Object colGetEntry = this.zzaeJ.colGetEntry(colGetSize, 0);
                Object colGetEntry2 = this.zzaeJ.colGetEntry(colGetSize, 1);
                colGetSize--;
                i += (colGetEntry2 != null ? colGetEntry2.hashCode() : 0) ^ (colGetEntry != null ? colGetEntry.hashCode() : 0);
            }
            return i;
        }

        public boolean isEmpty() {
            return this.zzaeJ.colGetSize() == 0;
        }

        public Iterator<Entry<K, V>> iterator() {
            return new zzd(this.zzaeJ);
        }

        public boolean remove(Object object) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection<?> collection) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return this.zzaeJ.colGetSize();
        }

        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        public <T> T[] toArray(T[] tArr) {
            throw new UnsupportedOperationException();
        }
    }

    /* compiled from: Unknown */
    final class zzc implements Set<K> {
        final /* synthetic */ zzlg zzaeJ;

        zzc(zzlg zzlg) {
            this.zzaeJ = zzlg;
        }

        public boolean add(K k) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends K> collection) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            this.zzaeJ.colClear();
        }

        public boolean contains(Object object) {
            return this.zzaeJ.colIndexOfKey(object) >= 0;
        }

        public boolean containsAll(Collection<?> collection) {
            return zzlg.containsAllHelper(this.zzaeJ.colGetMap(), collection);
        }

        public boolean equals(Object object) {
            return zzlg.equalsSetHelper(this, object);
        }

        public int hashCode() {
            int i = 0;
            for (int colGetSize = this.zzaeJ.colGetSize() - 1; colGetSize >= 0; colGetSize--) {
                Object colGetEntry = this.zzaeJ.colGetEntry(colGetSize, 0);
                i += colGetEntry != null ? colGetEntry.hashCode() : 0;
            }
            return i;
        }

        public boolean isEmpty() {
            return this.zzaeJ.colGetSize() == 0;
        }

        public Iterator<K> iterator() {
            return new zza(this.zzaeJ, 0);
        }

        public boolean remove(Object object) {
            int colIndexOfKey = this.zzaeJ.colIndexOfKey(object);
            if (colIndexOfKey < 0) {
                return false;
            }
            this.zzaeJ.colRemoveAt(colIndexOfKey);
            return true;
        }

        public boolean removeAll(Collection<?> collection) {
            return zzlg.removeAllHelper(this.zzaeJ.colGetMap(), collection);
        }

        public boolean retainAll(Collection<?> collection) {
            return zzlg.retainAllHelper(this.zzaeJ.colGetMap(), collection);
        }

        public int size() {
            return this.zzaeJ.colGetSize();
        }

        public Object[] toArray() {
            return this.zzaeJ.toArrayHelper(0);
        }

        public <T> T[] toArray(T[] array) {
            return this.zzaeJ.toArrayHelper(array, 0);
        }
    }

    /* compiled from: Unknown */
    final class zzd implements Iterator<Entry<K, V>>, Entry<K, V> {
        int mEnd;
        boolean mEntryValid = false;
        int mIndex;
        final /* synthetic */ zzlg zzaeJ;

        zzd(zzlg zzlg) {
            this.zzaeJ = zzlg;
            this.mEnd = zzlg.colGetSize() - 1;
            this.mIndex = -1;
        }

        public final boolean equals(Object o) {
            boolean z = true;
            if (!this.mEntryValid) {
                throw new IllegalStateException("This container does not support retaining Map.Entry objects");
            } else if (!(o instanceof Entry)) {
                return false;
            } else {
                Entry o2 = (Entry) o;
                if (zzle.equal(o2.getKey(), this.zzaeJ.colGetEntry(this.mIndex, 0))) {
                    if (!zzle.equal(o2.getValue(), this.zzaeJ.colGetEntry(this.mIndex, 1))) {
                    }
                    return z;
                }
                z = false;
                return z;
            }
        }

        public K getKey() {
            if (this.mEntryValid) {
                return this.zzaeJ.colGetEntry(this.mIndex, 0);
            }
            throw new IllegalStateException("This container does not support retaining Map.Entry objects");
        }

        public V getValue() {
            if (this.mEntryValid) {
                return this.zzaeJ.colGetEntry(this.mIndex, 1);
            }
            throw new IllegalStateException("This container does not support retaining Map.Entry objects");
        }

        public boolean hasNext() {
            return this.mIndex < this.mEnd;
        }

        public final int hashCode() {
            int i = 0;
            if (this.mEntryValid) {
                Object colGetEntry = this.zzaeJ.colGetEntry(this.mIndex, 0);
                Object colGetEntry2 = this.zzaeJ.colGetEntry(this.mIndex, 1);
                int hashCode = colGetEntry != null ? colGetEntry.hashCode() : 0;
                if (colGetEntry2 != null) {
                    i = colGetEntry2.hashCode();
                }
                return i ^ hashCode;
            }
            throw new IllegalStateException("This container does not support retaining Map.Entry objects");
        }

        public Entry<K, V> next() {
            this.mIndex++;
            this.mEntryValid = true;
            return this;
        }

        public void remove() {
            if (this.mEntryValid) {
                this.zzaeJ.colRemoveAt(this.mIndex);
                this.mIndex--;
                this.mEnd--;
                this.mEntryValid = false;
                return;
            }
            throw new IllegalStateException();
        }

        public V setValue(V object) {
            if (this.mEntryValid) {
                return this.zzaeJ.colSetValue(this.mIndex, object);
            }
            throw new IllegalStateException("This container does not support retaining Map.Entry objects");
        }

        public final String toString() {
            return getKey() + "=" + getValue();
        }
    }

    /* compiled from: Unknown */
    final class zze implements Collection<V> {
        final /* synthetic */ zzlg zzaeJ;

        zze(zzlg zzlg) {
            this.zzaeJ = zzlg;
        }

        public boolean add(V v) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends V> collection) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            this.zzaeJ.colClear();
        }

        public boolean contains(Object object) {
            return this.zzaeJ.colIndexOfValue(object) >= 0;
        }

        public boolean containsAll(Collection<?> collection) {
            for (Object contains : collection) {
                if (!contains(contains)) {
                    return false;
                }
            }
            return true;
        }

        public boolean isEmpty() {
            return this.zzaeJ.colGetSize() == 0;
        }

        public Iterator<V> iterator() {
            return new zza(this.zzaeJ, 1);
        }

        public boolean remove(Object object) {
            int colIndexOfValue = this.zzaeJ.colIndexOfValue(object);
            if (colIndexOfValue < 0) {
                return false;
            }
            this.zzaeJ.colRemoveAt(colIndexOfValue);
            return true;
        }

        public boolean removeAll(Collection<?> collection) {
            int i = 0;
            int colGetSize = this.zzaeJ.colGetSize();
            boolean z = false;
            while (i < colGetSize) {
                if (collection.contains(this.zzaeJ.colGetEntry(i, 1))) {
                    this.zzaeJ.colRemoveAt(i);
                    i--;
                    colGetSize--;
                    z = true;
                }
                i++;
            }
            return z;
        }

        public boolean retainAll(Collection<?> collection) {
            int i = 0;
            int colGetSize = this.zzaeJ.colGetSize();
            boolean z = false;
            while (i < colGetSize) {
                if (!collection.contains(this.zzaeJ.colGetEntry(i, 1))) {
                    this.zzaeJ.colRemoveAt(i);
                    i--;
                    colGetSize--;
                    z = true;
                }
                i++;
            }
            return z;
        }

        public int size() {
            return this.zzaeJ.colGetSize();
        }

        public Object[] toArray() {
            return this.zzaeJ.toArrayHelper(1);
        }

        public <T> T[] toArray(T[] array) {
            return this.zzaeJ.toArrayHelper(array, 1);
        }
    }

    zzlg() {
    }

    public static <K, V> boolean containsAllHelper(Map<K, V> map, Collection<?> collection) {
        for (Object containsKey : collection) {
            if (!map.containsKey(containsKey)) {
                return false;
            }
        }
        return true;
    }

    public static <T> boolean equalsSetHelper(Set<T> set, Object object) {
        boolean z = true;
        if (set == object) {
            return true;
        }
        if (!(object instanceof Set)) {
            return false;
        }
        Set set2 = (Set) object;
        try {
            if (set.size() == set2.size()) {
                if (!set.containsAll(set2)) {
                }
                return z;
            }
            z = false;
            return z;
        } catch (NullPointerException e) {
            return false;
        } catch (ClassCastException e2) {
            return false;
        }
    }

    public static <K, V> boolean removeAllHelper(Map<K, V> map, Collection<?> collection) {
        int size = map.size();
        for (Object remove : collection) {
            map.remove(remove);
        }
        return size != map.size();
    }

    public static <K, V> boolean retainAllHelper(Map<K, V> map, Collection<?> collection) {
        int size = map.size();
        Iterator it = map.keySet().iterator();
        while (it.hasNext()) {
            if (!collection.contains(it.next())) {
                it.remove();
            }
        }
        return size != map.size();
    }

    protected abstract void colClear();

    protected abstract Object colGetEntry(int i, int i2);

    protected abstract Map<K, V> colGetMap();

    protected abstract int colGetSize();

    protected abstract int colIndexOfKey(Object obj);

    protected abstract int colIndexOfValue(Object obj);

    protected abstract void colPut(K k, V v);

    protected abstract void colRemoveAt(int i);

    protected abstract V colSetValue(int i, V v);

    public Set<Entry<K, V>> getEntrySet() {
        if (this.zzaeG == null) {
            this.zzaeG = new zzb(this);
        }
        return this.zzaeG;
    }

    public Set<K> getKeySet() {
        if (this.zzaeH == null) {
            this.zzaeH = new zzc(this);
        }
        return this.zzaeH;
    }

    public Collection<V> getValues() {
        if (this.zzaeI == null) {
            this.zzaeI = new zze(this);
        }
        return this.zzaeI;
    }

    public Object[] toArrayHelper(int offset) {
        int colGetSize = colGetSize();
        Object[] objArr = new Object[colGetSize];
        for (int i = 0; i < colGetSize; i++) {
            objArr[i] = colGetEntry(i, offset);
        }
        return objArr;
    }

    public <T> T[] toArrayHelper(T[] array, int offset) {
        int colGetSize = colGetSize();
        if (array.length < colGetSize) {
            Object[] array2 = (Object[]) ((Object[]) Array.newInstance(array.getClass().getComponentType(), colGetSize));
        }
        for (int i = 0; i < colGetSize; i++) {
            array[i] = colGetEntry(i, offset);
        }
        if (array.length > colGetSize) {
            array[colGetSize] = null;
        }
        return array;
    }
}
