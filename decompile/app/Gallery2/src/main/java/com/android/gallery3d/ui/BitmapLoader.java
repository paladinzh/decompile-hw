package com.android.gallery3d.ui;

import android.graphics.Bitmap;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.huawei.gallery.util.GalleryPool;

public abstract class BitmapLoader implements FutureListener<Bitmap> {
    private Bitmap mBitmap;
    private int mState = 0;
    private Future<Bitmap> mTask;

    protected abstract void onLoadComplete(Bitmap bitmap);

    protected abstract void onPreviewLoad(Bitmap bitmap);

    protected abstract void recycleBitmap(Bitmap bitmap);

    protected abstract Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> futureListener);

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onFutureDone(Future<Bitmap> future) {
        synchronized (this) {
            this.mTask = null;
            this.mBitmap = (Bitmap) future.get();
            if (this.mState == 4) {
                if (this.mBitmap != null) {
                    recycleBitmap(this.mBitmap);
                    this.mBitmap = null;
                }
            } else if (!future.isCancelled() || this.mBitmap != null) {
                this.mState = this.mBitmap == null ? 3 : 2;
                onLoadComplete(this.mBitmap);
            } else if (this.mState == 1) {
                this.mTask = submitBitmapTask(this);
            }
        }
    }

    public synchronized void startLoad() {
        startLoad(false);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void startLoad(boolean force) {
        if (this.mState == 0) {
            if (!force) {
                Bitmap bmp = GalleryPool.get(getPath(), getTimeModified());
                if (bmp != null) {
                    this.mState = 2;
                    this.mBitmap = bmp;
                    onPreviewLoad(this.mBitmap);
                    return;
                }
            }
            this.mState = 1;
            if (this.mTask == null) {
                this.mTask = submitBitmapTask(this);
            }
        }
    }

    public synchronized void cancelLoad() {
        if (this.mState == 1) {
            this.mState = 0;
            if (this.mTask != null) {
                this.mTask.cancel();
            }
        }
    }

    public synchronized void recycle() {
        this.mState = 4;
        if (this.mBitmap != null) {
            recycleBitmap(this.mBitmap);
            this.mBitmap = null;
        }
        if (this.mTask != null) {
            this.mTask.cancel();
        }
    }

    public synchronized boolean isRequestInProgress() {
        boolean z = true;
        synchronized (this) {
            if (this.mState != 1) {
                z = false;
            }
        }
        return z;
    }

    public synchronized boolean isStateError() {
        return this.mState == 3;
    }

    public synchronized Bitmap getBitmap() {
        return this.mBitmap;
    }

    protected Path getPath() {
        return null;
    }

    protected long getTimeModified() {
        return 0;
    }
}
