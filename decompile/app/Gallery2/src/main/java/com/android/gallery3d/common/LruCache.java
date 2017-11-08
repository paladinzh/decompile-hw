package com.android.gallery3d.common;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class LruCache<K, V> {
    private final HashMap<K, V> mLruMap;
    private ReferenceQueue<V> mQueue = new ReferenceQueue();
    private final HashMap<K, Entry<K, V>> mWeakMap = new HashMap();

    private static class Entry<K, V> extends WeakReference<V> {
        K mKey;

        public Entry(K key, V value, ReferenceQueue<V> queue) {
            super(value, queue);
            this.mKey = key;
        }
    }

    public LruCache(int capacity) {
        final int i = capacity;
        this.mLruMap = new LinkedHashMap<K, V>(16, 0.75f, true) {
            protected boolean removeEldestEntry(java.util.Map.Entry<K, V> entry) {
                return size() > i;
            }
        };
    }

    private void cleanUpWeakMap() {
        Entry<K, V> entry = (Entry) this.mQueue.poll();
        while (entry != null) {
            this.mWeakMap.remove(entry.mKey);
            entry = (Entry) this.mQueue.poll();
        }
    }

    public synchronized boolean containsKey(K key) {
        cleanUpWeakMap();
        return this.mWeakMap.containsKey(key);
    }

    public synchronized V put(K key, V value) {
        V v = null;
        synchronized (this) {
            cleanUpWeakMap();
            this.mLruMap.put(key, value);
            Entry<K, V> entry = (Entry) this.mWeakMap.put(key, new Entry(key, value, this.mQueue));
            if (entry != null) {
                v = entry.get();
            }
        }
        return v;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized V get(K key) {
        V v = null;
        synchronized (this) {
            cleanUpWeakMap();
            V value = this.mLruMap.get(key);
            if (value != null) {
                return value;
            }
            Entry<K, V> entry = (Entry) this.mWeakMap.get(key);
            if (entry != null) {
                v = entry.get();
            }
        }
    }
}
