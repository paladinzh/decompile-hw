package com.android.systemui.recents.model;

import android.util.Log;
import android.util.LruCache;
import android.util.SparseArray;
import com.android.systemui.recents.model.Task.TaskKey;
import java.io.PrintWriter;

public class TaskKeyLruCache<V> {
    private final LruCache<Integer, V> mCache;
    private final EvictionCallback mEvictionCallback;
    private final SparseArray<TaskKey> mKeys;

    public interface EvictionCallback {
        void onEntryEvicted(TaskKey taskKey);
    }

    public TaskKeyLruCache(int cacheSize) {
        this(cacheSize, null);
    }

    public TaskKeyLruCache(int cacheSize, EvictionCallback evictionCallback) {
        this.mKeys = new SparseArray();
        this.mEvictionCallback = evictionCallback;
        this.mCache = new LruCache<Integer, V>(cacheSize) {
            protected void entryRemoved(boolean evicted, Integer taskId, V v, V v2) {
                if (TaskKeyLruCache.this.mEvictionCallback != null) {
                    TaskKeyLruCache.this.mEvictionCallback.onEntryEvicted(TaskKeyLruCache.this.getTaskKeyById(taskId.intValue()));
                }
                synchronized (TaskKeyLruCache.this.mKeys) {
                    TaskKeyLruCache.this.mKeys.remove(taskId.intValue());
                }
            }
        };
    }

    final V get(TaskKey key) {
        return this.mCache.get(Integer.valueOf(key.id));
    }

    final V getAndInvalidateIfModified(TaskKey key) {
        TaskKey lastKey = getTaskKeyById(key.id);
        if (lastKey == null || (lastKey.stackId == key.stackId && lastKey.lastActiveTime == key.lastActiveTime)) {
            return this.mCache.get(Integer.valueOf(key.id));
        }
        remove(key);
        return null;
    }

    final void put(TaskKey key, V value) {
        if (key == null || value == null) {
            Log.e("TaskKeyLruCache", "Unexpected null key or value: " + key + ", " + value);
            return;
        }
        synchronized (this.mKeys) {
            this.mKeys.put(key.id, key);
        }
        this.mCache.put(Integer.valueOf(key.id), value);
    }

    final void remove(TaskKey key) {
        this.mCache.remove(Integer.valueOf(key.id));
        synchronized (this.mKeys) {
            this.mKeys.remove(key.id);
        }
    }

    final void evictAll() {
        this.mCache.evictAll();
        synchronized (this.mKeys) {
            this.mKeys.clear();
        }
    }

    final void trimToSize(int cacheSize) {
        this.mCache.trimToSize(cacheSize);
    }

    public void dump(String prefix, PrintWriter writer) {
        String innerPrefix = prefix + "  ";
        writer.print(prefix);
        writer.print("TaskKeyLruCache");
        writer.print(" numEntries=");
        writer.print(this.mKeys.size());
        writer.println();
        int keyCount = this.mKeys.size();
        for (int i = 0; i < keyCount; i++) {
            writer.print(innerPrefix);
            writer.println(this.mKeys.get(this.mKeys.keyAt(i)));
        }
    }

    final TaskKey getTaskKeyById(int id) {
        TaskKey taskKey;
        synchronized (this.mKeys) {
            taskKey = (TaskKey) this.mKeys.get(id);
        }
        return taskKey;
    }
}
