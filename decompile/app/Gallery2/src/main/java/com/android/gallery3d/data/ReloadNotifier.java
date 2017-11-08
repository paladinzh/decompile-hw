package com.android.gallery3d.data;

import android.net.Uri;
import com.android.gallery3d.app.GalleryApp;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ReloadNotifier implements IReloadNotifier {
    private AtomicBoolean mContentDirty = new AtomicBoolean(true);
    private MediaSet mMediaSet;
    private int mReloadType = -1;

    public ReloadNotifier(MediaSet set, Uri uri, GalleryApp application) {
        this.mMediaSet = set;
        application.getDataManager().registerReloadNotifier(uri, this);
    }

    public synchronized boolean isDirty() {
        return this.mContentDirty.compareAndSet(true, false);
    }

    public synchronized int getReloadType() {
        if (this.mReloadType == -1) {
            return 6;
        }
        return this.mReloadType;
    }

    public synchronized void onChange(int newType) {
        if (this.mReloadType != newType) {
            this.mContentDirty.compareAndSet(false, true);
            this.mReloadType = newType;
            this.mMediaSet.notifyContentChanged();
        }
    }
}
