package com.android.mms.attachment.datamodel.media;

import android.os.SystemClock;
import com.huawei.cspcommon.MLog;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public abstract class RefCountedMediaResource {
    private final String mKey;
    private long mLastRefAddTimestamp;
    private final ReentrantLock mLock = new ReentrantLock();
    private int mRef = 0;
    private final ArrayList<String> mRefHistory = new ArrayList();

    protected abstract void close();

    public abstract int getMediaSize();

    public RefCountedMediaResource(String key) {
        this.mKey = key;
    }

    public String getKey() {
        return this.mKey;
    }

    public void addRef() {
        acquireLock();
        try {
            this.mRef++;
            this.mLastRefAddTimestamp = SystemClock.elapsedRealtime();
        } finally {
            releaseLock();
        }
    }

    public void release() {
        acquireLock();
        try {
            this.mRef--;
            if (this.mRef == 0) {
                close();
            }
            releaseLock();
        } catch (Throwable th) {
            releaseLock();
        }
    }

    public int getRefCount() {
        acquireLock();
        try {
            int i = this.mRef;
            return i;
        } finally {
            releaseLock();
        }
    }

    public long getLastRefAddTimestamp() {
        acquireLock();
        try {
            long j = this.mLastRefAddTimestamp;
            return j;
        } finally {
            releaseLock();
        }
    }

    public void assertSingularRefCount() {
        acquireLock();
        try {
            MLog.d("bugle_media_ref_history", "assertSingularRefCount is called");
        } finally {
            releaseLock();
        }
    }

    void acquireLock() {
        this.mLock.lock();
    }

    void releaseLock() {
        this.mLock.unlock();
    }

    void assertLockHeldByCurrentThread() {
    }

    boolean isEncoded() {
        return false;
    }

    boolean isCacheable() {
        return true;
    }

    MediaRequest<? extends RefCountedMediaResource> getMediaDecodingRequest(MediaRequest<? extends RefCountedMediaResource> mediaRequest) {
        return null;
    }

    MediaRequest<? extends RefCountedMediaResource> getMediaEncodingRequest(MediaRequest<? extends RefCountedMediaResource> mediaRequest) {
        return null;
    }
}
