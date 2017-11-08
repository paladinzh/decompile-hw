package com.huawei.gallery.editor.pipeline;

import android.graphics.Bitmap;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.cache.BitmapCache;

public class SharedBuffer {
    private volatile Buffer mBuffer = null;
    private volatile boolean mNeedUpdateTexture = false;

    public void setBuffer(Buffer buffer, BitmapCache cache) {
        if (this.mBuffer != null) {
            this.mBuffer.remove(cache);
        }
        this.mBuffer = buffer;
    }

    public void setBuffer(Bitmap producer) {
        if (this.mBuffer == null) {
            GalleryLog.e("SharedBuffer", "updatePreview error, mBuffer == null");
        } else if (this.mBuffer.isSameSize(producer)) {
            this.mBuffer.useBitmap(producer);
        } else {
            GalleryLog.e("SharedBuffer", "updatePreview error, mBufferSize not compatible.");
        }
    }

    public void initBuffer(Bitmap producer, BitmapCache cache) {
        if (this.mBuffer != null) {
            this.mBuffer.remove(cache);
        }
        this.mBuffer = new Buffer(producer, cache);
    }

    public synchronized Buffer getBuffer() {
        return this.mBuffer;
    }

    public synchronized void setUpdateTexture(boolean isNeed) {
        this.mNeedUpdateTexture = isNeed;
    }

    public synchronized boolean checkSwapNeeded() {
        return this.mNeedUpdateTexture;
    }

    public synchronized void clear() {
        this.mBuffer = null;
    }
}
