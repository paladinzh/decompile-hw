package com.huawei.gallery.wallpaper.ui;

import android.graphics.Bitmap;
import android.graphics.RectF;
import com.android.gallery3d.anim.FloatAnimation;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.ui.AnimationTime;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.ThreadPool;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.gallery.ui.stackblur.StackBlurUtils;
import com.huawei.gallery.util.MyPrinter;
import com.huawei.watermark.manager.parse.WMElement;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class GLImageView extends GLView {
    private static final MyPrinter LOG = new MyPrinter(GLImageView.class.getSimpleName());
    private static int START_DURATION = 300;
    private static int SWITCH_DURATION = SmsCheckResult.ESCT_200;
    private FloatAnimation mAnimation = new FloatAnimation(0.0f, WMElement.CAMERASIZEVALUE1B1, SWITCH_DURATION);
    private Bitmap mBitmap;
    private int mBlurFactor;
    private Future<Void> mBlurFuture;
    private BlurJob mBlurJob = new BlurJob();
    protected float mCenterX;
    protected float mCenterY;
    private GalleryContext mContext;
    protected int mImageHeight = -1;
    protected int mImageWidth = -1;
    private final RectF mOldSrcRect = new RectF();
    private final RectF mOldTarRect = new RectF();
    private BitmapTexture mOldTexture;
    private boolean mRangeDirty = true;
    protected float mScale = WMElement.CAMERASIZEVALUE1B1;
    protected final RectF mSourceRect = new RectF();
    private Bitmap mSrcBitmap;
    private float mStep = 0.0f;
    protected final RectF mTargetRect = new RectF();
    private BitmapTexture mTexture;
    private boolean mTextureDirty = true;
    private final ThreadPool mThreadPool;

    private class BlurJob extends BaseJob<Void> {
        private BlurJob() {
        }

        public String workContent() {
            return "make blur bitmap for crop view";
        }

        public Void run(JobContext jc) {
            int factor;
            do {
                factor = GLImageView.this.mBlurFactor;
                int radius = GLImageView.this.getBlurRadius(factor);
                if (radius == 0) {
                    GLImageView.this.mBitmap = GLImageView.this.mSrcBitmap;
                } else {
                    GLImageView.LOG.d("blurFactor(0,100) is " + factor + ", blurRadius is " + radius);
                    long start = System.currentTimeMillis();
                    GLImageView.this.mBitmap = StackBlurUtils.getBlurBitmap(GLImageView.this.mContext.getActivityContext(), GLImageView.this.mSrcBitmap, radius);
                    GLImageView.LOG.d("blur cost time: " + (System.currentTimeMillis() - start));
                }
                GLImageView.this.mTextureDirty = true;
                GLImageView.this.invalidate();
            } while (factor != GLImageView.this.mBlurFactor);
            return null;
        }
    }

    public GLImageView(GalleryContext context) {
        this.mContext = context;
        this.mThreadPool = context.getThreadPool();
    }

    public void setData(Bitmap data) {
        if (data != null) {
            this.mSrcBitmap = data;
            this.mImageWidth = data.getWidth();
            this.mImageHeight = data.getHeight();
        } else {
            this.mSrcBitmap = null;
            this.mImageWidth = 0;
            this.mImageHeight = 0;
        }
        LOG.d("setData mImageWidth=" + this.mImageWidth + ", mImageHeight=" + this.mImageHeight);
        this.mStep = Math.max(0.0f, ((float) Math.min(Math.min(this.mImageWidth, this.mImageHeight), 25)) / 100.0f);
        this.mBitmap = this.mSrcBitmap;
        this.mRangeDirty = true;
        this.mTextureDirty = true;
        invalidate();
    }

    public void setBlurFactor(int blurFactor) {
        this.mBlurFactor = blurFactor;
        if (this.mBlurFuture == null || this.mBlurFuture.isDone()) {
            this.mBlurFuture = this.mThreadPool.submit(this.mBlurJob);
        }
    }

    public int getBlurRadius(int factor) {
        factor = Utils.clamp(factor, 0, 100);
        if (factor <= 1) {
            return 0;
        }
        return Math.round(this.mStep * ((float) factor));
    }

    protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
        super.onLayout(changeSize, left, top, right, bottom);
        this.mRangeDirty = changeSize;
    }

    private void getRange() {
        int viewerWidth = getWidth();
        int viewerHeight = getHeight();
        int imageHeight = this.mImageHeight;
        int imageWidth = this.mImageWidth;
        LOG.d(String.format("viewerWidth=%s, viewerHeight=%s, imageWidth=%s, imageHeight=%s ", new Object[]{Integer.valueOf(viewerWidth), Integer.valueOf(viewerHeight), Integer.valueOf(imageWidth), Integer.valueOf(imageHeight)}));
        float cx = this.mCenterX;
        float cy = this.mCenterY;
        float scale = this.mScale;
        float floatLeft = cx - (((float) viewerWidth) / (2.0f * scale));
        float floatTop = cy - (((float) viewerHeight) / (2.0f * scale));
        int top = (int) Math.floor((double) floatTop);
        int right = (int) Math.ceil((double) ((((float) viewerWidth) / scale) + floatLeft));
        int bottom = (int) Math.ceil((double) ((((float) viewerHeight) / scale) + floatTop));
        this.mSourceRect.set((float) Math.max(0, (int) Math.floor((double) floatLeft)), (float) Math.max(0, top), (float) Math.min(imageWidth, right), (float) Math.min(imageHeight, bottom));
        int left = Math.round((((float) viewerWidth) / 2.0f) + ((this.mSourceRect.left - cx) * scale));
        top = Math.round((((float) viewerHeight) / 2.0f) + ((this.mSourceRect.top - cy) * scale));
        right = Math.round((((float) viewerWidth) / 2.0f) + ((this.mSourceRect.right - cx) * scale));
        bottom = Math.round((((float) viewerHeight) / 2.0f) + ((this.mSourceRect.bottom - cy) * scale));
        this.mTargetRect.set((float) Math.max(0, left), (float) Math.max(0, top), (float) Math.min(viewerWidth, right), (float) Math.min(viewerHeight, bottom));
        LOG.d(String.format("mSourceRect=%s, mTargetRect=%s ", new Object[]{this.mSourceRect, this.mTargetRect}));
    }

    private void updateTexture() {
        BitmapTexture toBeRemoved = this.mOldTexture;
        this.mOldTexture = this.mTexture;
        Bitmap bitmap = this.mBitmap;
        if (bitmap != null) {
            this.mTexture = new BitmapTexture(bitmap);
        } else {
            this.mTexture = null;
        }
        if (toBeRemoved != null) {
            toBeRemoved.recycle();
        }
    }

    public boolean setPosition(float centerX, float centerY, float scale) {
        if (Utils.equal(this.mCenterX, centerX) && Utils.equal(this.mCenterY, centerY) && Utils.equal(this.mScale, scale)) {
            return false;
        }
        this.mCenterX = centerX;
        this.mCenterY = centerY;
        this.mScale = scale;
        this.mRangeDirty = true;
        invalidate();
        return true;
    }

    protected void render(GLCanvas canvas) {
        if (this.mTextureDirty) {
            this.mOldSrcRect.set(this.mSourceRect);
            this.mOldTarRect.set(this.mTargetRect);
            if (this.mTexture != null) {
                this.mAnimation.setDuration(SWITCH_DURATION);
            } else {
                this.mAnimation.setDuration(START_DURATION + SWITCH_DURATION);
            }
            this.mAnimation.start();
        }
        if (this.mRangeDirty) {
            getRange();
            this.mRangeDirty = false;
        }
        if (this.mTextureDirty) {
            this.mTextureDirty = false;
            updateTexture();
        }
        canvas.save(1);
        if (this.mAnimation.calculate(AnimationTime.get())) {
            if (this.mOldTexture != null) {
                canvas.drawTexture(this.mOldTexture, this.mOldSrcRect, this.mOldTarRect);
            }
            canvas.multiplyAlpha(this.mAnimation.get());
            invalidate();
        }
        if (this.mTexture != null) {
            canvas.drawTexture(this.mTexture, this.mSourceRect, this.mTargetRect);
        }
        canvas.restore();
    }
}
