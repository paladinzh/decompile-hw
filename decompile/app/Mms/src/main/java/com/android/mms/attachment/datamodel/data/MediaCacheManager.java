package com.android.mms.attachment.datamodel.data;

import android.util.SparseArray;
import com.android.mms.attachment.datamodel.MemoryCacheManager;
import com.android.mms.attachment.datamodel.MemoryCacheManager.MemoryCache;
import com.android.mms.attachment.datamodel.media.MediaCache;
import com.android.mms.attachment.datamodel.media.PoolableImageCache;
import com.android.mms.attachment.datamodel.media.PoolableImageCache.ReusableImageResourcePool;

public abstract class MediaCacheManager implements MemoryCache {
    protected final SparseArray<MediaCache<?>> mCaches = new SparseArray();

    protected abstract MediaCache<?> createMediaCacheById(int i);

    public MediaCacheManager() {
        MemoryCacheManager.get().registerMemoryCache(this);
    }

    public void reclaim() {
        int count = this.mCaches.size();
        for (int i = 0; i < count; i++) {
            ((MediaCache) this.mCaches.valueAt(i)).destroy();
        }
        this.mCaches.clear();
    }

    public synchronized MediaCache<?> getOrCreateMediaCacheById(int id) {
        MediaCache<?> cache;
        cache = (MediaCache) this.mCaches.get(id);
        if (cache == null) {
            cache = createMediaCacheById(id);
            if (cache != null) {
                this.mCaches.put(id, cache);
            }
        }
        return cache;
    }

    public ReusableImageResourcePool getOrCreateBitmapPoolForCache(int cacheId) {
        MediaCache<?> cache = getOrCreateMediaCacheById(cacheId);
        if (cache == null || !(cache instanceof PoolableImageCache)) {
            return null;
        }
        return ((PoolableImageCache) cache).asReusableBitmapPool();
    }
}
