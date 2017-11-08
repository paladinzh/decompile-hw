package com.huawei.gallery.editor.filters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.editor.app.BaseMosaicState;
import com.huawei.gallery.editor.app.EditorState;
import com.huawei.gallery.editor.cache.DrawCache;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation.StrokeData;
import com.huawei.gallery.editor.filters.draw.DrawStyle;
import java.util.Stack;

public class ImageFilterMosaic extends ImageFilter {
    private static final float MOSAIC_SIZE = GalleryUtils.dpToPixel(6.0f);
    protected Bitmap mCacheAppliedMosaicBitmap;
    protected int mCacheMagicId;
    protected Bitmap mCacheOverlayBitmap;
    protected int mCacheStokeCount;
    protected DrawCache mDrawCache;
    protected DrawStyle[] mDrawingsTypes;
    protected FilterMosaicRepresentation mParameters;
    protected BaseMosaicState mState;
    protected Matrix mTempMatrix = new Matrix();

    private native void nativeApplyMosaic(Bitmap bitmap, int i, int i2, float f);

    public void useRepresentation(FilterRepresentation representation) {
        if (representation instanceof FilterMosaicRepresentation) {
            this.mParameters = (FilterMosaicRepresentation) representation;
        }
    }

    public void useEditorState(EditorState state) {
        if (state instanceof BaseMosaicState) {
            this.mState = (BaseMosaicState) state;
            this.mDrawingsTypes = this.mState.getSimpleEditorManager().getDrawingsTypes();
        }
    }

    protected void initCacheParameters(DrawCache drawCache) {
        this.mDrawCache = drawCache;
        synchronized (DrawCache.DRAW_CACHE_LOCK) {
            this.mCacheMagicId = drawCache.getMagicId();
            this.mCacheOverlayBitmap = drawCache.getOverlayBitmap();
            this.mCacheStokeCount = drawCache.getCachedStrokesCount();
            this.mCacheAppliedMosaicBitmap = drawCache.getAppliedMosaicBitmap();
        }
    }

    protected void resetCacheParameters() {
        this.mDrawCache = null;
        this.mCacheMagicId = -1;
        this.mCacheOverlayBitmap = null;
        this.mCacheStokeCount = -1;
        this.mCacheAppliedMosaicBitmap = null;
    }

    public Bitmap apply(Bitmap bitmap) {
        if (this.mParameters == null) {
            return bitmap;
        }
        Rect bounds = new Rect(this.mParameters.getBounds());
        if (bounds.isEmpty()) {
            GalleryLog.w("ImageFilterMosaic", "bounds is empty");
            return bitmap;
        }
        DrawCache drawCache = getEnvironment().getDrawCache();
        if (drawCache == null) {
            GalleryLog.w("ImageFilterMosaic", "drawCache is null");
            return bitmap;
        }
        initCacheParameters(drawCache);
        Bitmap appliedMosaicBitmap = computeAppliedMosaicBitmap(bitmap);
        this.mTempMatrix.reset();
        float scale = ((float) bitmap.getWidth()) / ((float) bounds.width());
        this.mTempMatrix.setTranslate(((float) bitmap.getWidth()) / 2.0f, ((float) bitmap.getHeight()) / 2.0f);
        this.mTempMatrix.setScale(scale, scale);
        drawAllStrokeData(new Canvas(bitmap), appliedMosaicBitmap, bounds, this.mTempMatrix);
        if (!this.mParameters.useDrawCache() || getEnvironment().getQuality() == 2) {
            if (appliedMosaicBitmap != null) {
                appliedMosaicBitmap.recycle();
            }
            this.mDrawCache.reset();
        }
        resetCacheParameters();
        return bitmap;
    }

    protected Bitmap computeAppliedMosaicBitmap(Bitmap src) {
        Bitmap appliedMosaicBitmap;
        if (getEnvironment().getQuality() == 2) {
            appliedMosaicBitmap = Bitmap.createScaledBitmap(src, this.mParameters.getBounds().width(), this.mParameters.getBounds().height(), true);
            if (appliedMosaicBitmap == null) {
                return null;
            }
            nativeApply(appliedMosaicBitmap);
            return appliedMosaicBitmap;
        }
        appliedMosaicBitmap = this.mCacheAppliedMosaicBitmap;
        if (appliedMosaicBitmap == null) {
            appliedMosaicBitmap = getEnvironment().getBitmapCopy(src);
            if (appliedMosaicBitmap == null) {
                return null;
            }
            nativeApply(appliedMosaicBitmap);
        }
        this.mDrawCache.setAppliedMosaicBitmap(appliedMosaicBitmap, this.mCacheMagicId);
        return appliedMosaicBitmap;
    }

    protected void nativeApply(Bitmap target) {
        nativeApplyMosaic(target, target.getWidth(), target.getHeight(), MOSAIC_SIZE);
    }

    private void drawAllStrokeData(Canvas canvas, Bitmap appliedMosaicBitmap, Rect bounds, Matrix scaleMatrix) {
        if (this.mParameters.getAppliedMosaic().isEmpty() && this.mParameters.getCurrentStrokeData() == null) {
            this.mDrawCache.setOverlayBitmap(null, getEnvironment().getBitmapCache(), this.mCacheMagicId);
            this.mDrawCache.setCachedStrokesCount(0, this.mCacheMagicId);
            return;
        }
        Bitmap overlayBitmap = this.mCacheOverlayBitmap;
        int cacheStokeCount = this.mCacheStokeCount;
        if (overlayBitmap == null || overlayBitmap.getWidth() != bounds.width() || overlayBitmap.getHeight() != bounds.height() || this.mParameters.getAppliedMosaic().size() < cacheStokeCount) {
            overlayBitmap = getEnvironment().getBitmap(bounds.width(), bounds.height());
            if (overlayBitmap != null) {
                new Canvas(overlayBitmap).drawColor(0, Mode.CLEAR);
                this.mDrawCache.setOverlayBitmap(overlayBitmap, getEnvironment().getBitmapCache(), this.mCacheMagicId);
                this.mDrawCache.setCachedStrokesCount(0, this.mCacheMagicId);
                cacheStokeCount = 0;
            } else {
                return;
            }
        }
        drawOverlayBitmap(canvas, overlayBitmap, appliedMosaicBitmap, cacheStokeCount, scaleMatrix);
    }

    protected void drawOverlayBitmap(Canvas canvas, Bitmap overlayBitmap, Bitmap appliedMosaicBitmap, int cacheStokeCount, Matrix scaleMatrix) {
        Matrix matrix = new Matrix();
        if (cacheStokeCount < this.mParameters.getAppliedMosaic().size()) {
            drawStrokeDataToOverlay(overlayBitmap, cacheStokeCount, appliedMosaicBitmap, matrix);
        }
        canvas.drawBitmap(overlayBitmap, scaleMatrix, null);
    }

    protected void drawStrokeDataToOverlay(Bitmap overlayBitmap, int cacheStrokesCount, Bitmap appliedMosaicBitmap, Matrix originalRotateToScreen) {
        Canvas drawCache = new Canvas(overlayBitmap);
        Stack<StrokeData> v = this.mParameters.getAppliedMosaic();
        int n = v.size();
        for (int i = Math.max(cacheStrokesCount, 0); i < n; i++) {
            StrokeData sd = (StrokeData) v.get(i);
            if (this.mDrawingsTypes != null && this.mDrawingsTypes.length > sd.type) {
                this.mDrawingsTypes[sd.type].paint(appliedMosaicBitmap, drawCache, originalRotateToScreen, (StrokeData) v.get(i), getEnvironment().getCustDrawBrushCache());
            }
        }
        this.mDrawCache.setCachedStrokesCount(n, this.mCacheMagicId);
    }
}
