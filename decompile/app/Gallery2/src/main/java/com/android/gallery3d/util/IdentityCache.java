package com.android.gallery3d.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class IdentityCache<K, V> {
    private ReferenceQueue<V> mQueue = new ReferenceQueue();
    private final HashMap<K, Entry<K, V>> mWeakMap = new HashMap();

    private static class Entry<K, V> extends WeakReference<V> {
        K mKey;

        public Entry(K key, V value, ReferenceQueue<V> queue) {
            super(value, queue);
            this.mKey = key;
        }
    }

    private void cleanUpWeakMap() {
        Entry<K, V> entry = (Entry) this.mQueue.poll();
        while (entry != null) {
            this.mWeakMap.remove(entry.mKey);
            entry = (Entry) this.mQueue.poll();
        }
    }

    public synchronized V put(K key, V value) {
        V v = null;
        synchronized (this) {
            cleanUpWeakMap();
            Entry<K, V> entry = (Entry) this.mWeakMap.put(key, new Entry(key, value, this.mQueue));
            if (entry != null) {
                v = entry.get();
            }
        }
        return v;
    }

    public synchronized V get(K key) {
        V v = null;
        synchronized (this) {
            cleanUpWeakMap();
            Entry<K, V> entry = (Entry) this.mWeakMap.get(key);
            if (entry != null) {
                v = entry.get();
            }
        }
        return v;
    }
}
