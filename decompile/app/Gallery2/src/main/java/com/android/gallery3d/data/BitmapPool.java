package com.android.gallery3d.data;

import android.graphics.Bitmap;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import java.util.ArrayList;

public class BitmapPool {
    private final int mHeight;
    private final boolean mOneSize;
    private final ArrayList<Bitmap> mPool;
    private final int mPoolLimit;
    private final int mWidth;

    public BitmapPool(int width, int height, int poolLimit) {
        this.mWidth = width;
        this.mHeight = height;
        this.mPoolLimit = poolLimit;
        this.mPool = new ArrayList(poolLimit);
        this.mOneSize = true;
    }

    public BitmapPool(int poolLimit) {
        this.mWidth = -1;
        this.mHeight = -1;
        this.mPoolLimit = poolLimit;
        this.mPool = new ArrayList(poolLimit);
        this.mOneSize = false;
    }

    public synchronized Bitmap getBitmap() {
        int size;
        Utils.assertTrue(this.mOneSize);
        size = this.mPool.size();
        return size > 0 ? (Bitmap) this.mPool.remove(size - 1) : null;
    }

    public synchronized Bitmap getBitmap(int width, int height) {
        boolean z = false;
        synchronized (this) {
            if (!this.mOneSize) {
                z = true;
            }
            Utils.assertTrue(z);
            for (int i = this.mPool.size() - 1; i >= 0; i--) {
                Bitmap b = (Bitmap) this.mPool.get(i);
                if (b.getWidth() == width && b.getHeight() == height) {
                    Bitmap bitmap = (Bitmap) this.mPool.remove(i);
                    return bitmap;
                }
            }
            return null;
        }
    }

    public void recycle(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            if (!this.mOneSize || (bitmap.getWidth() == this.mWidth && bitmap.getHeight() == this.mHeight)) {
                synchronized (this) {
                    if (this.mPool.size() >= this.mPoolLimit) {
                        this.mPool.remove(0);
                    }
                    this.mPool.add(bitmap);
                }
                return;
            }
            GalleryLog.w("BitmapPool", String.format("bitmapPool(%s) is called [mWidth=%d, mHeight=%d]", new Object[]{this, Integer.valueOf(this.mWidth), Integer.valueOf(this.mHeight)}));
            GalleryLog.w("BitmapPool", String.format("bitmap(%s) will be recycled[mWidth=%d, mHeight=%d]", new Object[]{bitmap, Integer.valueOf(bitmap.getWidth()), Integer.valueOf(bitmap.getHeight())}));
            bitmap.recycle();
        }
    }

    public synchronized void clear() {
        this.mPool.clear();
    }

    public boolean isOneSize() {
        return this.mOneSize;
    }
}
