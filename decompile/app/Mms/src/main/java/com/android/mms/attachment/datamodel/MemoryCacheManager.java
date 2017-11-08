package com.android.mms.attachment.datamodel;

import com.android.mms.attachment.Factory;
import java.util.HashSet;

public class MemoryCacheManager {
    private final Object mMemoryCacheLock = new Object();
    private final HashSet<MemoryCache> mMemoryCaches = new HashSet();

    public interface MemoryCache {
        void reclaim();
    }

    public static MemoryCacheManager get() {
        return Factory.get().getMemoryCacheManager();
    }

    public void registerMemoryCache(MemoryCache cache) {
        synchronized (this.mMemoryCacheLock) {
            this.mMemoryCaches.add(cache);
        }
    }

    public void reclaimMemory() {
        synchronized (this.mMemoryCacheLock) {
            HashSet<MemoryCache> shallowCopy = (HashSet) this.mMemoryCaches.clone();
        }
        for (MemoryCache cache : shallowCopy) {
            cache.reclaim();
        }
    }
}
