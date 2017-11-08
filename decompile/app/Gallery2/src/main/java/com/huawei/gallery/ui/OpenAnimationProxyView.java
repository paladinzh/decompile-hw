package com.huawei.gallery.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import com.android.gallery3d.R;
import com.android.gallery3d.data.DecodeUtils;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.ui.AnimationTime;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.PositionController;
import com.android.gallery3d.ui.TiledScreenNail;
import com.android.gallery3d.util.DisplayEngineUtils;
import com.android.gallery3d.util.DisplayEngineUtils.ExifInfo;
import com.android.gallery3d.util.DrmUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.displayengine.BoostFullScreenNailDisplay;
import com.huawei.gallery.displayengine.ScreenNailCommonDisplayEngine;
import com.huawei.gallery.displayengine.ScreenNailCommonDisplayEnginePool;
import com.huawei.watermark.manager.parse.WMElement;

public class OpenAnimationProxyView extends GLView {
    private boolean mHasClear;
    private boolean mHasInitReady = false;
    private BitmapTexture mProxyTexture;
    private final RectComputer mRectComputer = new RectComputer();
    private int mRotation;
    private Runnable mStopDisabledDownShiftAnimationRunnable;

    private static class RectComputer {
        RectF bmp;
        Rect bmpD;
        Rect bmpS;
        RectF render;
        Rect renderD;
        Rect renderS;

        private RectComputer() {
            this.bmpS = new Rect();
            this.bmpD = new Rect();
            this.renderS = new Rect();
            this.renderD = new Rect();
            this.bmp = new RectF();
            this.render = new RectF();
        }

        public void initParams(int bw, int bh, int rw, int rh, int rotation) {
            float scale;
            int bmin = Math.min(bw, bh);
            this.bmpS.set((bw - bmin) / 2, (bh - bmin) / 2, (bw + bmin) / 2, (bh + bmin) / 2);
            this.bmpD.set(0, 0, bw, bh);
            int rmin = Math.min(rw, rh);
            this.renderS.set((rw - rmin) / 2, (rh - rmin) / 2, (rw + rmin) / 2, (rh + rmin) / 2);
            if (rotation % 180 != 0) {
                scale = PositionController.getMinimalScale(bh, bw, rw, rh);
            } else {
                scale = PositionController.getMinimalScale(bw, bh, rw, rh);
            }
            float renderWidth = ((float) bw) * scale;
            float renderHeight = ((float) bh) * scale;
            int offsetX = Math.round((((float) rw) - renderWidth) / 2.0f);
            int offsetY = Math.round((((float) rh) - renderHeight) / 2.0f);
            this.renderD.set(offsetX, offsetY, Math.round(((float) offsetX) + renderWidth), Math.round(((float) offsetY) + renderHeight));
        }

        public RectF getBmp(float progress) {
            this.bmp.set((((float) (this.bmpD.left - this.bmpS.left)) * progress) + ((float) this.bmpS.left), (((float) (this.bmpD.top - this.bmpS.top)) * progress) + ((float) this.bmpS.top), (((float) (this.bmpD.right - this.bmpS.right)) * progress) + ((float) this.bmpS.right), (((float) (this.bmpD.bottom - this.bmpS.bottom)) * progress) + ((float) this.bmpS.bottom));
            return this.bmp;
        }

        public RectF getRender(float progress) {
            this.render.set((((float) (this.renderD.left - this.renderS.left)) * progress) + ((float) this.renderS.left), (((float) (this.renderD.top - this.renderS.top)) * progress) + ((float) this.renderS.top), (((float) (this.renderD.right - this.renderS.right)) * progress) + ((float) this.renderS.right), (((float) (this.renderD.bottom - this.renderS.bottom)) * progress) + ((float) this.renderS.bottom));
            return this.render;
        }
    }

    private class TextureTask extends AsyncTask<Void, Void, Bitmap> {
        private MediaItem mItem;

        public TextureTask(MediaItem item) {
            this.mItem = item;
        }

        protected Bitmap doInBackground(Void... params) {
            if (this.mItem == null) {
                return null;
            }
            TraceController.traceBegin("OpenAnimationProxyView get image");
            boolean ignoreACEProcess = false;
            Bitmap bmp = this.mItem.getScreenNailBitmap(1);
            if (bmp == null && this.mItem.getMediaType() == 2) {
                int size = MediaItem.getTargetSize(1) / 2;
                Options options = null;
                if (DrmUtils.isDrmFile(this.mItem.getFilePath()) && DrmUtils.haveCountConstraints(this.mItem.getFilePath(), 7)) {
                    options = new Options();
                    DrmUtils.inPreviewMode(options);
                }
                bmp = DecodeUtils.decodeThumbnail(ThreadPool.JOB_CONTEXT_STUB, this.mItem.getFilePath(), options, size, 8);
                ignoreACEProcess = true;
            }
            if (bmp == null) {
                TraceController.traceEnd();
                return null;
            }
            ExifInfo exifInfo = null;
            if (ignoreACEProcess) {
                TraceController.traceBegin("first load TYPE_THUMBNAIL, WideColorGamut check");
                if (DisplayEngineUtils.isDisplayEngineEnable() && DisplayEngineUtils.getCapWideColorGamut()) {
                    exifInfo = DisplayEngineUtils.getInfoFromEXIF(this.mItem);
                    if (DisplayEngineUtils.isWideColorGamut(exifInfo.colorSpace)) {
                        GalleryLog.d("OpenAnimationProxyView", "first load TYPE_THUMBNAIL do gmp for WideColorGamut");
                        ignoreACEProcess = false;
                    }
                }
                TraceController.traceEnd();
            }
            if (!ignoreACEProcess && DisplayEngineUtils.isDisplayEngineEnable()) {
                TraceController.traceBegin("OpenAnimationProxyView aceScreenNailProcess w=" + bmp.getWidth() + ",h=" + bmp.getHeight());
                ScreenNailCommonDisplayEnginePool commonDisplayEnginePool = new ScreenNailCommonDisplayEnginePool();
                ScreenNailCommonDisplayEngine commonDisplayEngine = DisplayEngineUtils.obtainScreenNailCommon(bmp, this.mItem, commonDisplayEnginePool, exifInfo);
                if (commonDisplayEngine != null) {
                    commonDisplayEnginePool.add(this.mItem, commonDisplayEngine);
                    DisplayEngineUtils.updateEffectImageReview(this.mItem, commonDisplayEngine);
                }
                bmp = DisplayEngineUtils.processScreenNailACE(bmp, this.mItem, commonDisplayEnginePool, 1, exifInfo);
                if (commonDisplayEngine != null) {
                    commonDisplayEngine.destroy();
                }
                commonDisplayEnginePool.clear();
                TraceController.traceEnd();
            }
            BoostFullScreenNailDisplay.preGenerateFullScreenNailAsync(ThreadPool.JOB_CONTEXT_STUB, this.mItem);
            TraceController.traceEnd();
            return bmp;
        }

        protected void onPostExecute(Bitmap bitmap) {
            int i = 0;
            if (OpenAnimationProxyView.this.mHasClear) {
                if (bitmap != null) {
                    bitmap.recycle();
                }
                return;
            }
            if (bitmap != null) {
                OpenAnimationProxyView.this.mProxyTexture = new BitmapTexture(bitmap);
                OpenAnimationProxyView.this.mHasInitReady = false;
            }
            OpenAnimationProxyView openAnimationProxyView = OpenAnimationProxyView.this;
            if (this.mItem != null) {
                i = this.mItem.getRotation();
            }
            openAnimationProxyView.mRotation = i;
            Runnable stopDisabledDownShiftAnimationRunnable = OpenAnimationProxyView.this.mStopDisabledDownShiftAnimationRunnable;
            if (stopDisabledDownShiftAnimationRunnable != null) {
                stopDisabledDownShiftAnimationRunnable.run();
            }
            OpenAnimationProxyView.this.invalidate();
        }
    }

    public OpenAnimationProxyView(Context context, int hintWidth, int hintHeight, Runnable stopDisabledDownShiftAnimationRunnable) {
        setBackgroundColor(GalleryUtils.intColorToFloatARGBArray(context.getResources().getColor(R.color.default_background)));
        this.mStopDisabledDownShiftAnimationRunnable = stopDisabledDownShiftAnimationRunnable;
    }

    protected void render(GLCanvas canvas) {
        TraceController.traceBegin("OpenAnimationProxyView render");
        boolean transitionActive = false;
        if (this.mTransition != null && this.mTransition.calculate(AnimationTime.get())) {
            GLRoot root = getGLRoot();
            if (root != null) {
                root.requestRenderForced();
            }
            transitionActive = this.mTransition.isActive();
        }
        renderBackground(canvas);
        canvas.save();
        if (transitionActive) {
            this.mTransition.applyContentTransform(this, canvas);
        }
        drawProxyTexture(canvas, this.mProxyTexture);
        canvas.restore();
        if (transitionActive) {
            this.mTransition.applyOverlay(this, canvas);
        }
        TraceController.traceEnd();
    }

    private void drawProxyTexture(GLCanvas canvas, BitmapTexture proxyTexture) {
        if (proxyTexture == null || this.mTransition == null) {
            canvas.fillRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), TiledScreenNail.getPlaceholderColor());
            return;
        }
        float p = this.mTransition.getProgress();
        if (!this.mHasInitReady) {
            this.mRectComputer.initParams(proxyTexture.getWidth(), proxyTexture.getHeight(), getWidth(), getHeight(), this.mRotation);
            this.mHasInitReady = true;
        }
        canvas.save();
        canvas.translate(((float) getWidth()) / 2.0f, ((float) getHeight()) / 2.0f);
        canvas.rotate((float) this.mRotation, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
        canvas.translate(((float) (-getWidth())) / 2.0f, ((float) (-getHeight())) / 2.0f);
        canvas.drawTexture(proxyTexture, this.mRectComputer.getBmp(p), this.mRectComputer.getRender(p));
        canvas.restore();
    }

    public boolean isProxyTextureNull() {
        return this.mProxyTexture == null;
    }

    public void clear() {
        BitmapTexture proxyTexture = this.mProxyTexture;
        if (proxyTexture != null) {
            proxyTexture.recycle();
            proxyTexture.getBitmap().recycle();
            this.mProxyTexture = null;
        }
        this.mHasInitReady = false;
        this.mHasClear = true;
    }

    public void updateProxyTexture(MediaItem mediaItem) {
        if (mediaItem != null) {
            new TextureTask(mediaItem).execute(new Void[0]);
        }
    }
}
