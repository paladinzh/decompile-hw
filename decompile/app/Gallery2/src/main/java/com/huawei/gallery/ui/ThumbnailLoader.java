package com.huawei.gallery.ui;

import android.graphics.Bitmap;
import com.android.gallery3d.ui.BitmapLoader;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.ThreadPool.Job;

public abstract class ThumbnailLoader extends BitmapLoader {
    private final ThumbnailLoaderListener mListener;
    public final int mSlotIndex;

    protected abstract int getThreadMode();

    protected abstract Job<Bitmap> requestJob();

    public ThumbnailLoader(ThumbnailLoaderListener listener, int slotIndex) {
        this.mListener = listener;
        this.mSlotIndex = slotIndex;
    }

    protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> futureListener) {
        return this.mListener.submit(requestJob(), this, getThreadMode());
    }

    protected void onLoadComplete(Bitmap bitmap) {
        this.mListener.onComplete(this);
    }

    protected void onPreviewLoad(Bitmap bitmap) {
        this.mListener.onPreviewLoad(this, bitmap);
    }
}
