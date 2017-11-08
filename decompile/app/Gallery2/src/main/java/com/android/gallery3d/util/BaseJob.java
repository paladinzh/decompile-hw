package com.android.gallery3d.util;

import com.android.gallery3d.util.ThreadPool.Job;

public abstract class BaseJob<T> implements Job<T> {
    public boolean isHeavyJob() {
        return false;
    }

    public String clazz() {
        return getClass().getName();
    }

    public boolean needDecodeVideoFromOrigin() {
        return false;
    }
}
