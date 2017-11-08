package com.android.contacts.hap.rcs.calllog;

import android.graphics.Bitmap;
import android.util.LruCache;

/* compiled from: RcsCallLogDetailHistoryHelper */
class RcsLruCache extends LruCache<String, Bitmap> {
    public RcsLruCache(int maxSize) {
        super(maxSize);
    }

    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }
}
