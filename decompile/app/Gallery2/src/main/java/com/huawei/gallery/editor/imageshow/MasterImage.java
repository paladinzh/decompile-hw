package com.huawei.gallery.editor.imageshow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.net.Uri;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.cache.BitmapCache;
import com.huawei.gallery.editor.cache.BubbleCache;
import com.huawei.gallery.editor.cache.CustDrawBrushCache;
import com.huawei.gallery.editor.cache.DrawCache;
import com.huawei.gallery.editor.cache.ImageLoader;
import com.huawei.gallery.editor.pipeline.Buffer;
import com.huawei.gallery.editor.pipeline.DelegateCache;
import com.huawei.gallery.editor.pipeline.ImagePreset;
import com.huawei.gallery.editor.pipeline.SharedBuffer;
import com.huawei.gallery.editor.pipeline.SharedPreset;
import com.huawei.gallery.editor.step.EditorStep;
import com.huawei.gallery.editor.step.StepStack;
import com.huawei.gallery.editor.step.StepStack.StackListener;
import com.huawei.gallery.editor.tools.EditorUtils;
import java.util.Stack;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class MasterImage implements DelegateCache {
    private BitmapCache mBitmapCache;
    protected BitmapTexture mBitmapTexture;
    private BubbleCache mBubbleCache = new BubbleCache();
    private Context mContext;
    private CustDrawBrushCache mCustDrawBrushCache = new CustDrawBrushCache();
    private Delegate mDelegate = null;
    private DrawCache mDrawCache = new DrawCache();
    private int mEditorType = 0;
    private Bitmap mGeometryOnlyBitmap = null;
    private BitmapTexture mGeometryOnlyBitmapTexture;
    private ImagePreset mGeometryOnlyPreset = null;
    private int mOrientation;
    private Bitmap mOriginalBitmapLarge = null;
    private Bitmap mOriginalBitmapLargeTemporary = null;
    private Bitmap mOriginalBitmapSmall = null;
    private ImagePreset mPreset = null;
    private SharedBuffer mPreviewBuffer = new SharedBuffer();
    private SharedPreset mPreviewPreset = new SharedPreset();
    private ImagePreset mSavedPreset = null;
    private StepStack mStepStack;
    private Bitmap mTemporaryThumbnail = null;
    private Uri mUri = null;

    public interface Delegate {
        GLRoot getGLRoot();

        Bitmap getSource();

        void invalidate(int i, Bitmap bitmap);

        void onStackChanged(boolean z, boolean z2, boolean z3);

        void post(Bitmap bitmap, ImagePreset imagePreset, int i);

        void updatePreviewBuffer();
    }

    public MasterImage(Context context, Delegate delegate, int editorType) {
        this.mContext = context;
        this.mEditorType = editorType;
        this.mDelegate = delegate;
        this.mBitmapCache = new BitmapCache(this);
        this.mStepStack = new StepStack(this.mBitmapCache, new StackListener() {
            public void onStackChanged(boolean canUndo, boolean canRedo, boolean canCompare) {
                MasterImage.this.mDelegate.onStackChanged(canUndo, canRedo, canCompare);
            }
        });
    }

    public Bitmap getOriginalBitmapLarge() {
        return this.mOriginalBitmapLarge;
    }

    public Uri getUri() {
        return this.mUri;
    }

    public Bitmap getOriginalBitmapLargeTemporary() {
        return this.mOriginalBitmapLargeTemporary;
    }

    public boolean loadTemporaryBitmap(Uri uri, Bitmap bitmap, int orientation) {
        this.mUri = uri;
        this.mOrientation = ImageLoader.getMetadataOrientation(orientation);
        this.mOriginalBitmapLargeTemporary = ImageLoader.orientBitmap(bitmap, this.mOrientation, true);
        this.mOriginalBitmapSmall = BitmapUtils.resizeAndCropCenter(this.mOriginalBitmapLargeTemporary, SmsCheckResult.ESCT_160, false);
        return true;
    }

    public Bitmap computeRenderTexture() {
        Bitmap bitmap = getFilteredImage();
        if ((this.mBitmapTexture == null || hasPreviewChange() || this.mBitmapTexture.getBitmap() != bitmap) && bitmap != null) {
            if (this.mBitmapTexture != null) {
                this.mBitmapTexture.recycle();
            }
            this.mBitmapTexture = new BitmapTexture(bitmap);
            setPreviewTexture(this.mBitmapTexture);
            this.mPreviewBuffer.setUpdateTexture(false);
        }
        if (this.mBitmapTexture == null) {
            return null;
        }
        if (bitmap == null) {
            bitmap = this.mBitmapTexture.getBitmap();
        }
        return bitmap;
    }

    public void resetImagePreset() {
        ImagePreset preset = new ImagePreset();
        preset.getEditorStepStack().addAll(getAppliedStack());
        if (preset.equals(getPreset())) {
            cacheLatestBitmap();
        } else {
            setPreset(preset);
        }
    }

    public void restoreSavedPreset() {
        if (this.mSavedPreset != null) {
            if (!this.mSavedPreset.equals(getCurrentPreset())) {
                setPreset(this.mSavedPreset);
            }
        }
    }

    public void showRepresentation(EditorStep editorStep) {
        ImagePreset preset = new ImagePreset();
        preset.getEditorStepStack().addAll(getAppliedStack());
        preset.getEditorStepStack().push(editorStep.copy());
        setPreset(preset);
    }

    public boolean updateBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            switchTemporaryBitmap();
        } else {
            this.mOriginalBitmapLarge = bitmap;
        }
        return true;
    }

    public void switchTemporaryBitmap() {
        this.mOriginalBitmapLarge = this.mOriginalBitmapLargeTemporary;
        this.mOriginalBitmapLargeTemporary = null;
    }

    public synchronized ImagePreset getPreset() {
        return this.mPreset;
    }

    public synchronized void setPreset(ImagePreset preset) {
        this.mPreset = preset;
        invalidatePreview();
        resetGeometryImages(false);
    }

    public synchronized void updatePreset(ImagePreset preset) {
        this.mPreset = preset;
    }

    public synchronized boolean hasModifications() {
        return this.mPreset != null ? this.mPreset.hasModifications() : false;
    }

    public void initSharedBuffer(ImagePreset preset, Bitmap largeBitmap) {
        this.mPreviewBuffer.initBuffer(largeBitmap, getBitmapCache());
        this.mPreviewBuffer.getBuffer().setPreset(preset);
        this.mPreviewBuffer.setUpdateTexture(true);
    }

    public void updateShareBuffer(ImagePreset imagePreset, Buffer buffer, Bitmap preview) {
        if (buffer != null) {
            this.mPreviewBuffer.setBuffer(buffer, this.mBitmapCache);
        } else if (preview != null) {
            this.mPreviewBuffer.setBuffer(preview);
        }
        this.mPreviewBuffer.getBuffer().setPreset(imagePreset);
        this.mPreviewBuffer.setUpdateTexture(true);
    }

    public SharedPreset getPreviewPreset() {
        return this.mPreviewPreset;
    }

    public boolean hasPreviewChange() {
        return this.mPreviewBuffer.checkSwapNeeded();
    }

    public boolean computePreviewIsSameSize(Bitmap preview) {
        Buffer buffer = this.mPreviewBuffer.getBuffer();
        if (buffer == null) {
            return false;
        }
        return buffer.isSameSize(preview);
    }

    public Bitmap getFilteredImage() {
        Buffer consumer = this.mPreviewBuffer.getBuffer();
        if (consumer != null) {
            return consumer.getBitmap();
        }
        GalleryLog.e("MasterImage", "consumer is null");
        return null;
    }

    public Bitmap getPreviewBitmap() {
        Bitmap bitmap = null;
        if (this.mPreset == null) {
            return null;
        }
        GLRoot root = this.mDelegate.getGLRoot();
        if (root == null) {
            return null;
        }
        root.lockRenderThread();
        try {
            Bitmap bmp = getFilteredImage();
            if (bmp == null) {
                return bitmap;
            }
            bitmap = true;
            Bitmap copy = bmp.copy(bmp.getConfig(), true);
            root.unlockRenderThread();
            return copy;
        } finally {
            root.unlockRenderThread();
        }
    }

    public Bitmap getGeometryOnlyImage() {
        return this.mGeometryOnlyBitmap;
    }

    public ImagePreset getCurrentPreset() {
        Buffer consumer = this.mPreviewBuffer.getBuffer();
        if (consumer != null) {
            return consumer.getPreset();
        }
        return null;
    }

    public void notifyObservers(int type, Bitmap preview) {
        if (type == 1) {
            cacheLatestBitmap();
        }
        this.mDelegate.invalidate(type, preview);
    }

    public void resetGeometryImages(boolean force) {
        if (this.mPreset != null) {
            ImagePreset newPresetGeometryOnly = new ImagePreset(this.mPreset);
            if (force || this.mGeometryOnlyPreset == null || !newPresetGeometryOnly.equalsGeometryOnly(this.mGeometryOnlyPreset)) {
                this.mGeometryOnlyPreset = newPresetGeometryOnly;
                this.mDelegate.post(null, this.mGeometryOnlyPreset, 1);
            }
        }
    }

    public void invalidatePreview() {
        if (this.mPreset != null) {
            this.mPreviewPreset.enqueuePreset(this.mPreset);
            this.mDelegate.updatePreviewBuffer();
        }
    }

    public void updateGeometryOnlyBitmap(Bitmap bitmap) {
        this.mBitmapCache.cache(this.mGeometryOnlyBitmap);
        this.mGeometryOnlyBitmap = bitmap;
    }

    public Bitmap getTemporaryThumbnailBitmap() {
        if (this.mTemporaryThumbnail == null && getThumbnailBitmap() != null) {
            this.mTemporaryThumbnail = getThumbnailBitmap().copy(Config.ARGB_8888, true);
            new Canvas(this.mTemporaryThumbnail).drawARGB(SmsCheckResult.ESCT_200, 80, 80, 80);
        }
        return this.mTemporaryThumbnail;
    }

    public Bitmap getThumbnailBitmap() {
        return this.mOriginalBitmapSmall;
    }

    public Bitmap getLargeThumbnailBitmap() {
        return getOriginalBitmapLarge();
    }

    public BitmapCache getBitmapCache() {
        return this.mBitmapCache;
    }

    public void saveCurrentPreset() {
        if (this.mPreset == null) {
            this.mSavedPreset = null;
        } else {
            this.mSavedPreset = new ImagePreset(this.mPreset);
        }
    }

    public void resetTemporaryBitmap() {
        if (this.mOriginalBitmapLargeTemporary != this.mOriginalBitmapSmall) {
            recycleBitmap(this.mOriginalBitmapLargeTemporary);
        }
        this.mOriginalBitmapLargeTemporary = null;
    }

    public void resetBitmap() {
        recycleBitmap(this.mOriginalBitmapSmall);
        recycleBitmap(this.mOriginalBitmapLarge);
        recycleBitmap(this.mTemporaryThumbnail);
        recycleBitmap(this.mGeometryOnlyBitmap);
        recycleBitmap(this.mOriginalBitmapLargeTemporary);
        this.mOriginalBitmapSmall = null;
        this.mOriginalBitmapLarge = null;
        this.mTemporaryThumbnail = null;
        this.mGeometryOnlyBitmap = null;
    }

    public void onLeaveEditor() {
        resetBitmap();
        this.mPreviewBuffer.clear();
        this.mPreviewPreset.clear();
        this.mStepStack.clear();
        synchronized (this) {
            this.mBitmapTexture = null;
            this.mGeometryOnlyBitmapTexture = null;
        }
        this.mCustDrawBrushCache.clear();
        this.mBubbleCache.clear();
    }

    private void recycleBitmap(Bitmap bmp) {
        if (bmp != null && this.mDelegate.getSource() != bmp && !bmp.isRecycled()) {
            bmp.recycle();
        }
    }

    public GLRoot getGLRoot() {
        return this.mDelegate.getGLRoot();
    }

    public Context getContext() {
        return this.mContext;
    }

    public void pushEditorStep(EditorStep step) {
        if (!step.isNil()) {
            this.mStepStack.pushEditorStep(step);
        }
    }

    public Stack<EditorStep> getAppliedStack() {
        return this.mStepStack.getAppliedStack();
    }

    public void redo() {
        this.mStepStack.redo();
    }

    public void undo() {
        this.mStepStack.undo();
    }

    public synchronized BitmapTexture getPreviewTexture() {
        return this.mBitmapTexture;
    }

    public synchronized BitmapTexture getGeometryTexture() {
        return this.mGeometryOnlyBitmapTexture;
    }

    public synchronized void setPreviewTexture(BitmapTexture texture) {
        this.mBitmapTexture = texture;
    }

    public synchronized void setGeometryTexture(BitmapTexture texture) {
        this.mGeometryOnlyBitmapTexture = texture;
    }

    public void cacheLatestBitmap() {
        if (getEditorType() != 1) {
            Buffer consumer = this.mPreviewBuffer.getBuffer();
            if (consumer != null && EditorUtils.equals(consumer.getPreset().getEditorStepStack(), this.mStepStack.getAppliedStack())) {
                Bitmap bmp = consumer.getBitmap();
                if (bmp != null && !bmp.isRecycled()) {
                    this.mStepStack.cache(bmp);
                }
            }
        }
    }

    public boolean findCachedBitmap(ImagePreset preset) {
        return this.mStepStack.findCachedBitmap(preset);
    }

    public void requestStackChangeCall() {
        this.mStepStack.requestStackChangeCall();
    }

    public Bitmap getCacheLatestBitmap() {
        return this.mStepStack.getLatestCachedBitmap();
    }

    public int getEditorType() {
        return this.mEditorType;
    }

    public DrawCache getDrawCache() {
        return this.mDrawCache;
    }

    public Bitmap getDrawCacheApplyBitmap() {
        return this.mDrawCache.getAppliedMosaicBitmap();
    }

    public void resetDrawCache() {
        this.mDrawCache.reset();
    }

    public CustDrawBrushCache getCustDrawBrushCache() {
        return this.mCustDrawBrushCache;
    }

    public BubbleCache getBubbleCache() {
        return this.mBubbleCache;
    }

    public void cache(Bitmap bitmap) {
        this.mBitmapCache.cache(bitmap);
    }

    public Bitmap getBitmapCacheCopy(Bitmap bitmap) {
        return this.mBitmapCache.getBitmapCopy(bitmap);
    }
}
