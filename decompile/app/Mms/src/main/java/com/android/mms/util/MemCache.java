package com.android.mms.util;

import android.graphics.drawable.Drawable;
import android.util.LruCache;

public class MemCache extends LruCache<Long, Drawable> {
    public MemCache(int maxSize) {
        super(maxSize);
    }

    protected int sizeOf(Long threadId, Drawable drawable) {
        return drawable.getIntrinsicWidth() * drawable.getIntrinsicHeight();
    }
}
