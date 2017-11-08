package com.huawei.gallery.editor.cache;

import android.graphics.Bitmap;
import com.android.gallery3d.util.GalleryLog;

public class DrawCache {
    public static final Object DRAW_CACHE_LOCK = new Object();
    private Bitmap mAppliedMosaicBitmap;
    private int mCachedStrokes = 0;
    private int mMagicId = 0;
    private Bitmap mOverlayBitmap;

    public Bitmap getOverlayBitmap() {
        return this.mOverlayBitmap;
    }

    public void setOverlayBitmap(Bitmap overlayBitmap, BitmapCache bitmapCache, int magic) {
        synchronized (DRAW_CACHE_LOCK) {
            if (magic != this.mMagicId) {
                GalleryLog.w("DrawCache", "setOverlayBitmap magic=" + magic + ", current magic:" + this.mMagicId);
                return;
            }
            bitmapCache.cache(this.mOverlayBitmap);
            this.mOverlayBitmap = overlayBitmap;
        }
    }

    public int getCachedStrokesCount() {
        return this.mCachedStrokes;
    }

    public void setCachedStrokesCount(int count, int magic) {
        synchronized (DRAW_CACHE_LOCK) {
            if (magic != this.mMagicId) {
                GalleryLog.w("DrawCache", "setCachedStrokesCount magic=" + magic + ", current magic:" + this.mMagicId);
                return;
            }
            this.mCachedStrokes = count;
        }
    }

    public Bitmap getAppliedMosaicBitmap() {
        return this.mAppliedMosaicBitmap;
    }

    public void setAppliedMosaicBitmap(Bitmap appliedMosaicBitmap, int magic) {
        synchronized (DRAW_CACHE_LOCK) {
            if (magic != this.mMagicId) {
                GalleryLog.w("DrawCache", "setAppliedMosaicBitmap magic=" + magic + ", current magic:" + this.mMagicId);
                return;
            }
            this.mAppliedMosaicBitmap = appliedMosaicBitmap;
        }
    }

    public void reset() {
        synchronized (DRAW_CACHE_LOCK) {
            this.mAppliedMosaicBitmap = null;
            this.mOverlayBitmap = null;
            this.mCachedStrokes = 0;
            this.mMagicId++;
        }
    }

    public int getMagicId() {
        return this.mMagicId;
    }
}
