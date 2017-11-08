package com.huawei.gallery.app;

import com.android.gallery3d.app.LoadingListener;
import com.android.gallery3d.data.MediaItem;

public abstract class AlbumDataLoader {
    protected volatile boolean mReloadLock;

    public abstract MediaItem get(int i);

    public abstract void pause();

    public abstract int preSize();

    public abstract void resume();

    public abstract void setLoadingListener(LoadingListener loadingListener);

    public abstract int size();

    public void freeze() {
        this.mReloadLock = true;
    }

    public void unfreeze() {
        this.mReloadLock = false;
    }

    public boolean isFreezed() {
        return this.mReloadLock;
    }
}
