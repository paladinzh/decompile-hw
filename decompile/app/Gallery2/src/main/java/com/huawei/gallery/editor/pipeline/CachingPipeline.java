package com.huawei.gallery.editor.pipeline;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.filters.FiltersManager;
import com.huawei.gallery.editor.imageshow.MasterImage;
import java.util.concurrent.locks.ReentrantLock;

public class CachingPipeline implements PipelineInterface {
    private FilterEnvironment mEnvironment = new FilterEnvironment();
    private FiltersManager mFiltersManager = null;
    private final ReentrantLock mLock = new ReentrantLock();
    private MasterImage mMasterImage;
    private volatile String mName = "";
    private volatile Bitmap mOriginalBitmap = null;

    public CachingPipeline(FiltersManager filtersManager, String name) {
        this.mFiltersManager = filtersManager;
        this.mName = name;
    }

    public boolean tryLockPipeline() {
        return this.mLock.tryLock();
    }

    public void unlockPipeline() {
        this.mLock.unlock();
    }

    public synchronized void reset() {
        this.mLock.lock();
        try {
            this.mOriginalBitmap = null;
        } finally {
            this.mLock.unlock();
        }
    }

    private String getType(RenderingRequest request) {
        if (request.getType() == 0) {
            return "ICON_RENDERING";
        }
        if (request.getType() == 1) {
            return "GEOMETRY_RENDERING";
        }
        return "UNKNOWN TYPE!";
    }

    private void setupEnvironment() {
        this.mEnvironment.setPipeline(this);
        this.mEnvironment.setFiltersManager(this.mFiltersManager);
        this.mEnvironment.setBitmapCache(this.mMasterImage.getBitmapCache());
        this.mEnvironment.setStop(false);
        this.mEnvironment.setDrawCache(this.mMasterImage.getDrawCache());
        this.mEnvironment.setCustDrawBrushCache(this.mMasterImage.getCustDrawBrushCache());
        this.mEnvironment.setBubbleCache(this.mMasterImage.getBubbleCache());
    }

    public void setOriginal(Bitmap bitmap) {
        this.mOriginalBitmap = bitmap;
        GalleryLog.v("CachingPipeline", "setOriginal, size " + bitmap.getWidth() + " x " + bitmap.getHeight());
        setupEnvironment();
    }

    public void renderGeometry(RenderingRequest request) {
        this.mLock.lock();
        try {
            ImagePreset preset = request.getImagePreset();
            setupEnvironment();
            Bitmap bitmap = this.mMasterImage.getOriginalBitmapLarge() != null ? this.mMasterImage.getOriginalBitmapLarge() : this.mOriginalBitmap;
            if (bitmap != null) {
                bitmap = preset.applyEditorStepOnlyGeometry(this.mEnvironment.getBitmapCopy(bitmap), this.mEnvironment);
                if (this.mEnvironment.needsStop()) {
                    this.mEnvironment.cache(bitmap);
                } else {
                    request.setBitmap(bitmap);
                }
                this.mFiltersManager.freeFilterResources(preset);
                this.mLock.unlock();
            }
        } finally {
            this.mLock.unlock();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void renderIcon(RenderingRequest request) {
        this.mLock.lock();
        try {
            if (request.getImagePreset() == null) {
                GalleryLog.w("CachingPipeline", "render image of type " + getType(request) + " preset is null");
            } else {
                Bitmap bitmap;
                ImagePreset preset = request.getImagePreset();
                setupEnvironment();
                this.mFiltersManager.freeFilterResources(preset);
                Rect iconBounds = request.getIconBounds();
                Bitmap source = this.mMasterImage.getCacheLatestBitmap();
                if (source == null) {
                    source = this.mMasterImage.getThumbnailBitmap();
                    if (source == null) {
                        this.mLock.unlock();
                        return;
                    }
                }
                if (iconBounds != null) {
                    if (iconBounds.width() > source.getWidth() * 2) {
                        source = this.mMasterImage.getLargeThumbnailBitmap();
                    }
                    bitmap = this.mEnvironment.getBitmap(iconBounds.width(), iconBounds.height());
                    Canvas canvas = new Canvas(bitmap);
                    Matrix m = new Matrix();
                    float scale = ((float) Math.max(iconBounds.width(), iconBounds.height())) / ((float) Math.min(source.getWidth(), source.getHeight()));
                    m.setScale(scale, scale);
                    m.postTranslate((((float) iconBounds.width()) - (((float) source.getWidth()) * scale)) / 2.0f, (((float) iconBounds.height()) - (((float) source.getHeight()) * scale)) / 2.0f);
                    Paint paint = new Paint(2);
                    paint.setXfermode(new PorterDuffXfermode(Mode.SRC));
                    canvas.drawBitmap(source, m, paint);
                } else {
                    bitmap = this.mEnvironment.getBitmapCopy(source);
                }
                if (bitmap == null) {
                    this.mLock.unlock();
                    return;
                }
                Bitmap bmp = preset.applyEditorStepWithOutGeometry(bitmap, this.mEnvironment);
                if (!this.mEnvironment.needsStop()) {
                    request.setBitmap(bmp);
                }
                this.mFiltersManager.freeFilterResources(preset);
                this.mLock.unlock();
            }
        } finally {
            this.mLock.unlock();
        }
    }

    public synchronized Bitmap renderFinalImage(Bitmap bitmap, ImagePreset preset) {
        this.mLock.lock();
        try {
            setupEnvironment();
            this.mEnvironment.setQuality(2);
            this.mFiltersManager.freeFilterResources(preset);
            bitmap = preset.applyEditorStepToFinalBitmap(bitmap, this.mEnvironment, this.mMasterImage.getEditorType());
        } finally {
            this.mLock.unlock();
        }
        return bitmap;
    }

    public Bitmap compute(ImagePreset preset) {
        this.mLock.lock();
        try {
            setupEnvironment();
            Bitmap result = null;
            if (!this.mMasterImage.findCachedBitmap(preset)) {
                result = this.mEnvironment.getBitmapCopy(this.mOriginalBitmap);
            }
            Bitmap applyEditorStep = preset.applyEditorStep(result, this.mEnvironment, this.mMasterImage.getEditorType());
            return applyEditorStep;
        } finally {
            this.mLock.unlock();
        }
    }

    public void clearCache() {
        if (this.mMasterImage != null) {
            this.mEnvironment.setBitmapCache(this.mMasterImage.getBitmapCache());
        }
    }

    public void setMasterImage(MasterImage image) {
        this.mMasterImage = image;
    }
}
