package com.google.android.gms.internal;

import java.util.Map;

/* compiled from: Unknown */
public class zzlh<K, V> {
    static Object[] mBaseCache;
    static int mBaseCacheSize;
    static Object[] mTwiceBaseCache;
    static int mTwiceBaseCacheSize;
    Object[] mArray = zzle.EMPTY_OBJECTS;
    int[] mHashes = zzle.EMPTY_INTS;
    int mSize = 0;

    private static void zza(int[] iArr, Object[] objArr, int i) {
        int i2;
        if (iArr.length == 8) {
            synchronized (zzld.class) {
                if (mTwiceBaseCacheSize < 10) {
                    objArr[0] = mTwiceBaseCache;
                    objArr[1] = iArr;
                    for (i2 = (i << 1) - 1; i2 >= 2; i2--) {
                        objArr[i2] = null;
                    }
                    mTwiceBaseCache = objArr;
                    mTwiceBaseCacheSize++;
                }
            }
        } else if (iArr.length == 4) {
            synchronized (zzld.class) {
                if (mBaseCacheSize < 10) {
                    objArr[0] = mBaseCache;
                    objArr[1] = iArr;
                    for (i2 = (i << 1) - 1; i2 >= 2; i2--) {
                        objArr[i2] = null;
                    }
                    mBaseCache = objArr;
                    mBaseCacheSize++;
                }
            }
        }
    }

    private void zzbJ(int i) {
        Object[] objArr;
        if (i == 8) {
            synchronized (zzld.class) {
                if (mTwiceBaseCache == null) {
                } else {
                    objArr = mTwiceBaseCache;
                    this.mArray = objArr;
                    mTwiceBaseCache = (Object[]) objArr[0];
                    this.mHashes = (int[]) objArr[1];
                    objArr[1] = null;
                    objArr[0] = null;
                    mTwiceBaseCacheSize--;
                    return;
                }
            }
        } else if (i == 4) {
            synchronized (zzld.class) {
                if (mBaseCache == null) {
                } else {
                    objArr = mBaseCache;
                    this.mArray = objArr;
                    mBaseCache = (Object[]) objArr[0];
                    this.mHashes = (int[]) objArr[1];
                    objArr[1] = null;
                    objArr[0] = null;
                    mBaseCacheSize--;
                    return;
                }
            }
        }
        this.mHashes = new int[i];
        this.mArray = new Object[(i << 1)];
    }

    public void clear() {
        if (this.mSize != 0) {
            zza(this.mHashes, this.mArray, this.mSize);
            this.mHashes = zzle.EMPTY_INTS;
            this.mArray = zzle.EMPTY_OBJECTS;
            this.mSize = 0;
        }
    }

    public boolean containsKey(Object key) {
        if (key != null) {
            if (indexOf(key, key.hashCode()) < 0) {
                return false;
            }
        } else if (indexOfNull() < 0) {
            return false;
        }
        return true;
    }

    public boolean containsValue(Object value) {
        return indexOfValue(value) >= 0;
    }

    public void ensureCapacity(int minimumCapacity) {
        if (this.mHashes.length < minimumCapacity) {
            Object obj = this.mHashes;
            Object obj2 = this.mArray;
            zzbJ(minimumCapacity);
            if (this.mSize > 0) {
                System.arraycopy(obj, 0, this.mHashes, 0, this.mSize);
                System.arraycopy(obj2, 0, this.mArray, 0, this.mSize << 1);
            }
            zza(obj, obj2, this.mSize);
        }
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Map)) {
            return false;
        }
        Map map = (Map) object;
        if (size() != map.size()) {
            return false;
        }
        int i = 0;
        while (i < this.mSize) {
            try {
                Object keyAt = keyAt(i);
                Object valueAt = valueAt(i);
                Object obj = map.get(keyAt);
                if (valueAt != null) {
                    if (!valueAt.equals(obj)) {
                        return false;
                    }
                } else if (obj != null || !map.containsKey(keyAt)) {
                    return false;
                }
                i++;
            } catch (NullPointerException e) {
                return false;
            } catch (ClassCastException e2) {
                return false;
            }
        }
        return true;
    }

    public V get(Object key) {
        int indexOf = key != null ? indexOf(key, key.hashCode()) : indexOfNull();
        return indexOf < 0 ? null : this.mArray[(indexOf << 1) + 1];
    }

    public int hashCode() {
        int[] iArr = this.mHashes;
        Object[] objArr = this.mArray;
        int i = this.mSize;
        int i2 = 1;
        int i3 = 0;
        int i4 = 0;
        while (i3 < i) {
            Object obj = objArr[i2];
            i4 += (obj != null ? obj.hashCode() : 0) ^ iArr[i3];
            i3++;
            i2 += 2;
        }
        return i4;
    }

    int indexOf(Object key, int hash) {
        int i = this.mSize;
        if (i == 0) {
            return -1;
        }
        int binarySearch = zzle.binarySearch(this.mHashes, i, hash);
        if (binarySearch < 0 || key.equals(this.mArray[binarySearch << 1])) {
            return binarySearch;
        }
        int i2 = binarySearch + 1;
        while (i2 < i && this.mHashes[i2] == hash) {
            if (key.equals(this.mArray[i2 << 1])) {
                return i2;
            }
            i2++;
        }
        int i3 = binarySearch - 1;
        while (i3 >= 0 && this.mHashes[i3] == hash) {
            if (key.equals(this.mArray[i3 << 1])) {
                return i3;
            }
            i3--;
        }
        return i2 ^ -1;
    }

    int indexOfNull() {
        int i = this.mSize;
        if (i == 0) {
            return -1;
        }
        int binarySearch = zzle.binarySearch(this.mHashes, i, 0);
        if (binarySearch < 0 || this.mArray[binarySearch << 1] == null) {
            return binarySearch;
        }
        int i2 = binarySearch + 1;
        while (i2 < i && this.mHashes[i2] == 0) {
            if (this.mArray[i2 << 1] == null) {
                return i2;
            }
            i2++;
        }
        int i3 = binarySearch - 1;
        while (i3 >= 0 && this.mHashes[i3] == 0) {
            if (this.mArray[i3 << 1] == null) {
                return i3;
            }
            i3--;
        }
        return i2 ^ -1;
    }

    int indexOfValue(Object value) {
        int i = 1;
        int i2 = this.mSize * 2;
        Object[] objArr = this.mArray;
        if (value != null) {
            while (i < i2) {
                if (value.equals(objArr[i])) {
                    return i >> 1;
                }
                i += 2;
            }
        } else {
            while (i < i2) {
                if (objArr[i] == null) {
                    return i >> 1;
                }
                i += 2;
            }
        }
        return -1;
    }

    public boolean isEmpty() {
        return this.mSize <= 0;
    }

    public K keyAt(int index) {
        return this.mArray[index << 1];
    }

    public V put(K key, V value) {
        int hashCode;
        int indexOf;
        int i = 4;
        if (key != null) {
            hashCode = key.hashCode();
            indexOf = indexOf(key, hashCode);
        } else {
            indexOf = indexOfNull();
            hashCode = 0;
        }
        if (indexOf < 0) {
            indexOf ^= -1;
            if (this.mSize >= this.mHashes.length) {
                if (this.mSize >= 8) {
                    i = this.mSize + (this.mSize >> 1);
                } else if (this.mSize >= 4) {
                    i = 8;
                }
                Object obj = this.mHashes;
                Object obj2 = this.mArray;
                zzbJ(i);
                if (this.mHashes.length > 0) {
                    System.arraycopy(obj, 0, this.mHashes, 0, obj.length);
                    System.arraycopy(obj2, 0, this.mArray, 0, obj2.length);
                }
                zza(obj, obj2, this.mSize);
            }
            if (indexOf < this.mSize) {
                System.arraycopy(this.mHashes, indexOf, this.mHashes, indexOf + 1, this.mSize - indexOf);
                System.arraycopy(this.mArray, indexOf << 1, this.mArray, (indexOf + 1) << 1, (this.mSize - indexOf) << 1);
            }
            this.mHashes[indexOf] = hashCode;
            this.mArray[indexOf << 1] = key;
            this.mArray[(indexOf << 1) + 1] = value;
            this.mSize++;
            return null;
        }
        i = (indexOf << 1) + 1;
        V v = this.mArray[i];
        this.mArray[i] = value;
        return v;
    }

    public V remove(Object key) {
        int indexOf = key != null ? indexOf(key, key.hashCode()) : indexOfNull();
        return indexOf < 0 ? null : removeAt(indexOf);
    }

    public V removeAt(int index) {
        int i = 8;
        V v = this.mArray[(index << 1) + 1];
        if (this.mSize <= 1) {
            zza(this.mHashes, this.mArray, this.mSize);
            this.mHashes = zzle.EMPTY_INTS;
            this.mArray = zzle.EMPTY_OBJECTS;
            this.mSize = 0;
        } else if (this.mHashes.length > 8 && this.mSize < this.mHashes.length / 3) {
            if (this.mSize > 8) {
                i = this.mSize + (this.mSize >> 1);
            }
            Object obj = this.mHashes;
            Object obj2 = this.mArray;
            zzbJ(i);
            this.mSize--;
            if (index > 0) {
                System.arraycopy(obj, 0, this.mHashes, 0, index);
                System.arraycopy(obj2, 0, this.mArray, 0, index << 1);
            }
            if (index < this.mSize) {
                System.arraycopy(obj, index + 1, this.mHashes, index, this.mSize - index);
                System.arraycopy(obj2, (index + 1) << 1, this.mArray, index << 1, (this.mSize - index) << 1);
            }
        } else {
            this.mSize--;
            if (index < this.mSize) {
                System.arraycopy(this.mHashes, index + 1, this.mHashes, index, this.mSize - index);
                System.arraycopy(this.mArray, (index + 1) << 1, this.mArray, index << 1, (this.mSize - index) << 1);
            }
            this.mArray[this.mSize << 1] = null;
            this.mArray[(this.mSize << 1) + 1] = null;
        }
        return v;
    }

    public V setValueAt(int index, V value) {
        index = (index << 1) + 1;
        V v = this.mArray[index];
        this.mArray[index] = value;
        return v;
    }

    public int size() {
        return this.mSize;
    }

    public String toString() {
        if (isEmpty()) {
            return "{}";
        }
        StringBuilder stringBuilder = new StringBuilder(this.mSize * 28);
        stringBuilder.append('{');
        for (int i = 0; i < this.mSize; i++) {
            if (i > 0) {
                stringBuilder.append(", ");
            }
            zzlh keyAt = keyAt(i);
            if (keyAt == this) {
                stringBuilder.append("(this Map)");
            } else {
                stringBuilder.append(keyAt);
            }
            stringBuilder.append('=');
            keyAt = valueAt(i);
            if (keyAt == this) {
                stringBuilder.append("(this Map)");
            } else {
                stringBuilder.append(keyAt);
            }
        }
        stringBuilder.append('}');
        return stringBuilder.toString();
    }

    public V valueAt(int index) {
        return this.mArray[(index << 1) + 1];
    }
}
