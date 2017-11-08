package com.huawei.gallery.editor.filters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.cache.DrawCache;

public class ImageFilterSplash extends ImageFilterMosaic {
    private native void nativeApplySplash(Bitmap bitmap, int i, int i2, int i3, int i4, int i5, Bitmap bitmap2);

    public Bitmap apply(Bitmap bitmap) {
        if (this.mParameters == null || !(this.mParameters instanceof FilterSplashRepresentation)) {
            return bitmap;
        }
        Rect bounds = new Rect(this.mParameters.getBounds());
        if (bounds.isEmpty()) {
            GalleryLog.w("ImageFilterSplash", "bounds is empty");
            return bitmap;
        }
        DrawCache drawCache = getEnvironment().getDrawCache();
        if (drawCache == null) {
            GalleryLog.w("ImageFilterSplash", "drawCache is null");
            return bitmap;
        }
        initCacheParameters(drawCache);
        Bitmap grayBitmap = computeAppliedMosaicBitmap(bitmap);
        if (grayBitmap != null) {
            this.mTempMatrix.reset();
            float scale = ((float) bitmap.getWidth()) / ((float) bounds.width());
            this.mTempMatrix.setTranslate(((float) bitmap.getWidth()) / 2.0f, ((float) bitmap.getHeight()) / 2.0f);
            this.mTempMatrix.setScale(scale, scale);
            if (getEnvironment().getQuality() == 2) {
                drawStrokeDataToOverlay(grayBitmap, 0, null, new Matrix());
                int color = ((FilterSplashRepresentation) this.mParameters).getColor();
                synchronized (ImageFilterFx.FILTER_LOCK) {
                    nativeApplySplash(bitmap, bitmap.getWidth(), bitmap.getHeight(), Color.red(color), Color.green(color), Color.blue(color), grayBitmap);
                }
            } else {
                drawAllStrokeData(bitmap, grayBitmap, bounds, this.mTempMatrix);
            }
        }
        if (!this.mParameters.useDrawCache() || getEnvironment().getQuality() == 2) {
            this.mDrawCache.reset();
        }
        resetCacheParameters();
        return bitmap;
    }

    protected void nativeApply(Bitmap target) {
        synchronized (ImageFilterFx.FILTER_LOCK) {
            nativeApplySplash(target, target.getWidth(), target.getHeight(), 0, 0, 0, null);
        }
    }

    private void drawAllStrokeData(Bitmap bitmap, Bitmap appliedMosaicBitmap, Rect bounds, Matrix scaleMatrix) {
        if (this.mParameters instanceof FilterSplashRepresentation) {
            Canvas canvas = new Canvas(bitmap);
            Bitmap overlayBitmap = this.mCacheOverlayBitmap;
            int cacheStokeCount = this.mCacheStokeCount;
            if (overlayBitmap == null || overlayBitmap.getWidth() != bounds.width() || overlayBitmap.getHeight() != bounds.height() || this.mParameters.getAppliedMosaic().size() < cacheStokeCount || ((FilterSplashRepresentation) this.mParameters).needChange()) {
                overlayBitmap = getEnvironment().getBitmap(bounds.width(), bounds.height());
                if (overlayBitmap != null) {
                    Canvas c = new Canvas(overlayBitmap);
                    Matrix matrix = new Matrix();
                    scaleMatrix.invert(matrix);
                    c.drawBitmap(bitmap, matrix, null);
                    int color = ((FilterSplashRepresentation) this.mParameters).getColor();
                    synchronized (ImageFilterFx.FILTER_LOCK) {
                        nativeApplySplash(overlayBitmap, overlayBitmap.getWidth(), overlayBitmap.getHeight(), Color.red(color), Color.green(color), Color.blue(color), null);
                    }
                    this.mDrawCache.setOverlayBitmap(overlayBitmap, getEnvironment().getBitmapCache(), this.mCacheMagicId);
                    this.mDrawCache.setCachedStrokesCount(0, this.mCacheMagicId);
                    cacheStokeCount = 0;
                } else {
                    return;
                }
            }
            canvas.drawBitmap(appliedMosaicBitmap, new Matrix(), null);
            drawOverlayBitmap(canvas, overlayBitmap, appliedMosaicBitmap, cacheStokeCount, scaleMatrix);
        }
    }
}
