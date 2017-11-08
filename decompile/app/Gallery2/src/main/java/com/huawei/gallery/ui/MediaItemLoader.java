package com.huawei.gallery.ui;

import android.graphics.Bitmap;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.ThreadPool.Job;
import com.huawei.gallery.util.GalleryPool;

public class MediaItemLoader extends ThumbnailLoader {
    private final MediaItem mItem;

    public MediaItemLoader(ThumbnailLoaderListener listener, int slotIndex, MediaItem item) {
        super(listener, slotIndex);
        this.mItem = item;
    }

    protected void recycleBitmap(Bitmap bitmap) {
        GalleryPool.recycle(getPath(), this.mItem.getDateModifiedInSec(), bitmap, this.mItem.isDrm());
    }

    protected Job<Bitmap> requestJob() {
        return this.mItem.requestImage(2);
    }

    protected int getThreadMode() {
        return 1;
    }

    protected Path getPath() {
        return this.mItem.getPath();
    }

    protected long getTimeModified() {
        return this.mItem.getDateModifiedInSec();
    }
}
