package com.android.mms.attachment.datamodel.media;

import android.util.LruCache;
import com.google.android.gms.location.places.Place;
import com.huawei.cspcommon.MLog;

public class MediaCache<T extends RefCountedMediaResource> extends LruCache<String, T> {
    private final int mId;
    private final String mName;

    public MediaCache(int maxSize, int id, String name) {
        super(maxSize);
        this.mId = id;
        this.mName = name;
    }

    public void destroy() {
        evictAll();
    }

    public String getName() {
        return this.mName;
    }

    public synchronized T fetchResourceFromCache(String key) {
        RefCountedMediaResource ret;
        ret = (RefCountedMediaResource) get(key);
        if (ret != null) {
            if (MLog.isLoggable("Mms_app", 2)) {
                MLog.v("MediaCache", "cache hit in mediaCache @ " + getName() + ", total cache hit = " + hitCount() + ", total cache miss = " + missCount());
            }
            ret.addRef();
        } else if (MLog.isLoggable("Mms_app", 2)) {
            MLog.v("MediaCache", "cache miss in mediaCache @ " + getName() + ", total cache hit = " + hitCount() + ", total cache miss = " + missCount());
        }
        return ret;
    }

    public synchronized T addResourceToCache(String key, T mediaResource) {
        mediaResource.addRef();
        return (RefCountedMediaResource) put(key, mediaResource);
    }

    protected synchronized void entryRemoved(boolean evicted, String key, T oldValue, T t) {
        oldValue.release();
    }

    protected int sizeOf(String key, T value) {
        int mediaSizeInKilobytes = value.getMediaSize() / Place.TYPE_SUBLOCALITY_LEVEL_2;
        return mediaSizeInKilobytes == 0 ? 1 : mediaSizeInKilobytes;
    }
}
