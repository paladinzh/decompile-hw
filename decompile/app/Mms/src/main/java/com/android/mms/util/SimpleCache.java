package com.android.mms.util;

import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

public class SimpleCache<K, V> {
    private final HardReferenceMap mHardReferences;
    private final SoftReferenceMap mSoftReferences;

    private class HardReferenceMap extends LinkedHashMap<K, V> {
        private final int mMaxCapacity;

        public HardReferenceMap(int initialCapacity, int maxCapacity, float loadFactor) {
            super(initialCapacity, loadFactor, true);
            this.mMaxCapacity = maxCapacity;
        }

        protected boolean removeEldestEntry(Entry<K, V> entry) {
            return size() > this.mMaxCapacity;
        }
    }

    private class SoftReferenceMap extends LinkedHashMap<K, SoftReference<V>> {
        private final int mMaxCapacity;

        public SoftReferenceMap(int initialCapacity, int maxCapacity, float loadFactor) {
            super(initialCapacity, loadFactor, true);
            this.mMaxCapacity = maxCapacity;
        }

        protected boolean removeEldestEntry(Entry<K, SoftReference<V>> entry) {
            return size() > this.mMaxCapacity;
        }
    }

    private static <V> V unwrap(SoftReference<V> ref) {
        return ref != null ? ref.get() : null;
    }

    public SimpleCache(int initialCapacity, int maxCapacity, float loadFactor, boolean useHardReferences) {
        if (useHardReferences) {
            this.mSoftReferences = null;
            this.mHardReferences = new HardReferenceMap(initialCapacity, maxCapacity, loadFactor);
            return;
        }
        this.mSoftReferences = new SoftReferenceMap(initialCapacity, maxCapacity, loadFactor);
        this.mHardReferences = null;
    }

    public V get(Object key) {
        if (this.mSoftReferences != null) {
            return unwrap((SoftReference) this.mSoftReferences.get(key));
        }
        return this.mHardReferences.get(key);
    }

    public V put(K key, V value) {
        if (this.mSoftReferences != null) {
            return unwrap((SoftReference) this.mSoftReferences.put(key, new SoftReference(value)));
        }
        return this.mHardReferences.put(key, value);
    }

    public void clear() {
        if (this.mSoftReferences != null) {
            this.mSoftReferences.clear();
        } else {
            this.mHardReferences.clear();
        }
    }

    public V remove(K key) {
        if (this.mSoftReferences != null) {
            return unwrap((SoftReference) this.mSoftReferences.remove(key));
        }
        return this.mHardReferences.remove(key);
    }

    public Set<K> keySet() {
        Set<K> keySet = new HashSet();
        if (this.mSoftReferences != null) {
            keySet.addAll(this.mSoftReferences.keySet());
        }
        if (this.mHardReferences != null) {
            keySet.addAll(this.mHardReferences.keySet());
        }
        return keySet;
    }
}
