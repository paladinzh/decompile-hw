package com.android.gallery3d.data;

import java.util.concurrent.atomic.AtomicBoolean;

public class SnailAlbum extends SingleItemAlbum {
    private AtomicBoolean mDirty = new AtomicBoolean(false);

    public SnailAlbum(Path path, SnailItem item) {
        super(path, item);
    }

    public long reload() {
        if (this.mDirty.compareAndSet(true, false)) {
            ((SnailItem) getItem()).updateVersion();
            this.mDataVersion = MediaObject.nextVersionNumber();
        }
        return this.mDataVersion;
    }
}
