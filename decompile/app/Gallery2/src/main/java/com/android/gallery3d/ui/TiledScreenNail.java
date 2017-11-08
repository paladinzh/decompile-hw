package com.android.gallery3d.ui;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.SystemProperties;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.BitmapPool;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.TraceController;
import com.huawei.watermark.manager.parse.WMElement;

public class TiledScreenNail extends AbstractGifScreenNail implements ScreenNail {
    private static boolean mDrawPlaceholder = true;
    private static StringTexture mLoading;
    private static int sMaxSide = 640;
    private static int sPlaceholderColor = -14540254;
    private static boolean sThumbnailLoading = SystemProperties.getBoolean("ro.gallery.thumbnailLoading", false);
    private float canvasRotation;
    private int centerX;
    private int centerY;
    private long mAnimationStartTime;
    private Bitmap mBitmap;
    private boolean mBitmapFromCache;
    private boolean mDrawLoadingTip;
    private boolean mFileSaveComplete;
    private int mHeight;
    private boolean mNeedFreeBitmap;
    private BitmapTexture mTexture;
    private int mWidth;

    public TiledScreenNail(Bitmap bitmap) {
        this(bitmap, false, true);
    }

    public TiledScreenNail(Bitmap bitmap, boolean fromCache, boolean fileSaveComplete) {
        this.mAnimationStartTime = -1;
        this.mBitmapFromCache = false;
        this.mFileSaveComplete = true;
        this.mNeedFreeBitmap = false;
        this.mDrawLoadingTip = false;
        this.canvasRotation = 0.0f;
        this.centerX = 0;
        this.centerY = 0;
        TraceController.traceBegin("TiledScreenNail.TiledScreenNail, this = " + this + ", fromCache = " + fromCache + ", fileSaveComplete:" + fileSaveComplete);
        this.mWidth = bitmap.getWidth();
        this.mHeight = bitmap.getHeight();
        this.mBitmapFromCache = fromCache;
        if (sThumbnailLoading) {
            fileSaveComplete = false;
        }
        this.mFileSaveComplete = fileSaveComplete;
        this.mBitmap = bitmap;
        this.mTexture = new BitmapTexture(bitmap);
        TraceController.traceEnd();
    }

    public TiledScreenNail(int width, int height) {
        this.mAnimationStartTime = -1;
        this.mBitmapFromCache = false;
        this.mFileSaveComplete = true;
        this.mNeedFreeBitmap = false;
        this.mDrawLoadingTip = false;
        this.canvasRotation = 0.0f;
        this.centerX = 0;
        this.centerY = 0;
        setSize(width, height);
    }

    public TiledScreenNail(Bitmap bitmap, boolean fromCache, boolean fileSaveComplete, boolean needFreeBitmap) {
        this(bitmap, fromCache, fileSaveComplete);
        this.mNeedFreeBitmap = needFreeBitmap;
    }

    public static void setPlaceholderColor(int color) {
        sPlaceholderColor = color;
    }

    public static int getPlaceholderColor() {
        return sPlaceholderColor;
    }

    public static void setLoadingTip(String loadingTip) {
        mLoading = StringTexture.newInstance(loadingTip, (float) GalleryUtils.dpToPixel(18), -1);
    }

    private void setSize(int width, int height) {
        if (width == 0 || height == 0) {
            width = sMaxSide;
            height = (sMaxSide * 3) / 4;
        }
        float scale = Math.min(WMElement.CAMERASIZEVALUE1B1, ((float) sMaxSide) / ((float) Math.max(width, height)));
        this.mWidth = Math.round(((float) width) * scale);
        this.mHeight = Math.round(((float) height) * scale);
    }

    private static void recycleBitmap(BitmapPool pool, Bitmap bitmap) {
        if (pool != null && bitmap != null) {
            pool.recycle(bitmap);
        }
    }

    public ScreenNail combine(ScreenNail other) {
        TraceController.traceBegin("TiledScreenNail.combine");
        if (other == null) {
            TraceController.traceEnd();
            return this;
        } else if (other instanceof TiledScreenNail) {
            TiledScreenNail newer = (TiledScreenNail) other;
            this.mWidth = newer.mWidth;
            this.mHeight = newer.mHeight;
            if (newer.mTexture != null) {
                if (!this.mNeedFreeBitmap) {
                    recycleBitmap(MediaItem.getThumbPool(), this.mBitmap);
                }
                if (this.mTexture != null) {
                    this.mTexture.recycle();
                }
                TraceController.beginSection("TiledScreen.combine: fromCache changed from " + this.mBitmapFromCache + " to " + newer.mBitmapFromCache + ", " + this.mFileSaveComplete + " to " + newer.mFileSaveComplete);
                this.mBitmapFromCache = newer.mBitmapFromCache;
                this.mFileSaveComplete = newer.mFileSaveComplete;
                TraceController.endSection();
                if (!(!this.mNeedFreeBitmap || this.mBitmap == null || this.mBitmap.isRecycled())) {
                    this.mBitmap.recycle();
                    this.mBitmap = null;
                }
                this.mNeedFreeBitmap = newer.mNeedFreeBitmap;
                this.mBitmap = newer.mBitmap;
                this.mTexture = newer.mTexture;
                newer.mBitmap = null;
                newer.mTexture = null;
                newer.mBitmapFromCache = false;
                newer.mFileSaveComplete = true;
            }
            newer.recycle();
            TraceController.traceEnd();
            return this;
        } else {
            recycle();
            TraceController.traceEnd();
            return other;
        }
    }

    public void updatePlaceholderSize(int width, int height) {
        if (this.mBitmap == null && width != 0 && height != 0) {
            setSize(width, height);
        }
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public void noDraw() {
    }

    public void recycle() {
        TraceController.traceBegin("TiledScreenNail.recycle");
        if (this.mTexture != null) {
            this.mTexture.recycle();
            this.mTexture = null;
        }
        if (!(!this.mNeedFreeBitmap || this.mBitmap == null || this.mBitmap.isRecycled())) {
            this.mBitmap.recycle();
            this.mBitmap = null;
        }
        recycleBitmap(MediaItem.getThumbPool(), this.mBitmap);
        this.mBitmap = null;
        this.mBitmapFromCache = false;
        this.mFileSaveComplete = true;
        super.recycle();
        TraceController.traceEnd();
    }

    public static void disableDrawPlaceholder() {
        mDrawPlaceholder = false;
    }

    public static void enableDrawPlaceholder() {
        mDrawPlaceholder = true;
    }

    public void draw(GLCanvas canvas, int x, int y, int width, int height) {
        if (!super.drawGifIfNecessary(canvas, x, y, width, height)) {
            TraceController.traceBegin("TiledScreenNail.draw, this = " + this + ", mBitmapFromCache = " + this.mBitmapFromCache + ", mFileSaveComplete = " + this.mFileSaveComplete);
            if (this.mTexture == null) {
                if (this.mAnimationStartTime == -1) {
                    this.mAnimationStartTime = -2;
                }
                if (mDrawPlaceholder) {
                    canvas.fillRect((float) x, (float) y, (float) width, (float) height, sPlaceholderColor);
                    if (this.mDrawLoadingTip) {
                        canvas.save();
                        if (this.canvasRotation != 0.0f) {
                            canvas.translate((float) this.centerX, (float) this.centerY);
                            canvas.rotate(-this.canvasRotation, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
                            canvas.translate((float) (-this.centerX), (float) (-this.centerY));
                        }
                        mLoading.draw(canvas, this.centerX - (mLoading.getWidth() / 2), this.centerY - (((this.canvasRotation % BitmapDescriptorFactory.HUE_CYAN == 0.0f ? height : width) * 9) / 20));
                        canvas.restore();
                    }
                }
                TraceController.traceEnd();
                return;
            }
            if (this.mAnimationStartTime == -2) {
                this.mAnimationStartTime = AnimationTime.get();
            }
            if (isAnimating()) {
                canvas.drawMixed(this.mTexture, sPlaceholderColor, getRatio(), x, y, width, height);
            } else {
                this.mTexture.draw(canvas, x, y, width, height);
            }
            if (this.mBitmapFromCache && !this.mFileSaveComplete) {
                canvas.fillRect((float) x, (float) y, (float) width, (float) height, -1291845632);
                mLoading.draw(canvas, Math.round(((float) x) + (((float) (width - mLoading.getWidth())) / 2.0f)), Math.round(((float) y) + (((float) (height - mLoading.getHeight())) / 2.0f)));
            }
            TraceController.traceEnd();
        }
    }

    public boolean isBitmapFromCache() {
        return this.mBitmapFromCache;
    }

    public void draw(GLCanvas canvas, RectF source, RectF dest) {
        TraceController.traceBegin("TiledScreenNail.draw(GLCanvas canvas, RectF source, RectF dest)");
        if (this.mTexture == null) {
            canvas.fillRect(dest.left, dest.top, dest.width(), dest.height(), sPlaceholderColor);
            TraceController.traceEnd();
            return;
        }
        canvas.drawTexture(this.mTexture, source, dest);
        TraceController.traceEnd();
    }

    public boolean isAnimating() {
        if (this.mTexture == null) {
            return true;
        }
        if (this.mAnimationStartTime < 0) {
            return false;
        }
        if (AnimationTime.get() - this.mAnimationStartTime < 180) {
            return true;
        }
        this.mAnimationStartTime = -3;
        return false;
    }

    private float getRatio() {
        return Utils.clamp(WMElement.CAMERASIZEVALUE1B1 - (((float) (AnimationTime.get() - this.mAnimationStartTime)) / BitmapDescriptorFactory.HUE_CYAN), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
    }

    public boolean isShowingPlaceholder() {
        return this.mBitmap != null ? isAnimating() : true;
    }

    public static void setMaxSide(int size) {
        sMaxSide = size;
    }

    public Bitmap getBitmap() {
        return this.mBitmap;
    }

    public void enableLoadingTip(boolean enable) {
        this.mDrawLoadingTip = enable;
    }

    public void setPreCanvasOperateParam(float rotateDegree, int centerX, int centerY) {
        this.canvasRotation = rotateDegree;
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public boolean isLoaded() {
        return this.mTexture != null ? this.mTexture.isLoaded() : true;
    }
}
